package com.bjfu.service;

import com.bjfu.pojo.EntityDTO;

import java.util.List;
import java.util.Map;

public interface EntityService {

    // 获取所有实体
    List<EntityDTO> fetchEntities(String search);

    // 添加新的实体
    void addEntity(String name, String type, String description, Map<String, Object> additionalProperties);

    // 删除实体
    void deleteEntity(String id, String type);

    // 更新实体
    void updateEntity(String id, String newType, String name, String description, Map<String, Object> properties, List<String> propertiesToRemove);

    // 检查实体是否存在
    boolean entityExists(String type, String name);

    // 获取实体及其类型和颜色
    Map<String, Object> fetchEntitiesWithTypes();
}
