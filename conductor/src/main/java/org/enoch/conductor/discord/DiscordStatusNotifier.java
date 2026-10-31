package org.enoch.conductor.discord;

import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class DiscordStatusNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordStatusNotifier.class);

    private final ProfileRepository profileRepository;
    private final DiscordNotificationDispatcher dispatcher;

    public DiscordStatusNotifier(ProfileRepository profileRepository, DiscordNotificationDispatcher dispatcher) {
        this.profileRepository = profileRepository;
        this.dispatcher = dispatcher;
    }

    public void publishLifecycle(String instanceId, String event) {
        if (instanceId == null || instanceId.isBlank()) {
            LOGGER.warn("discord.bot.lifecycle.failed reason=missing_instance_id event={}", event);
            return;
        }

        try {
            List<InstanceProfile> profiles = profileRepository.loadAll();
            InstanceProfile profile = profiles.stream()
                    .filter(candidate -> Objects.equals(candidate.getId(), instanceId))
                    .findFirst()
                    .orElse(null);

            if (profile == null) {
                LOGGER.warn("discord.bot.lifecycle.failed instanceId={} reason=profile_missing event={}", instanceId, event);
                return;
            }

            String channelId = profile.getDiscordChannelId();
            if (channelId == null || channelId.isBlank()) {
                return;
            }

            String summary = "instance status: " + (event == null || event.isBlank() ? "updated" : event);
            dispatcher.dispatchStatusChange(instanceId, summary);
        } catch (Exception e) {
            LOGGER.warn("discord.bot.lifecycle.failed instanceId={} event={} reason={}", instanceId, event, e.getMessage(), e);
        }
    }
}
