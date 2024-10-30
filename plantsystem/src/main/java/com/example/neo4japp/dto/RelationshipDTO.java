package com.example.neo4japp.dto;

import lombok.Data;
import java.util.Map;

@Data
public class RelationshipDTO {
    private String startEntityId;
    private String startEntityName;
    private String relationshipType;
    private String endEntityId;
    private String endEntityName;
    private String create_time;
    private String modify_time;
    private String description;
    private Map<String, Object> properties;
}
