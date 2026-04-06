package io.github.joseetoon.osv.world.placer;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;

import net.minecraft.util.RandomSource;

import static io.github.joseetoon.genlib.serialization.CodecUtils.asParent;
import static io.github.joseetoon.genlib.serialization.CodecUtils.simpleEither;

public interface BlockPlacer {

    Codec<BlockPlacer> EITHER_CODEC =
        simpleEither(asParent(StoneBlockPlacer.CODEC), asParent(VariantBlockPlacer.CODEC))
            .withEncoder(p -> p instanceof StoneBlockPlacer ? asParent(StoneBlockPlacer.CODEC)
                : asParent(VariantBlockPlacer.CODEC));

    boolean place(final WorldGenLevel level, final RandomSource rand, final BlockPos pos);
    boolean placeUnchecked(final ChunkAccess chunk, final RandomSource rand, final int x, final int y, final int z);
    int getId();
}
