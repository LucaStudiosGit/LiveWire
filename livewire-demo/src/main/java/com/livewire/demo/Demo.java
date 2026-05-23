package com.livewire.demo;

import com.livewire.config.LiveConfig;

import java.nio.file.Path;

public final class Demo {
    public static void main(String[] args) throws Exception {
        Path configFile = Path.of("livewire-config.json");
        int port = 7777;

        GameSettings settings = new GameSettings();

        LiveConfig config = new LiveConfig(configFile, port);
        config.register(settings);
        config.registerStatic(GameSettings.class);
        config.start();

        System.out.println("[demo] LiveWire running.");
        System.out.println("[demo]   config file: " + configFile.toAbsolutePath());
        System.out.println("[demo]   web UI:      http://localhost:" + config.webPort() + "/");
        System.out.println("[demo] Edit the JSON file OR use the web UI to change values live.");
        System.out.println("[demo] Ctrl-C to stop.");

        while (true) {
            Thread.sleep(3000);
            System.out.printf("[tick] maxHealth=%d moveSpeed=%.2f doubleJump=%b name=%s difficulty=%s globalSpawnRate=%d%n",
                    settings.maxHealth, settings.moveSpeed, settings.doubleJumpEnabled,
                    settings.playerName, settings.difficulty, GameSettings.globalSpawnRate);
        }
    }
}
