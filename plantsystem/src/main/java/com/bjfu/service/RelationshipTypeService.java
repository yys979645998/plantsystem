package com.bjfu.service;

import com.bjfu.pojo.RelationshipTypeDTO;

import java.util.List;
import java.util.Map;

public interface RelationshipTypeService {

    // 获取所有关系类型
    List<RelationshipTypeDTO> fetchRelationshipTypes(String searchQuery);

    // 添加新的关系类型
    void addRelationshipType(String name, String description, Map<String, Object> additionalProperties);

    // 删除关系类型
    void deleteRelationshipType(String id);

    // 更新关系类型
    void updateRelationshipType(String id, String description, Map<String, Object> properties);

    // 检查关系类型是否存在
    boolean relationshipTypeExists(String name);

    // 获取关系类型的数量
    List<Map<String, Object>> fetchRelationshipsCount();
}
