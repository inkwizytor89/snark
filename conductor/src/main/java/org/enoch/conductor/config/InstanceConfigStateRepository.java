package org.enoch.conductor.config;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InstanceConfigStateRepository {

    private final ConcurrentHashMap<String, List<InstanceConfigDto>> currentConfigsByInstance = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<InstanceConfigDto>> definitionsByInstance = new ConcurrentHashMap<>();

    public void saveConfigs(String instanceId, List<InstanceConfigDto> configs) {
        currentConfigsByInstance.put(instanceId, new ArrayList<>(configs));
    }

    public List<InstanceConfigDto> getConfigs(String instanceId) {
        return currentConfigsByInstance.getOrDefault(instanceId, new ArrayList<>());
    }

    public void saveDefinitions(String instanceId, List<InstanceConfigDto> definitions) {
        definitionsByInstance.put(instanceId, new ArrayList<>(definitions));
    }

    public List<InstanceConfigDto> getDefinitions(String instanceId) {
        return definitionsByInstance.getOrDefault(instanceId, new ArrayList<>());
    }

    public void clear(String instanceId) {
        currentConfigsByInstance.remove(instanceId);
        definitionsByInstance.remove(instanceId);
    }
}

