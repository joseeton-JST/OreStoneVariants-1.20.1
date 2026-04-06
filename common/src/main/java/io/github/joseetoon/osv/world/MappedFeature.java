package io.github.joseetoon.osv.world;

import lombok.Value;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import io.github.joseetoon.genlib.data.BiomePredicate;

@Value
public class MappedFeature {
    BiomePredicate biomes;
    PlacedFeature feature;

    public static MappedFeature global(final PlacedFeature feature) {
        return new MappedFeature(BiomePredicate.ALL_BIOMES, feature);
    }
}
