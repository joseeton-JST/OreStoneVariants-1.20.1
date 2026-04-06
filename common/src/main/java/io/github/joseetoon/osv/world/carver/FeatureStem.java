package io.github.joseetoon.osv.world.carver;

import lombok.Value;
import io.github.joseetoon.osv.preset.data.PlacedFeatureSettings;
import io.github.joseetoon.osv.world.placer.BlockPlacer;

@Value
public class FeatureStem {
    PlacedFeatureSettings<?, ?> config;
    BlockPlacer placer;
}
