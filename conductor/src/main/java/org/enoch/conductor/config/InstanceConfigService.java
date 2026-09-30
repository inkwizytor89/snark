package org.enoch.conductor.config;

import org.enoch.common.command.CommandMessage;
import org.enoch.conductor.ws.WorkerSocketHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class InstanceConfigService {

    private static final long REQUEST_TIMEOUT_SECONDS = 5L;

    private final InstanceConfigStateRepository stateRepository;
    private final WorkerSocketHandler workerSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, CompletableFuture<List<InstanceConfigDto>>> pendingRequests = new ConcurrentHashMap<>();
    private final Map<String, String> pendingRequestInstances = new ConcurrentHashMap<>();

    public InstanceConfigService(InstanceConfigStateRepository stateRepository,
                                 WorkerSocketHandler workerSocketHandler) {
        this.stateRepository = stateRepository;
        this.workerSocketHandler = workerSocketHandler;
    }

    @PostConstruct
    void registerWithSocketHandler() {
        workerSocketHandler.setInstanceConfigService(this);
    }

    public List<InstanceConfigDto> fetchConfigs(String instanceId) throws Exception {
        List<InstanceConfigDto> result = sendConfigRequest(instanceId, "request:config:list");
        if (result != null) {
            stateRepository.saveConfigs(instanceId, result);
        }
        return result;
    }

    public List<InstanceConfigDto> fetchDefinitions(String instanceId) throws Exception {
        List<InstanceConfigDto> result = sendConfigRequest(instanceId, "request:config:definitions");
        if (result != null) {
            stateRepository.saveDefinitions(instanceId, result);
        }
        return result;
    }

    public List<InstanceConfigDto> setConfigs(String instanceId, List<InstanceConfigDto> configs) throws Exception {
        List<InstanceConfigDto> result = sendConfigRequest(instanceId, "request:config:set", configs);
        if (result != null) {
            stateRepository.saveConfigs(instanceId, result);
        }
        return result;
    }

    public List<InstanceConfigDto> deleteConfigs(String instanceId, List<InstanceConfigDto> configs) throws Exception {
        List<InstanceConfigDto> result = sendConfigRequest(instanceId, "request:config:delete", configs);
        if (result != null) {
            stateRepository.saveConfigs(instanceId, result);
        }
        return result;
    }

    private List<InstanceConfigDto> sendConfigRequest(String instanceId, String type) throws Exception {
        return sendConfigRequest(instanceId, type, null);
    }

    private List<InstanceConfigDto> sendConfigRequest(String instanceId, String type, List<InstanceConfigDto> configs) throws Exception {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<List<InstanceConfigDto>> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        pendingRequestInstances.put(requestId, instanceId);

        try {
            CommandMessage message = new CommandMessage();
            message.setId(requestId);
            message.setType(type);
            message.setFrom("conductor");
            message.setTo(instanceId);
            message.setTimestamp(System.currentTimeMillis());
            message.setStatus(100);
            message.setPayload(configs);

            workerSocketHandler.send(instanceId, objectMapper.writeValueAsString(message));

            return future.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Config request timed out", e);
        } finally {
            pendingRequests.remove(requestId);
            pendingRequestInstances.remove(requestId);
        }
    }

    public void handleConfigResponse(CommandMessage response) {
        CompletableFuture<List<InstanceConfigDto>> future = pendingRequests.get(response.getId());
        if (future == null) {
            return;
        }

        if (response.getStatus() == 200) {
            List<InstanceConfigDto> configs = extractConfigList(response.getPayload());
            future.complete(configs);
        } else {
            String errorMsg = response.getError() != null ? response.getError() : "Unknown error";
            future.completeExceptionally(new RuntimeException("Config request failed: " + errorMsg));
        }
    }

    @SuppressWarnings("unchecked")
    private List<InstanceConfigDto> extractConfigList(Object payload) {
        if (payload == null) {
            return List.of();
        }

        return objectMapper.convertValue(payload, new TypeReference<List<InstanceConfigDto>>() {});
    }

    public void clearInstanceState(String instanceId) {
        stateRepository.clear(instanceId);
    }

    public void cancelPendingRequests(String instanceId) {
        pendingRequestInstances.forEach((requestId, requestInstanceId) -> {
            if (instanceId.equals(requestInstanceId)) {
                CompletableFuture<List<InstanceConfigDto>> future = pendingRequests.get(requestId);
                if (future != null && !future.isDone()) {
                    future.completeExceptionally(new RuntimeException("Instance disconnected"));
                }
            }
        });
    }
}
