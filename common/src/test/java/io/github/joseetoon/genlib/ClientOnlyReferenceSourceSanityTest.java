package io.github.joseetoon.genlib;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ClientOnlyReferenceSourceSanityTest {

    @Test
    void serverLoadedCommonClassesDoNotImportClientMinecraftTypes() throws IOException {
        final List<String> serverLoadedSources = List.of(
            "src/main/java/io/github/joseetoon/genlib/event/error/LibErrorContext.java",
            "src/main/java/io/github/joseetoon/genlib/exception/FormattedException.java",
            "src/main/java/io/github/joseetoon/genlib/command/CommandContextWrapper.java",
            "src/main/java/io/github/joseetoon/osv/preset/data/ModelSettings.java"
        );

        for (final String sourcePath : serverLoadedSources) {
            final String source = Files.readString(resolveSource(sourcePath));
            assertFalse(source.contains("import net.minecraft.client"), sourcePath);
            assertFalse(source.contains("net.minecraft.client."), sourcePath);
        }
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
