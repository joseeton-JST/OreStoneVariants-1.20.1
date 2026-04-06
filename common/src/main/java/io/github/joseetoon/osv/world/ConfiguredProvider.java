package io.github.joseetoon.osv.world;

import lombok.Value;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import io.github.joseetoon.osv.preset.data.PlacedFeatureSettings;
import io.github.joseetoon.osv.world.feature.FeatureProvider;
import io.github.joseetoon.osv.world.placement.PlacementProvider;

@Value
public class ConfiguredProvider {
    PlacedFeatureSettings.Type type;
    FeatureProvider<?> feature;
    PlacementProvider<?> decorator;
    PlacedFeature source;
}
