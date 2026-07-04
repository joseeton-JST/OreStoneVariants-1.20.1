package io.github.joseetoon.osv.world.providers;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import io.github.joseetoon.genlib.data.Range;
import io.github.joseetoon.genlib.serialization.CodecUtils;
import io.github.joseetoon.osv.preset.reader.CommonHeightAccessor;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.util.RandomSource;

@ParametersAreNonnullByDefault
public class SimpleHeight extends HeightProvider implements CommonHeightAccessor {

    public static final Codec<SimpleHeight> CODEC =
        Range.CODEC.xmap(r -> new SimpleHeight(r.min, r.max), p -> new Range(p.min, p.max));

    public static final HeightProviderType<?> TYPE =
        () -> com.mojang.serialization.MapCodec.assumeMapUnsafe(CodecUtils.asParent(CODEC));

    public final int min;
    public final int max;

    public SimpleHeight(final int min, final int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public int sample(final RandomSource rand, final WorldGenerationContext ctx) {
        if (this.max <= this.min) {
            return this.min;
        }
        return this.min + rand.nextInt(this.max - this.min + 1);
    }

    @Override
    public HeightProviderType<?> getType() {
        return TYPE;
    }

    @Override
    public VerticalAnchor getMinInclusive() {
        return VerticalAnchor.absolute(this.min);
    }

    @Override
    public VerticalAnchor getMaxInclusive() {
        return VerticalAnchor.absolute(this.max);
    }
}
