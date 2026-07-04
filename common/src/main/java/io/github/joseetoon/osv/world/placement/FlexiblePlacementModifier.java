package io.github.joseetoon.osv.world.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import io.github.joseetoon.osv.preset.data.FlexiblePlacementSettings;
import io.github.joseetoon.osv.preset.reader.HeightProviderReader;
import io.github.joseetoon.osv.preset.reader.IntProviderReader;
import io.github.joseetoon.osv.world.providers.SimpleCount;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.util.RandomSource;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.github.joseetoon.genlib.serialization.CodecUtils.codecOf;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.defaulted;

@ParametersAreNonnullByDefault
public class FlexiblePlacementModifier extends PlacementModifier {

    public static final Codec<FlexiblePlacementModifier> CODEC = codecOf(
        defaulted(IntProviderReader.CODEC, "count", new SimpleCount(8, 8), c -> c.count),
        defaulted(HeightProviderReader.CODEC, "height", FlexiblePlacementSettings.DEFAULT_HEIGHT, c -> c.height),
        defaulted(Codec.INT, "bias", 0, c -> c.bias),
        defaulted(Codec.DOUBLE, "chance", 1.0, c -> c.chance),
        FlexiblePlacementModifier::new
    );

    public static final PlacementModifierType<FlexiblePlacementModifier> TYPE =
        () -> com.mojang.serialization.MapCodec.assumeMapUnsafe(CODEC);

    public final IntProvider count;
    public final HeightProvider height;
    public final int bias;
    public final double chance;

    public FlexiblePlacementModifier(final IntProvider count, final HeightProvider height, final int bias, final double chance) {
        this.count = count;
        this.height = height;
        this.bias = bias;
        this.chance = chance;
    }

    @Override
    public Stream<BlockPos> getPositions(final PlacementContext ctx, final RandomSource rand, final BlockPos origin) {
        return IntStream.range(0, this.count.sample(rand))
            .filter(i -> this.chance == 1 || rand.nextFloat() <= this.chance)
            .mapToObj(i -> this.genPos(rand, ctx, origin));
    }

    private BlockPos genPos(final RandomSource rand, final PlacementContext ctx, final BlockPos origin) {
        return new BlockPos(
            rand.nextInt(16) + origin.getX(),
            this.genHeight(rand, ctx),
            rand.nextInt(16) + origin.getZ()
        );
    }

    private int genHeight(final RandomSource rand, final PlacementContext ctx) {
        final int offset = -ctx.getMinGenY();
        int y = this.height.sample(rand, ctx) + offset;
        for (int i = 0; y > 0 && i < this.bias; i++) {
            y = rand.nextInt(y + 1);
        }
        return y - offset;
    }

    @Override
    public PlacementModifierType<?> type() {
        return TYPE;
    }
}
