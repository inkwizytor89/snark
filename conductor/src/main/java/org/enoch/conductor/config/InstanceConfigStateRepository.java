package org.enoch.conductor.config;

import org.enoch.common.config.ConfigDefinition;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InstanceConfigStateRepository {

    private final ConcurrentHashMap<String, List<ConfigDefinition>> currentConfigsByInstance = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<ConfigDefinition>> definitionsByInstance = new ConcurrentHashMap<>();

    public void saveConfigs(String instanceId, List<ConfigDefinition> configs) {
        currentConfigsByInstance.put(instanceId, new ArrayList<>(configs));
    }

    public List<ConfigDefinition> getConfigs(String instanceId) {
        return currentConfigsByInstance.getOrDefault(instanceId, new ArrayList<>());
    }

    public void saveDefinitions(String instanceId, List<ConfigDefinition> definitions) {
        definitionsByInstance.put(instanceId, new ArrayList<>(definitions));
    }

    public List<ConfigDefinition> getDefinitions(String instanceId) {
        return definitionsByInstance.getOrDefault(instanceId, new ArrayList<>());
    }

    public void clear(String instanceId) {
        currentConfigsByInstance.remove(instanceId);
        definitionsByInstance.remove(instanceId);
    }
}

