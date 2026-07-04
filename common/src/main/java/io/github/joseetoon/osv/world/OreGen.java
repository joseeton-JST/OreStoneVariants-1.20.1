package io.github.joseetoon.osv.world;

import lombok.extern.log4j.Log4j2;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import io.github.joseetoon.genlib.data.DimensionPredicate;
import io.github.joseetoon.genlib.data.SafeRegistry;
import io.github.joseetoon.genlib.data.MultiValueHashMap;
import io.github.joseetoon.genlib.data.MultiValueMap;
import io.github.joseetoon.genlib.event.world.FeatureModificationContext;
import io.github.joseetoon.genlib.event.registry.CommonRegistries;
import io.github.joseetoon.genlib.util.LibStringUtils;
import io.github.joseetoon.osv.ModRegistries;
import io.github.joseetoon.osv.block.OreVariant;
import io.github.joseetoon.osv.config.Cfg;
import io.github.joseetoon.osv.preset.OrePreset;
import io.github.joseetoon.osv.preset.StonePreset;
import io.github.joseetoon.osv.preset.data.PlacedFeatureSettings;
import io.github.joseetoon.osv.preset.resolver.FeatureSettingsResolver;
import io.github.joseetoon.osv.util.Reference;
import io.github.joseetoon.osv.world.carver.DimensionLocalCarver;
import io.github.joseetoon.osv.world.carver.DimensionLocalCarverConfig;
import io.github.joseetoon.osv.world.carver.FeatureStem;
import io.github.joseetoon.osv.world.carver.GlobalFeature;
import io.github.joseetoon.osv.world.carver.GlobalFeatureProvider;
import io.github.joseetoon.osv.world.feature.PassiveOreSwapFeature;
import io.github.joseetoon.osv.world.placer.StoneBlockPlacer;
import io.github.joseetoon.osv.world.placer.VariantBlockPlacer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
public class OreGen {

    // Placed once per biome: scans every chunk and replaces vanilla ores with OSV variants.
    private static final PlacedFeature PASSIVE_SWAP = new PlacedFeature(
        Holder.direct(new ConfiguredFeature<>(PassiveOreSwapFeature.INSTANCE, new NoneFeatureConfiguration())),
        List.of());
    private static final AtomicLong DEBUG_BIOMES = new AtomicLong();

    private static final SafeRegistry<ResourceLocation, PlacedFeature> DISABLED_FEATURES =
        SafeRegistry.of(OreGen::loadDisabledFeatures)
            .canBeReset(true);

    private static final SafeRegistry<ResourceLocation, Block> DISABLED_BLOCKS =
        SafeRegistry.of(OreGen::loadDisabledBlocks)
            .canBeReset(true);

    private static final SafeRegistry<ResourceLocation, MappedFeature> ENABLED_STONES =
        SafeRegistry.of(OreGen::loadStoneFeatures)
            .canBeReset(true);

    private static final SafeRegistry<ResourceLocation, MappedFeature> ENABLED_ORES =
        SafeRegistry.of(OreGen::loadOreFeatures)
            .canBeReset(true);

    private static final SafeRegistry<ResourceLocation, ConfiguredWorldCarver<?>> GLOBAL_STONES =
        SafeRegistry.of(OreGen::loadGlobalStones)
            .canBeReset(true);

    private static final SafeRegistry<ResourceLocation, ConfiguredWorldCarver<?>> GLOBAL_ORES =
        SafeRegistry.of(OreGen::loadGlobalOres)
            .canBeReset(true);

