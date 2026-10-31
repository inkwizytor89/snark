package org.enoch.common.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandDefinition {
    private String name;
    private String label;
    private String className;
    private String description;
    private List<CommandFieldDefinition> fields;
}
