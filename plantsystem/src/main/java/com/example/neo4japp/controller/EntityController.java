package com.example.neo4japp.controller;

import com.example.neo4japp.dto.EntityDTO;
import com.example.neo4japp.service.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entities")
public class EntityController {

    @Autowired
    private EntityService entityService;

    // 获取所有实体
    @GetMapping
    public List<EntityDTO> fetchEntities(@RequestParam(required = false) String search) {
        return entityService.fetchEntities(search);
    }

    // 添加新的实体
    @PostMapping
    public void addEntity(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String type = (String) payload.get("type");
        String description = (String) payload.get("description");
        Map<String, Object> additionalProperties = (Map<String, Object>) payload.get("additionalProperties");
        entityService.addEntity(name, type, description, additionalProperties);
    }

    // 删除实体
    @DeleteMapping("/{id}")
    public void deleteEntity(@PathVariable String id, @RequestParam String type) {
        entityService.deleteEntity(id, type);
    }

    // 更新实体
    @PutMapping("/{id}")
    public void updateEntity(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        String newType = (String) payload.get("newType");
        String name = (String) payload.get("name");
        String description = (String) payload.get("description");
        Map<String, Object> properties = (Map<String, Object>) payload.get("properties");
        List<String> propertiesToRemove = (List<String>) payload.get("propertiesToRemove");
        entityService.updateEntity(id, newType, name, description, properties, propertiesToRemove);
    }

    // 检查实体是否存在
    @GetMapping("/exists")
    public boolean entityExists(@RequestParam String type, @RequestParam String name) {
        return entityService.entityExists(type, name);
    }

    // 获取实体及其类型和颜色
    @GetMapping("/with-types")
    public Map<String, Object> fetchEntitiesWithTypes() {
        return entityService.fetchEntitiesWithTypes();
    }
}
