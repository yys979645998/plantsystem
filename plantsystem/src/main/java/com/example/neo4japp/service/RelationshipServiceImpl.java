package com.example.neo4japp.service;

import com.example.neo4japp.dto.RelationshipDTO;
import com.example.neo4japp.util.Utils;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class RelationshipServiceImpl implements RelationshipService {

    @Autowired
    private Driver driver;

    @Autowired
    private EntityTypeService entityTypeService;

    // 获取所有关系
    @Override
    public List<RelationshipDTO> fetchRelationships(String searchQuery) {
        List<RelationshipDTO> relationships = new ArrayList<>();
        try (Session session = driver.session()) {
            StringBuilder query = new StringBuilder("MATCH (start)-[r]->(end) " +
                    "MATCH (start)-[:实体类型]->(:EntityType) " +
                    "MATCH (end)-[:实体类型]->(:EntityType)");
            Map<String, Object> params = new HashMap<>();

            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                query.append(" WHERE toLower(start.name) CONTAINS toLower($searchQuery)")
                        .append(" OR toLower(type(r)) CONTAINS toLower($searchQuery)")
                        .append(" OR toLower(end.name) CONTAINS toLower($searchQuery)");
                params.put("searchQuery", searchQuery);
            }

            query.append(" RETURN start, type(r) AS relationshipType, end, r, keys(r) AS propertyKeys");

            Result result = session.run(query.toString(), params);

            while (result.hasNext()) {
                Record record = result.next();
                Map<String, Object> start = record.get("start").asNode().asMap();
                Map<String, Object> end = record.get("end").asNode().asMap();
                Map<String, Object> r = record.get("r").asMap();
                List<String> propertyKeys = record.get("propertyKeys").asList(Value::asString);
                Map<String, Object> properties = new HashMap<>();

                for (String key : propertyKeys) {
                    if (!Arrays.asList("create_time", "modify_time", "description").contains(key)) {
                        properties.put(key, r.get(key));
                    }
                }

                RelationshipDTO dto = new RelationshipDTO();
                dto.setStartEntityId(record.get("start").asNode().id() + "");
                dto.setStartEntityName((String) start.get("name"));
                dto.setRelationshipType(record.get("relationshipType").asString());
                dto.setEndEntityId(record.get("end").asNode().id() + "");
                dto.setEndEntityName((String) end.get("name"));
                dto.setCreate_time(Utils.formatDate(r.get("create_time")));
                dto.setModify_time(Utils.formatDate(r.get("modify_time")));
                dto.setDescription((String) r.get("description"));
                dto.setProperties(properties);

                relationships.add(dto);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
        return relationships;
    }

    // 添加新的关系
    @Override
    public void addRelationship(String startEntityId, String relationshipType, String endEntityId, String description, Map<String, Object> additionalProperties) {
        try (Session session = driver.session()) {
            String create_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String modify_time = create_time;

            Map<String, Object> properties = new HashMap<>();
            properties.put("description", description);
            properties.put("create_time", create_time);
            properties.put("modify_time", modify_time);
            if (additionalProperties != null) {
                properties.putAll(additionalProperties);
            }

            String query = "MATCH (a), (b) " +
                    "WHERE id(a) = $startEntityId AND id(b) = $endEntityId " +
                    "CREATE (a)-[r:" + relationshipType + " $properties]->(b)";

            Map<String, Object> params = new HashMap<>();
            params.put("startEntityId", Long.valueOf(startEntityId));
            params.put("endEntityId", Long.valueOf(endEntityId));
            params.put("properties", properties);

            session.run(query, params);
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
    }

    // 删除关系
    @Override
    public void deleteRelationship(String startEntityId, String relationshipType, String endEntityId) {
        try (Session session = driver.session()) {
            String query = "MATCH (a)-[r:" + relationshipType + "]->(b) " +
                    "WHERE id(a) = $startEntityId AND id(b) = $endEntityId " +
                    "DELETE r";

            Map<String, Object> params = new HashMap<>();
            params.put("startEntityId", Long.valueOf(startEntityId));
            params.put("endEntityId", Long.valueOf(endEntityId));

            session.run(query, params);
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
    }

    // 更新关系
    @Override
    public void updateRelationship(String startEntityId, String relationshipType, String endEntityId, String description, Map<String, Object> properties) {
        try (Session session = driver.session()) {
            String modify_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Map<String, Object> newProperties = new HashMap<>();
            newProperties.put("description", description);
            newProperties.put("modify_time", modify_time);
            if (properties != null) {
                newProperties.putAll(properties);
            }

            String query = "MATCH (a)-[r:" + relationshipType + "]->(b) " +
                    "WHERE id(a) = $startEntityId AND id(b) = $endEntityId " +
                    "SET r += $newProperties";

            Map<String, Object> params = new HashMap<>();
            params.put("startEntityId", Long.valueOf(startEntityId));
            params.put("endEntityId", Long.valueOf(endEntityId));
            params.put("newProperties", newProperties);

            session.run(query, params);
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
    }
}
