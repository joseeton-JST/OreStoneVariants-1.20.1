package io.github.joseetoon.osv.tag;

import lombok.extern.log4j.Log4j2;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import io.github.joseetoon.osv.ModRegistries;
import io.github.joseetoon.osv.block.OreVariant;
import io.github.joseetoon.osv.config.Cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class TagHelper {

    private static volatile Map<TagKey<Block>, List<ResourceLocation>> cachedBlockTags = Collections.emptyMap();
    private static volatile Map<TagKey<Item>, List<ResourceLocation>> cachedItemTags = Collections.emptyMap();

    /**
     * Called from TagsUpdatedEvent (after tags are baked into registries).
     * Computes which tags each variant's fg/bg blocks belong to and caches the result.
     * On the next data pack reload, OsvTagPackResources will emit tag JSON files
     * built from this cache — so variants inherit the tags of their source blocks.
     */
    public static void injectTags() {
        if (!Cfg.copyTags()) {
            log.debug("OSV tag copying is disabled.");
            return;
        }

        final Map<TagKey<Block>, List<ResourceLocation>> blockTags = new HashMap<>();
        final Map<TagKey<Item>, List<ResourceLocation>> itemTags = new HashMap<>();

        ModRegistries.VARIANTS.forEach((id, variant) -> {
            final Block fg = variant.getFg();
            final Block bg = variant.getBg();

            if (Cfg.copyBlockTags()) {
                if (Cfg.copyFgTags()) {
                    BuiltInRegistries.BLOCK.getTagNames()
                        .filter(tag -> fg.builtInRegistryHolder().is(tag))
                        .forEach(tag -> blockTags.computeIfAbsent(tag, k -> new ArrayList<>()).add(id));
                }
                if (Cfg.copyBgTags()) {
                    BuiltInRegistries.BLOCK.getTagNames()
                        .filter(tag -> bg.builtInRegistryHolder().is(tag))
                        .forEach(tag -> blockTags.computeIfAbsent(tag, k -> new ArrayList<>()).add(id));
                }
            }

            if (Cfg.copyItemTags()) {
                final Item fgItem = fg.asItem();
                final Item bgItem = bg.asItem();

                if (Cfg.copyFgTags() && fgItem != Items.AIR) {
                    BuiltInRegistries.ITEM.getTagNames()
                        .filter(tag -> fgItem.builtInRegistryHolder().is(tag))
                        .forEach(tag -> itemTags.computeIfAbsent(tag, k -> new ArrayList<>()).add(id));
                }
                if (Cfg.copyBgTags() && bgItem != Items.AIR) {
                    BuiltInRegistries.ITEM.getTagNames()
                        .filter(tag -> bgItem.builtInRegistryHolder().is(tag))
                        .forEach(tag -> itemTags.computeIfAbsent(tag, k -> new ArrayList<>()).add(id));
                }
            }
        });

        cachedBlockTags = Collections.unmodifiableMap(blockTags);
        cachedItemTags = Collections.unmodifiableMap(itemTags);

        final int blockEntries = blockTags.values().stream().mapToInt(List::size).sum();
        final int itemEntries  = itemTags.values().stream().mapToInt(List::size).sum();
        log.info("OSV: Tag cache updated — {} block-tag entries across {} tags, {} item-tag entries across {} tags.",
            blockEntries, blockTags.size(), itemEntries, itemTags.size());
        if (blockEntries > 0 || itemEntries > 0) {
            log.info("OSV: Run '/reload' to apply tag changes to this session.");
        }
    }

    public static Map<TagKey<Block>, List<ResourceLocation>> getCachedBlockTags() {
        return cachedBlockTags;
    }

    public static Map<TagKey<Item>, List<ResourceLocation>> getCachedItemTags() {
        return cachedItemTags;
    }
}
