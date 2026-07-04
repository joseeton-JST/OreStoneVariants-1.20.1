package io.github.joseetoon.osv.preset.reader;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ComponentReaderDefaultsTest {

    @Test
    void defaultFormatterMapsUseTomlSerializableMapImplementations() {
        assertTomlSerializableMap(ComponentReader.DEFAULT_DENSE.getClass().getName());
        assertTomlSerializableMap(ComponentReader.DEFAULT_NORMAL.getClass().getName());
    }

    private static void assertTomlSerializableMap(final String className) {
        assertFalse(className.contains("Immutable"), className);
        assertFalse(className.contains("BiMap"), className);
    }

    @Test
    void defaultFormatterTextMatchesCurrentRuntimeBehavior() {
        assertEquals("{osv.denseKey} {fg} ({bg})", ComponentReader.DEFAULT_DENSE.get("text"));
        assertEquals("{fg} ({bg})", ComponentReader.DEFAULT_NORMAL.get("text"));
    }

    @Test
    void optionalFormatterKeysDoNotChangeResultingStyle() {
        final Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("text", "{fg} ({bg})");
        raw.put("bold", true);
        raw.put("italic", true);
        raw.put("underlined", true);
        raw.put("color", "#123456");
        raw.put("font", "minecraft:default");

        final Component component = ComponentReader.fromRaw(raw);
        final Component baseline = net.minecraft.network.chat.Component.literal("{fg} ({bg})");

        assertEquals("{fg} ({bg})", component.getString());
        assertEquals(baseline.getStyle(), component.getStyle());
    }
}
