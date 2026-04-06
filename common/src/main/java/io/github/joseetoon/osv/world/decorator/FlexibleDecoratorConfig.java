package io.github.joseetoon.osv.world.decorator;

import com.mojang.serialization.Codec;
import io.github.joseetoon.genlib.data.Range;

import static io.github.joseetoon.genlib.serialization.CodecUtils.codecOf;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.defaulted;

/**
 * [1.20.1 Migration] FlexibleDecoratorConfig was previously implementing
 * DecoratorConfiguration which was removed in 1.18.
 * This is now a plain POJO used by FlexibleVariantDecorator (PlacementModifier).
 */
public class FlexibleDecoratorConfig {

    public static final Codec<FlexibleDecoratorConfig> CODEC = codecOf(
        defaulted(Range.CODEC, "count", Range.of(8, 8), c -> c.count),
        defaulted(Range.CODEC, "height", Range.of(0, 32), c -> c.height),
        defaulted(Codec.DOUBLE, "chance", 1.0, c -> c.chance),
        FlexibleDecoratorConfig::new
    );

    public final Range count;
    public final Range height;
    public final double chance;

    public FlexibleDecoratorConfig(final Range count, final Range height, final double chance) {
        this.count = count;
        this.height = height;
        this.chance = chance;
    }
}
