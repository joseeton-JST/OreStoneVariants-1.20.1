package io.github.joseetoon.osv.preset.data;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ModelSettingsSourceSanityTest {

    @Test
    void modelSettingsDoesNotEagerlyLoadClientGenerators() throws IOException {
        final String source = Files.readString(resolveSource(
            "src/main/java/io/github/joseetoon/osv/preset/data/ModelSettings.java"
        ));

        assertFalse(source.contains("io.github.joseetoon.osv.client.model"));
        assertFalse(source.contains("new SingleLayerModelGenerator"));
        assertFalse(source.contains("new OverlayModelGenerator"));
        assertFalse(source.contains("ModelGenerator generator"));
    }

    private static Path resolveSource(final String relative) {
        final Path cwd = Path.of("").toAbsolutePath().normalize();
        final Path direct = cwd.resolve(relative);
        if (Files.exists(direct)) {
            return direct;
        }
        final Path nested = cwd.resolve("common").resolve(relative);
        if (Files.exists(nested)) {
            return nested;
        }
        throw new IllegalStateException("Missing source file: " + relative);
    }
}
