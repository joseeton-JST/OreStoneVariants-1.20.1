package io.github.joseetoon.genlib;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BiomeModifierRegistrationSanityTest {

    @Test
    void neoforgeBiomeModifierDatapackEntryExistsInExpectedPath() throws IOException {
        try (InputStream in = getClass().getClassLoader()
            .getResourceAsStream("data/genlib/neoforge/biome_modifier/catlib_modifier.json")) {
            assertNotNull(in, "Missing NeoForge biome modifier datapack entry");
            final String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("\"type\": \"genlib:catlib_modifier\""));
        }
    }

    @Test
    void legacyForgeBiomeModifierPathIsNotShippedAnymore() {
        final InputStream legacy = getClass().getClassLoader()
            .getResourceAsStream("data/genlib/forge/biome_modifier/catlib_modifier.json");
        assertTrue(legacy == null, "Legacy Forge biome modifier path still packaged");
    }

    @Test
    void catLibStillRegistersBiomeModifierSerializer() throws IOException {
        final String source = Files.readString(resolveSource(
            "src/main/java/io/github/joseetoon/genlib/CatLib.java"
        ));

        assertTrue(source.contains("NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS"));
        assertTrue(source.contains("ResourceLocation.fromNamespaceAndPath(LibReference.MOD_ID, \"catlib_modifier\")"));
        assertFalse(source.contains("data/genlib/forge/biome_modifier"));
    }

    private static Path resolveSource(final String relative) {
        final Path cwd = Path.of("").toAbsolutePath().normalize();
        final Path direct = cwd.resolve(relative);
        if (Files.exists(direct)) {
            return direct;
        }
        final Path nested = cwd.resolve("neoforge").resolve(relative);
        if (Files.exists(nested)) {
            return nested;
        }
        throw new IllegalStateException("Missing source file: " + relative);
    }
}
