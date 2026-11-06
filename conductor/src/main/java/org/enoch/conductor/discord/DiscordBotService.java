package org.enoch.conductor.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.ProfileRepository;
import org.enoch.conductor.process.ProcessService;
import org.enoch.conductor.settings.ConductorSettings;
import org.springframework.beans.factory.ObjectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.Locale;

@Service
public class DiscordBotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordBotService.class);

    private final ObjectProvider<DiscordCommandRouter> commandRouterProvider;
    private final ObjectProvider<DiscordStatusNotifier> discordStatusNotifierProvider;
    private final ObjectProvider<ProcessService> processServiceProvider;
    private final ConductorSettings conductorSettings;
    private final ProfileRepository profileRepository;
    private volatile JDA jda;
    private String activeToken;

    public DiscordBotService(ObjectProvider<DiscordCommandRouter> commandRouterProvider,
                             ObjectProvider<DiscordStatusNotifier> discordStatusNotifierProvider,
                             ObjectProvider<ProcessService> processServiceProvider,
                             ConductorSettings conductorSettings,
                             ProfileRepository profileRepository) {
        this.commandRouterProvider = commandRouterProvider;
        this.discordStatusNotifierProvider = discordStatusNotifierProvider;
        this.processServiceProvider = processServiceProvider;
        this.conductorSettings = conductorSettings;
        this.profileRepository = profileRepository;
    }

    @PostConstruct
    public void initialize() {
        refresh();
    }

    public synchronized void refresh() {
        String token = conductorSettings.getDiscordBotToken();
        boolean hasConfiguredChannel;
        try {
            hasConfiguredChannel = profileRepository.loadAll().stream()
                    .map(InstanceProfile::getDiscordChannelId)
                    .anyMatch(DiscordChannelValidator::isAllowedForNotification);
        } catch (Exception e) {
            LOGGER.error("discord.bot.configuration.failed reason=profiles_unavailable", e);
            stopBot();
            return;
        }

        if (token == null || token.isBlank()) {
            LOGGER.info("discord.bot.status=disabled reason=missing_token");
            stopBot();
            return;
        }
        if (!hasConfiguredChannel) {
            LOGGER.info("discord.bot.status=disabled reason=no_configured_channel");
            stopBot();
            return;
        }
        if (jda != null && token.equals(activeToken) && jda.getStatus() != JDA.Status.SHUTDOWN) {
            return;
        }

        stopBot();
        try {
            JDA startedJda = JDABuilder.createDefault(token)
                    .addEventListeners(new ListenerAdapter() {
                        @Override
                        public void onReady(ReadyEvent event) {
                            jda = event.getJDA();
                            activeToken = token;
                            LOGGER.info("discord.bot.status=ready");
                            publishCurrentStatuses();
                        }

                        @Override
                        public void onMessageReceived(MessageReceivedEvent event) {
                            if (event.getAuthor().isBot() || event.getAuthor().isSystem()) {
                                return;
                            }
                            String rawContent = event.getMessage().getContentRaw();
                            if (!rawContent.contains("<@") && !rawContent.startsWith("!")) {
                                return;
                            }
                            String command = normalizeCommand(rawContent);
                            if (command == null || command.isBlank()) {
                                return;
                            }
                            LOGGER.info("discord.bot.command.received user={} command={}", event.getAuthor().getName(), command);
                            String reply = renderReply(rawContent, event.getJDA().getSelfUser().getName(), command);
                            event.getChannel().sendMessage(reply).queue();
                        }
                    })
                    .build();
            jda = startedJda;
            activeToken = token;
            LOGGER.info("discord.bot.status=starting reason=token_and_channel_configured");
        } catch (Exception e) {
            LOGGER.error("discord.bot.startup status=failed reason={}", e.getMessage(), e);
            stopBot();
        }
    }

    private void publishCurrentStatuses() {
        try {
            ProcessService processService = processServiceProvider.getObject();
            commandRouterProvider.getObject().resolveDefaultActiveInstanceId();
            DiscordStatusNotifier notifier = discordStatusNotifierProvider.getObject();
            for (InstanceProfile profile : profileRepository.loadAll()) {
                if (DiscordChannelValidator.isAllowedForNotification(profile.getDiscordChannelId())) {
                    notifier.publishState(profile.getId(), processService.getState(profile.getId()));
                }
            }
        } catch (Exception e) {
            LOGGER.error("discord.bot.startup_status.failed reason={}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        stopBot();
    }

    private synchronized void stopBot() {
        if (jda != null) {
            LOGGER.info("discord.bot.status=stopping");
            jda.shutdownNow();
            jda = null;
        }
        activeToken = null;
    }

    public boolean isEnabled() {
        String token = conductorSettings.getDiscordBotToken();
        return token != null && !token.isBlank() && jda != null && jda.getStatus() != JDA.Status.SHUTDOWN;
    }

    public String handleCommand(String message) {
        if (!isEnabled()) {
            return "Discord bot is not active. Configure a Discord token and an instance channel in the conductor.";
        }

        return commandRouterProvider.getObject().handle(message).render();
    }

    String renderReply(String rawContent, String botName, String command) {
        String reply = commandRouterProvider.getObject().handle(command).render();
        if ("sylwia".equalsIgnoreCase(botName)
                && rawContent != null
                && rawContent.toLowerCase(Locale.ROOT).contains("kamil")
                && reply.startsWith("Unknown action: ")) {
            return "Zajmę się tym wieczorem ;)";
        }
        return reply;
    }

    public String getDefaultActiveInstanceId() {
        return commandRouterProvider.getObject().resolveDefaultActiveInstanceId();
    }

    public JDA getJda() {
        return jda;
    }

    private String normalizeCommand(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return null;
        }

        String trimmed = rawMessage.trim();
        if (trimmed.startsWith("<@") || trimmed.startsWith("<@!")) {
            int spaceIndex = trimmed.indexOf(' ');
            if (spaceIndex >= 0) {
                trimmed = trimmed.substring(spaceIndex + 1).trim();
            } else {
                return "";
            }
        }
        if (trimmed.startsWith("!")) {
            trimmed = trimmed.substring(1).trim();
        }
        return trimmed.isBlank() ? null : trimmed;
    }
}
