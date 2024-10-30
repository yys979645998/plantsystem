package com.example.neo4japp.service;

import com.example.neo4japp.dto.PageBean;
import com.example.neo4japp.dto.Plant;
import com.example.neo4japp.dto.PlantNum;

import java.util.List;

public interface PlantService {
    List<PlantNum> getPlantsNum();

    List<Plant> getPlants(String protectionlevel);

    Plant getOnePlant(String plantname);

    PageBean fetchEntitiy(Integer page, Integer pageSize);

    Plant fetchEntityById(Integer id);
}
