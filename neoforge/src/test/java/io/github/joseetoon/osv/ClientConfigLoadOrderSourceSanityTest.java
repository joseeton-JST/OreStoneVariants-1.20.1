package io.github.joseetoon.osv;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientConfigLoadOrderSourceSanityTest {

    @Test
    void modelCacheCheckHappensInsideLoadCompleteListener() throws IOException {
        final String source = Files.readString(resolveSource(
            "src/main/java/io/github/joseetoon/osv/forge/OSV.java"
        ));

        final int initClient = source.indexOf("private void initClient");
        final int listener = source.indexOf("modBus.addListener(EventPriority.LOWEST, (FMLLoadCompleteEvent e) -> {", initClient);
        final int modelCacheCheck = source.indexOf("OsvTrackers.modelCache().isUpdated()", initClient);

        assertTrue(initClient >= 0, "Missing initClient method");
        assertTrue(listener >= 0, "Missing FMLLoadComplete listener");
        assertTrue(modelCacheCheck > listener, "Model cache check still runs before config load");
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
