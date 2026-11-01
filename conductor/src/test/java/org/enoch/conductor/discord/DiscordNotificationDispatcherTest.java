package org.enoch.conductor.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DiscordNotificationDispatcherTest {

    private static final String CHANNEL_ID = "123456789012345678";

    @Test
    void shouldSuppressRepeatedStateButAllowLaterReturnToPreviousState() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ObjectProvider<DiscordBotService> botProvider = mock(ObjectProvider.class);
        DiscordBotService botService = mock(DiscordBotService.class);
        JDA jda = mock(JDA.class);
        TextChannel channel = mock(TextChannel.class);
        MessageCreateAction sendAction = mock(MessageCreateAction.class);
        when(profileRepository.loadAll()).thenReturn(List.of(profile(CHANNEL_ID)));
        when(botProvider.getIfAvailable()).thenReturn(botService);
        when(botService.getJda()).thenReturn(jda);
        when(jda.getTextChannelById(CHANNEL_ID)).thenReturn(channel);
        when(channel.canTalk()).thenReturn(true);
        when(channel.sendMessage(anyString())).thenReturn(sendAction);
        doAnswer(invocation -> {
            Consumer<Message> success = invocation.getArgument(0);
            success.accept(mock(Message.class));
            return null;
        }).when(sendAction).queue(any(), any());

        DiscordNotificationDispatcher dispatcher = new DiscordNotificationDispatcher(profileRepository, botProvider);
        dispatcher.dispatchStatusChange("worker#1", "status changed to running");
        dispatcher.dispatchStatusChange("worker#1", "status changed to running");
        dispatcher.dispatchStatusChange("worker#1", "status changed to stopped");
        dispatcher.dispatchStatusChange("worker#1", "status changed to running");

        verify(channel, times(3)).sendMessage(anyString());
    }

    @Test
    void shouldRetryNotificationAfterAsynchronousDeliveryFailure() throws Exception {
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ObjectProvider<DiscordBotService> botProvider = mock(ObjectProvider.class);
        DiscordBotService botService = mock(DiscordBotService.class);
        JDA jda = mock(JDA.class);
        TextChannel channel = mock(TextChannel.class);
        MessageCreateAction sendAction = mock(MessageCreateAction.class);
        when(profileRepository.loadAll()).thenReturn(List.of(profile(CHANNEL_ID)));
        when(botProvider.getIfAvailable()).thenReturn(botService);
        when(botService.getJda()).thenReturn(jda);
        when(jda.getTextChannelById(CHANNEL_ID)).thenReturn(channel);
        when(channel.canTalk()).thenReturn(true);
        when(channel.sendMessage(anyString())).thenReturn(sendAction);
        doAnswer(invocation -> {
            Consumer<Throwable> failure = invocation.getArgument(1);
            failure.accept(new IllegalStateException("Discord unavailable"));
            return null;
        }).doAnswer(invocation -> {
            Consumer<Message> success = invocation.getArgument(0);
            success.accept(mock(Message.class));
            return null;
        }).when(sendAction).queue(any(), any());

        DiscordNotificationDispatcher dispatcher = new DiscordNotificationDispatcher(profileRepository, botProvider);
        dispatcher.dispatchStatusChange("worker#1", "status changed to running");
        dispatcher.dispatchStatusChange("worker#1", "status changed to running");

        verify(channel, times(2)).sendMessage(anyString());
    }

    @Test
    void shouldSendToTheInstancesCurrentChannel() throws Exception {
        String updatedChannelId = "223456789012345678";
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        ObjectProvider<DiscordBotService> botProvider = mock(ObjectProvider.class);
        DiscordBotService botService = mock(DiscordBotService.class);
        JDA jda = mock(JDA.class);
        TextChannel originalChannel = readyChannel(jda, CHANNEL_ID);
        TextChannel updatedChannel = readyChannel(jda, updatedChannelId);
        MessageCreateAction sendAction = successfulAction();
        when(profileRepository.loadAll()).thenReturn(
                List.of(profile(CHANNEL_ID)),
                List.of(profile(updatedChannelId))
        );
        when(botProvider.getIfAvailable()).thenReturn(botService);
        when(botService.getJda()).thenReturn(jda);
        when(originalChannel.sendMessage(anyString())).thenReturn(sendAction);
        when(updatedChannel.sendMessage(anyString())).thenReturn(sendAction);

        DiscordNotificationDispatcher dispatcher = new DiscordNotificationDispatcher(profileRepository, botProvider);
        dispatcher.dispatchStatusChange("worker#1", "status changed to running");
        dispatcher.dispatchStatusChange("worker#1", "status changed to running");

        verify(originalChannel).sendMessage(anyString());
        verify(updatedChannel).sendMessage(anyString());
    }

    private TextChannel readyChannel(JDA jda, String channelId) {
        TextChannel channel = mock(TextChannel.class);
        when(jda.getTextChannelById(channelId)).thenReturn(channel);
        when(channel.canTalk()).thenReturn(true);
        return channel;
    }

    private MessageCreateAction successfulAction() {
        MessageCreateAction action = mock(MessageCreateAction.class);
        doAnswer(invocation -> {
            Consumer<Message> success = invocation.getArgument(0);
            success.accept(mock(Message.class));
            return null;
        }).when(action).queue(any(), any());
        return action;
    }

    private InstanceProfile profile(String channelId) {
        InstanceProfile profile = new InstanceProfile();
        profile.setId("worker#1");
        profile.setDiscordChannelId(channelId);
        return profile;
    }
}
