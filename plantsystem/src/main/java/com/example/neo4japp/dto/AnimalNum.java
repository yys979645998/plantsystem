package com.example.neo4japp.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnimalNum {
    private String speciesCategory;
    private Integer count;
}
