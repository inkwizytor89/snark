package org.enoch.conductor.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DiscordNotificationDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordNotificationDispatcher.class);
    private static final Set<String> LAST_SENT = ConcurrentHashMap.newKeySet();

    private final ProfileRepository profileRepository;
    private final DiscordBotService discordBotService;

    public DiscordNotificationDispatcher(ProfileRepository profileRepository, DiscordBotService discordBotService) {
        this.profileRepository = profileRepository;
        this.discordBotService = discordBotService;
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

            String dedupeKey = instanceId + "|" + channelId + "|" + stateSummary;
            if (!LAST_SENT.add(dedupeKey)) {
                return;
            }

            JDA jda = discordBotService.getJda();
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
            channel.sendMessage(message).queue(
                    success -> LOGGER.info("discord.bot.notification.sent instanceId={} channelId={} message={}", instanceId, channelId, message),
                    failure -> LOGGER.warn("discord.bot.notification.failed instanceId={} channelId={} reason={} message={}", instanceId, channelId, failure.getMessage(), message)
            );
        } catch (Exception e) {
            LOGGER.warn("discord.bot.notification.failed instanceId={} reason={}", instanceId, e.getMessage(), e);
        }
    }
}
