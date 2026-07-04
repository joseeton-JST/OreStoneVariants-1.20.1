package io.github.joseetoon.genlib;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TitleScreenErrorLoggingSourceSanityTest {

    @Test
    void titleScreenLogsLibErrorContextBeforeOpeningErrorMenu() throws IOException {
        final String source = Files.readString(resolveSource(
            "src/main/java/io/github/joseetoon/genlib/mixin/TitleScreenMixin.java"
        ));

        final int hasErrors = source.indexOf("if (LibErrorContext.hasErrors())");
        final int logCall = source.indexOf("LibErrorContext.logClientErrors();", hasErrors);
        final int menuOpen = source.indexOf("Minecraft.getInstance().setScreen(new LibErrorMenu(this));", hasErrors);

        assertTrue(hasErrors >= 0, "Missing LibErrorContext gate");
        assertTrue(logCall > hasErrors, "Missing client error log before LibErrorMenu");
        assertTrue(menuOpen > logCall, "LibErrorMenu opens before error summary is logged");
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
