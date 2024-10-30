package com.example.neo4japp.service;

import com.example.neo4japp.dto.Animal;
import com.example.neo4japp.dto.AnimalNum;

import java.util.List;

public interface AnimalService {

    List<Animal> fetchAllAnimal();

    List<Animal> fetchLevelAnimal(String speciescategory);

    Animal fetchOneAnimal(Integer id);

    List<AnimalNum> fetchAnimalClassList();
}
