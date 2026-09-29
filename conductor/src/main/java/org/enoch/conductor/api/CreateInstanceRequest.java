package org.enoch.conductor.api;

public record CreateInstanceRequest(String server, String login, String templateName, String databaseName) {
}
