package com.bjfu.service;


import com.bjfu.mapper.AnimalMapper;
import com.bjfu.pojo.Animal;
import com.bjfu.pojo.AnimalNum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnimalServiceImpl implements AnimalService {

    @Autowired
    private AnimalMapper animalMapper;

    @Override
    public List<Animal> fetchAllAnimal() {
        return  animalMapper.fetchAllAnimal();
    }

    @Override
    public List<Animal> fetchLevelAnimal(String speciescategory) {
        return animalMapper.fetchLevelAnimal(speciescategory);
    }

    @Override
    public Animal fetchOneAnimal(Integer id) {
        return animalMapper.fetchOneAnimal(id);
    }

    @Override
    public List<AnimalNum> fetchAnimalClassList() {
        return animalMapper.fetchAnimalClassList();
    }
}
