package com.example.neo4japp.controller;

import com.example.neo4japp.service.PlantService;
import com.example.neo4japp.dto.PageBean;
import com.example.neo4japp.dto.Plant;
import com.example.neo4japp.dto.PlantNum;
import com.example.neo4japp.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class PlantController {
    @Autowired
    private PlantService plantService;
    @GetMapping("/getplantsnum")
    public Result getplantsnum(){
        List<PlantNum> data = plantService.getPlantsNum();
        return Result.success(data);
    }

    @GetMapping("/getPlants")
    Result getPlants(@RequestParam String protectionlevel){
        List<Plant> data = plantService.getPlants(protectionlevel);
        return Result.success(data);
    }

    @GetMapping("/getOnePlant")
    Result getOnePlant(@RequestParam String plantname){
        Plant data = plantService.getOnePlant(plantname);
        return Result.success(data);
    }

    @GetMapping("/fetchEntitiy")
    Result fetchEntitiy(@RequestParam( defaultValue = "1")Integer page, @RequestParam( defaultValue = "5")Integer pageSize){
        PageBean data= plantService.fetchEntitiy(page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/fetchEntityById/{id}")
    Result fetchEntityById(@PathVariable("id") Integer id){
        Plant data = plantService.fetchEntityById(id);
        return Result.success(data);
    }
}
