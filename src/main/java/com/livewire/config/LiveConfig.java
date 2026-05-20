package com.livewire.config;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public final class LiveConfig implements AutoCloseable {
    private final ConfigRegistry registry;
    private final ConfigStore store;
    private final ConfigWatcher watcher;
    private final ConfigWebServer webServer;

    public LiveConfig(Path configFile, int webPort) throws IOException {
        this.registry = new ConfigRegistry();
        this.store = new ConfigStore(configFile);
        this.watcher = new ConfigWatcher(configFile, this::reload);
        this.webServer = new ConfigWebServer(webPort, registry, store, watcher);
    }

    public ConfigRegistry registry() { return registry; }
    public int webPort() { return webServer.port(); }

    public LiveConfig register(Object instance) {
        registry.register(instance);
        return this;
    }

    public LiveConfig registerStatic(Class<?> clazz) {
        registry.registerStatic(clazz);
        return this;
    }

    public void start() throws IOException {
        loadOrInit();
        watcher.start();
        webServer.start();
    }

    private void loadOrInit() throws IOException {
        Map<String, JsonNode> onDisk = store.read();
        if (onDisk.isEmpty()) {
            watcher.suppress(500);
            store.write(registry);
            return;
        }
        applyFromDisk(onDisk);
        boolean newKeys = registry.bindings().stream().anyMatch(b -> !onDisk.containsKey(b.key()));
        if (newKeys) {
            watcher.suppress(500);
            store.write(registry);
        }
    }

    private void reload() {
        try {
            Map<String, JsonNode> onDisk = store.read();
            applyFromDisk(onDisk);
            System.out.println("[livewire] reloaded " + onDisk.size() + " values from " + store.file());
        } catch (IOException e) {
            System.err.println("[livewire] reload failed: " + e.getMessage());
        }
    }

    private void applyFromDisk(Map<String, JsonNode> onDisk) {
        for (Map.Entry<String, JsonNode> e : onDisk.entrySet()) {
            ConfigBinding b = registry.get(e.getKey());
            if (b == null) continue;
            try {
                registry.setFromJson(e.getKey(), e.getValue(), store.mapper());
            } catch (Exception ex) {
                System.err.println("[livewire] failed to apply " + e.getKey() + ": " + ex.getMessage());
            }
        }
    }

    @Override
    public void close() throws IOException {
        webServer.close();
        watcher.close();
    }
}
