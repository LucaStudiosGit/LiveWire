package com.livewire.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public final class ConfigRegistry {
    private final Map<String, ConfigBinding> bindings = new ConcurrentHashMap<>();
    private final List<BiConsumer<String, Object>> listeners = new CopyOnWriteArrayList<>();

    public void register(Object instance) {
        if (instance == null) throw new IllegalArgumentException("instance is null");
        scan(instance.getClass(), instance);
    }

    public void registerStatic(Class<?> clazz) {
        scan(clazz, null);
    }

    private void scan(Class<?> clazz, Object instance) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                ExposedConfig anno = f.getAnnotation(ExposedConfig.class);
                if (anno == null) continue;
                boolean isStatic = Modifier.isStatic(f.getModifiers());
                if (instance == null && !isStatic) continue;
                if (instance != null && isStatic) continue;
                String key = anno.key().isEmpty()
                        ? c.getSimpleName() + "." + f.getName()
                        : anno.key();
                if (bindings.containsKey(key)) {
                    throw new IllegalStateException("Duplicate config key: " + key);
                }
                bindings.put(key, new ConfigBinding(key, f, instance, anno.description()));
            }
        }
    }

    public Collection<ConfigBinding> bindings() {
        return bindings.values();
    }

    public ConfigBinding get(String key) {
        return bindings.get(key);
    }

    public void setFromJson(String key, JsonNode node, ObjectMapper mapper) {
        ConfigBinding b = bindings.get(key);
        if (b == null) throw new IllegalArgumentException("Unknown key: " + key);
        b.writeFromJson(node, mapper);
        Object newValue = b.read();
        for (BiConsumer<String, Object> l : listeners) l.accept(key, newValue);
    }

    public void addChangeListener(BiConsumer<String, Object> listener) {
        listeners.add(listener);
    }
}
