package org.enoch.conductor.api;

import java.util.Map;

public record ApplyPlaceholdersRequest(
        String server,
        String login,
        String templateName,
        String databaseName,
        Map<String, String> placeholders
) {
}
