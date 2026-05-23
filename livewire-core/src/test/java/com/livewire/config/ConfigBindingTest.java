package com.livewire.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class ConfigBindingTest {

    enum Mode { OFF, ON, AUTO }

    static class Holder {
        int i = 1;
        double d = 1.5;
        boolean b = false;
        String s = "x";
        Mode m = Mode.OFF;
    }

    private static Field field(String name) throws NoSuchFieldException {
        return Holder.class.getDeclaredField(name);
    }

    @Test
    void readWriteTypedValues() throws Exception {
        Holder h = new Holder();
        new ConfigBinding("i", field("i"), h, "").write(42);
        new ConfigBinding("d", field("d"), h, "").write(3.14);
        new ConfigBinding("b", field("b"), h, "").write(true);
        new ConfigBinding("s", field("s"), h, "").write("hi");
        new ConfigBinding("m", field("m"), h, "").write(Mode.AUTO);

        assertEquals(42, h.i);
        assertEquals(3.14, h.d);
        assertTrue(h.b);
        assertEquals("hi", h.s);
        assertEquals(Mode.AUTO, h.m);
    }

    @Test
    void writeFromJsonCoercesViaJackson() throws Exception {
        Holder h = new Holder();
        ObjectMapper mapper = new ObjectMapper();

        new ConfigBinding("i", field("i"), h, "").writeFromJson(mapper.readTree("99"), mapper);
        new ConfigBinding("m", field("m"), h, "").writeFromJson(mapper.readTree("\"ON\""), mapper);

        assertEquals(99, h.i);
        assertEquals(Mode.ON, h.m);
    }

    @Test
    void nonStaticFieldRequiresInstance() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> new ConfigBinding("i", field("i"), null, ""));
    }
}
