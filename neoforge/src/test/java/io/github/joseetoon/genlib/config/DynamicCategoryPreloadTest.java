package io.github.joseetoon.genlib.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicCategoryPreloadTest {

    @Test
    void booleanCategoryReadsAndWritesBeforeForgeConfigLoads() {
        final CommentedConfig cfg = CommentedConfig.inMemory();
        final CommentedConfig modSupport = CommentedConfig.inMemory();
        modSupport.set("minecraft", false);
        modSupport.set("create", true);
        cfg.set("modSupport", modSupport);

        final DynamicCategory<Boolean> category = DynamicCategoryBuilder.withPath("modSupport")
            .withBooleanEntries(Map.of("minecraft", true, "create", true))
            .withDefaultValue(true)
            .build(new ForgeConfigSpec.Builder(), cfg);

        assertFalse(category.get("minecraft"));
        assertTrue(category.get("create"));
        assertTrue(category.get("missing"));

        assertFalse(category.put("minecraft", true));
        assertTrue(category.get("minecraft"));
        assertEquals(Boolean.TRUE, ((CommentedConfig) cfg.get("modSupport")).get("minecraft"));
    }

    @Test
    void listCategoriesExposePreloadValuesThroughGetEntrySetAndValues() {
        final CommentedConfig cfg = CommentedConfig.inMemory();
        final CommentedConfig blockRegistry = CommentedConfig.inMemory();
        final CommentedConfig propertyGroups = CommentedConfig.inMemory();
        final CommentedConfig blockGroups = CommentedConfig.inMemory();

        propertyGroups.set("minecraft", List.of("minecraft:coal_ore", "minecraft:copper_ore"));
        propertyGroups.set("create", List.of("create:zinc_ore"));
        blockGroups.set("minecraft", List.of("minecraft:stone", "minecraft:granite"));
        blockGroups.set("create", List.of("create:limestone"));

        blockRegistry.set("propertyGroups", propertyGroups);
        blockRegistry.set("blockGroups", blockGroups);
        cfg.set("blockRegistry", blockRegistry);

        final DynamicCategory<List<String>> oreCategory = DynamicCategoryBuilder.withPath("blockRegistry.propertyGroups")
            .withList("minecraft", List.of("minecraft:coal_ore", "minecraft:copper_ore"))
            .withList("create", List.of("create:zinc_ore"))
            .build(new ForgeConfigSpec.Builder(), cfg);
        final DynamicCategory<List<String>> stoneCategory = DynamicCategoryBuilder.withPath("blockRegistry.blockGroups")
            .withList("minecraft", List.of("minecraft:stone", "minecraft:granite"))
            .withList("create", List.of("create:limestone"))
            .build(new ForgeConfigSpec.Builder(), cfg);

        assertEquals(List.of("minecraft:coal_ore", "minecraft:copper_ore"), oreCategory.get("minecraft"));
        assertEquals(List.of("minecraft:stone", "minecraft:granite"), stoneCategory.get("minecraft"));
        assertTrue(oreCategory.entrySet().stream().anyMatch(e -> e.getKey().equals("create") && e.getValue().contains("create:zinc_ore")));
        assertTrue(stoneCategory.values().stream().anyMatch(v -> v.contains("create:limestone")));
    }

    @Test
    void preloadIterationCanBuildNonEmptyLookupMapsForVariantLoading() {
        final CommentedConfig cfg = CommentedConfig.inMemory();
        final CommentedConfig blockRegistry = CommentedConfig.inMemory();
        final CommentedConfig propertyGroups = CommentedConfig.inMemory();
        final CommentedConfig blockGroups = CommentedConfig.inMemory();

        propertyGroups.set("minecraft", List.of("minecraft:coal_ore", "minecraft:copper_ore"));
        propertyGroups.set("create", List.of("create:zinc_ore"));
        blockGroups.set("minecraft", List.of("minecraft:stone", "minecraft:granite"));
        blockGroups.set("create", List.of("create:limestone"));

        blockRegistry.set("propertyGroups", propertyGroups);
        blockRegistry.set("blockGroups", blockGroups);
        cfg.set("blockRegistry", blockRegistry);

        final DynamicCategory<List<String>> oreCategory = DynamicCategoryBuilder.withPath("blockRegistry.propertyGroups")
            .withList("minecraft", List.of("minecraft:coal_ore", "minecraft:copper_ore"))
            .withList("create", List.of("create:zinc_ore"))
            .build(new ForgeConfigSpec.Builder(), cfg);
        final DynamicCategory<List<String>> stoneCategory = DynamicCategoryBuilder.withPath("blockRegistry.blockGroups")
            .withList("minecraft", List.of("minecraft:stone", "minecraft:granite"))
            .withList("create", List.of("create:limestone"))
            .build(new ForgeConfigSpec.Builder(), cfg);

        final Map<String, List<String>> oreLookup = new LinkedHashMap<>();
        for (final Map.Entry<String, List<String>> entry : oreCategory.entrySet()) {
            oreLookup.put(entry.getKey(), entry.getValue());
        }
        final Map<String, List<String>> stoneLookup = new LinkedHashMap<>();
        for (final Map.Entry<String, List<String>> entry : stoneCategory.entrySet()) {
            stoneLookup.put(entry.getKey(), entry.getValue());
        }

        assertNotNull(oreLookup.get("minecraft"));
        assertNotNull(stoneLookup.get("minecraft"));
        assertFalse(oreLookup.get("minecraft").isEmpty());
        assertFalse(stoneLookup.get("minecraft").isEmpty());
        assertTrue(oreLookup.containsKey("create"));
        assertTrue(stoneLookup.containsKey("create"));
    }
}
