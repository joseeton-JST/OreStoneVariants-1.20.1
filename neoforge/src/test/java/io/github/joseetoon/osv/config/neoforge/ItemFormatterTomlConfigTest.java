package io.github.joseetoon.osv.config.neoforge;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemFormatterTomlConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void roundTripsDedicatedFormatterFileWithOptionalKeys() throws IOException {
        final Path configDir = this.tempDir.resolve("config");
        Files.createDirectories(configDir);

        final Map<String, Object> dense = formatter("{osv.denseKey} {fg} ({bg})");
        dense.put("bold", true);
        dense.put("color", "#123456");

        final Map<String, Object> normal = formatter("{fg} ({bg})");
        normal.put("italic", true);
        normal.put("font", "minecraft:default");

        final LinkedHashMap<String, List<Map<String, Object>>> source = new LinkedHashMap<>();
        source.put("dense=true", List.of(dense));
        source.put("", List.of(normal));

        ItemFormatterTomlConfig.save(configDir, source);
        final LinkedHashMap<String, List<Map<String, Object>>> loaded = ItemFormatterTomlConfig.load(configDir);

        assertEquals(source, loaded);

        final String written = Files.readString(configDir.resolve("osv-client-formatters.toml"));
        assertTrue(written.contains("[[formatter]]"));
        assertTrue(written.contains("when = \"dense=true\""));
        assertTrue(written.contains("text = \"{fg} ({bg})\""));
    }

    @Test
    void migratesLegacyItemsFormattersFromClientTomlOnce() throws IOException {
        final Path configDir = this.tempDir.resolve("config");
        Files.createDirectories(configDir);

        Files.writeString(configDir.resolve("osv-client.toml"), """
            [items]

              [items.formatters]
                [[items.formatters.\"dense=true\"]]
                text = "{osv.denseKey} {fg} ({bg})"
                bold = true

                [[items.formatters.\"\"]]
                text = "{fg} ({bg})"
                color = "#123456"
            """);

        final LinkedHashMap<String, List<Map<String, Object>>> loaded = ItemFormatterTomlConfig.load(configDir);

        assertEquals("{osv.denseKey} {fg} ({bg})", loaded.get("dense=true").getFirst().get("text"));
        assertEquals(true, loaded.get("dense=true").getFirst().get("bold"));
        assertEquals("{fg} ({bg})", loaded.get("").getFirst().get("text"));
        assertEquals("#123456", loaded.get("").getFirst().get("color"));

        final Path formatterFile = configDir.resolve("osv-client-formatters.toml");
        assertTrue(Files.exists(formatterFile));
        assertTrue(Files.readString(formatterFile).contains("[[formatter]]"));

        final String clientToml = Files.readString(configDir.resolve("osv-client.toml"));
        assertFalse(clientToml.contains("[items.formatters]"));
        assertTrue(clientToml.contains("formattersFile = \"osv-client-formatters.toml\""));
    }

    private static Map<String, Object> formatter(final String text) {
        final Map<String, Object> formatter = new LinkedHashMap<>();
        formatter.put("text", text);
        return formatter;
    }
}
