package org.enoch.common.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandFieldDefinition {
    private String name;
    private String label;
    private String type;
    private String format;
    private String description;
    private String example;
    private boolean required;
    private String expressionHint;
}
