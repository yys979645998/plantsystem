package com.bjfu.service;

import com.bjfu.pojo.PageBean;
import com.bjfu.pojo.Plant;
import com.bjfu.pojo.PlantNum;

import java.util.List;

public interface PlantService {
    List<PlantNum> getPlantsNum();

    List<Plant> getPlants(String protectionlevel);

    Plant getOnePlant(String plantname);

    PageBean fetchEntitiy(Integer page, Integer pageSize);

    Plant fetchEntityById(Integer id);
}
