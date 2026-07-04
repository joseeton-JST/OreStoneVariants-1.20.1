package io.github.joseetoon.genlib.event.world;

import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.ApiStatus;

/**
 * In 1.20.1, biome feature modification is handled by {@link CatLibBiomeModifier}
 * via Forge's BiomeModifier system. This class is retained as a stub.
 */
public class FeatureModificationHook {

    @ApiStatus.Internal
    public static void onRegistryAccess(final RegistryAccess holder) {
        // No-op: feature modification now happens in CatLibBiomeModifier.modify()
    }
}
