package org.enoch.conductor.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.enoch.common.command.CommandMessage;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.conductor.ws.WorkerSocketHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class InstanceThreadConfigurationService {

    private static final long REQUEST_TIMEOUT_SECONDS = 5L;

    private final WorkerSocketHandler workerSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Map<String, CompletableFuture<List<ThreadDefinition>>> pendingRequests = new ConcurrentHashMap<>();
    private final Map<String, PendingRequest> pendingRequestDetails = new ConcurrentHashMap<>();

    public InstanceThreadConfigurationService(WorkerSocketHandler workerSocketHandler) {
        this.workerSocketHandler = workerSocketHandler;
    }

    @PostConstruct
    void registerWithSocketHandler() {
        workerSocketHandler.setInstanceThreadConfigurationService(this);
    }

    public List<ThreadDefinition> fetchManagedDefinitions(String instanceId) {
        String requestId = UUID.randomUUID().toString();
        String sessionId = workerSocketHandler.getRegisteredSessionId(instanceId);
        if (sessionId == null) {
            throw new RuntimeException("Instance is offline");
        }
        CompletableFuture<List<ThreadDefinition>> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        pendingRequestDetails.put(requestId, new PendingRequest(instanceId, sessionId));

        try {
            CommandMessage request = new CommandMessage();
            request.setId(requestId);
            request.setType("request:thread:configurations");
            request.setFrom("conductor");
            request.setTo(instanceId);
            request.setTimestamp(System.currentTimeMillis());
            request.setStatus(100);
            request.setPayload(Map.of());

            workerSocketHandler.send(instanceId, objectMapper.writeValueAsString(request));
            return future.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Thread configuration request timed out", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread configuration request interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Thread configuration request failed: " + e.getMessage(), e);
        } finally {
            pendingRequests.remove(requestId);
            pendingRequestDetails.remove(requestId);
        }
    }

    public void handleResponse(String instanceId, String sessionId, CommandMessage response) {
        String requestId = response.getId();
        if (requestId == null || requestId.isBlank()) {
            return;
        }
        CompletableFuture<List<ThreadDefinition>> future = pendingRequests.get(requestId);
        if (future == null) {
            return;
        }

        PendingRequest pending = pendingRequestDetails.get(requestId);
        if (pending == null
                || !instanceId.equals(pending.instanceId())
                || !sessionId.equals(pending.sessionId())
                || !instanceId.equals(response.getFrom())
                || !"conductor".equals(response.getTo())) {
            future.completeExceptionally(new RuntimeException("Unmatched thread configuration response"));
            return;
        }

        if ("error:thread:configurations".equals(response.getType()) || response.getStatus() != 200) {
            String error = response.getError() == null ? "Unknown worker error" : response.getError();
            future.completeExceptionally(new RuntimeException("Thread configuration request failed: " + error));
            return;
        }
        if (!"response:thread:configurations".equals(response.getType())) {
            future.completeExceptionally(new RuntimeException("Unexpected thread configuration response type"));
            return;
        }

        try {
            Map<String, Object> payload = objectMapper.convertValue(
                    response.getPayload(), new TypeReference<Map<String, Object>>() {});
            if (!instanceId.equals(payload.get("instanceId"))) {
                throw new IllegalArgumentException("Thread configuration response has an invalid instance ID");
            }
            List<ThreadDefinition> definitions = objectMapper.convertValue(
                    payload.get("definitions"), new TypeReference<List<ThreadDefinition>>() {});
            if (definitions == null || definitions.stream().anyMatch(this::isInvalidDefinition)) {
                throw new IllegalArgumentException("Thread configuration response contains invalid definitions");
            }
            future.complete(definitions);
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Malformed thread configuration response", e));
        }
    }

    private boolean isInvalidDefinition(ThreadDefinition definition) {
        return definition == null || definition.getThread() == null || definition.getThread().isBlank()
                || definition.getConfigs() == null
                || definition.getConfigs().stream().anyMatch(config ->
                config == null || config.getKey() == null || config.getKey().isBlank());
    }

    public void cancelPendingRequests(String instanceId) {
        pendingRequestDetails.forEach((requestId, request) -> {
            if (instanceId.equals(request.instanceId())) {
                CompletableFuture<List<ThreadDefinition>> future = pendingRequests.get(requestId);
                if (future != null && !future.isDone()) {
                    future.completeExceptionally(new RuntimeException("Instance disconnected"));
                }
            }
        });
    }

    private record PendingRequest(String instanceId, String sessionId) {
    }
}
