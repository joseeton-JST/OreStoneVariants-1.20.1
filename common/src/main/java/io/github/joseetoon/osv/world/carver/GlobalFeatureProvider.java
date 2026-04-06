package io.github.joseetoon.osv.world.carver;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import io.github.joseetoon.osv.preset.OrePreset;
import io.github.joseetoon.osv.preset.StonePreset;
import io.github.joseetoon.osv.preset.data.PlacedFeatureSettings;
import io.github.joseetoon.osv.world.feature.FeatureProvider;

public interface GlobalFeatureProvider<T> extends FeatureProvider<T> {

    @Override
    @Deprecated
    default ConfiguredFeature<?, ?> createOreFeature(final OrePreset ore, final PlacedFeatureSettings<?, ?> cfg) {
        return new ConfiguredFeature<>(Feature.NO_OP, FeatureConfiguration.NONE);
    }

    @Override
    @Deprecated
    default ConfiguredFeature<?, ?> createStoneFeature(final StonePreset stone, final PlacedFeatureSettings<?, ?> cfg) {
        return new ConfiguredFeature<>(Feature.NO_OP, FeatureConfiguration.NONE);
    }

    GlobalFeature<?> getFeatureType();
}
