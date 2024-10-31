package com.bjfu.service;

import com.bjfu.pojo.EntityTypeDTO;

import java.util.List;
import java.util.Map;

public interface EntityTypeService {

    // 获取所有实体类型
    List<EntityTypeDTO> fetchEntityTypes(String searchQuery);

    // 添加新的实体类型
    void addEntityType(String name, String description, Map<String, Object> additionalProperties);

    // 删除实体类型
    void deleteEntityType(String id);

    // 更新实体类型
    void updateEntityType(String id, String description, Map<String, Object> properties);

    // 检查实体类型是否存在
    boolean entityTypeExists(String name);

    // 获取所有EntityType的标签
    Map<String, String> getAllEntityTypeLabels();

    // 获取EntityType的数量
    List<Map<String, Object>> fetchEntityTypesCount();

    // 获取EntityType的标签（单个）
    String getLabelByEntityType(String typeName) throws Exception;
}
