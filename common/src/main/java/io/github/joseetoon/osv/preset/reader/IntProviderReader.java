package io.github.joseetoon.osv.preset.reader;

import com.mojang.serialization.Codec;
import net.minecraft.util.valueproviders.IntProvider;

public final class IntProviderReader {

    public static final Codec<IntProvider> CODEC = IntProvider.CODEC;

    private IntProviderReader() {}
}
