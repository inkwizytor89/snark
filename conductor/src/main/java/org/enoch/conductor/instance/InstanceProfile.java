package org.enoch.conductor.instance;

import lombok.Data;

@Data
public class InstanceProfile {

    private String id;
    private boolean autoStart;
    private String databaseName;
    private String properties;
}
