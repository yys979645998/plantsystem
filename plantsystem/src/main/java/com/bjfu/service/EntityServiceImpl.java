package com.bjfu.service;

import com.bjfu.pojo.EntityDTO;
import com.bjfu.util.Utils;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

import java.util.UUID;

@Service
public class EntityServiceImpl implements EntityService {

    @Autowired
    private Driver driver;

    @Autowired
    private EntityTypeService entityTypeService;

    // 获取所有实体
    @Override
    public List<EntityDTO> fetchEntities(String search) {
        List<EntityDTO> entities = new ArrayList<>();
        try (Session session = driver.session()) {
            StringBuilder query = new StringBuilder("MATCH (e)-[:实体类型]->(et:EntityType)");
            Map<String, Object> params = new HashMap<>();

            if (search != null && !search.trim().isEmpty()) {
                query.append(" WHERE toLower(e.name) CONTAINS toLower($search)")
                        .append(" OR toLower(et.name) CONTAINS toLower($search)");
                params.put("search", search);
            }

            query.append(" RETURN e, et, keys(e) AS propertyKeys");

            Result result = session.run(query.toString(), params);

            while (result.hasNext()) {
                Record record = result.next();
                Map<String, Object> e = record.get("e").asMap();
                Map<String, Object> et = record.get("et").asMap();
                List<String> propertyKeys = record.get("propertyKeys").asList(Value::asString);
                Map<String, Object> properties = new HashMap<>();

                for (String key : propertyKeys) {
                    if (!Arrays.asList("name", "create_time", "modify_time", "description", "entity_type").contains(key)) {
                        properties.put(key, e.get(key));
                    }
                }

                EntityDTO dto = new EntityDTO();
                dto.setId(record.get("e").asNode().id() + "");
                dto.setName((String) e.get("name"));
                dto.setType((String) et.get("name"));
                dto.setCreate_time(Utils.formatDate(e.get("create_time")));
                dto.setModify_time(Utils.formatDate(e.get("modify_time")));
                dto.setDescription((String) e.get("description"));
                dto.setProperties(properties);

                entities.add(dto);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
        return entities;
    }

    // 添加新的实体
    @Override
    public void addEntity(String name, String type, String description, Map<String, Object> additionalProperties) {
        try (Session session = driver.session()) {
            String create_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String modify_time = create_time;

            // 获取对应的标签
            String label = entityTypeService.getLabelByEntityType(type);

            // 基础属性
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", name);
            properties.put("description", description);
            properties.put("create_time", create_time);
            properties.put("modify_time", modify_time);

            // 合并额外属性
            if (additionalProperties != null) {
                properties.putAll(additionalProperties);
            }

            // 如果是 '植物' 类型，添加特定的属性
            if ("植物".equals(type)) {
                properties.put("plant_id", UUID.randomUUID().toString());
            }

            String query = "MATCH (et:EntityType {name: $type}) " +
                    "CREATE (e) " +
                    "SET e:" + label + " " +
                    "SET e += $properties " +
                    "CREATE (e)-[:实体类型]->(et)";

            Map<String, Object> params = new HashMap<>();
            params.put("type", type);
            params.put("properties", properties);

            session.run(query, params);
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
    }

    // 删除实体
    @Override
    public void deleteEntity(String id, String type) {
        try (Session session = driver.session()) {
            String label = entityTypeService.getLabelByEntityType(type);
            String query = "MATCH (e:" + label + ")-[:实体类型]->(:EntityType {name: $type}) " +
                    "WHERE id(e) = $id " +
                    "DETACH DELETE e";

            Map<String, Object> params = new HashMap<>();
            params.put("id", Long.valueOf(id));
            params.put("type", type);

            session.run(query, params);
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
    }

    // 更新实体
    @Override
    public void updateEntity(String id, String newType, String name, String description, Map<String, Object> properties, List<String> propertiesToRemove) {
        try (Session session = driver.session()) {
            // 1. 获取当前实体的类型和标签
            String fetchQuery = "MATCH (e)-[:实体类型]->(et:EntityType) " +
                    "WHERE id(e) = $id " +
                    "RETURN et.name AS currentType, et.label AS currentLabel";

            Record fetchRecord = session.run(fetchQuery, Collections.singletonMap("id", Long.valueOf(id))).single();

            if (fetchRecord == null) {
                throw new RuntimeException("Entity with ID " + id + " not found.");
            }

            String currentType = fetchRecord.get("currentType").asString();
            String currentLabel = fetchRecord.get("currentLabel").asString();

            // 2. 获取新的标签
            String newLabel = entityTypeService.getLabelByEntityType(newType);

            // 3. 合并属性
            String modify_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Map<String, Object> newProperties = new HashMap<>();
            newProperties.put("name", name);
            newProperties.put("description", description);
            newProperties.put("modify_time", modify_time);
            if (properties != null) {
                newProperties.putAll(properties);
            }

            Map<String, Object> params = new HashMap<>();
            params.put("id", Long.valueOf(id));
            params.put("currentType", currentType);
            params.put("newType", newType);
            params.put("newLabel", newLabel);
            params.put("newProperties", newProperties);

            StringBuilder query = new StringBuilder();

            if (!currentType.equals(newType)) {
                // 类型发生变化
                query.append("MATCH (e)-[r:实体类型]->(et:EntityType {name: $currentType}) ")
                        .append("WHERE id(e) = $id ")
                        .append("MATCH (newEt:EntityType {name: $newType}) ")
                        .append("REMOVE e:" + currentLabel + " ")
                        .append("SET e:" + newLabel + " ")
                        .append("SET e += $newProperties ")
                        .append("DELETE r ")
                        .append("MERGE (e)-[:实体类型]->(newEt) ");
            } else {
                // 类型未变化
                query.append("MATCH (e)-[:实体类型]->(et:EntityType {name: $newType}) ")
                        .append("WHERE id(e) = $id ")
                        .append("SET e += $newProperties ");
            }

            // 处理被删除的属性
            if (propertiesToRemove != null && !propertiesToRemove.isEmpty()) {
                List<String> removeProps = new ArrayList<>();
                for (String prop : propertiesToRemove) {
                    removeProps.add("e.`" + prop + "`");
                }
                query.append("REMOVE " + String.join(", ", removeProps) + " ");
                System.out.println("Removing properties: " + String.join(", ", removeProps));
            }

            session.run(query.toString(), params);
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
    }

    // 检查实体是否存在
    @Override
    public boolean entityExists(String type, String name) {
        boolean exists = false;
        try (Session session = driver.session()) {
            String query = "MATCH (e)-[:实体类型]->(et:EntityType {name: $type}) " +
                    "WHERE e.name = $name " +
                    "RETURN count(e) > 0 AS exists";
            Map<String, Object> params = new HashMap<>();
            params.put("type", type);
            params.put("name", name);
            Record record = session.run(query, params).single();
            exists = record.get("exists").asBoolean();
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }
        return exists;
    }

    // 获取实体及其类型和分配颜色
    @Override
    public Map<String, Object> fetchEntitiesWithTypes() {
        Map<String, Object> resultMap = new HashMap<>();
        List<EntityDTO> entities = new ArrayList<>();
        Set<String> entityTypes = new HashSet<>();
        Map<String, String> typeLabels = new HashMap<>();

        try (Session session = driver.session()) {
            String query = "MATCH (e)-[:实体类型]->(et:EntityType) " +
                    "RETURN e, et, keys(e) AS propertyKeys";
            Result result = session.run(query);

            while (result.hasNext()) {
                Record record = result.next();
                Map<String, Object> e = record.get("e").asMap();
                Map<String, Object> et = record.get("et").asMap();
                List<String> propertyKeys = record.get("propertyKeys").asList(Value::asString);
                Map<String, Object> properties = new HashMap<>();

                for (String key : propertyKeys) {
                    if (!Arrays.asList("name", "create_time", "modify_time", "description", "entity_type").contains(key)) {
                        properties.put(key, e.get(key));
                    }
                }

                EntityDTO dto = new EntityDTO();
                dto.setId(record.get("e").asNode().id() + "");
                dto.setName((String) e.get("name"));
                dto.setType((String) et.get("name"));
                dto.setCreate_time(Utils.formatDate(e.get("create_time")));
                dto.setModify_time(Utils.formatDate(e.get("modify_time")));
                dto.setDescription((String) e.get("description"));
                dto.setProperties(properties);

                entities.add(dto);
                entityTypes.add((String) et.get("name"));
            }

            // 获取所有EntityType的名称和标签
            typeLabels = entityTypeService.getAllEntityTypeLabels();

            // 分配颜色
            // 由于 Java 不直接支持 D3.js，您需要在前端处理颜色分配
            // 这里只返回类型列表和标签

            resultMap.put("entities", entities);
            resultMap.put("entityTypes", new ArrayList<>(entityTypes));
            resultMap.put("typeLabels", typeLabels);
        } catch (Exception ex) {
            ex.printStackTrace();
            // 可以根据需要抛出自定义异常
        }

        return resultMap;
    }
}
