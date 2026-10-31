package org.enoch.conductor.command;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.enoch.common.command.CommandCatalogResponse;
import org.enoch.common.command.CommandMessage;
import org.enoch.common.command.CommandSubmitRequest;
import org.enoch.common.command.CommandSubmitResponse;
import org.enoch.conductor.ws.WorkerSocketHandler;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class CommandRequestService {

    private static final long REQUEST_TIMEOUT_SECONDS = 5L;

    private final WorkerSocketHandler workerSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, CompletableFuture<CommandCatalogResponse>> catalogFutures = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<CommandSubmitResponse>> submitFutures = new ConcurrentHashMap<>();
    private final Map<String, String> pendingRequestInstances = new ConcurrentHashMap<>();

    public CommandRequestService(WorkerSocketHandler workerSocketHandler) {
        this.workerSocketHandler = workerSocketHandler;
    }

    @PostConstruct
    void registerWithSocketHandler() {
        workerSocketHandler.setInstanceCommandService(this);
    }

    public CommandCatalogResponse fetchCatalog(String instanceId) throws Exception {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<CommandCatalogResponse> future = new CompletableFuture<>();
        catalogFutures.put(requestId, future);
        pendingRequestInstances.put(requestId, instanceId);
        try {
            CommandMessage message = new CommandMessage();
            message.setId(requestId);
            message.setType("request:command:catalog");
            message.setFrom("conductor");
            message.setTo(instanceId);
            message.setTimestamp(System.currentTimeMillis());
            message.setStatus(100);
            workerSocketHandler.send(instanceId, objectMapper.writeValueAsString(message));
            return future.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Command catalog request timed out", e);
        } finally {
            catalogFutures.remove(requestId);
            pendingRequestInstances.remove(requestId);
        }
    }

    public CommandSubmitResponse submitRecipe(String instanceId, CommandSubmitRequest request) throws Exception {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<CommandSubmitResponse> future = new CompletableFuture<>();
        submitFutures.put(requestId, future);
        pendingRequestInstances.put(requestId, instanceId);
        try {
            CommandMessage message = new CommandMessage();
            message.setId(requestId);
            message.setType("request:command:submit");
            message.setFrom("conductor");
            message.setTo(instanceId);
            message.setTimestamp(System.currentTimeMillis());
            message.setStatus(100);
            message.setPayload(request == null ? new CommandSubmitRequest() : request);
            workerSocketHandler.send(instanceId, objectMapper.writeValueAsString(message));
            return future.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Command submit request timed out", e);
        } finally {
            submitFutures.remove(requestId);
            pendingRequestInstances.remove(requestId);
        }
    }

    public void handleCommandResponse(CommandMessage response) {
        if (response == null) {
            return;
        }
        if ("response:command:catalog".equals(response.getType()) || "error:command:catalog".equals(response.getType())) {
            CompletableFuture<CommandCatalogResponse> future = catalogFutures.get(response.getId());
            if (future == null) {
                return;
            }
            if (response.getStatus() == 200) {
                future.complete(objectMapper.convertValue(response.getPayload(), new TypeReference<CommandCatalogResponse>() {}));
            } else {
                future.completeExceptionally(new RuntimeException(response.getError() == null ? "Command catalog request failed" : response.getError()));
            }
            return;
        }
        if ("response:command:submit".equals(response.getType()) || "error:command:submit".equals(response.getType())) {
            CompletableFuture<CommandSubmitResponse> future = submitFutures.get(response.getId());
            if (future == null) {
                return;
            }
            if (response.getStatus() == 200) {
                future.complete(objectMapper.convertValue(response.getPayload(), new TypeReference<CommandSubmitResponse>() {}));
            } else {
                future.completeExceptionally(new RuntimeException(response.getError() == null ? "Command submit request failed" : response.getError()));
            }
        }
    }

    public void cancelPendingRequests(String instanceId) {
        pendingRequestInstances.forEach((requestId, requestInstanceId) -> {
            if (instanceId.equals(requestInstanceId)) {
                CompletableFuture<CommandCatalogResponse> catalogFuture = catalogFutures.remove(requestId);
                if (catalogFuture != null && !catalogFuture.isDone()) {
                    catalogFuture.completeExceptionally(new RuntimeException("Instance disconnected"));
                }
                CompletableFuture<CommandSubmitResponse> submitFuture = submitFutures.remove(requestId);
                if (submitFuture != null && !submitFuture.isDone()) {
                    submitFuture.completeExceptionally(new RuntimeException("Instance disconnected"));
                }
            }
        });
    }
}
