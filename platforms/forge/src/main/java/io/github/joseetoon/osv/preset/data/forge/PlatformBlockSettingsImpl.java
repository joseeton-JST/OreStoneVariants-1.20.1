package io.github.joseetoon.osv.preset.data.forge;

import com.mojang.serialization.Codec;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import io.github.joseetoon.genlib.serialization.CodecUtils;
import io.github.joseetoon.osv.preset.OrePreset;
import io.github.joseetoon.osv.preset.data.PlatformBlockSettings;

/**
 * [1.20.1 Migration] harvestLevel and harvestTool (Forge-specific APIs) were removed in 1.17.
 * In 1.20.1, tool requirements are defined via block tags (e.g. minecraft:needs_iron_tool).
 * This class is kept as an empty stub for codec compatibility.
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class PlatformBlockSettingsImpl extends PlatformBlockSettings {

    public static final Codec<PlatformBlockSettingsImpl> CODEC =
        Codec.unit(PlatformBlockSettingsImpl::new);

    public static final PlatformBlockSettingsImpl EMPTY = new PlatformBlockSettingsImpl();

    public static Codec<PlatformBlockSettings> getCodec() {
        return CodecUtils.asParent(CODEC);
    }

    public static PlatformBlockSettings getEmpty() {
        return EMPTY;
    }

    @Override
    public Codec<PlatformBlockSettings> codec() {
        return CodecUtils.asParent(CODEC);
    }

    @Override
    public void apply(final BlockBehaviour.Properties properties, final OrePreset preset, final Block bg, final Block fg) {
        // No-op: harvestLevel/harvestTool were removed in 1.17.
        // Tool requirements are now tag-based (e.g. minecraft:needs_iron_tool).
    }
}
