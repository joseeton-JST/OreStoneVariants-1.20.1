package io.github.joseetoon.osv.world.carver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import io.github.joseetoon.genlib.util.HashGenerator;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.BitSet;
import java.util.Collection;
import net.minecraft.util.RandomSource;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class GiantSphereCarver extends GlobalFeature<GiantSphereCollection> {

    public static final GiantSphereCarver INSTANCE = new GiantSphereCarver();

    private GiantSphereCarver() {
        super(GiantSphereCollection.CODEC);
    }

    @Override
    public ConfiguredWorldCarver<?> configured(final Collection<FeatureStem> configs) {
        final Collection<GiantSphereConfig> mapped = configs.stream()
            .map(GiantSphereConfig::fromStem)
            .collect(Collectors.toList());
        return this.configured(new GiantSphereCollection(mapped));
    }

    @Override
    public boolean carve(CarvingContext ctx, GiantSphereCollection configs, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> biomes, RandomSource rand, Aquifer aquifer,
                         ChunkPos pos, CarvingMask mask) {
        final ChunkPos currentChunk = chunk.getPos();
        final int cX = currentChunk.x;
        final int cZ = currentChunk.z;
        final int dX = pos.x;
        final int dZ = pos.z;

        final Biome b = biomes.apply(new BlockPos((cX << 4) + 8, 64, (cZ << 4) + 8)).value();
        final BitSet flags = new BitSet();
        // rand is already seeded by Minecraft with worldSeed XOR chunkCoords;
        // read once before the loop so each world produces distinct sphere positions.
        final long carverSeed = rand.nextLong();
        boolean placed = false;

        for (final GiantSphereConfig cfg : configs.configs) {
            if (!cfg.biomes.test(b)) {
                continue;
            }
            for (int i = 0; i < cfg.count; i++) {
                final double hash = HashGenerator.getHash(carverSeed + i, dX, 1024 + cfg.placer.getId(), dZ);

                if (hash > cfg.threshold) {
                    rand.setSeed(Double.doubleToLongBits(hash));
                    placed |= gen(chunk, rand, dX, dZ, cX, cZ, mask, flags, cfg);
                }
            }
        }
        return placed;
    }

    private static boolean gen(
            ChunkAccess chunk, RandomSource rand, int dX, int dZ, int cX, int cZ, CarvingMask mask, BitSet flags, GiantSphereConfig cfg) {

        int count = 0;
        final Random javaRand = new Random(rand.nextLong());

        final int radX = cfg.radiusX.rand(javaRand) - (cfg.radiusX.diff() / 2);
        if ((radX >> 4) < Mth.abs(dX - cX) - 1) return false;
        final int radZ = cfg.radiusZ.rand(javaRand) - (cfg.radiusZ.diff() / 2);
        if ((radZ >> 4) < Mth.abs(dZ - cZ) - 1) return false;
        final int radY = cfg.radiusY.rand(javaRand) - (cfg.radiusY.diff() / 2);

        final int rX2 = radX * radX;
        final int rZ2 = radZ * radZ;
        final int rY2 = radY * radY;
        final int aX = (dX << 4) + 8;
        final int aY = cfg.height.rand(javaRand);
        final int aZ = (dZ << 4) + 8;

        final int minBuildHeight = chunk.getMinBuildHeight();
        final int minY = Math.max(minBuildHeight, aY - radY);
        final int maxY = Math.min(chunk.getMaxBuildHeight() - 1, aY + radY);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    // normalizedY maps [-64,319] -> [0,383], fitting in 9 bits.
                    // x/z each need 4 bits; shift accordingly to avoid collisions.
                    final int normalizedY = y - minBuildHeight;
                    final int flag = (x << 13) | (z << 9) | normalizedY;
                    if (mask.get(x, y, z) || flags.get(flag)) continue;

                    final double distX = ((cX << 4) + x) - aX;
                    final double distY = y - aY;
                    final double distZ = ((cZ << 4) + z) - aZ;
                    final double distX2 = distX * distX;
                    final double distY2 = distY * distY;
                    final double distZ2 = distZ * distZ;

                    final double sum = distX2 / rX2 + distY2 / rY2 + distZ2 / rZ2;
                    if (sum <= 1.0) {
                        if (cfg.placer.placeUnchecked(chunk, rand, x, y, z)) {
                            flags.set(flag);
                            count++;
                        }
                    }
                }
            }
        }
        return count > 0;
    }
}
