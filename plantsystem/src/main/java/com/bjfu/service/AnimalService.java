package com.bjfu.service;

import com.bjfu.pojo.Animal;
import com.bjfu.pojo.AnimalNum;

import java.util.List;

public interface AnimalService {

    List<Animal> fetchAllAnimal();

    List<Animal> fetchLevelAnimal(String speciescategory);

    Animal fetchOneAnimal(Integer id);

    List<AnimalNum> fetchAnimalClassList();
}
