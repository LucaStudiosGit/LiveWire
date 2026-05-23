package com.livewire.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigStoreTest {

    static class Settings {
        @ExposedConfig int health = 100;
        @ExposedConfig String name = "Adventurer";
    }

    @Test
    void readMissingFileReturnsEmpty(@TempDir Path dir) throws Exception {
        ConfigStore store = new ConfigStore(dir.resolve("missing.json"));
        assertTrue(store.read().isEmpty());
    }

    @Test
    void writeThenReadRoundtripsAllBindings(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("config.json");
        ConfigStore store = new ConfigStore(file);

        ConfigRegistry registry = new ConfigRegistry();
        registry.register(new Settings());
        store.write(registry);

        assertTrue(Files.exists(file));

        Map<String, JsonNode> back = store.read();
        assertEquals(100, back.get("Settings.health").asInt());
        assertEquals("Adventurer", back.get("Settings.name").asText());
    }

    @Test
    void writeCreatesParentDirectories(@TempDir Path dir) throws Exception {
        Path nested = dir.resolve("a/b/c/config.json");
        ConfigStore store = new ConfigStore(nested);

        ConfigRegistry registry = new ConfigRegistry();
        registry.register(new Settings());
        store.write(registry);

        assertTrue(Files.exists(nested));
    }
}
