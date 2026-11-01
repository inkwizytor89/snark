package org.enoch.conductor.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.ProfileRepository;
import org.enoch.conductor.process.ProcessService;
import org.enoch.conductor.settings.ConductorSettings;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DiscordBotServiceTest {

    @Test
    void shouldStartWhenTokenAndChannelAreConfiguredAndReconfigureDynamically() throws Exception {
        ObjectProvider<DiscordCommandRouter> routerProvider = mock(ObjectProvider.class);
        ObjectProvider<DiscordStatusNotifier> notifierProvider = mock(ObjectProvider.class);
        ObjectProvider<ProcessService> processServiceProvider = mock(ObjectProvider.class);
        ConductorSettings settings = mock(ConductorSettings.class);
        ProfileRepository profileRepository = mock(ProfileRepository.class);
        DiscordBotService service = new DiscordBotService(
                routerProvider, notifierProvider, processServiceProvider, settings, profileRepository);
        JDABuilder builder = mock(JDABuilder.class);
        JDA initialJda = mock(JDA.class);
        JDA updatedJda = mock(JDA.class);
        InstanceProfile profile = new InstanceProfile();
        profile.setId("worker#1");
        when(settings.getDiscordBotToken()).thenReturn(
                "initial-token", "initial-token", "updated-token", "updated-token", "updated-token");
        when(profileRepository.loadAll()).thenReturn(
                List.of(), List.of(profile), List.of(profile), List.of(profile), List.of());
        when(builder.addEventListeners(any(Object[].class))).thenReturn(builder);
        when(builder.build()).thenReturn(initialJda, updatedJda);

        try (MockedStatic<JDABuilder> builderFactory = mockStatic(JDABuilder.class)) {
            builderFactory.when(() -> JDABuilder.createDefault("initial-token")).thenReturn(builder);
            builderFactory.when(() -> JDABuilder.createDefault("updated-token")).thenReturn(builder);

            service.refresh();
            assertNull(service.getJda());

            profile.setDiscordChannelId("123456789012345678");
            service.refresh();
            assertNotNull(service.getJda());

            service.refresh();
            verify(initialJda).shutdownNow();
            verify(builder, times(2)).build();

            service.refresh();
            verify(builder, times(2)).build();

            profile.setDiscordChannelId(null);
            service.refresh();
            assertNull(service.getJda());
            verify(updatedJda).shutdownNow();
        }
    }
}
