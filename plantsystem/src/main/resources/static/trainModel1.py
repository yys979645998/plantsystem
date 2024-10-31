import os
import torch
import torch.nn as nn
import torch.optim as optim
from PIL import Image
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms, models
from tqdm import tqdm
from sklearn.model_selection import train_test_split

# 定义文件路径
image_dataset_path = r"E:\LLM\dataset\110plant\train"
model_save_path = "resnet50Enhanced.pth"

# 加载ResNet50模型，并调整为对应GPU或CPU
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
resnet_model = models.resnet50(pretrained=True).to(device)


# 添加注意力机制
class ChannelAttentionModule(nn.Module):
    def __init__(self, num_channels, reduction_ratio=16):
        super(ChannelAttentionModule, self).__init__()
        self.avg_pool = nn.AdaptiveAvgPool2d(1)
        self.max_pool = nn.AdaptiveMaxPool2d(1)
        self.fc = nn.Sequential(
            nn.Linear(num_channels, num_channels // reduction_ratio, bias=False),
            nn.ReLU(),
            nn.Linear(num_channels // reduction_ratio, num_channels, bias=False),
            nn.Sigmoid()
        )

    def forward(self, x):
        avg_out = self.fc(self.avg_pool(x).view(x.size(0), -1)).to(x.device)
        max_out = self.fc(self.max_pool(x).view(x.size(0), -1)).to(x.device)
        out = avg_out + max_out
        return x * out.view(x.size(0), x.size(1), 1, 1)


class SpatialAttentionModule(nn.Module):
    def __init__(self, kernel_size=7):
        super(SpatialAttentionModule, self).__init__()
        assert kernel_size % 2 == 1, "Kernel size should be odd for proper padding."
        padding_size = kernel_size // 2
        self.conv = nn.Conv2d(2, 1, kernel_size=kernel_size, padding=padding_size, bias=False)
        self.sigmoid = nn.Sigmoid()

    def forward(self, x):
        max_out, _ = torch.max(x, dim=1, keepdim=True)
        avg_out = torch.mean(x, dim=1, keepdim=True)
        out = torch.cat([max_out, avg_out], dim=1)
        out = self.conv(out)
        return x * self.sigmoid(out)


def insert_attention_modules(model):
    num_channels = model.layer4[-1].conv1.out_channels
    ca_module = ChannelAttentionModule(num_channels, reduction_ratio=8).to(device)
    sa_module = SpatialAttentionModule().to(device)
    model.layer4[-1].conv1 = nn.Sequential(
        model.layer4[-1].conv1,
        ca_module,
        sa_module
    )
    return model


resnet_model = insert_attention_modules(resnet_model)

# 图像预处理
preprocess = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
])


class CustomDataset(Dataset):
    def __init__(self, image_paths, labels):
        self.image_paths = image_paths
        self.labels = labels

    def __len__(self):
        return len(self.image_paths)

    def __getitem__(self, idx):
        image_path = self.image_paths[idx]
        label_index = self.labels[idx]
        image = Image.open(image_path).convert('RGB')
        image_tensor = preprocess(image)
        return image_tensor, label_index


def load_data(image_dataset_path):
    image_paths = []
    labels = []
    label_to_index = {}
    index = 0

    for species_name in os.listdir(image_dataset_path):
        species_dir = os.path.join(image_dataset_path, species_name)
        if os.path.isdir(species_dir):
            for image_name in os.listdir(species_dir):
                image_paths.append(os.path.join(species_dir, image_name))
                if species_name not in label_to_index:
                    label_to_index[species_name] = index
                    index += 1
                labels.append(label_to_index[species_name])

    # Split data
    train_paths, val_paths, train_labels, val_labels = train_test_split(
        image_paths, labels, test_size=0.1, random_state=42, stratify=labels
    )
    return train_paths, train_labels, val_paths, val_labels, len(label_to_index)


train_paths, train_labels, val_paths, val_labels, num_classes = load_data(image_dataset_path)
train_dataset = CustomDataset(train_paths, train_labels)
val_dataset = CustomDataset(val_paths, val_labels)

train_loader = DataLoader(train_dataset, batch_size=32, shuffle=True)
val_loader = DataLoader(val_dataset, batch_size=32, shuffle=False)

# 更新模型的全连接层以匹配类别数
resnet_model.fc = nn.Linear(resnet_model.fc.in_features, num_classes).to(device)

# 定义优化器和损失函数
optimizer = optim.Adam(filter(lambda p: p.requires_grad, resnet_model.parameters()), lr=1e-4)
loss_fn = nn.CrossEntropyLoss()
scheduler = optim.lr_scheduler.StepLR(optimizer, step_size=3, gamma=0.1)


def train_and_validate(num_epochs=15):
    for epoch in range(num_epochs):
        # Training
        resnet_model.train()
        total_loss = 0
        correct = 0
        total = 0
        progress_bar = tqdm(enumerate(train_loader), total=len(train_loader), desc=f"Epoch {epoch + 1}/{num_epochs}")
        for batch_idx, (images, labels) in progress_bar:
            images = images.to(device)
            labels = labels.to(device)

            optimizer.zero_grad()
            outputs = resnet_model(images)
            loss = loss_fn(outputs, labels)
            loss.backward()
            optimizer.step()

            total_loss += loss.item()
            _, predicted = torch.max(outputs.data, 1)
            total += labels.size(0)
            correct += (predicted == labels).sum().item()
            progress_bar.set_description(
                f"Train Loss: {total_loss / (batch_idx + 1):.4f} Acc: {100. * correct / total:.2f}%")

        # Validation
        resnet_model.eval()
        val_loss = 0
        val_correct = 0
        val_total = 0
        with torch.no_grad():
            for images, labels in val_loader:
                images = images.to(device)
                labels = labels.to(device)
                outputs = resnet_model(images)
                loss = loss_fn(outputs, labels)
                val_loss += loss.item()
                _, predicted = torch.max(outputs.data, 1)
                val_total += labels.size(0)
                val_correct += (predicted == labels).sum().item()
        val_acc = 100. * val_correct / val_total
        print(f'Validation Loss: {val_loss / len(val_loader):.4f} Acc: {val_acc:.2f}%')

        scheduler.step()

    torch.save(resnet_model.state_dict(), model_save_path)


if __name__ == '__main__':
    train_and_validate()
