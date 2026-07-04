package io.github.joseetoon.osv.world.providers;

import com.mojang.serialization.Codec;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;
import io.github.joseetoon.genlib.data.Range;
import io.github.joseetoon.genlib.serialization.CodecUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.util.RandomSource;

@ParametersAreNonnullByDefault
public class SimpleCount extends IntProvider {

    public static final Codec<SimpleCount> CODEC =
        Range.CODEC.xmap(r -> new SimpleCount(r.min, r.max), p -> new Range(p.min, p.max));

    public static final IntProviderType<?> TYPE =
        () -> com.mojang.serialization.MapCodec.assumeMapUnsafe(CodecUtils.asParent(CODEC));

    public final int min;
    public final int max;

    public SimpleCount(final int min, final int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public int sample(final RandomSource rand) {
        if (this.max <= this.min) {
            return this.min;
        }
        return this.min + rand.nextInt(this.max - this.min + 1);
    }

    @Override
    public int getMinValue() {
        return this.min;
    }

    @Override
    public int getMaxValue() {
        return this.max;
    }

    @Override
    public IntProviderType<?> getType() {
        return TYPE;
    }
}
