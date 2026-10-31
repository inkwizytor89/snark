package org.enoch.common.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandSubmitResponse {
    private String commandName;
    private int generatedCount;
    private int queuedCount;
    private String message;
}
