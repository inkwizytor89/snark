package org.enoch.conductor.discord;

public class DiscordCommandResult {

    public enum Status {
        SUCCESS,
        FAILURE,
        INACTIVE,
        INFO
    }

    private final String command;
    private final Status status;
    private final String message;
    private final String details;

    public DiscordCommandResult(String command, Status status, String message, String details) {
        this.command = command;
        this.status = status;
        this.message = message;
        this.details = details;
    }

    public static DiscordCommandResult success(String command, String message) {
        return new DiscordCommandResult(command, Status.SUCCESS, message, null);
    }

    public static DiscordCommandResult failure(String command, String message, String details) {
        return new DiscordCommandResult(command, Status.FAILURE, message, details);
    }

    public static DiscordCommandResult inactive(String command, String message) {
        return new DiscordCommandResult(command, Status.INACTIVE, message, null);
    }

    public static DiscordCommandResult info(String command, String message) {
        return new DiscordCommandResult(command, Status.INFO, message, null);
    }

    public boolean isAccepted() {
        return status == Status.SUCCESS || status == Status.INFO;
    }

    public String render() {
        StringBuilder builder = new StringBuilder();
        builder.append("Command: ").append(command).append(System.lineSeparator());
        builder.append("Accepted: ").append(isAccepted() ? "yes" : "no").append(System.lineSeparator());
        builder.append("Result: ").append(status.name().toLowerCase()).append(System.lineSeparator());
        builder.append("Action: ").append(command).append(System.lineSeparator());
        if (message != null && !message.isBlank()) {
            builder.append("Summary: ").append(message).append(System.lineSeparator());
        }
        if (details != null && !details.isBlank()) {
            builder.append("Details: ").append(details).append(System.lineSeparator());
        }
        return builder.toString().trim();
    }
}
