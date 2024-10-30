package com.bjfu.controller;

import com.bjfu.pojo.EntityTypeDTO;
import com.bjfu.service.EntityTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entity-types")
public class EntityTypeController {

    @Autowired
    private EntityTypeService entityTypeService;

    // 获取所有实体类型
    @GetMapping
    public List<EntityTypeDTO> fetchEntityTypes(@RequestParam(required = false) String searchQuery) {
        return entityTypeService.fetchEntityTypes(searchQuery);
    }

    // 添加新的实体类型
    @PostMapping
    public void addEntityType(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String description = (String) payload.get("description");
        Map<String, Object> additionalProperties = (Map<String, Object>) payload.get("additionalProperties");
        entityTypeService.addEntityType(name, description, additionalProperties);
    }

    // 删除实体类型
    @DeleteMapping("/{id}")
    public void deleteEntityType(@PathVariable String id) {
        entityTypeService.deleteEntityType(id);
    }

    // 更新实体类型
    @PutMapping("/{id}")
    public void updateEntityType(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        String description = (String) payload.get("description");
        Map<String, Object> properties = (Map<String, Object>) payload.get("properties");
        entityTypeService.updateEntityType(id, description, properties);
    }

    // 检查实体类型是否存在
    @GetMapping("/exists")
    public boolean entityTypeExists(@RequestParam String name) {
        return entityTypeService.entityTypeExists(name);
    }

    // 获取实体类型数量
    @GetMapping("/count")
    public List<Map<String, Object>> fetchEntityTypesCount() {
        return entityTypeService.fetchEntityTypesCount();
    }
}
