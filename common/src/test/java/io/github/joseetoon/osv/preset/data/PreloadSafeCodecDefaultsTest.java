package io.github.joseetoon.osv.preset.data;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import io.github.joseetoon.genlib.util.HjsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreloadSafeCodecDefaultsTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void modelSettingsCodecUsesLiteralDefaultsWhenFieldsAreOmitted() {
        final ModelSettings settings = assertDoesNotThrow(
            () -> HjsonUtils.readThrowing(ModelSettings.CODEC, new JsonObject())
        );

        assertEquals(ModelSettings.Type.SINGLE, settings.getType());
        assertTrue(settings.isShade());
    }

    @Test
    void variantSettingsCodecUsesLiteralDefaultsWhenFieldsAreOmitted() {
        final VariantSettings settings = assertDoesNotThrow(
            () -> HjsonUtils.readThrowing(VariantSettings.CODEC, new JsonObject())
        );

        assertNull(settings.getOriginal());
        assertNull(settings.getXp());
        assertNull(settings.getTranslationKey());
        assertTrue(settings.isCopyTags());
        assertTrue(settings.isCanBeDense());
        assertTrue(settings.isBgImitation());
        assertTrue(settings.isBgDuplication());
    }

    @Test
    void placedFeatureSettingsCodecUsesLiteralDenseRatioDefaultWhenFieldIsOmitted() throws IOException {
        final JsonObject feature = readFirstBundledStoneFeature("minecraft/granite.xjs");

        final PlacedFeatureSettings<?, ?> settings = assertDoesNotThrow(
            () -> HjsonUtils.readThrowing(PlacedFeatureSettings.CODEC, feature)
        );

        assertEquals(0.09D, settings.getDenseRatio());
    }

    @Test
    void bundledStonePresetStillParsesWithoutInjectingDenseRatio() throws IOException {
        final StoneSettings settings = assertDoesNotThrow(
            () -> readBundledStoneSettings("minecraft/granite.xjs")
        );

        assertEquals(1, settings.getGen().getFeatures().size());
        assertEquals(0.09D, settings.getGen().getFeatures().get(0).getDenseRatio());
    }

    private static StoneSettings readBundledStoneSettings(final String relative) throws IOException {
        final JsonObject root = readBundledObject(relative);
        root.set(StoneSettings.Fields.stone, "minecraft:stone");
        return HjsonUtils.readThrowing(StoneSettings.CODEC, root);
    }

    private static JsonObject readFirstBundledStoneFeature(final String relative) throws IOException {
        final JsonObject root = readBundledObject(relative);
        return HjsonUtils.getRegularObjects(root, StoneSettings.Fields.gen).get(0);
    }

    private static JsonObject readBundledObject(final String relative) throws IOException {
        final Path preset = resolveStonePresetRoot().resolve(relative);
        final String contents = Files.readString(preset);
        final JsonValue json = HjsonUtils.readValue(contents).unwrap();
        return json.asObject();
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
