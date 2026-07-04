package io.github.joseetoon.osv.preset.reader;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import org.hjson.JsonObject;
import org.hjson.JsonArray;
import org.hjson.JsonValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import io.github.joseetoon.genlib.util.HjsonUtils;
import io.github.joseetoon.osv.preset.data.PlacedFeatureSettings;
import io.github.joseetoon.osv.preset.data.StoneSettings;
import io.github.joseetoon.osv.world.providers.SimpleHeight;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeightProviderReaderLegacyTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void legacyAbsoluteHeightRangesDecodeFromInlineArrays() {
        final JsonValue value = HjsonUtils.readValue("[ 0, 80 ]").unwrap();

        final HeightProvider provider = assertDoesNotThrow(() -> HjsonUtils.readThrowing(HeightProviderReader.CODEC, value));

        assertNotNull(provider);
    }

    @Test
    void legacyAbsoluteHeightRangesEncodeBackToInlineArrays() {
        final JsonValue encoded = assertDoesNotThrow(
            () -> HjsonUtils.writeThrowing(HeightProviderReader.CODEC, new SimpleHeight(0, 80))
        );

        assertTrue(encoded.isArray());
        final JsonArray array = encoded.asArray();
        assertEquals(2, array.size());
        assertEquals(0, array.get(0).asInt());
        assertEquals(80, array.get(1).asInt());
    }

    @Test
    void representativeBundledStonePresetsStillParse() throws IOException {
        for (final String name : List.of("andesite.xjs", "granite.xjs", "gravel.xjs", "magma_block.xjs")) {
            final StoneSettings settings = assertDoesNotThrow(() -> readStoneSettings(resolveMinecraftStonePreset(name)), name);

            assertNotNull(settings, name);
            assertNotNull(settings.getStone(), name);
        }
    }

    @Test
    void allBundledStonePresetsStillParse() throws IOException {
        final Path root = resolveStonePresetRoot();
        final List<Path> presets = Files.walk(root)
            .filter(Files::isRegularFile)
            .filter(path -> {
                final String fileName = path.getFileName().toString();
                return fileName.endsWith(".xjs") || fileName.endsWith(".hjson") || fileName.endsWith(".json");
            })
            .filter(path -> !path.getFileName().toString().equalsIgnoreCase("TUTORIAL.xjs"))
            .toList();

        for (final Path preset : presets) {
            final StoneSettings settings = assertDoesNotThrow(
                () -> readStoneSettings(preset),
                preset.toString()
            );

            assertNotNull(settings, preset.toString());
        }
    }

    private static StoneSettings readStoneSettings(final Path preset) throws IOException {
        final String contents = Files.readString(preset);
        final JsonValue json = HjsonUtils.readValue(contents).unwrap();
        final JsonObject root = json.asObject();
        root.set(StoneSettings.Fields.stone, "minecraft:stone");
        for (final JsonObject gen : HjsonUtils.getRegularObjects(root, StoneSettings.Fields.gen)) {
            if (!gen.has(PlacedFeatureSettings.Fields.denseRatio)) {
                gen.add(PlacedFeatureSettings.Fields.denseRatio, 0.09D);
            }
        }
        return HjsonUtils.readThrowing(StoneSettings.CODEC, root);
    }

    private static Path resolveMinecraftStonePreset(final String relative) {
        return resolveStonePresetRoot().resolve("minecraft").resolve(relative);
    }

    private static Path resolveStonePresetRoot() {
        final Path cwd = Path.of("").toAbsolutePath().normalize();
        final Path base = cwd.resolve("src/main/resources/data/osv/stone");
        if (Files.exists(base)) {
            return base;
        }
        final Path nested = cwd.resolve("common/src/main/resources/data/osv/stone");
        if (Files.exists(nested)) {
            return nested;
        }
        throw new IllegalStateException("Missing bundled stone preset directory");
    }
}
