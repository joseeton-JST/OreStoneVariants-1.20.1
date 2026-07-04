package io.github.joseetoon.osv.world.feature;

import lombok.extern.log4j.Log4j2;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import io.github.joseetoon.osv.ModRegistries;
import io.github.joseetoon.osv.block.OreVariant;
import io.github.joseetoon.osv.block.SharedStateBlock;
import io.github.joseetoon.osv.config.Cfg;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Passive post-processing feature:
 * keep vanilla/modded ore placement, then swap ore blocks to OSV variants
 * based on surrounding stone.
 */
@Log4j2
public class PassiveOreSwapFeature extends Feature<NoneFeatureConfiguration> {

    public static final PassiveOreSwapFeature INSTANCE = new PassiveOreSwapFeature();
    private static final int NON_STONE_BIAS = 2;
    private static final int STONE_DOMINANCE_MARGIN = 2;
    private static final int DEBUG_SAMPLE_RATE = 64;
    private static final Direction[] NEIGHBOR_PRIORITY = {
        Direction.DOWN,
        Direction.NORTH,
        Direction.SOUTH,
        Direction.WEST,
        Direction.EAST,
        Direction.UP
    };

    private static volatile Map<Block, Map<Block, OreVariant>> lookup;
    private static final AtomicLong DEBUG_CHUNKS = new AtomicLong();

    private PassiveOreSwapFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(final FeaturePlaceContext<NoneFeatureConfiguration> context) {
        final Map<Block, Map<Block, OreVariant>> map = getLookup();
        if (map.isEmpty()) {
            return false;
        }
        final WorldGenLevel level = context.level();
        final BlockPos origin = context.origin();
        final ChunkAccess chunk = level.getChunk(origin.getX() >> 4, origin.getZ() >> 4);
        final boolean debug = Cfg.debugStartup() && log.isDebugEnabled();
        final long startNs = debug ? System.nanoTime() : 0L;
        final SwapStats stats = new SwapStats();
        final boolean replaced = this.swapChunk(level, chunk, map, stats);
        if (debug) {
            final long sample = DEBUG_CHUNKS.incrementAndGet();
            if (sample % DEBUG_SAMPLE_RATE == 1) {
                final long elapsedMicros = (System.nanoTime() - startNs) / 1_000L;
                final ChunkPos pos = chunk.getPos();
                log.debug(
                    "PassiveOreSwapFeature sample={} chunk=[{},{}] time={}us scanned={} candidate={} replaced={} changed={}",
                    sample, pos.x, pos.z, elapsedMicros, stats.scanned, stats.candidates, stats.replaced, replaced
                );
            }
        }
        return replaced;
    }

    public static void clearCache() {
        lookup = null;
    }

    private static Map<Block, Map<Block, OreVariant>> getLookup() {
        Map<Block, Map<Block, OreVariant>> cache = lookup;
        if (cache != null) {
            return cache;
        }
        synchronized (PassiveOreSwapFeature.class) {
            cache = lookup;
            if (cache == null) {
                lookup = cache = buildLookup();
            }
        }
        return cache;
    }

    private static Map<Block, Map<Block, OreVariant>> buildLookup() {
        final Map<Block, Map<Block, OreVariant>> byForeground = new HashMap<>();
        for (final OreVariant variant : ModRegistries.VARIANTS) {
            byForeground
                .computeIfAbsent(variant.getFg(), k -> new HashMap<>())
                .put(variant.getBg(), variant);

            final Block deepslateAlias = getDeepslateAlias(variant.getFg());
            if (deepslateAlias != null) {
                byForeground
                    .computeIfAbsent(deepslateAlias, k -> new HashMap<>())
                    .putIfAbsent(variant.getBg(), variant);
            }
        }

        byForeground.replaceAll((fg, map) -> Collections.unmodifiableMap(map));
        return Collections.unmodifiableMap(byForeground);
    }

