package org.enoch.conductor.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DiscordNotificationDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordNotificationDispatcher.class);
    private final Map<String, LastNotification> lastSentState = new ConcurrentHashMap<>();
    private final java.util.Set<String> inFlight = ConcurrentHashMap.newKeySet();

    private final ProfileRepository profileRepository;
    private final ObjectProvider<DiscordBotService> discordBotServiceProvider;

    public DiscordNotificationDispatcher(ProfileRepository profileRepository, ObjectProvider<DiscordBotService> discordBotServiceProvider) {
        this.profileRepository = profileRepository;
        this.discordBotServiceProvider = discordBotServiceProvider;
    }

    public void resetLastNotifiedState(String instanceId) {
        if (instanceId != null) {
            lastSentState.remove(instanceId);
        }
    }

    public void dispatchStatusChange(String instanceId, String stateSummary) {
        if (instanceId == null || instanceId.isBlank()) {
            LOGGER.warn("discord.bot.notification.failed instanceId={} reason=missing_instance_id", instanceId);
            return;
        }
        if (stateSummary == null || stateSummary.isBlank()) {
            LOGGER.warn("discord.bot.notification.failed instanceId={} reason=missing_summary", instanceId);
            return;
        }

        String requestKey = null;
        try {
            List<InstanceProfile> profiles = profileRepository.loadAll();
            InstanceProfile profile = profiles.stream()
                    .filter(candidate -> Objects.equals(candidate.getId(), instanceId))
                    .findFirst()
                    .orElse(null);

            if (profile == null) {
                LOGGER.warn("discord.bot.notification.failed instanceId={} reason=profile_missing", instanceId);
                return;
            }

            String channelId = profile.getDiscordChannelId();
            if (!DiscordChannelValidator.isAllowedForNotification(channelId)) {
                LOGGER.warn("discord.bot.notification.failed instanceId={} channelId={} reason=invalid_channel_id", instanceId, channelId);
                return;
            }

            DiscordBotService discordBotService = discordBotServiceProvider.getIfAvailable();
            JDA jda = discordBotService == null ? null : discordBotService.getJda();
            if (jda == null) {
                LOGGER.warn("discord.bot.notification.failed instanceId={} channelId={} reason=bot_unavailable", instanceId, channelId);
                return;
            }

            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel == null) {
                LOGGER.warn("discord.bot.notification.failed instanceId={} channelId={} reason=channel_not_found", instanceId, channelId);
                return;
            }
            if (!channel.canTalk()) {
                LOGGER.warn("discord.bot.notification.failed instanceId={} channelId={} reason=missing_permission_send_messages", instanceId, channelId);
                return;
            }

            String message = "[" + instanceId + "] " + stateSummary;
            requestKey = instanceId + "|" + channelId + "|" + stateSummary;
            LastNotification lastNotification = lastSentState.get(instanceId);
            if ((lastNotification != null
                    && channelId.equals(lastNotification.channelId())
                    && stateSummary.equals(lastNotification.stateSummary()))
                    || !inFlight.add(requestKey)) {
                return;
            }
            String notificationKey = requestKey;
            channel.sendMessage(message).queue(
                    success -> {
                        lastSentState.put(instanceId, new LastNotification(channelId, stateSummary));
                        inFlight.remove(notificationKey);
                        LOGGER.info("discord.bot.notification.sent instanceId={} channelId={} message={}", instanceId, channelId, message);
                    },
                    failure -> {
                        inFlight.remove(notificationKey);
                        LOGGER.warn("discord.bot.notification.failed instanceId={} channelId={} reason={} message={}", instanceId, channelId, failure.getMessage(), message);
                    }
            );
        } catch (Exception e) {
            if (requestKey != null) {
                inFlight.remove(requestKey);
            }
            LOGGER.warn("discord.bot.notification.failed instanceId={} reason={}", instanceId, e.getMessage(), e);
        }
    }

    private record LastNotification(String channelId, String stateSummary) {
    }
}
