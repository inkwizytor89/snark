package org.enoch.snark.config;

import org.enoch.common.config.ConfigEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThreadTypeTest {

    @Test
    void resolvesTypeKeyIgnoringCase() {
        ConfigEntry entry = new ConfigEntry("global", "custom", "TYPE", "ExPeDiTiOn");
        assertEquals("expedition", ThreadType.resolve(entry));
    }

    @Test
    void resolvesTypeFromThreadNameIgnoringCase() {
        ConfigEntry entry = new ConfigEntry("global", "MAIN_FLEET_SAVE", "source", "");
        assertEquals("fleet", ThreadType.resolve(entry));
    }

    @Test
    void defaultsToFleetForUnknownThreadName() {
        ConfigEntry entry = new ConfigEntry("global", "custom", "source", "");
        assertEquals("fleet", ThreadType.resolve(entry));
    }
}
