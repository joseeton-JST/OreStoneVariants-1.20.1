package io.github.joseetoon.osv.tag.forge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import io.github.joseetoon.osv.block.OreVariant;

import java.util.stream.Stream;

public class TagHelperTagUpdateContextImpl {

    private static final String ORES_IN_GROUND_PREFIX = "forge:ores_in_ground/";

    public static <T> Stream<TagKey<T>> getPlatformKeys(final TagKey<T> key, final OreVariant ore, final boolean fg) {
        if (fg && key.location().toString().startsWith(ORES_IN_GROUND_PREFIX)) {
            final ResourceLocation bg = BuiltInRegistries.BLOCK.getKey(ore.getBg());
            if (bg == null) {
                return Stream.empty();
            }
            final ResourceLocation updated = new ResourceLocation(ORES_IN_GROUND_PREFIX + bg.getPath());
            return Stream.of(TagKey.create(key.registry(), updated));
        }
        return Stream.of(key);
    }
}
