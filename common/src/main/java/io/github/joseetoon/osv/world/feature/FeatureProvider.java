package io.github.joseetoon.osv.world.feature;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import io.github.joseetoon.osv.preset.OrePreset;
import io.github.joseetoon.osv.preset.StonePreset;
import io.github.joseetoon.osv.preset.data.DynamicSerializable;
import io.github.joseetoon.osv.preset.data.PlacedFeatureSettings;

public interface FeatureProvider<T> extends DynamicSerializable<T> {
    ConfiguredFeature<?, ?> createOreFeature(final OrePreset ore, final PlacedFeatureSettings<?, ?> cfg);
    ConfiguredFeature<?, ?> createStoneFeature(final StonePreset stone, final PlacedFeatureSettings<?, ?> cfg);
}
