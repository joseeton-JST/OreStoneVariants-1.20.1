package io.github.joseetoon.osv.config.neoforge;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CfgImplSourceSanityTest {

    @Test
    void earlyConfigGettersUsePreloadSafeHelpers() throws IOException {
        final String source = Files.readString(resolveSource(
            "src/main/java/io/github/joseetoon/osv/config/neoforge/CfgImpl.java"
        ));

        assertTrue(source.contains("return getBooleanValue(FORCE_COMPATIBILITY_MODE"));
        assertTrue(source.contains("return getEnumValue(MODEL_TYPE"));
        assertTrue(source.contains("return getBooleanValue(OVERLAY_MODEL_SHADE"));
        assertTrue(source.contains("return getBooleanValue(BG_IMITATION"));
        assertTrue(source.contains("return getBooleanValue(BG_DUPLICATION"));
        assertTrue(source.contains("return getBooleanValue(DEBUG_STARTUP"));
        assertTrue(source.contains("return getListValue(BLOCK_ENTRIES"));
        assertTrue(source.contains("return getDoubleValue(OVERLAY_SCALE"));
        assertTrue(source.contains("return getListValue(DISABLED_FEATURES"));
        assertTrue(source.contains("ItemFormatterTomlConfig.FILE_NAME"));
        assertTrue(source.contains("ItemFormatterTomlConfig.load()"));
        assertTrue(source.contains("define(\"items.formattersFile\""));
        assertFalse(source.contains("DynamicCategory<List<Map<String, Object>>> FORMATTERS"));
        assertFalse(source.contains("withPath(\"items.formatters\")"));
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
