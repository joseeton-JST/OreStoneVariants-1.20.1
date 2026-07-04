package io.github.joseetoon.osv;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ConfigPreloadSourceSanityTest {

    @Test
    void criticalClassesDoNotCaptureCfgValuesInEagerStatics() throws IOException {
        assertFalse(read("src/main/java/io/github/joseetoon/osv/preset/data/ModelSettings.java")
            .contains("public static final ModelSettings EMPTY = new ModelSettings(Cfg."));
        assertFalse(read("src/main/java/io/github/joseetoon/osv/preset/data/VariantSettings.java")
            .contains("new VariantSettings(null, null, null, true, true, Cfg."));
        assertFalse(read("src/main/java/io/github/joseetoon/osv/preset/data/VariantSettings.java")
            .contains("return new VariantSettings(id, null, null, true, true, Cfg."));
        assertFalse(read("src/main/java/io/github/joseetoon/osv/world/interceptor/InterceptorDispatcher.java")
            .contains("public static final boolean COMPATIBILITY_MODE = Cfg.forceCompatibilityMode()"));
        assertFalse(read("src/main/java/io/github/joseetoon/osv/config/OsvTrackers.java")
            .contains(".track(new ModelCache())"));
        assertFalse(read("src/main/java/io/github/joseetoon/osv/preset/data/ModelSettings.java")
            .contains("defaultGet(Type.CODEC, Fields.type, Cfg::modelType"));
        assertFalse(read("src/main/java/io/github/joseetoon/osv/preset/data/ModelSettings.java")
            .contains("defaultGet(Codec.BOOL, Fields.shade, Cfg::overlayShade"));
        assertFalse(read("src/main/java/io/github/joseetoon/osv/preset/data/VariantSettings.java")
            .contains("defaultGet(Codec.BOOL, Fields.bgImitation, Cfg::bgImitation"));
        assertFalse(read("src/main/java/io/github/joseetoon/osv/preset/data/VariantSettings.java")
            .contains("defaultGet(Codec.BOOL, Fields.bgDuplication, Cfg::bgDuplication"));
        assertFalse(read("src/main/java/io/github/joseetoon/osv/preset/data/PlacedFeatureSettings.java")
            .contains("ctx.readDouble(Fields.denseRatio, Cfg::denseChance)"));
    }

    private static String read(final String relative) throws IOException {
        final Path cwd = Path.of("").toAbsolutePath().normalize();
        final Path direct = cwd.resolve(relative);
        if (Files.exists(direct)) {
            return Files.readString(direct);
        }
        final Path nested = cwd.resolve("common").resolve(relative);
        if (Files.exists(nested)) {
            return Files.readString(nested);
        }
        throw new IllegalStateException("Missing source file: " + relative);
    }
}
