package org.enoch.conductor.command;

import lombok.Data;
import java.util.Map;

@Data
public class CommandMessage {

    // Required fields
    private String id;                      // Unique request ID (UUID) - for correlating requests with responses
    private String type;                    // E.g., "request:restart", "response:restart", "error:restart"
    private String from;                    // Who sends: "conductor", "kamil#Erth", etc.
    private String to;                      // To whom: "kamil#Erth", "conductor", "broadcast"
    private long timestamp;                 // System.currentTimeMillis()

    // Optional fields
    private int status;                     // HTTP-like codes: 100=pending, 200=ok, 400=bad_request, 500=error
    private Object payload;                 // Command/response data (HashMap, String, Object, etc.)
    private String error;                   // Error description if something went wrong
    private Map<String, String> metadata;   // Additional info: {version, environment, executionTime, errorCode, etc}
}