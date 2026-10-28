package org.enoch.common.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadDefinition {
    private String thread;
    private String description;
    private List<ConfigDefinition> configs = new ArrayList<>();
}
