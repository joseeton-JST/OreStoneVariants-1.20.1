package io.github.joseetoon.genlib.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibReferenceSourceSanityTest {

    @Test
    void vendoredLibReferenceHasFixedInternalMetadata() throws IOException {
        final String source = Files.readString(resolveSource(
            "src/main/java/io/github/joseetoon/genlib/util/LibReference.java"
        ));

        assertFalse(source.contains("@MOD_"));
        assertTrue(source.contains("MOD_ID = \"genlib\""));
        assertTrue(source.contains("MOD_NAME = \"GenLib\""));
        assertTrue(source.contains("Version.parse(\"1.2.22\")"));
    }

    private static Path resolveSource(final String relative) {
        final Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
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
