package io.github.joseetoon.genlib.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibConfigSourceSanityTest {

    @Test
    void vendoredLibConfigUsesToml() throws IOException {
        final String source = Files.readString(resolveSource(
            "src/main/java/io/github/joseetoon/genlib/config/LibConfig.java"
        ));

        assertTrue(source.contains(".toml"));
        assertTrue(source.contains("TomlFormat.instance()"));
        assertFalse(source.contains("new HjsonFileConfig"));
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
