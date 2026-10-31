package org.enoch.conductor.discord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class DiscordBotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordBotService.class);

    private final DiscordCommandRouter commandRouter;

    @Value("${discord.bot.enabled:false}")
    private boolean enabled;

    @Value("${discord.bot.token:}")
    private String token;

    public DiscordBotService(DiscordCommandRouter commandRouter) {
        this.commandRouter = commandRouter;
    }

    @PostConstruct
    public void initialize() {
        if (token == null || token.isBlank()) {
            LOGGER.info("Discord bot is disabled. Set discord.bot.token in application.properties to enable it.");
            return;
        }

        if (!enabled) {
            LOGGER.info("Discord bot configured but disabled by discord.bot.enabled=false. Set it to true to start the bot.");
            return;
        }

        String defaultInstance = commandRouter.resolveDefaultActiveInstanceId();
        LOGGER.info("Discord bot started. Default active instance: {}", defaultInstance == null ? "none" : defaultInstance);
    }

    public boolean isEnabled() {
        return enabled && token != null && !token.isBlank();
    }

    public String handleCommand(String message) {
        if (!isEnabled()) {
            return "Discord bot is not active. Set discord.bot.token and enable discord.bot.enabled=true before use.";
        }

        return commandRouter.handle(message).render();
    }

    public String getDefaultActiveInstanceId() {
        return commandRouter.resolveDefaultActiveInstanceId();
    }
}
