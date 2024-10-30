package com.example.neo4japp.controller;

import com.example.neo4japp.dto.RelationshipDTO;
import com.example.neo4japp.service.RelationshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/relationships")
public class RelationshipController {

    @Autowired
    private RelationshipService relationshipService;

    // 获取所有关系
    @GetMapping
    public List<RelationshipDTO> fetchRelationships(@RequestParam(required = false) String searchQuery) {
        return relationshipService.fetchRelationships(searchQuery);
    }

    // 添加新的关系
    @PostMapping
    public void addRelationship(@RequestBody Map<String, Object> payload) {
        String startEntityId = (String) payload.get("startEntityId");
        String relationshipType = (String) payload.get("relationshipType");
        String endEntityId = (String) payload.get("endEntityId");
        String description = (String) payload.get("description");
        Map<String, Object> additionalProperties = (Map<String, Object>) payload.get("additionalProperties");
        relationshipService.addRelationship(startEntityId, relationshipType, endEntityId, description, additionalProperties);
    }

    // 删除关系
    @DeleteMapping
    public void deleteRelationship(@RequestParam String startEntityId, @RequestParam String relationshipType, @RequestParam String endEntityId) {
        relationshipService.deleteRelationship(startEntityId, relationshipType, endEntityId);
    }

    // 更新关系
    @PutMapping
    public void updateRelationship(@RequestBody Map<String, Object> payload) {
        String startEntityId = (String) payload.get("startEntityId");
        String relationshipType = (String) payload.get("relationshipType");
        String endEntityId = (String) payload.get("endEntityId");
        String description = (String) payload.get("description");
        Map<String, Object> properties = (Map<String, Object>) payload.get("properties");
        relationshipService.updateRelationship(startEntityId, relationshipType, endEntityId, description, properties);
    }
}
