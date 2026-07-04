package io.github.joseetoon.osv.preset.reader;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import io.github.joseetoon.genlib.data.Range;
import io.github.joseetoon.genlib.serialization.SimpleEitherCodec;
import io.github.joseetoon.osv.preset.data.FlexibleHeightSettings;
import io.github.joseetoon.osv.world.providers.SimpleHeight;

public final class HeightProviderReader {

    private static final Codec<HeightProvider> LEGACY_ABSOLUTE_RANGE_CODEC =
        Range.CODEC.xmap(
            range -> new SimpleHeight(range.min, range.max),
            HeightProviderReader::toAbsoluteRange
        );

    private static final Codec<HeightProvider> LEGACY_CODEC =
        new SimpleEitherCodec<>(LEGACY_ABSOLUTE_RANGE_CODEC, FlexibleHeightSettings.HEIGHT_PROVIDER_CODEC)
            .withEncoder(provider -> isAbsoluteRange(provider)
                ? LEGACY_ABSOLUTE_RANGE_CODEC
                : FlexibleHeightSettings.HEIGHT_PROVIDER_CODEC);

    public static final Codec<HeightProvider> CODEC =
        new SimpleEitherCodec<>(LEGACY_CODEC, HeightProvider.CODEC)
            .withEncoder(provider -> FlexibleHeightSettings.isSupportedProvider(provider)
                ? LEGACY_CODEC
                : HeightProvider.CODEC);

    private HeightProviderReader() {}

    private static boolean isAbsoluteRange(final HeightProvider provider) {
        if (provider instanceof CommonHeightAccessor accessor) {
            return accessor.getMinInclusive() instanceof VerticalAnchor.Absolute
                && accessor.getMaxInclusive() instanceof VerticalAnchor.Absolute;
        }
        return false;
    }

    private static Range toAbsoluteRange(final HeightProvider provider) {
        if (provider instanceof CommonHeightAccessor accessor
            && accessor.getMinInclusive() instanceof VerticalAnchor.Absolute min
            && accessor.getMaxInclusive() instanceof VerticalAnchor.Absolute max) {
            return new Range(min.y(), max.y());
        }
        throw new UnsupportedOperationException("height provider is not an absolute range");
    }
}
