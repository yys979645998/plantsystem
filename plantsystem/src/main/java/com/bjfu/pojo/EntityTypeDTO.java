package com.bjfu.pojo;

import lombok.Data;
import java.util.Map;

@Data
public class EntityTypeDTO {
    private String id;
    private String name;
    private String label;
    private String create_time;
    private String modify_time;
    private String description;
    private Map<String, Object> properties;
}
