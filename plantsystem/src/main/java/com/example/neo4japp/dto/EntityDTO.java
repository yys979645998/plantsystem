package com.example.neo4japp.dto;

import lombok.Data;
import java.util.Map;

@Data
public class EntityDTO {
    private String id;
    private String name;
    private String type;
    private String create_time;
    private String modify_time;
    private String description;
    private Map<String, Object> properties;
}
