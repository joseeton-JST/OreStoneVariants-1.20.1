package io.github.joseetoon.genlib.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.experimental.UtilityClass;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import personthecat.overwritevalidator.annotations.Inherit;
import personthecat.overwritevalidator.annotations.InheritMissingMembers;
import personthecat.overwritevalidator.annotations.Overwrite;
import personthecat.overwritevalidator.annotations.OverwriteClass;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.github.joseetoon.genlib.util.Shorthand.full;
import static io.github.joseetoon.genlib.util.Shorthand.nullable;

@UtilityClass
@OverwriteClass
@InheritMissingMembers
@SuppressWarnings("unused")
public class ValueLookup {

    /** A map of MCP sound names which don't overlap with the originals. */
    private static final Map<String, SoundType> MCP_SOUND_MAP = ImmutableMap.<String, SoundType>builder()
        .put("GROUND", SoundType.GRAVEL)
        .put("PLANT", SoundType.GRASS)
        .put("LILY_PADS", SoundType.LILY_PAD)
        .put("CLOTH", SoundType.WOOL)
        .put("SLIME", SoundType.SLIME_BLOCK)
        .put("HONEY", SoundType.HONEY_BLOCK)
        .put("CORAL", SoundType.CORAL_BLOCK)
        .put("STEM", SoundType.HARD_CROP)
        .put("HYPHAE", SoundType.STEM)
        .put("ROOT", SoundType.ROOTS)
        .put("NETHER_VINE", SoundType.WEEPING_VINES)
        .put("NETHER_VINE_LOWER_PITCH", SoundType.TWISTING_VINES)
        .put("NETHER_BRICK", SoundType.NETHER_BRICKS)
        .put("NETHER_SPROUT", SoundType.NETHER_SPROUTS)
        .put("BONE", SoundType.BONE_BLOCK)
        .put("NETHERITE", SoundType.NETHERITE_BLOCK)
        .put("NETHER_GOLD", SoundType.NETHER_GOLD_ORE)
        .build();

    @Inherit
    private static final BiMap<String, SoundType> SOUND_MAP = ImmutableBiMap.<String, SoundType>builder().build();

