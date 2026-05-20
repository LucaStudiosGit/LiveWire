package com.livewire.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigStore {
    private final Path file;
    private final ObjectMapper mapper;

    public ConfigStore(Path file) {
        this.file = file;
        this.mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public ObjectMapper mapper() { return mapper; }
    public Path file() { return file; }

    public Map<String, JsonNode> read() throws IOException {
        if (!Files.exists(file)) return new LinkedHashMap<>();
        JsonNode root = mapper.readTree(file.toFile());
        Map<String, JsonNode> out = new LinkedHashMap<>();
        root.fields().forEachRemaining(e -> out.put(e.getKey(), e.getValue()));
        return out;
    }

    public void write(ConfigRegistry registry) throws IOException {
        ObjectNode root = mapper.createObjectNode();
        for (ConfigBinding b : registry.bindings()) {
            root.set(b.key(), mapper.valueToTree(b.read()));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), root);
    }
}
