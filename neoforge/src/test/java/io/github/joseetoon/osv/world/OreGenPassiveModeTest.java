package io.github.joseetoon.osv.world;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OreGenPassiveModeTest {

    @Test
    void passiveSwapFeatureIsInjectedIntoTopLayerModificationStepSource() throws IOException {
        final String source = Files.readString(resolveCommonSource(
            "src/main/java/io/github/joseetoon/osv/world/OreGen.java"
        ));

        assertTrue(source.contains("private static final PlacedFeature PASSIVE_SWAP = new PlacedFeature("));
        assertTrue(source.contains("PassiveOreSwapFeature.INSTANCE"));
        assertTrue(source.contains("ctx.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, PASSIVE_SWAP);"));
        assertTrue(source.contains("if (Cfg.enableOSVOres()) {"));
        assertTrue(source.contains("addPassiveSwapFeature(ctx);"));
    }

    @Test
    void disabledGenerationPresetsDoNotFeedCustomOreWorldgenSource() throws IOException {
        final String source = Files.readString(resolveCommonSource(
            "src/main/java/io/github/joseetoon/osv/world/OreGen.java"
        ));

        assertTrue(source.contains("if (!preset.getGen().isEnabled()) {"));
        assertTrue(source.contains("continue;"));
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
}
