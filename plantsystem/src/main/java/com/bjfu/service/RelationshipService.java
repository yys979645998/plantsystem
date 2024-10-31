package com.bjfu.service;

import com.bjfu.pojo.RelationshipDTO;

import java.util.List;
import java.util.Map;

public interface RelationshipService {

    // 获取所有关系
    List<RelationshipDTO> fetchRelationships(String searchQuery);

    // 添加新的关系
    void addRelationship(String startEntityId, String relationshipType, String endEntityId, String description, Map<String, Object> additionalProperties);

    // 删除关系
    void deleteRelationship(String startEntityId, String relationshipType, String endEntityId);

    // 更新关系
    void updateRelationship(String startEntityId, String relationshipType, String endEntityId, String description, Map<String, Object> properties);
}
