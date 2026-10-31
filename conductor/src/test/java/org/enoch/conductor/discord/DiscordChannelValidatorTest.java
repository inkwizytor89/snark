package org.enoch.conductor.discord;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscordChannelValidatorTest {

    @Test
    void shouldAcceptNumericDiscordChannelIds() {
        assertTrue(DiscordChannelValidator.isAllowedForNotification("123456789012345678"));
    }

    @Test
    void shouldRejectInvalidChannelIds() {
        assertFalse(DiscordChannelValidator.isAllowedForNotification("abc"));
        assertFalse(DiscordChannelValidator.isAllowedForNotification(""));
        assertFalse(DiscordChannelValidator.isAllowedForNotification("0"));
    }
}
