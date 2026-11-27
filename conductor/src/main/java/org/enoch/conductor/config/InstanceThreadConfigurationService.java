package org.enoch.conductor.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.enoch.common.command.CommandMessage;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.common.config.ModuleDefinition;
import org.enoch.conductor.ws.WorkerSocketHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.enoch.conductor.process.WorkerRestPortResolver;

@Service
public class InstanceThreadConfigurationService {

    private static final long REQUEST_TIMEOUT_SECONDS = 10L;

    private final WorkerSocketHandler workerSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Map<String, CompletableFuture<List<ThreadDefinition>>> pendingRequests = new ConcurrentHashMap<>();
    private final Map<String, List<ModuleDefinition>> modulesByInstance = new ConcurrentHashMap<>();
    private final Map<String, List<String>> availableTypesByInstance = new ConcurrentHashMap<>();
    private final Map<String, PendingRequest> pendingRequestDetails = new ConcurrentHashMap<>();

    public InstanceThreadConfigurationService(WorkerSocketHandler workerSocketHandler) {
        this.workerSocketHandler = workerSocketHandler;
    }

    @PostConstruct
    void registerWithSocketHandler() {
        workerSocketHandler.setInstanceThreadConfigurationService(this);
    }

    public List<ThreadDefinition> fetchManagedDefinitions(String instanceId) {
        if (workerSocketHandler.getRegisteredSessionId(instanceId) == null) {
            throw new RuntimeException("Instance is offline");
        }

        int port = WorkerRestPortResolver.resolve(instanceId);
        try {
            URI uri = URI.create("http://127.0.0.1:" + port
                    + "/api/internal/configurations?instanceId=" + java.net.URLEncoder.encode(instanceId, "UTF-8"));
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(java.time.Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(
                    request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Worker configuration REST returned HTTP " + response.statusCode());
            }

            RestConfigurationResponse payload = objectMapper.readValue(response.body(), RestConfigurationResponse.class);
            if (!instanceId.equals(payload.instanceId())) {
                throw new RuntimeException("Worker configuration REST returned a different instance ID");
            }
            modulesByInstance.put(instanceId, payload.modules() == null ? List.of() : payload.modules());
            availableTypesByInstance.put(instanceId, payload.availableTypes() == null ? List.of() : payload.availableTypes());
            return payload.definitions() == null ? List.of() : payload.definitions();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Worker configuration REST request interrupted", e);
        } catch (Exception e) {
            return fetchManagedDefinitionsOverWebSocket(instanceId);
        }
    }

    private List<ThreadDefinition> fetchManagedDefinitionsOverWebSocket(String instanceId) {
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
            List<ModuleDefinition> modules = objectMapper.convertValue(
                    payload.getOrDefault("modules", List.of()),
                    new TypeReference<List<ModuleDefinition>>() {});
            if (modules == null || modules.stream().anyMatch(module ->
                    module == null || module.getName() == null || module.getName().isBlank())) {
                throw new IllegalArgumentException("Thread configuration response contains invalid modules");
            }
            List<String> availableTypes = objectMapper.convertValue(
                    payload.getOrDefault("availableTypes", List.of()),
                    new TypeReference<List<String>>() {});
            if (availableTypes == null || availableTypes.stream().anyMatch(type -> type == null || type.isBlank())) {
                throw new IllegalArgumentException("Thread configuration response contains invalid available types");
            }
            modulesByInstance.put(instanceId, modules);
            availableTypesByInstance.put(instanceId, availableTypes);
            future.complete(definitions);
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Malformed thread configuration response", e));
        }
    }

    private boolean isInvalidDefinition(ThreadDefinition definition) {
        return definition == null || definition.getName() == null || definition.getName().isBlank()
                || definition.getConfigs() == null
                || definition.getConfigs().stream().anyMatch(config ->
                config == null || config.getKey() == null || config.getKey().isBlank());
    }

    public List<ModuleDefinition> getModules(String instanceId) {
        return modulesByInstance.getOrDefault(instanceId, List.of());
    }

    public List<String> getAvailableTypes(String instanceId) {
        return availableTypesByInstance.getOrDefault(instanceId, List.of());
    }

    public void clearInstanceState(String instanceId) {
        modulesByInstance.remove(instanceId);
        availableTypesByInstance.remove(instanceId);
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

    private record RestConfigurationResponse(
            String instanceId,
            List<ThreadDefinition> definitions,
            List<ModuleDefinition> modules,
            List<String> availableTypes
    ) {
    }

    private record PendingRequest(String instanceId, String sessionId) {
    }
}
