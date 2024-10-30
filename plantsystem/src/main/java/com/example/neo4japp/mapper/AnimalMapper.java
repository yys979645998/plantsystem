package com.example.neo4japp.mapper;


import com.example.neo4japp.dto.Animal;
import com.example.neo4japp.dto.AnimalNum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnimalMapper {


    @Select("select * from species")
    List<Animal> fetchAllAnimal();


    @Select("select * from species where speciescategory=#{speciescategory}")
    List<Animal> fetchLevelAnimal(String speciescategory);


    @Select("select * from species where id=#{id}")
    Animal fetchOneAnimal(Integer id);


    @Select("select speciesCategory, count(*) as count from species group by speciesCategory")
    List<AnimalNum> fetchAnimalClassList();
}
