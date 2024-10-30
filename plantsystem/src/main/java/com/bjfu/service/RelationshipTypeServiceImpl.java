package com.bjfu.service;

import com.bjfu.pojo.RelationshipTypeDTO;
import com.bjfu.util.Utils;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class RelationshipTypeServiceImpl implements RelationshipTypeService {

    @Autowired
    private Driver driver;

    // 获取所有关系类型
    @Override
    public List<RelationshipTypeDTO> fetchRelationshipTypes(String searchQuery) {
        List<RelationshipTypeDTO> relationships = new ArrayList<>();
        try (Session session = driver.session()) {
            StringBuilder query = new StringBuilder("MATCH (rt:RelationshipType)");
            Map<String, Object> params = new HashMap<>();

            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                query.append(" WHERE toLower(rt.name) CONTAINS toLower($searchQuery)")
                        .append(" OR toLower(rt.description) CONTAINS toLower($searchQuery)");
                params.put("searchQuery", searchQuery);
            }

            query.append(" RETURN rt, keys(rt) AS propertyKeys");

            Result result = session.run(query.toString(), params);

            while (result.hasNext()) {
                Record record = result.next();
                Map<String, Object> rt = record.get("rt").asMap();
                List<String> propertyKeys = record.get("propertyKeys").asList(Value::asString);
                Map<String, Object> properties = new HashMap<>();

                for (String key : propertyKeys) {
                    if (!Arrays.asList("name", "create_time", "modify_time", "description").contains(key)) {
                        properties.put(key, rt.get(key));
                    }
                }

                RelationshipTypeDTO dto = new RelationshipTypeDTO();
                dto.setId(record.get("rt").asNode().id() + "");
                dto.setName((String) rt.get("name"));
                dto.setCreate_time(Utils.formatDate(rt.get("create_time")));
                dto.setModify_time(Utils.formatDate(rt.get("modify_time")));
                dto.setDescription((String) rt.get("description"));
                dto.setProperties(properties);

                relationships.add(dto);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
        return relationships;
    }

    // 添加新的关系类型
    @Override
    public void addRelationshipType(String name, String description, Map<String, Object> additionalProperties) {
        try (Session session = driver.session()) {
            String create_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String modify_time = create_time;

            Map<String, Object> properties = new HashMap<>();
            properties.put("name", name);
            properties.put("description", description);
            properties.put("create_time", create_time);
            properties.put("modify_time", modify_time);
            if (additionalProperties != null) {
                properties.putAll(additionalProperties);
            }

            String query = "CREATE (rt:RelationshipType $properties)";

            session.run(query, Collections.singletonMap("properties", properties));
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
    }

    // 删除关系类型
    @Override
    public void deleteRelationshipType(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (rt:RelationshipType) WHERE id(rt) = $id DELETE rt";
            session.run(query, Collections.singletonMap("id", Long.valueOf(id)));
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
    }

    // 更新关系类型
    @Override
    public void updateRelationshipType(String id, String description, Map<String, Object> properties) {
        try (Session session = driver.session()) {
            String modify_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Map<String, Object> newProperties = new HashMap<>();
            newProperties.put("description", description);
            newProperties.put("modify_time", modify_time);
            if (properties != null) {
                newProperties.putAll(properties);
            }

            String query = "MATCH (rt:RelationshipType) WHERE id(rt) = $id SET rt += $newProperties";

            Map<String, Object> params = new HashMap<>();
            params.put("id", Long.valueOf(id));
            params.put("newProperties", newProperties);

            session.run(query, params);
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
    }

    // 检查关系类型是否存在
    @Override
    public boolean relationshipTypeExists(String name) {
        boolean exists = false;
        try (Session session = driver.session()) {
            String query = "MATCH (rt:RelationshipType {name: $name}) RETURN count(rt) > 0 AS exists";
            Map<String, Object> params = Collections.singletonMap("name", name);
            Record record = session.run(query, params).single();
            exists = record.get("exists").asBoolean();
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
        return exists;
    }

    // 获取关系类型的数量
    @Override
    public List<Map<String, Object>> fetchRelationshipsCount() {
        List<Map<String, Object>> counts = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = "MATCH ()-[r]->() " +
                    "WHERE EXISTS((startNode(r))-[:实体类型]->(:EntityType)) AND " +
                    "EXISTS((endNode(r))-[:实体类型]->(:EntityType)) " +
                    "RETURN type(r) AS type, count(r) AS count " +
                    "ORDER BY count DESC";
            Result result = session.run(query);
            while (result.hasNext()) {
                Record record = result.next();
                Map<String, Object> map = new HashMap<>();
                map.put("type", record.get("type").asString());
                map.put("count", record.get("count").asLong());
                counts.add(map);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
        return counts;
    }
}
