package com.example.neo4japp.mapper;


import com.example.neo4japp.dto.Plant;
import com.example.neo4japp.dto.PlantNum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlantMapper {
    @Select("SELECT protectionlevel, COUNT(*) as count\n" +
            "FROM plant\n" +
            "GROUP BY protectionlevel;\n")
    List<PlantNum> getPlantsNum();

    @Select("select * from plant where protectionlevel = #{protectionlevel};")
    List<Plant> getPlants(String protectionlevel);

    @Select("select * from plant where cn = #{plantname}")
    Plant getOnePlant(String plantname);

    @Select("SELECT COUNT(*) as total FROM plant;")
    Integer getEntityTotal();


    @Select("Select * FROM plant LIMIT #{start}, #{pageSize};")
    List<Plant> getEntityList(int start, Integer pageSize);


    @Select("SELECT * FROM plant WHERE id = #{id};")
    Plant fetchEntityById(Integer id);
}
