package com.livewire.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class ConfigBinding {
    private final String key;
    private final Field field;
    private final Object instance;
    private final String description;

    public ConfigBinding(String key, Field field, Object instance, String description) {
        if (!Modifier.isStatic(field.getModifiers()) && instance == null) {
            throw new IllegalArgumentException("Non-static field requires an instance: " + key);
        }
        field.setAccessible(true);
        this.key = key;
        this.field = field;
        this.instance = instance;
        this.description = description;
    }

    public String key() { return key; }
    public Field field() { return field; }
    public Object instance() { return instance; }
    public String description() { return description; }
    public Class<?> type() { return field.getType(); }

    public Object read() {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to read " + key, e);
        }
    }

    public void write(Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to write " + key, e);
        }
    }

    public void writeFromJson(JsonNode node, ObjectMapper mapper) {
        Object converted = mapper.convertValue(node, field.getType());
        write(converted);
    }
}
