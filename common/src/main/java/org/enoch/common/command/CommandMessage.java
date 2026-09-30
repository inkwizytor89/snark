package org.enoch.common.command;

import lombok.Data;

import java.util.Map;

@Data
public class CommandMessage {

    private String id;
    private String type;
    private String from;
    private String to;
    private long timestamp;

    private int status;
    private Object payload;
    private String error;
    private Map<String, String> metadata;
}
