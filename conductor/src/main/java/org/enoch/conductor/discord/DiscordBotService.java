package org.enoch.conductor.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.enoch.conductor.settings.ConductorSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class DiscordBotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordBotService.class);

    private final DiscordCommandRouter commandRouter;
    private final ConductorSettings conductorSettings;
    private volatile JDA jda;

    public DiscordBotService(DiscordCommandRouter commandRouter, ConductorSettings conductorSettings) {
        this.commandRouter = commandRouter;
        this.conductorSettings = conductorSettings;
    }

    @PostConstruct
    public void initialize() {
        String token = conductorSettings.getDiscordBotToken();
        boolean enabled = conductorSettings.isDiscordBotEnabled();

        if (token == null || token.isBlank()) {
            LOGGER.info("discord.bot.startup status=disabled reason=missing_token");
            return;
        }

        if (!enabled) {
            LOGGER.info("discord.bot.startup status=disabled reason=feature_off token_present=true");
            return;
        }

        try {
            jda = JDABuilder.createDefault(token)
                    .addEventListeners(new ListenerAdapter() {
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
                            String reply = commandRouter.handle(command).render();
                            event.getChannel().sendMessage(reply).queue();
                        }
                    })
                    .build();
            String defaultInstance = commandRouter.resolveDefaultActiveInstanceId();
            LOGGER.info("discord.bot.startup status=started default_instance={}", defaultInstance == null ? "none" : defaultInstance);
        } catch (Exception e) {
            LOGGER.error("discord.bot.startup status=failed reason={} token_present=true", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (jda != null) {
            LOGGER.info("discord.bot.shutdown status=stopping");
            jda.shutdown();
        }
    }

    public boolean isEnabled() {
        String token = conductorSettings.getDiscordBotToken();
        return conductorSettings.isDiscordBotEnabled() && token != null && !token.isBlank() && jda != null;
    }

    public String handleCommand(String message) {
        if (!isEnabled()) {
            return "Discord bot is not active. Set a Discord token and enable the bot in the global settings before use.";
        }

        return commandRouter.handle(message).render();
    }

    public String getDefaultActiveInstanceId() {
        return commandRouter.resolveDefaultActiveInstanceId();
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
