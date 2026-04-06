package io.github.joseetoon.osv.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import io.github.joseetoon.osv.preset.data.ClusterSettings;
import io.github.joseetoon.osv.world.placer.BlockPlacer;

import static io.github.joseetoon.genlib.serialization.CodecUtils.codecOf;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.defaulted;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.field;

public class ClusterConfig implements FeatureConfiguration {

    public static final Codec<ClusterConfig> CODEC = codecOf(
        defaulted(Codec.INT, ClusterSettings.Fields.size, 8, c -> c.size),
        field(BlockPlacer.EITHER_CODEC, "placer", c -> c.placer),
        ClusterConfig::new
    );

    final int size;
    final BlockPlacer placer;

    public ClusterConfig(final int size, final BlockPlacer placer) {
        this.size = size;
        this.placer = placer;
    }
}
