package com.bjfu.controller;

import com.bjfu.pojo.RelationshipTypeDTO;
import com.bjfu.service.RelationshipTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/relationship-types")
public class RelationshipTypeController {

    @Autowired
    private RelationshipTypeService relationshipTypeService;

    // 获取所有关系类型
    @GetMapping
    public List<RelationshipTypeDTO> fetchRelationshipTypes(@RequestParam(required = false) String searchQuery) {
        return relationshipTypeService.fetchRelationshipTypes(searchQuery);
    }

    // 添加新的关系类型
    @PostMapping
    public void addRelationshipType(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String description = (String) payload.get("description");
        Map<String, Object> additionalProperties = (Map<String, Object>) payload.get("additionalProperties");
        relationshipTypeService.addRelationshipType(name, description, additionalProperties);
    }

    // 删除关系类型
    @DeleteMapping("/{id}")
    public void deleteRelationshipType(@PathVariable String id) {
        relationshipTypeService.deleteRelationshipType(id);
    }

    // 更新关系类型
    @PutMapping("/{id}")
    public void updateRelationshipType(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        String description = (String) payload.get("description");
        Map<String, Object> properties = (Map<String, Object>) payload.get("properties");
        relationshipTypeService.updateRelationshipType(id, description, properties);
    }

    // 检查关系类型是否存在
    @GetMapping("/exists")
    public boolean relationshipTypeExists(@RequestParam String name) {
        return relationshipTypeService.relationshipTypeExists(name);
    }

    // 获取关系类型数量
    @GetMapping("/count")
    public List<Map<String, Object>> fetchRelationshipsCount() {
        return relationshipTypeService.fetchRelationshipsCount();
    }
}
