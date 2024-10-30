package com.example.neo4japp.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Animal {
    private Integer id; // 自增唯一
    private String speciesName; // 物种名称
    private String scientificName; // 学名
    private String classification; // 分类
    private String description; // 描述
    private String habitatInfo; //
    private String biologyInfo; // 生物学信息
    private String behaviorInfo; // 行为信息
    private String conservationInfo; // 保护信息
    private String identifyingCharacteristics; // 鉴别特征
    private String speciesCategory; // 物种类别
    private String image; // 图片
}
