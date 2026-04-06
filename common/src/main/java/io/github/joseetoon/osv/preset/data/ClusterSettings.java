package io.github.joseetoon.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import io.github.joseetoon.osv.preset.OrePreset;
import io.github.joseetoon.osv.preset.StonePreset;
import io.github.joseetoon.osv.world.feature.ClusterConfig;
import io.github.joseetoon.osv.world.feature.ClusterFeature;
import io.github.joseetoon.osv.world.feature.FeatureProvider;
import io.github.joseetoon.osv.world.placer.StoneBlockPlacer;
import io.github.joseetoon.osv.world.placer.VariantBlockPlacer;

import static io.github.joseetoon.genlib.serialization.CodecUtils.codecOf;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.defaulted;

@Value
@Builder
@FieldNameConstants
public class ClusterSettings implements FeatureProvider<ClusterSettings> {

    @Default int size = 8;

    public static final Codec<ClusterSettings> CODEC = codecOf(
        defaulted(Codec.INT, Fields.size, 8, ClusterSettings::getSize),
        ClusterSettings::new
    );

    @Override
    public ConfiguredFeature<?, ?> createOreFeature(final OrePreset ore, final PlacedFeatureSettings<?, ?> cfg) {
        return new ConfiguredFeature<>(ClusterFeature.INSTANCE, new ClusterConfig(this.size, new VariantBlockPlacer(cfg, ore)));
    }

    @Override
    public ConfiguredFeature<?, ?> createStoneFeature(final StonePreset stone, final PlacedFeatureSettings<?, ?> cfg) {
        return new ConfiguredFeature<>(ClusterFeature.INSTANCE, new ClusterConfig(this.size, new StoneBlockPlacer(stone)));
    }

    @Override
    public Codec<ClusterSettings> codec() {
        return CODEC;
    }
}