    /** A map of every vanilla map color to its name. */
    private static final BiMap<String, MapColor> COLOR_MAP = ImmutableBiMap.<String, MapColor>builder()
        .put("NONE", MapColor.NONE)
        .put("GRASS", MapColor.GRASS)
        .put("SAND", MapColor.SAND)
        .put("WOOL", MapColor.WOOL)
        .put("FIRE", MapColor.FIRE)
        .put("ICE", MapColor.ICE)
        .put("METAL", MapColor.METAL)
        .put("PLANT", MapColor.PLANT)
        .put("SNOW", MapColor.SNOW)
        .put("CLAY", MapColor.CLAY)
        .put("DIRT", MapColor.DIRT)
        .put("STONE", MapColor.STONE)
        .put("WATER", MapColor.WATER)
        .put("WOOD", MapColor.WOOD)
        .put("QUARTZ", MapColor.QUARTZ)
        .put("COLOR_ORANGE", MapColor.COLOR_ORANGE)
        .put("COLOR_MAGENTA", MapColor.COLOR_MAGENTA)
        .put("COLOR_LIGHT_BLUE", MapColor.COLOR_LIGHT_BLUE)
        .put("COLOR_YELLOW", MapColor.COLOR_YELLOW)
        .put("COLOR_LIGHT_GREEN", MapColor.COLOR_LIGHT_GREEN)
        .put("COLOR_PINK", MapColor.COLOR_PINK)
        .put("COLOR_GRAY", MapColor.COLOR_GRAY)
        .put("COLOR_LIGHT_GRAY", MapColor.COLOR_LIGHT_GRAY)
        .put("COLOR_CYAN", MapColor.COLOR_CYAN)
        .put("COLOR_PURPLE", MapColor.COLOR_PURPLE)
        .put("COLOR_BLUE", MapColor.COLOR_BLUE)
        .put("COLOR_BROWN", MapColor.COLOR_BROWN)
        .put("COLOR_GREEN", MapColor.COLOR_GREEN)
        .put("COLOR_RED", MapColor.COLOR_RED)
        .put("COLOR_BLACK", MapColor.COLOR_BLACK)
        .put("GOLD", MapColor.GOLD)
        .put("DIAMOND", MapColor.DIAMOND)
        .put("LAPIS", MapColor.LAPIS)
        .put("EMERALD", MapColor.EMERALD)
        .put("PODZOL", MapColor.PODZOL)
        .put("NETHER", MapColor.NETHER)
        .put("TERRACOTTA_WHITE", MapColor.TERRACOTTA_WHITE)
        .put("TERRACOTTA_ORANGE", MapColor.TERRACOTTA_ORANGE)
        .put("TERRACOTTA_MAGENTA", MapColor.TERRACOTTA_MAGENTA)
        .put("TERRACOTTA_LIGHT_BLUE", MapColor.TERRACOTTA_LIGHT_BLUE)
        .put("TERRACOTTA_YELLOW", MapColor.TERRACOTTA_YELLOW)
        .put("TERRACOTTA_LIGHT_GREEN", MapColor.TERRACOTTA_LIGHT_GREEN)
        .put("TERRACOTTA_PINK", MapColor.TERRACOTTA_PINK)
        .put("TERRACOTTA_GRAY", MapColor.TERRACOTTA_GRAY)
        .put("TERRACOTTA_LIGHT_GRAY", MapColor.TERRACOTTA_LIGHT_GRAY)
        .put("TERRACOTTA_CYAN", MapColor.TERRACOTTA_CYAN)
        .put("TERRACOTTA_PURPLE", MapColor.TERRACOTTA_PURPLE)
        .put("TERRACOTTA_BLUE", MapColor.TERRACOTTA_BLUE)
        .put("TERRACOTTA_BROWN", MapColor.TERRACOTTA_BROWN)
        .put("TERRACOTTA_GREEN", MapColor.TERRACOTTA_GREEN)
        .put("TERRACOTTA_RED", MapColor.TERRACOTTA_RED)
        .put("TERRACOTTA_BLACK", MapColor.TERRACOTTA_BLACK)
        .put("CRIMSON_NYLIUM", MapColor.CRIMSON_NYLIUM)
        .put("CRIMSON_STEM", MapColor.CRIMSON_STEM)
        .put("CRIMSON_HYPHAE", MapColor.CRIMSON_HYPHAE)
        .put("WARPED_NYLIUM", MapColor.WARPED_NYLIUM)
        .put("WARPED_STEM", MapColor.WARPED_STEM)
        .put("WARPED_HYPHAE", MapColor.WARPED_HYPHAE)
        .put("WARPED_WART_BLOCK", MapColor.WARPED_WART_BLOCK)
        .put("DEEPSLATE", MapColor.DEEPSLATE)
        .put("RAW_IRON", MapColor.RAW_IRON)
        .put("GLOW_LICHEN", MapColor.GLOW_LICHEN)
        .build();

    /** A codec for serializing map colors in config files. */
    public static final Codec<MapColor> COLOR_CODEC = Codec.STRING.flatXmap(
        key -> getColor(key).map(DataResult::success)
            .orElse(DataResult.error(() -> "No such color: " + key)),
        color -> serializeColor(color).map(DataResult::success)
            .orElse(DataResult.error(() -> "Unknown color: " + color))
    );

    /** A codec for serializing sound types in config files. */
    public static final Codec<SoundType> SOUND_CODEC = Codec.STRING.flatXmap(
        key -> getSoundType(key).map(DataResult::success)
            .orElse(DataResult.error(() -> "No such sound type: " + key)),
        sound -> serializeSound(sound).map(DataResult::success)
            .orElse(DataResult.error(() -> "Unknown sound type: " + sound))
    );

    @Overwrite
    public static Optional<SoundType> getSoundType(final String key) {
        final String caps = key.toUpperCase();
        final SoundType moj = SOUND_MAP.get(caps);
        return moj != null ? full(moj) : nullable(MCP_SOUND_MAP.get(caps));
    }

    public static Optional<MapColor> getColor(final String key) {
        return nullable(COLOR_MAP.get(key.toUpperCase()));
    }

    public static Set<String> getSoundNames() {
        return SOUND_MAP.keySet();
    }

    public static Set<SoundType> getSoundValues() {
        return SOUND_MAP.values();
    }

    public static Optional<String> serializeSound(final SoundType value) {
        return nullable(SOUND_MAP.inverse().get(value));
    }

    public static Set<String> getColorNames() {
        return COLOR_MAP.keySet();
    }

    public static Set<MapColor> getColorValues() {
        return COLOR_MAP.values();
    }

    public static Optional<String> serializeColor(final MapColor color) {
        return nullable(COLOR_MAP.inverse().get(color));
    }
}
