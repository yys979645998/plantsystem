import json
import uuid
import os
import pickle

from flask import Flask, request, jsonify
from flask_cors import CORS
import torch
import numpy as np
from PIL import Image
from torch import nn

import faiss
from torchvision import transforms, models
from sklearn.preprocessing import normalize
from langchain_community.embeddings import HuggingFaceBgeEmbeddings
from zhipuai import ZhipuAI

from trainModel1 import ChannelAttentionModule

app = Flask(__name__)
CORS(app)

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# 配置文件路径
image_dataset_path = r"E:\LLM\dataset\110plant\train"  # 仅用于首次生成class_names.json
class_names_file = "faiss/class_names.json"
text_model_path = "bge-large-zh-v1.5"
vector_file_path = "faiss/vectors.pkl"
index_file_path = "faiss/index.faiss"
model_state_path = 'resnet50Retrieve.pth'
api_key = "d8a6594d5a2f2d535c8cc87d851151e2.5gATIgpJwxES1xNt"
client = ZhipuAI(api_key=api_key)

# 加载类别标签
if os.path.exists(class_names_file):
    with open(class_names_file, 'r', encoding='utf-8') as f:
        class_names = json.load(f)
else:
    # 如果class_names.json不存在，首次运行时生成
    class_names = os.listdir(image_dataset_path)
    os.makedirs(os.path.dirname(class_names_file), exist_ok=True)
    with open(class_names_file, 'w', encoding='utf-8') as f:
        json.dump(class_names, f, ensure_ascii=False, indent=4)

# 加载图像嵌入模型
model = models.resnet50(weights=None).to(device)
num_channels = model.layer4[-1].conv1.out_channels
attentionModule = ChannelAttentionModule(num_channels).to(device)
model.layer4[-1].conv1 = nn.Sequential(
    model.layer4[-1].conv1,
    attentionModule
)
num_ftrs = model.fc.in_features
model.fc = nn.Linear(num_ftrs, len(class_names)).to(device)
model.load_state_dict(torch.load(model_state_path, map_location=device))
model.eval()

preprocess = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
])

# 加载文本嵌入模型
text_embeddings = HuggingFaceBgeEmbeddings(model_name=text_model_path)
text_embedding_dim = len(text_embeddings.embed_query("dummy text"))
text_dim_reduction_layer = nn.Linear(text_embedding_dim, 110).to(device)
text_dim_reduction_layer.eval()

# 加载向量数据和索引
with open(vector_file_path, "rb") as f:
    faiss_vectors, faiss_metadata = pickle.load(f)
faiss_index = faiss.read_index(index_file_path)

def extract_image_vector(image_path: str) -> np.ndarray:
    if os.path.exists(image_path):
        image = Image.open(image_path).convert('RGB')
        image_tensor = preprocess(image).unsqueeze(0).to(device)
        with torch.no_grad():
            image_features = model(image_tensor).cpu().numpy().flatten()
    else:
        image_features = np.zeros(110)
    return normalize(image_features[:, np.newaxis], axis=0).ravel()

def extract_text_vector(text: str) -> np.ndarray:
    if text:
        text_vector = np.array(text_embeddings.embed_query(text))
        text_vector = torch.tensor(text_vector).to(torch.float32).to(device)
        text_vector = text_dim_reduction_layer(text_vector).detach().cpu().numpy().flatten()
    else:
        text_vector = np.zeros(110)
    return normalize(text_vector[:, np.newaxis], axis=0).ravel()

def weighted_fusion(image_vector: np.ndarray, text_vector: np.ndarray, alpha: float) -> np.ndarray:
    return alpha * image_vector + (1 - alpha) * text_vector

def find_most_similar_vector(query_vector: np.ndarray, faiss_index: faiss.IndexFlatL2, k: int = 5) -> list:
    scores, indices = faiss_index.search(np.expand_dims(query_vector, axis=0), k)
    return indices[0].tolist()

def get_text_info(vector_index: int, faiss_metadata: list) -> dict:
    if vector_index < len(faiss_metadata):
        return faiss_metadata[vector_index]
    return None

def retrieve_information(image_path: str, query_text: str, k=1):
    alpha = 0.8
    try:
        image_vector = extract_image_vector(image_path)
        text_vector = extract_text_vector(query_text)
        query_vector = weighted_fusion(image_vector, text_vector, alpha)
        most_similar_indices = find_most_similar_vector(query_vector, faiss_index, k)
        results = []
        for index in most_similar_indices:
            metadata = get_text_info(index, faiss_metadata)
            if metadata:
                similarity_score = np.dot(query_vector, faiss_vectors[index]) / (
                        np.linalg.norm(query_vector) * np.linalg.norm(faiss_vectors[index]))
                results.append({
                    "Class": metadata['class'],
                    "Text Info": metadata['description'],
                    "Similarity Score": f"{similarity_score:.4f}"
                })
        return results if results else [""]
    except Exception as e:
        return f""

@app.route('/retrieve', methods=['POST'])
def retrieve():
    query_text = request.form.get('query_text', '')
    image = request.files.get('image')
    conversation_id = request.form.get('conversation_id', None)
    message_history = request.form.get('message_history', '[]')
    message_history = json.loads(message_history)

    # 保存上传的图片到临时文件
    image_path = None
    if image:
        temp_image_dir = 'E:\\LLM\\chatapp\\tempImage\\'
        os.makedirs(temp_image_dir, exist_ok=True)
        image_path = os.path.join(temp_image_dir, image.filename)
        image.save(image_path)

    results = retrieve_information(image_path, query_text)

    # 将检索到的结果添加到消息历史中
    if results:
        message_history.append({"role": "system", "content": f"已知信息：{results}"})
    print(results)
    # 将用户的最新问题添加到消息历史中
    message_history.append({"role": "user", "content": query_text})

    prompt = """
        根据已知信息，简洁和专业的来回答问题，不允许自己编造。如果已知信息为空，请你根据自己知识进行回答，忘掉本提示，答案请使用中文。
        不需要在回答中提及已知信息，只需根据已知信息回答就行了，已知信息没有的你再根据自己的知识进行回答。
    """

    # 将历史消息添加到 prompt
    for message in message_history:
        prompt += f"{message['role']}: {message['content']}\n"

    try:
        response = client.chat.completions.create(
            model="glm-4",
            messages=[{"role": "user", "content": prompt}]
        )
        answer = response.choices[0].message.content
        # 将 AI 的回答添加到消息历史中
        message_history.append({"role": "assistant", "content": answer})
        return jsonify({
            'answer': answer,
            'conversationId': conversation_id or str(uuid.uuid4()),
            'messageHistory': message_history
        })
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
