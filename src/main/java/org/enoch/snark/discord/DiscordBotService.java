package org.enoch.snark.discord;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.enoch.snark.Cmd;
import org.enoch.snark.ai.Gemini;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscordBotService extends ListenerAdapter {

    @Value("${discord.bot.token}")
    private String token;

    @Value("${discord.bot.channel.id}")
    private String channelId;

    private final  Gemini gemini;
    private final Cmd cmd;

    private JDA jda;

    @PostConstruct
    public void init() throws Exception {
        // If token is not configured, do not start the bot and keep the service inert
        if (token == null || token.isBlank()) {
            System.out.println("Discord token not configured - DiscordBotService will be inert");
            return;
        }

        jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(this)
                .build();
        jda.awaitReady();
        // announce only if channelId is present and channel exists
        if (channelId != null && !channelId.isBlank()) sendMessage("https://tenor.com/view/wake-up-robot-creepy-gif-7411486299698814094");
    }

    @PreDestroy
    public void shutdown() {
        // if bot wasn't started, nothing to shutdown
        if (jda == null) return;
        if (channelId != null && !channelId.isBlank()) sendMessage("Skibidi");
        try {
            jda.shutdown();
        } catch (Exception e) {
            System.out.println("Failed to shutdown JDA: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        sendMessage(channelId, message);
    }
    public void sendMessage(String channelId, String message) {
        // Do nothing if JDA wasn't started or channelId is missing
        if (jda == null) {
            System.out.println("JDA not initialized - skipping sendMessage");
            return;
        }
        if (channelId == null || channelId.isEmpty()) {
            System.out.println("No channel ID provided - skipping sendMessage");
            return;
        }
        try {
            var textChannel = jda.getTextChannelById(channelId);
            if (textChannel != null) {
                textChannel.sendMessage(message).queue();
                System.out.println("Send to discord: " + message);
            } else {
                System.out.println("No channel found for ID: " + channelId);
            }
        } catch (Exception e) {
            System.out.println("Failed to send message to Discord: " + e.getMessage());
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // If bot not initialized or token missing, ignore events
        if (jda == null) return;
        if (event.getAuthor().isBot()) return;

        String message = extractMessageContent(event);
        if (message == null) return;

        List<String> expressionsToEvaluate = new ArrayList<>();
        if (message.startsWith("/")) {
            expressionsToEvaluate = List.of(message.substring(1).trim().split("/"));
        } else {
            expressionsToEvaluate = gemini.transformToSpellFunctions(message);
        }
        for (String expression : expressionsToEvaluate) {
            sendMessage(event.getChannel().getId(), message + " -> " + expression);
            cmd.execute(expression);
        }
    }

    private String extractMessageContent(MessageReceivedEvent event) {
        boolean shouldProcess = false;
        String raw = event.getMessage().getContentRaw();
        String selfMention = event.getJDA().getSelfUser().getAsMention();
        if (raw.startsWith(selfMention)) {
            shouldProcess = true;
            raw = raw.substring(selfMention.length()).trim().toLowerCase();
        }
        if (raw.startsWith("/")) shouldProcess = true;
        return shouldProcess ? raw : null;
    }
}
