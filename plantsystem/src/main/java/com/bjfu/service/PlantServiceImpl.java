package com.bjfu.service;

import com.bjfu.mapper.PlantMapper;
import com.bjfu.pojo.PageBean;
import com.bjfu.pojo.Plant;
import com.bjfu.pojo.PlantNum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlantServiceImpl implements PlantService {

    @Autowired
    private PlantMapper plantMapper;
    @Override
    public List<PlantNum> getPlantsNum() {
        return plantMapper.getPlantsNum();

    }

    @Override
    public List<Plant> getPlants(String protectionlevel) {
        return plantMapper.getPlants(protectionlevel);
    }

    @Override
    public Plant getOnePlant(String plantname) {
        return plantMapper.getOnePlant(plantname);
    }

    @Override
    public PageBean fetchEntitiy(Integer page, Integer pageSize) {
        Integer total = plantMapper.getEntityTotal();
        List<Plant> rows = plantMapper.getEntityList((page - 1) * pageSize, pageSize);


        return new PageBean(total, rows);
    }

    @Override
    public Plant fetchEntityById(Integer id) {
        return plantMapper.fetchEntityById(id);
    }
}
