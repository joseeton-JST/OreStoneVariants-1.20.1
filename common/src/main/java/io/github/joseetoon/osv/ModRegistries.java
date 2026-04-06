package io.github.joseetoon.osv;

import net.minecraft.resources.ResourceLocation;
import io.github.joseetoon.genlib.data.SafeRegistry;
import io.github.joseetoon.osv.block.OreVariant;
import io.github.joseetoon.osv.config.BlockEntry;
import io.github.joseetoon.osv.config.BlockList;
import io.github.joseetoon.osv.config.VariantDescriptor;
import io.github.joseetoon.osv.init.GroupSerializer;
import io.github.joseetoon.osv.init.PresetLoadingContext;
import io.github.joseetoon.osv.init.VariantLoadingContext;
import io.github.joseetoon.osv.item.VariantItem;
import io.github.joseetoon.osv.preset.OrePreset;
import io.github.joseetoon.osv.preset.StonePreset;
import io.github.joseetoon.osv.util.Group;

import java.util.List;

import static io.github.joseetoon.genlib.util.Shorthand.f;

public class ModRegistries {

    public static final SafeRegistry<BlockEntry, List<VariantDescriptor>> BLOCK_LIST =
        SafeRegistry.of(BlockList::loadEntries)
            .respondsWith(entry -> f("Entry was not loaded on block list event: {}", entry))
            .canBeReset(true);

    public static final SafeRegistry<String, OrePreset> ORE_PRESETS =
        SafeRegistry.of(PresetLoadingContext::getOres)
            .respondsWith(name -> f("No ore preset was found with name: {}", name))
            .canBeReset(true);

    public static final SafeRegistry<String, StonePreset> STONE_PRESETS =
        SafeRegistry.of(PresetLoadingContext::getStones)
            .respondsWith(name -> f("No stone preset was found with name: {}", name))
            .canBeReset(true);

    public static final SafeRegistry<String, Group> PROPERTY_GROUPS =
        SafeRegistry.of(GroupSerializer::loadPropertyGroups)
            .respondsWith(name -> f("Ore group is undefined: {}", name))
            .canBeReset(true);

    public static final SafeRegistry<String, Group> BLOCK_GROUPS =
        SafeRegistry.of(GroupSerializer::loadBlockGroups)
            .respondsWith(name -> f("Block group is undefined: {}", name))
            .canBeReset(true);

    public static final SafeRegistry<ResourceLocation, OreVariant> VARIANTS =
        SafeRegistry.of(VariantLoadingContext::getVariants)
            .respondsWith(id -> f("Block loaded after variants were drained: {}", id));

    public static final SafeRegistry<ResourceLocation, VariantItem> ITEMS =
        SafeRegistry.of(VariantLoadingContext::getItems)
            .respondsWith(id -> f("No item created for variant: {}", id));

    public static void resetAll() {
        SafeRegistry.resetAll(BLOCK_LIST, ORE_PRESETS, STONE_PRESETS, PROPERTY_GROUPS, BLOCK_GROUPS);
    }

    public static void deepReload() {
        PresetLoadingContext.reloadOres();
        PresetLoadingContext.reloadStones();
        SafeRegistry.reloadAll(BLOCK_LIST, ORE_PRESETS, STONE_PRESETS, PROPERTY_GROUPS, BLOCK_GROUPS);
    }
}
