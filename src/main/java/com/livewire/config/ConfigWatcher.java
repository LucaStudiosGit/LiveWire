package com.livewire.config;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class ConfigWatcher implements AutoCloseable {
    private final Path file;
    private final Runnable onChange;
    private final WatchService watchService;
    private final Thread thread;
    private final AtomicLong ignoreUntilNanos = new AtomicLong(0L);
    private volatile boolean running = true;

    public ConfigWatcher(Path file, Runnable onChange) throws IOException {
        this.file = file.toAbsolutePath();
        this.onChange = onChange;
        this.watchService = FileSystems.getDefault().newWatchService();
        Path dir = this.file.getParent();
        if (dir == null) throw new IOException("File has no parent directory: " + file);
        dir.register(watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE);
        this.thread = new Thread(this::loop, "livewire-config-watcher");
        this.thread.setDaemon(true);
    }

    public void start() {
        thread.start();
    }

    // Suppress watcher callbacks for the next `millis` ms (used right before our own writes).
    public void suppress(long millis) {
        ignoreUntilNanos.set(System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(millis));
    }

    @SuppressWarnings("unchecked")
    private void loop() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                return;
            }
            boolean triggered = false;
            for (WatchEvent<?> ev : key.pollEvents()) {
                if (ev.kind() == StandardWatchEventKinds.OVERFLOW) continue;
                Path changed = ((WatchEvent<Path>) ev).context();
                Path resolved = file.getParent().resolve(changed).toAbsolutePath();
                if (resolved.equals(file)) {
                    triggered = true;
                }
            }
            if (!key.reset()) return;
            if (!triggered) continue;
            if (System.nanoTime() < ignoreUntilNanos.get()) continue;
            try {
                Thread.sleep(50); // debounce noisy editors
            } catch (InterruptedException e) {
                return;
            }
            try {
                onChange.run();
            } catch (Exception e) {
                System.err.println("[livewire] reload handler threw: " + e.getMessage());
            }
        }
    }

    @Override
    public void close() throws IOException {
        running = false;
        watchService.close();
        thread.interrupt();
    }
}
