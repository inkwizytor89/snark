package org.enoch.conductor.discord;

import java.util.concurrent.atomic.AtomicReference;

public class DiscordCommandContext {

    private final AtomicReference<String> activeInstanceId = new AtomicReference<>();

    public String getActiveInstanceId() {
        return activeInstanceId.get();
    }

    public void setActiveInstanceId(String activeInstanceId) {
        this.activeInstanceId.set(activeInstanceId);
    }

    public boolean hasActiveInstance() {
        return activeInstanceId.get() != null && !activeInstanceId.get().isBlank();
    }
}
