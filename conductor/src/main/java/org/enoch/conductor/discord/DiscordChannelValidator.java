package org.enoch.conductor.discord;

import java.util.Objects;

public final class DiscordChannelValidator {

    private DiscordChannelValidator() {
    }

    public static boolean isValidChannelId(String channelId) {
        if (channelId == null) {
            return false;
        }
        String normalized = channelId.trim();
        return !normalized.isEmpty() && normalized.matches("\\d{17,20}");
    }

    public static boolean isAllowedForNotification(String channelId) {
        return isValidChannelId(channelId) && !Objects.equals(channelId, "0");
    }
}
