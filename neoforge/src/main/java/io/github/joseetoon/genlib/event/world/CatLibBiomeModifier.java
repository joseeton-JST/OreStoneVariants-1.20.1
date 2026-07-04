package io.github.joseetoon.genlib.event.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import io.github.joseetoon.genlib.config.LibConfig;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Singleton BiomeModifier that fires CatLib's {@link FeatureModificationEvent}
 * for every biome during world load. Registered via RegisterEvent in CatLib.java
 * and activated by the JSON data file at
 * {@code data/genlib/neoforge/biome_modifier/catlib_modifier.json}.
 */
@Log4j2
public class CatLibBiomeModifier implements BiomeModifier {

    public static final CatLibBiomeModifier INSTANCE = new CatLibBiomeModifier();
    public static final MapCodec<CatLibBiomeModifier> CODEC = MapCodec.unit(INSTANCE);
    private static final AtomicLong DEBUG_BIOMES = new AtomicLong();

    private CatLibBiomeModifier() {}

    @Override
    public void modify(final Holder<Biome> biome,
                       final Phase phase,
                       final ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD) return;
        if (FeatureModificationEvent.EVENT.isEmpty()) return;
        if (LibConfig.debugStartup() && log.isDebugEnabled()) {
            final long sample = DEBUG_BIOMES.incrementAndGet();
            if (sample <= 8L || sample % 64L == 0L) {
                log.debug(
                    "CatLibBiomeModifier.modify sample={} biome={} phase={}",
                    sample,
                    biome.unwrapKey().map(key -> key.location().toString()).orElse("<direct>"),
                    phase
                );
            }
        }

        final Consumer<FeatureModificationContext> invoker = FeatureModificationEvent.EVENT.invoker();
        invoker.accept(new FeatureModificationContext(biome, builder));
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
