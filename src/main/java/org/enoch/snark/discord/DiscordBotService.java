package org.enoch.snark.discord;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.enoch.snark.ai.Gemini;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscordBotService extends ListenerAdapter {

    @Value("${discord.bot.token}")
    private String token;

    @Value("${discord.bot.channel.id}")
    private String channelId;

    private final  Gemini gemini;

    private JDA jda;

    @PostConstruct
    public void init() throws Exception {
        jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(this)
                .build();
        jda.awaitReady();
        sendMessage("Serwus");
    }

    @PreDestroy
    public void shutdown() {
        sendMessage("Skibidi");
    }

    public void sendMessage(String message) {
        if (channelId != null && !channelId.isEmpty()) {
            var textChannel = jda.getTextChannelById(channelId);
            if (textChannel != null) {
                textChannel.sendMessage(message).queue();
                System.out.println("Send to discord: " + message);
            } else {
                System.out.println("No channel ID: " + channelId);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!shouldProcess(event)) return;

        String message = extractMessageContent(event);
        MessageChannel channel = event.getChannel();

        List<String> expressionsToEvaluate = new ArrayList<>();
        if (message.startsWith("/")) {
           expressionsToEvaluate = List.of(message.substring(1).trim().split("/"));
        } else {
            expressionsToEvaluate = gemini.transformToSpellFunctions(message);
        }
        sendMessage(message + " -> " +expressionsToEvaluate.stream().collect(Collectors.joining("\n")));
    }

    private boolean shouldProcess(MessageReceivedEvent event) {
        boolean mentioned = event.getMessage().getMentions().isMentioned(event.getJDA().getSelfUser());
        if (!mentioned) {
            return false;
        }

        String message = event.getMessage().getContentRaw();
        String selfMention = event.getJDA().getSelfUser().getAsMention();
        return message.startsWith(selfMention);
    }

    private String extractMessageContent(MessageReceivedEvent event) {
        String raw = event.getMessage().getContentRaw();
        String selfMention = event.getJDA().getSelfUser().getAsMention();
        if (raw.startsWith(selfMention)) {
            return raw.substring(selfMention.length()).trim().toLowerCase();
        }
        return raw.trim().toLowerCase();
    }
}
