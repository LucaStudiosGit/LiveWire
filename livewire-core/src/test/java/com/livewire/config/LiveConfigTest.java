package com.livewire.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class LiveConfigTest {

    static class Settings {
        @ExposedConfig int health = 100;
        @ExposedConfig String name = "Adventurer";
        @ExposedConfig boolean doubleJump = true;
    }

    @Test
    void startWritesDefaultsWhenFileMissing(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("config.json");
        Settings s = new Settings();

        try (LiveConfig config = new LiveConfig(file, 0)) {
            config.register(s);
            config.start();

            assertTrue(Files.exists(file));
            JsonNode root = new ObjectMapper().readTree(file.toFile());
            assertEquals(100, root.get("Settings.health").asInt());
            assertEquals("Adventurer", root.get("Settings.name").asText());
            assertTrue(root.get("Settings.doubleJump").asBoolean());
        }
    }

    @Test
    void startLoadsValuesFromExistingFile(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("config.json");
        Files.writeString(file, """
                {
                  "Settings.health": 250,
                  "Settings.name": "Boss",
                  "Settings.doubleJump": false
                }
                """);

        Settings s = new Settings();
        try (LiveConfig config = new LiveConfig(file, 0)) {
            config.register(s);
            config.start();

            assertEquals(250, s.health);
            assertEquals("Boss", s.name);
            assertFalse(s.doubleJump);
        }
    }

    @Test
    void startAppendsMissingKeysToExistingFile(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("config.json");
        Files.writeString(file, """
                { "Settings.health": 250 }
                """);

        Settings s = new Settings();
        try (LiveConfig config = new LiveConfig(file, 0)) {
            config.register(s);
            config.start();
        }

        JsonNode root = new ObjectMapper().readTree(file.toFile());
        assertEquals(250, root.get("Settings.health").asInt());
        assertEquals("Adventurer", root.get("Settings.name").asText());
        assertNotNull(root.get("Settings.doubleJump"));
    }

    @Test
    void webApiUpdatesLiveValue(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("config.json");
        Settings s = new Settings();

        try (LiveConfig config = new LiveConfig(file, 0)) {
            config.register(s);
            config.start();
            int port = config.webPort();

            HttpClient http = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .build();

            HttpRequest post = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/config"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "{\"key\":\"Settings.health\",\"value\":777}"))
                    .build();
            HttpResponse<String> resp = http.send(post, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, resp.statusCode(), resp.body());
            assertEquals(777, s.health);

            JsonNode onDisk = new ObjectMapper().readTree(file.toFile());
            assertEquals(777, onDisk.get("Settings.health").asInt());
        }
    }

    @Test
    void webApiRejectsUnknownKey(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("config.json");
        try (LiveConfig config = new LiveConfig(file, 0)) {
            config.register(new Settings());
            config.start();
            int port = config.webPort();

            HttpClient http = HttpClient.newHttpClient();
            HttpRequest post = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/config"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "{\"key\":\"does.not.exist\",\"value\":1}"))
                    .build();
            HttpResponse<String> resp = http.send(post, HttpResponse.BodyHandlers.ofString());

            assertEquals(404, resp.statusCode());
        }
    }
}
