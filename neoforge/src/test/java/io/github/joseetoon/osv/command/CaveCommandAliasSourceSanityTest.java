package io.github.joseetoon.osv.command;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CaveCommandAliasSourceSanityTest {

    @Test
    void defaultLibCommandsExposeCaveTestAliasBuilder() throws IOException {
        final String source = Files.readString(resolveCommonSource(
            "src/main/java/io/github/joseetoon/genlib/command/DefaultLibCommands.java"
        ));

        assertTrue(source.contains("createCaveTestAlias"), "Missing /cave test alias builder");
        assertTrue(source.contains("LibCommandBuilder.named(\"cave\")"), "Alias must register root /cave literal");
        assertTrue(source.contains(".then(literal(\"test\")"), "Alias must expose /cave test");
    }

    @Test
    void osvRegistersCaveTestAliasAsRootCommand() throws IOException {
        final String source = Files.readString(resolveNeoForgeSource(
            "src/main/java/io/github/joseetoon/osv/forge/OSV.java"
        ));

        assertTrue(source.contains("LibCommandRegistrar.registerCommand("), "OSV must register root alias explicitly");
        assertTrue(source.contains("DefaultLibCommands.createCaveTestAlias"), "OSV must register the cave alias builder");
    }

    private static Path resolveCommonSource(final String relative) {
        final Path cwd = Path.of("").toAbsolutePath().normalize();
        final Path direct = cwd.resolve(relative);
        if (Files.exists(direct)) {
            return direct;
        }
        final Path nested = cwd.resolve("common").resolve(relative);
        if (Files.exists(nested)) {
            return nested;
        }
        final Path sibling = cwd.getParent() != null ? cwd.getParent().resolve("common").resolve(relative) : null;
        if (sibling != null && Files.exists(sibling)) {
            return sibling;
        }
        throw new IllegalStateException("Missing source file: " + relative);
    }

    private static Path resolveNeoForgeSource(final String relative) {
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
