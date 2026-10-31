package org.enoch.conductor.discord;

import org.enoch.conductor.settings.ConductorSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class DiscordBotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordBotService.class);

    private final DiscordCommandRouter commandRouter;
    private final ConductorSettings conductorSettings;

    public DiscordBotService(DiscordCommandRouter commandRouter, ConductorSettings conductorSettings) {
        this.commandRouter = commandRouter;
        this.conductorSettings = conductorSettings;
    }

    @PostConstruct
    public void initialize() {
        String token = conductorSettings.getDiscordBotToken();
        boolean enabled = conductorSettings.isDiscordBotEnabled();

        if (token == null || token.isBlank()) {
            LOGGER.info("Discord bot is disabled. Set discord.bot.token in the global settings to enable it.");
            return;
        }

        if (!enabled) {
            LOGGER.info("Discord bot configured but disabled by discord.bot.enabled=false. Set it to true in the global settings to start the bot.");
            return;
        }

        String defaultInstance = commandRouter.resolveDefaultActiveInstanceId();
        LOGGER.info("Discord bot started. Default active instance: {}", defaultInstance == null ? "none" : defaultInstance);
    }

    public boolean isEnabled() {
        String token = conductorSettings.getDiscordBotToken();
        return conductorSettings.isDiscordBotEnabled() && token != null && !token.isBlank();
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
}
