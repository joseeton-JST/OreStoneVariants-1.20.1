package io.github.joseetoon.osv.preset.reader;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public final class HeightProviderReader {

    public static final Codec<HeightProvider> CODEC = HeightProvider.CODEC;

    private HeightProviderReader() {}
}
