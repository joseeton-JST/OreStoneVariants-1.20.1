package io.github.joseetoon.osv.preset.writer;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DynamicOps;
import lombok.extern.log4j.Log4j2;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import org.apache.commons.lang3.mutable.MutableInt;
import io.github.joseetoon.genlib.data.BiomePredicate;
import io.github.joseetoon.genlib.io.FileIO;
import io.github.joseetoon.genlib.serialization.HjsonOps;
import io.github.joseetoon.genlib.util.JsonTransformer;
import io.github.joseetoon.genlib.util.JsonTransformer.ObjectResolver;
import io.github.joseetoon.genlib.util.HjsonUtils;
import io.github.joseetoon.genlib.util.McUtils;
import io.github.joseetoon.osv.ModRegistries;
import io.github.joseetoon.osv.client.texture.BackgroundSelector;
import io.github.joseetoon.osv.preset.OrePreset;
import io.github.joseetoon.osv.preset.data.BlockSettings;
import io.github.joseetoon.osv.preset.data.ClusterSettings;
import io.github.joseetoon.osv.preset.data.DropSettings;
import io.github.joseetoon.osv.preset.data.FlexibleHeightSettings;
import io.github.joseetoon.osv.preset.data.FlexiblePlacementSettings;
import io.github.joseetoon.osv.preset.data.GenerationSettings;
import io.github.joseetoon.osv.preset.data.ItemSettings;
import io.github.joseetoon.osv.preset.data.ModelSettings;
import io.github.joseetoon.osv.preset.data.OreSettings;
import io.github.joseetoon.osv.preset.data.PlacedFeatureSettings;
import io.github.joseetoon.osv.preset.data.RecipeSettings;
import io.github.joseetoon.osv.preset.data.StateSettings;
import io.github.joseetoon.osv.preset.data.TextureSettings;
import io.github.joseetoon.osv.preset.data.VariantSettings;
import io.github.joseetoon.osv.util.JsonCompat;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Log4j2
public class PresetWriter {

    private static final ObjectResolver PRESET_FIELD_SORTER =
        JsonTransformer.root()
            .reorder(
                List.of(OreSettings.Fields.variant, OreSettings.Fields.recipe, OreSettings.Fields.loot),
                List.of(OreSettings.Fields.gen))
            .freeze();

    public static void savePresets(final RegistryAccess access) {
        final MutableInt updated = new MutableInt(0);

        for (final OrePreset preset : ModRegistries.ORE_PRESETS) {
            if (preset.isUpdated()) {
                FileIO.mkdirsOrThrow(preset.getFile().getParentFile());
                HjsonUtils.writeJson(updateContents(preset, access), preset.getFile())
                    .ifErr(e -> log.warn("Could not save {}. Ignoring...", preset.getName()))
                    .ifErr(e -> log.debug("Updating preset", e))
                    .ifOk(t -> preset.onPresetSaved())
                    .ifOk(t -> updated.increment());
            }
        }
        if (updated.getValue() == 0) {
            log.info("Nothing to save. All presets are up to date.");
        } else if (updated.getValue() == 1) {
            log.info("1 preset was dynamically updated.");
        } else {
            log.info("{} presets were dynamically updated.", updated.getValue());
        }
    }

    private static JsonObject updateContents(final OrePreset preset, final RegistryAccess access) {
        final DynamicOps<JsonValue> ops = RegistryOps.create(HjsonOps.INSTANCE, access);
        final OreSettings settings = generateSettings(preset);
        final JsonValue cfg = OreSettings.CODEC.encodeStart(ops, settings).result().orElseGet(() -> {
            log.warn("Error encoding settings: {}", settings);
            return new JsonObject();
        });
        if (preset.isReloadTextures()) removeTextures(preset.getRaw());
        JsonCompat.setDefaults(preset.getRaw(), format(cfg.asObject()));
        return preset.getRaw();
    }

    private static OreSettings generateSettings(final OrePreset preset) {
        return new OreSettings(
            createVariant(preset),
            BlockSettings.EMPTY,
            StateSettings.EMPTY,
            ItemSettings.EMPTY,
            createDrops(preset),
            createGen(preset),
            createRecipe(preset),
            createTexture(preset),
            ModelSettings.EMPTY,
            Collections.emptyList()
        );
    }

    private static VariantSettings createVariant(final OrePreset preset) {
        return VariantSettings.withOriginal(preset.getOreId());
    }

    private static DropSettings createDrops(final OrePreset preset) {
        if (!preset.hasLootId()) return DropSettings.EMPTY;
        return new DropSettings(Either.left(preset.getLootReference()));
    }

    private static GenerationSettings createGen(final OrePreset preset) {
        return new GenerationSettings(false, preset.getFeatures());
    }

    private static RecipeSettings createRecipe(final OrePreset preset) {
        return RecipeSettings.fromChecked(preset.getCheckedRecipe());
    }

    private static TextureSettings createTexture(final OrePreset preset) {
        if (McUtils.isDedicatedServer()) return TextureSettings.EMPTY;

        final TextureSettings cfg = preset.getTexture();
        return new TextureSettings(cfg.isShade(), cfg.getThreshold(), preset.getBackgroundTexture(),
            preset.getBackgroundIds(), preset.getOverlayIds(), null);
    }

