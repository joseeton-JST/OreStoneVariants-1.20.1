package io.github.joseetoon.osv.block.neoforge;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import io.github.joseetoon.osv.block.OreVariant;
import io.github.joseetoon.osv.block.StateConfig;
import io.github.joseetoon.osv.preset.OrePreset;

public class OreVariantImpl {

    public static OreVariant createPlatformVariant(
            final OrePreset preset, final Properties properties, final StateConfig config) {
        // 1.20.1 fallback: base OreVariant already contains the merged behavior.
        return new OreVariant(preset, properties, config);
    }
}
