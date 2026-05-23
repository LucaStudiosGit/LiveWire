package com.livewire.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigRegistryTest {

    static class Sample {
        @ExposedConfig int health = 100;
        @ExposedConfig(key = "speed") double moveSpeed = 4.5;
        @ExposedConfig static int globalRate = 5;
        int notExposed = 99;
    }

    static class Duplicate {
        @ExposedConfig(key = "shared") int a = 1;
        @ExposedConfig(key = "shared") int b = 2;
    }

    @Test
    void registersInstanceFieldsOnly() {
        ConfigRegistry r = new ConfigRegistry();
        r.register(new Sample());

        assertNotNull(r.get("Sample.health"));
        assertNotNull(r.get("speed"));
        assertNull(r.get("Sample.globalRate"), "static fields are not picked up by register(instance)");
        assertNull(r.get("Sample.notExposed"));
    }

    @Test
    void registersStaticFieldsOnly() {
        ConfigRegistry r = new ConfigRegistry();
        r.registerStatic(Sample.class);

        assertNotNull(r.get("Sample.globalRate"));
        assertNull(r.get("Sample.health"), "instance fields are not picked up by registerStatic");
    }

    @Test
    void duplicateKeyThrows() {
        ConfigRegistry r = new ConfigRegistry();
        assertThrows(IllegalStateException.class, () -> r.register(new Duplicate()));
    }

    @Test
    void changeListenerFiresOnApply() {
        ConfigRegistry r = new ConfigRegistry();
        Sample s = new Sample();
        r.register(s);

        StringBuilder seen = new StringBuilder();
        r.addChangeListener((key, value) -> seen.append(key).append('=').append(value).append(';'));

        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        r.setFromJson("Sample.health", mapper.valueToTree(250), mapper);

        assertEquals(250, s.health);
        assertEquals("Sample.health=250;", seen.toString());
    }
}