    private static void removeTextures(final JsonObject raw) {
        final JsonValue texture = raw.get(OreSettings.Fields.texture);
        if (texture != null) {
            final JsonObject textureObject = texture.asObject();
            textureObject.remove(TextureSettings.Fields.original);
            textureObject.remove(TextureSettings.Fields.overlay);
        }
    }

    private static JsonObject format(final JsonObject generated) {
        final JsonObject variant = JsonCompat.getAsserted(generated, OreSettings.Fields.variant).asObject();
        variant.remove(VariantSettings.Fields.bgDuplication);
        variant.remove(VariantSettings.Fields.bgImitation);
        variant.remove(VariantSettings.Fields.canBeDense);
        variant.remove(VariantSettings.Fields.copyTags);

        final JsonObject texture = JsonCompat.getAsserted(generated, OreSettings.Fields.texture).asObject();
        texture.remove(TextureSettings.Fields.shade);
        final JsonValue background = texture.get(TextureSettings.Fields.background);
        if (background != null && BackgroundSelector.STONE_ID.toString().equals(background.asString())) {
            texture.remove(TextureSettings.Fields.background);
        }
        JsonCompat.getOptional(generated, OreSettings.Fields.recipe, JsonValue::asObject).ifPresent(recipe -> {
            removeIf(recipe, RecipeSettings.Fields.count, v -> JsonCompat.matches(v, JsonValue.valueOf(1)));
            removeIf(recipe, RecipeSettings.Fields.time, v -> JsonCompat.matches(v, JsonValue.valueOf(200)));
        });
        removeIf(generated, OreSettings.Fields.loot, JsonValue::isNull);
        HjsonUtils.getRegularObjects(generated, OreSettings.Fields.gen).forEach(gen -> {
            gen.setLineLength(1);
            gen.remove(PlacedFeatureSettings.Fields.nested);
            gen.remove(PlacedFeatureSettings.Fields.denseRatio);
            removeIf(gen, PlacedFeatureSettings.Fields.dimensions, v -> v.isArray() && v.asArray().isEmpty());
            removeIf(gen, PlacedFeatureSettings.Fields.type,
                v -> v.isString() && v.asString().equalsIgnoreCase(PlacedFeatureSettings.Type.CLUSTER.name()));
            removeIf(gen, FlexiblePlacementSettings.Fields.bias, v -> JsonCompat.matches(v, JsonValue.valueOf(0)));
            removeIf(gen, FlexiblePlacementSettings.Fields.plateau, v -> v.isNumber() && v.asInt() == Integer.MIN_VALUE);
            removeIf(gen, FlexiblePlacementSettings.Fields.chance, v -> JsonCompat.matches(v, JsonValue.valueOf(1.0)));
            removeIf(gen, FlexiblePlacementSettings.Fields.spread, v -> JsonCompat.matches(v, JsonValue.valueOf(0)));
            removeIf(gen, FlexiblePlacementSettings.Fields.modifiers, v -> v.isArray() && v.asArray().isEmpty());
            removeIf(gen, FlexiblePlacementSettings.Fields.count, v -> JsonCompat.matches(v, JsonValue.valueOf(2)));
            JsonCompat.getOptional(gen, FlexiblePlacementSettings.Fields.count, JsonValue::asArray)
                .ifPresent(array -> array.setCondensed(true));
            removeIf(gen, FlexiblePlacementSettings.Fields.height, v ->
                JsonCompat.matches(v, new JsonObject()
                    .add(FlexibleHeightSettings.BOTTOM, new JsonArray().add(0).add(128))));
            JsonCompat.getOptional(gen, FlexiblePlacementSettings.Fields.height).ifPresent(height -> {
                if (height.isArray()) {
                    for (final JsonValue value : height.asArray()) {
                        if (value.isArray()) {
                            value.asArray().setCondensed(true);
                        } else if (value.isObject()) {
                            value.asObject().setLineLength(1);
                        }
                    }
                    height.asArray().setCondensed(true);
                } else if (height.isObject()) {
                    for (final JsonObject.Member member : height.asObject()) {
                        if (member.getValue().isArray()) {
                            member.getValue().asArray().setCondensed(true);
                        } else if (member.getValue().isObject()) {
                            member.getValue().asObject().setLineLength(1);
                        }
                    }
                }
            });
            removeIf(gen, ClusterSettings.Fields.size, v -> JsonCompat.matches(v, JsonValue.valueOf(8)));
            JsonCompat.getOptional(gen, PlacedFeatureSettings.Fields.biomes, JsonValue::asObject).ifPresent(biomes -> {
                removeIf(biomes, BiomePredicate.Fields.mods, v -> v.isArray() && v.asArray().isEmpty());
                removeIf(biomes, BiomePredicate.Fields.names, v -> v.isArray() && v.asArray().isEmpty());
                removeIf(biomes, BiomePredicate.Fields.blacklist, JsonValue::isFalse);
            });
        });
        generated.remove(OreSettings.Fields.block)
            .remove(OreSettings.Fields.item)
            .remove(McUtils.getPlatform())
            .remove(OreSettings.Fields.state)
            .remove(OreSettings.Fields.model)
            .remove(OreSettings.Fields.nested);
        PRESET_FIELD_SORTER.updateAll(generated);
        return generated;
    }

    private static void removeIf(final JsonObject o, final String key, final Predicate<JsonValue> predicate) {
        if (JsonCompat.getOptional(o, key).filter(predicate).isPresent()) {
            o.remove(key);
        }
    }
}
