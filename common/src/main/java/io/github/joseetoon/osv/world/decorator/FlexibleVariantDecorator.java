package io.github.joseetoon.osv.world.decorator;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import io.github.joseetoon.genlib.data.Range;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.github.joseetoon.genlib.serialization.CodecUtils.codecOf;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.defaulted;

/**
 * Replaces the legacy FeatureDecorator<FlexibleDecoratorConfig> from 1.16.5.
 * In 1.20.1, decorators are implemented as PlacementModifiers.
 */
public class FlexibleVariantDecorator extends PlacementModifier {

    public static final Codec<FlexibleVariantDecorator> CODEC = codecOf(
        defaulted(Range.CODEC, "count", Range.of(8, 8), d -> d.count),
        defaulted(Range.CODEC, "height", Range.of(0, 32), d -> d.height),
        defaulted(Codec.DOUBLE, "chance", 1.0, d -> d.chance),
        FlexibleVariantDecorator::new
    );

    public static final PlacementModifierType<FlexibleVariantDecorator> TYPE =
        () -> com.mojang.serialization.MapCodec.assumeMapUnsafe(CODEC);

    private final Range count;
    private final Range height;
    private final double chance;

    public FlexibleVariantDecorator(final Range count, final Range height, final double chance) {
        this.count = count;
        this.height = height;
        this.chance = chance;
    }

    @Override
    public Stream<BlockPos> getPositions(final PlacementContext ctx, final RandomSource rand, final BlockPos origin) {
        return IntStream.range(0, randRange(this.count, rand))
            .filter(i -> this.chance == 1 || rand.nextFloat() <= this.chance)
            .mapToObj(i -> genPos(rand, origin));
    }

    private BlockPos genPos(final RandomSource rand, final BlockPos origin) {
        return new BlockPos(
            rand.nextInt(16) + origin.getX(),
            randRange(this.height, rand),
            rand.nextInt(16) + origin.getZ()
        );
    }

    private static int randRange(final Range range, final RandomSource rand) {
        final int d = range.diff();
        return range.min + (d > 0 ? rand.nextInt(d + 1) : 0);
    }

    @Override
    public PlacementModifierType<?> type() {
        return TYPE;
    }
}
