package com.example.neo4japp.service;

import com.example.neo4japp.dto.EntityTypeDTO;
import com.example.neo4japp.util.Utils;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class EntityTypeServiceImpl implements EntityTypeService {

    @Autowired
    private Driver driver;

    // 获取所有实体类型
    @Override
    public List<EntityTypeDTO> fetchEntityTypes(String searchQuery) {
        List<EntityTypeDTO> entities = new ArrayList<>();
        try (Session session = driver.session()) {
            StringBuilder query = new StringBuilder("MATCH (et:EntityType)");
            Map<String, Object> params = new HashMap<>();

            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                query.append(" WHERE toLower(et.name) CONTAINS toLower($searchQuery)")
                        .append(" OR toLower(et.description) CONTAINS toLower($searchQuery)");
                params.put("searchQuery", searchQuery);
            }

            query.append(" RETURN et, keys(et) AS propertyKeys");

            Result result = session.run(query.toString(), params);

            while (result.hasNext()) {
                Record record = result.next();
                Map<String, Object> et = record.get("et").asMap();
                List<String> propertyKeys = record.get("propertyKeys").asList(Value::asString);
                Map<String, Object> properties = new HashMap<>();

                for (String key : propertyKeys) {
                    if (!Arrays.asList("name", "label", "create_time", "modify_time", "description").contains(key)) {
                        properties.put(key, et.get(key));
                    }
                }

                EntityTypeDTO dto = new EntityTypeDTO();
                dto.setId(record.get("et").asNode().id() + "");
                dto.setName((String) et.get("name"));
                dto.setLabel((String) et.get("label"));
                dto.setCreate_time(Utils.formatDate(et.get("create_time")));
                dto.setModify_time(Utils.formatDate(et.get("modify_time")));
                dto.setDescription((String) et.get("description"));
                dto.setProperties(properties);

                entities.add(dto);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
        return entities;
    }

    // 添加新的实体类型
    @Override
    public void addEntityType(String name, String description, Map<String, Object> additionalProperties) {
        try (Session session = driver.session()) {
            String create_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String modify_time = create_time;
            String label = generateLabel(name);

            Map<String, Object> properties = new HashMap<>();
            properties.put("name", name);
            properties.put("label", label);
            properties.put("description", description);
            properties.put("create_time", create_time);
            properties.put("modify_time", modify_time);
            if (additionalProperties != null) {
                properties.putAll(additionalProperties);
            }

            String query = "CREATE (et:EntityType $properties)";

            session.run(query, Collections.singletonMap("properties", properties));
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
    }

    // 删除实体类型
    @Override
    public void deleteEntityType(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (et:EntityType) WHERE id(et) = $id DETACH DELETE et";
            session.run(query, Collections.singletonMap("id", Long.valueOf(id)));
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
    }

    // 更新实体类型
    @Override
    public void updateEntityType(String id, String description, Map<String, Object> properties) {
        try (Session session = driver.session()) {
            String modify_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Map<String, Object> newProperties = new HashMap<>();
            newProperties.put("description", description);
            newProperties.put("modify_time", modify_time);
            if (properties != null) {
                newProperties.putAll(properties);
            }

            String query = "MATCH (et:EntityType) WHERE id(et) = $id SET et += $newProperties";

            Map<String, Object> params = new HashMap<>();
            params.put("id", Long.valueOf(id));
            params.put("newProperties", newProperties);

            session.run(query, params);
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
    }

    // 检查实体类型是否存在
    @Override
    public boolean entityTypeExists(String name) {
        boolean exists = false;
        try (Session session = driver.session()) {
            String query = "MATCH (et:EntityType {name: $name}) RETURN count(et) > 0 AS exists";
            Map<String, Object> params = Collections.singletonMap("name", name);
            Record record = session.run(query, params).single();
            exists = record.get("exists").asBoolean();
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
        return exists;
    }

    // 获取所有EntityType的标签
    @Override
    public Map<String, String> getAllEntityTypeLabels() {
        Map<String, String> typeLabels = new HashMap<>();
        try (Session session = driver.session()) {
            String query = "MATCH (et:EntityType) RETURN et.name AS name, et.label AS label";
            Result result = session.run(query);
            while (result.hasNext()) {
                Record record = result.next();
                typeLabels.put(record.get("name").asString(), record.get("label").asString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
        return typeLabels;
    }

    // 获取实体类型的数量
    @Override
    public List<Map<String, Object>> fetchEntityTypesCount() {
        List<Map<String, Object>> counts = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = "MATCH (e)-[:实体类型]->(et:EntityType) " +
                    "RETURN et.name AS type, count(e) AS count " +
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

    // 获取EntityType的标签（单个）
    @Override
    public String getLabelByEntityType(String typeName) throws Exception {
        String label = "";
        try (Session session = driver.session()) {
            String query = "MATCH (et:EntityType {name: $typeName}) RETURN et.label AS label";
            Map<String, Object> params = Collections.singletonMap("typeName", typeName);
            Record record = session.run(query, params).single();
            if (record == null || record.get("label") == null) {
                throw new Exception("EntityType with name \"" + typeName + "\" not found.");
            }
            label = record.get("label").asString();
        }
        return label;
    }

    // 辅助方法：生成标签（可选，如果不想依赖 Utils）
    private String generateLabel(String name) {
        return Utils.generateLabel(name);
    }
}
