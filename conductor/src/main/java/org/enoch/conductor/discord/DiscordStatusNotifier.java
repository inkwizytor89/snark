package org.enoch.conductor.discord;

import org.springframework.stereotype.Service;

@Service
public class DiscordStatusNotifier {

    private final DiscordNotificationDispatcher dispatcher;

    public DiscordStatusNotifier(DiscordNotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void publishLifecycle(String instanceId, String event) {
        if (event == null || event.isBlank()) {
            return;
        }
        publishState(instanceId, event);
    }

    public void publishState(String instanceId, String state) {
        if (instanceId == null || instanceId.isBlank() || state == null || state.isBlank()) {
            return;
        }
        dispatcher.dispatchStatusChange(instanceId, "status changed to " + state);
    }

    public void resetLastNotifiedState(String instanceId) {
        dispatcher.resetLastNotifiedState(instanceId);
    }
}