    private static Block getDeepslateAlias(final Block foreground) {
        final ResourceLocation id = BuiltInRegistries.BLOCK.getKey(foreground);
        if (id == null || id.getPath().startsWith("deepslate_")) {
            return null;
        }
        final ResourceLocation aliasId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "deepslate_" + id.getPath());
        if (!BuiltInRegistries.BLOCK.containsKey(aliasId)) {
            return null;
        }
        return BuiltInRegistries.BLOCK.get(aliasId);
    }

    private boolean swapChunk(
        final WorldGenLevel level,
        final ChunkAccess chunk,
        final Map<Block, Map<Block, OreVariant>> byForeground,
        final SwapStats stats
    ) {
        boolean replacedAny = false;
        final ChunkPos chunkPos = chunk.getPos();
        final int minBuildY = level.getMinBuildHeight();
        final LevelChunkSection[] sections = chunk.getSections();
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        final ReplacementScratch scratch = new ReplacementScratch();

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            final LevelChunkSection section = sections[sectionIndex];
            if (section == null || section.hasOnlyAir()) {
                continue;
            }
            final int sectionY = minBuildY + (sectionIndex << 4);

            for (int localY = 0; localY < 16; localY++) {
                final int y = sectionY + localY;
                for (int localX = 0; localX < 16; localX++) {
                    final int x = chunkPos.getBlockX(localX);
                    for (int localZ = 0; localZ < 16; localZ++) {
                        stats.scanned++;
                        final BlockState state = section.getBlockState(localX, localY, localZ);
                        if (state.getBlock() instanceof OreVariant) {
                            continue;
                        }
                        final Map<Block, OreVariant> byBackground = byForeground.get(state.getBlock());
                        if (byBackground == null || byBackground.isEmpty()) {
                            continue;
                        }
                        stats.candidates++;
                        pos.set(x, y, chunkPos.getBlockZ(localZ));
                        final BlockState replacement = pickReplacement(level, pos, state, byBackground, scratch);
                        if (replacement != null) {
                            section.setBlockState(localX, localY, localZ, replacement, false);
                            stats.replaced++;
                            replacedAny = true;
                        }
                    }
                }
            }
        }
        return replacedAny;
    }

    private static BlockState pickReplacement(
        final WorldGenLevel level,
        final BlockPos pos,
        final BlockState oreState,
        final Map<Block, OreVariant> byBackground,
        final ReplacementScratch scratch
    ) {
        scratch.reset();
        final BlockPos.MutableBlockPos neighborPos = scratch.neighborPos;
        final Candidate[] candidates = scratch.candidates;
        int candidateCount = 0;

        for (int i = 0; i < NEIGHBOR_PRIORITY.length; i++) {
            final Direction direction = NEIGHBOR_PRIORITY[i];
            neighborPos.setWithOffset(pos, direction);
            final BlockState bgState = asBackgroundState(level.getBlockState(neighborPos));
            final Block bg = bgState.getBlock();
            final OreVariant variant = byBackground.get(bg);
            if (variant == null) {
                continue;
            }

            int slot = -1;
            for (int j = 0; j < candidateCount; j++) {
                if (candidates[j].bg == bg) {
                    slot = j;
                    break;
                }
            }
            if (slot == -1) {
                slot = candidateCount++;
                candidates[slot].reset(bg, variant, bgState);
            }
            final Candidate candidate = candidates[slot];
            candidate.count++;
            candidate.weightedCount += direction.getAxis().isHorizontal() ? 2 : 1;
            if (i < candidate.tieBreakPriority) {
                candidate.tieBreakPriority = i;
            }
        }

        if (candidateCount == 0) {
            return null;
        }
        final Candidate winner = chooseWinner(candidates, candidateCount);
        return SharedStateBlock.copyInto(winner.variant.defaultBlockState(), oreState, winner.bgState);
    }

    private static Candidate chooseWinner(final Candidate[] candidates, final int size) {
        Candidate winner = null;
        Candidate winnerNonStone = null;
        Candidate winnerStone = null;

        for (int i = 0; i < size; i++) {
            final Candidate candidate = candidates[i];
            if (winner == null || isBetterCandidate(candidate, winner)) {
                winner = candidate;
            }
            if (candidate.variant.getBg() == Blocks.STONE) {
                winnerStone = candidate;
            } else if (winnerNonStone == null || isBetterCandidate(candidate, winnerNonStone)) {
                winnerNonStone = candidate;
            }
        }

        if (winnerStone != null
            && winnerNonStone != null
            && winnerStone.weightedCount < winnerNonStone.weightedCount + STONE_DOMINANCE_MARGIN) {
            return winnerNonStone;
        }
        return winner;
    }

    private static boolean isBetterCandidate(final Candidate candidate, final Candidate winner) {
        final int candidateScore = score(candidate);
        final int winnerScore = score(winner);
        if (candidateScore != winnerScore) {
            return candidateScore > winnerScore;
        }
        if (candidate.count != winner.count) {
            return candidate.count > winner.count;
        }
        return winsTieBreak(candidate, winner);
    }

    private static int score(final Candidate candidate) {
        final Block bg = candidate.variant.getBg();
        if (bg == Blocks.STONE) return candidate.weightedCount;
        if (bg == Blocks.DEEPSLATE) return candidate.weightedCount + 1;
        return candidate.weightedCount + NON_STONE_BIAS;
    }

    private static boolean winsTieBreak(final Candidate candidate, final Candidate winner) {
        final int candidateTier = tier(candidate.variant.getBg());
        final int winnerTier = tier(winner.variant.getBg());
        if (candidateTier != winnerTier) {
            return candidateTier > winnerTier;
        }
        return candidate.tieBreakPriority < winner.tieBreakPriority;
    }

    private static int tier(final Block bg) {
        if (bg == Blocks.STONE) return 0;
        if (bg == Blocks.DEEPSLATE) return 1;
        return 2;
    }

    private static BlockState asBackgroundState(final BlockState state) {
        if (state.getBlock() instanceof OreVariant variant) {
            return variant.asBg(state);
        }
        return state;
    }

    private static final class SwapStats {
        private long scanned;
        private long candidates;
        private long replaced;
    }

    private static final class ReplacementScratch {
        private final BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
        private final Candidate[] candidates = {
            new Candidate(),
            new Candidate(),
            new Candidate(),
            new Candidate(),
            new Candidate(),
            new Candidate()
        };

        private void reset() {
            // Intentionally empty: candidate state is overwritten when slot is reused.
        }
    }

    private static final class Candidate {
        private Block bg;
        private OreVariant variant;
        private BlockState bgState;
        private int count;
        private int weightedCount;
        private int tieBreakPriority;

        private Candidate() {
        }

        private void reset(final Block bg, final OreVariant variant, final BlockState bgState) {
            this.bg = bg;
            this.variant = variant;
            this.bgState = bgState;
            this.count = 0;
            this.weightedCount = 0;
            this.tieBreakPriority = Integer.MAX_VALUE;
        }
    }
}
