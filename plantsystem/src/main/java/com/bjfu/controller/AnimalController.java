package com.bjfu.controller;


import com.bjfu.service.AnimalService;
import com.bjfu.pojo.Animal;
import com.bjfu.pojo.AnimalNum;
import com.bjfu.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AnimalController {

    @Autowired
    private AnimalService animalService;

    @GetMapping("/allAnimal")
    public Result fetchAllAnimal()
    {
        List<Animal> data=animalService.fetchAllAnimal();
        return Result.success(data);
    }


    @GetMapping("/level")
    public Result fetchLevelAnimal(@RequestParam String speciescategory)
    {
        List<Animal> data=animalService.fetchLevelAnimal(speciescategory);
        return Result.success(data);
    }


    @GetMapping("/oneanimal/{id}")
    public Result fetchOneAnimal(@PathVariable Integer id)
    {

        return  Result.success(animalService.fetchOneAnimal(id));
    }

    @GetMapping("/animalclasslist")
    public Result fetchAnimalClassList()
    {
        List<AnimalNum> data=animalService.fetchAnimalClassList();
        return Result.success(data);
    }
}