    public static void setupOreFeatures(final FeatureModificationContext ctx) {
        log.debug("Injecting changes to biome: {}", ctx.getName());

        DISABLED_FEATURES.forEach((id, feature) -> ctx.removeFeature(id));
        for (final String id : Cfg.disabledFeatures()) {
            ctx.removeFeature(ResourceLocation.parse(id));
        }
        ENABLED_STONES.forEach((id, feature) -> {
            if (feature.getBiomes().test(ctx.getBiome())) {
                ctx.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature.getFeature());
            }
        });
        ENABLED_ORES.forEach((id, feature) -> {
            if (feature.getBiomes().test(ctx.getBiome())) {
                ctx.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, feature.getFeature());
            }
        });
        if (Cfg.enableOSVOres()) {
            addPassiveSwapFeature(ctx);
        }
        GLOBAL_STONES.forEach((id, carver) -> ctx.addCarver(GenerationStep.Carving.LIQUID, carver));
        GLOBAL_ORES.forEach((id, carver) -> ctx.addCarver(GenerationStep.Carving.LIQUID, carver));
    }

    static void addPassiveSwapFeature(final FeatureModificationContext ctx) {
        // Passive swap: scans every chunk and replaces vanilla ores with OSV variants
        // based on the surrounding stone type. Runs once per biome entry.
        ctx.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, PASSIVE_SWAP);
        if (Cfg.debugStartup() && log.isDebugEnabled()) {
            final long sample = DEBUG_BIOMES.incrementAndGet();
            if (sample <= 8L || sample % 64L == 0L) {
                log.debug("OreGen added passive swap biome modifier entry for biome={} sample={}", ctx.getName(), sample);
            }
        }
    }

    public static void onWorldClosed() {
        SafeRegistry.resetAll(DISABLED_FEATURES, DISABLED_BLOCKS, ENABLED_ORES, ENABLED_STONES);
    }

    private static Map<ResourceLocation, PlacedFeature> loadDisabledFeatures() {
        final Map<ResourceLocation, PlacedFeature> features = new HashMap<>();
        addDynamicallyDisabledFeatures(features);
        return features;
    }

    private static void addDynamicallyDisabledFeatures(final Map<ResourceLocation, PlacedFeature> features) {
        // Not available through current registry facade.
        // Explicitly-listed disabledFeatures are removed directly in setupOreFeatures.
    }

    private static boolean isDisabled(final PlacedFeature feature) {
        for (final Block block : DISABLED_BLOCKS) {
            if (FeatureSettingsResolver.featureContainsBlock(feature, block.defaultBlockState())) {
                return true;
            }
        }
        return false;
    }

    private static Map<ResourceLocation, Block> loadDisabledBlocks() {
        final Map<ResourceLocation, Block> disabled = new HashMap<>();
        if (Cfg.autoDisableStone()) {
            addDisabledStones(disabled);
        }
        if (Cfg.autoDisableOres()) {
            addDisabledOres(disabled);
        }
        return disabled;
    }

    private static void addDisabledStones(final Map<ResourceLocation, Block> disabled) {
        for (final StonePreset preset : ModRegistries.STONE_PRESETS) {
            final Block stone = preset.getStone().getBlock();
            disabled.put(CommonRegistries.BLOCKS.getKey(stone), stone);
        }
    }

    private static void addDisabledOres(final Map<ResourceLocation, Block> disabled) {
        for (final OreVariant variant : ModRegistries.VARIANTS) {
            final Block ore = variant.getFg();
            disabled.put(CommonRegistries.BLOCKS.getKey(ore), ore);
        }
    }

    private static Map<ResourceLocation, MappedFeature> loadStoneFeatures() {
        final Map<ResourceLocation, MappedFeature> features = new HashMap<>();
        if (Cfg.enableOSVStone()) {
            addStoneFeatures(features);
        }
        return features;
    }

    private static void addStoneFeatures(final Map<ResourceLocation, MappedFeature> features) {
        for (final StonePreset preset : ModRegistries.STONE_PRESETS) {
            for (final PlacedFeatureSettings<?, ?> cfg : preset.getFeatures()) {
                if (!cfg.isGlobal()) {
                    final ResourceLocation id = randId("stone_");
                    final MappedFeature feature = cfg.createStoneFeature(preset);
                    features.put(id, feature);
                }
            }
        }
    }

    private static Map<ResourceLocation, MappedFeature> loadOreFeatures() {
        final Map<ResourceLocation, MappedFeature> features = new HashMap<>();
        if (Cfg.enableOSVOres()) {
            addOreFeatures(features);
        }
        return features;
    }

    private static void addOreFeatures(final Map<ResourceLocation, MappedFeature> features) {
        for (final OrePreset preset : ModRegistries.ORE_PRESETS) {
            if (!preset.getGen().isEnabled()) {
                continue;
            }
            for (final PlacedFeatureSettings<?, ?> cfg : preset.getFeatures()) {
                if (!cfg.isGlobal()) {
                    final ResourceLocation id = randId("ore_");
                    final MappedFeature feature = cfg.createOreFeature(preset);
                    features.put(id, feature);
                }
            }
        }
    }

    private static Map<ResourceLocation, ConfiguredWorldCarver<?>> loadGlobalStones() {
        final Map<ResourceLocation, ConfiguredWorldCarver<?>> features = new HashMap<>();
        if (Cfg.enableOSVStone()) {
            addGlobalStones(features);
        }
        return features;
    }

    private static void addGlobalStones(final Map<ResourceLocation, ConfiguredWorldCarver<?>> features) {
        final MultiValueMap<GlobalFeature<?>, FeatureStem> globalConfigs = new MultiValueHashMap<>();
        for (final StonePreset preset : ModRegistries.STONE_PRESETS) {
            for (final PlacedFeatureSettings<?, ?> cfg : preset.getFeatures()) {
                if (cfg.isGlobal()) {
                    final GlobalFeatureProvider<?> provider = (GlobalFeatureProvider<?>) cfg.getConfig();
                    globalConfigs.add(provider.getFeatureType(), new FeatureStem(cfg, new StoneBlockPlacer(preset)));
                }
            }
        }
        for (final ConfiguredWorldCarver<?> carver : sort(globalConfigs)) {
            features.put(randId("global_stone_"), carver);
        }
    }

    private static Map<ResourceLocation, ConfiguredWorldCarver<?>> loadGlobalOres() {
        final Map<ResourceLocation, ConfiguredWorldCarver<?>> features = new HashMap<>();
        if (Cfg.enableOSVOres()) {
            addGlobalOres(features);
        }
        return features;
    }

    private static void addGlobalOres(final Map<ResourceLocation, ConfiguredWorldCarver<?>> features) {
        final MultiValueMap<GlobalFeature<?>, FeatureStem> globalConfigs = new MultiValueHashMap<>();
        for (final OrePreset preset : ModRegistries.ORE_PRESETS) {
            if (!preset.getGen().isEnabled()) {
                continue;
            }
            for (final PlacedFeatureSettings<?, ?> cfg : preset.getFeatures()) {
                if (cfg.isGlobal()) {
                    final GlobalFeatureProvider<?> provider = (GlobalFeatureProvider<?>) cfg.getConfig();
                    globalConfigs.add(provider.getFeatureType(), new FeatureStem(cfg, new VariantBlockPlacer(cfg, preset)));
                }
            }
        }
        for (final ConfiguredWorldCarver<?> carver : sort(globalConfigs)) {
            features.put(randId("global_ore_"), carver);
        }
    }

    private static List<ConfiguredWorldCarver<?>> sort(final MultiValueMap<GlobalFeature<?>, FeatureStem> globals) {
        final List<ConfiguredWorldCarver<?>> carvers = new ArrayList<>();
        globals.forEach((feature, stems) -> {
            final MultiValueMap<DimensionPredicate, FeatureStem> sorted = new MultiValueHashMap<>();
            for (final FeatureStem stem : stems) {
                sorted.add(stem.getConfig().getDimensions(), stem);
            }
            sorted.forEach((dims, sortedStems) -> {
                final ConfiguredWorldCarver<?> configured = feature.configured(sortedStems);
                if (dims.equals(DimensionPredicate.ALL_DIMENSIONS)) {
                    carvers.add(configured);
                } else {
                    carvers.add(DimensionLocalCarver.INSTANCE.configured(
                        new DimensionLocalCarverConfig(dims, configured)));
                }
            });
        });
        return carvers;
    }

    private static ResourceLocation randId(final String prefix) {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, prefix + LibStringUtils.randId(8));
    }
}
