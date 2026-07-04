package io.github.joseetoon.osv.config.neoforge;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import io.github.joseetoon.genlib.config.DynamicCategory;
import io.github.joseetoon.genlib.config.DynamicCategoryBuilder;
import io.github.joseetoon.genlib.data.Lazy;
import io.github.joseetoon.osv.config.DefaultOres;
import io.github.joseetoon.osv.config.DefaultStones;
import io.github.joseetoon.osv.config.PresetUpdatePreference;
import io.github.joseetoon.osv.preset.data.ModelSettings;
import io.github.joseetoon.osv.preset.reader.ComponentReader;
import io.github.joseetoon.osv.util.Group;
import io.github.joseetoon.osv.util.Reference;
import io.github.joseetoon.osv.util.StateMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CfgImpl {

    private static final CommentedFileConfig COMMON_CFG = readConfig(false);
    private static final CommentedFileConfig CLIENT_CFG = readConfig(true);

    private static final Builder COMMON = new Builder();
    private static final Builder CLIENT = new Builder();

    public static final ConfigValue<List<String>> BLOCK_ENTRIES = COMMON
        .comment("  You can use this registry to add as many new ore types as you like using any",
            "background block at all. Block models, textures, world generation, and other",
            "properties are handled dynamically. These blocks will only spawn in whichever",
            "block or block state is listed as the background block. The ores retain a mixture",
            "of their original properties + those of their background.",
            "  The basic syntax is like this: \"<ore> <background>.\" For example, to spawn",
            "coal ore inside of sand, you would type \"coal_ore sand.\" Alternatively, you",
            "can list out any number of ore blocks inside of a new or existing PropertyGroup",
            "below and use its name in the place of \"<ore>.\" Likewise, \"<background>\" can",
            "be replaced with the name of a BlockGroup, as registered below.",
            "  Some block and property groups have special names. Those are \"all\" and",
            "\"default.\" Using \"all\" in the place of either will gather all of the respective",
            "entries from below and add them to the list dynamically. Using \"default\" in the",
            "place of either will gather all of the entries that are listed *by default*.")
        .define("blockRegistry.values", Collections.singletonList("all all"), Objects::nonNull);

    private static final DynamicCategory<List<String>> BLOCK_GROUPS =
        DynamicCategoryBuilder.withPath("blockRegistry.blockGroups")
            .withListEntries(Group.toIdMap(DefaultStones.LISTED))
            .build(COMMON, COMMON_CFG);

    private static final DynamicCategory<List<String>> PROPERTY_GROUPS =
        DynamicCategoryBuilder.withPath("blockRegistry.propertyGroups")
            .withListEntries(Group.toIdMap(DefaultOres.LISTED))
            .build(COMMON, COMMON_CFG);

    private static final DynamicCategory<Boolean> ENABLED_MODS =
        DynamicCategoryBuilder.withPath("modSupport")
            .withBooleanEntries(Reference.SUPPORTED_MODS)
            .withDefaultValue(true)
            .build(COMMON, COMMON_CFG);

    public static final BooleanValue CHECK_FOR_DUPLICATES = COMMON
        .comment("Whether to test the block registry for duplicate entries.")
        .define("blockRegistry.checkForDuplicates", true);

    public static final EnumValue<PresetUpdatePreference> UPDATE_PRESETS = COMMON
        .comment("Whether to run transformations on the ore presets for backwards",
            "compatibility.")
        .defineEnum("general.updatePresets", PresetUpdatePreference.MOD_UPDATED);

    public static final BooleanValue FORCE_COMPATIBILITY_MODE = COMMON
        .comment("Whether to forcibly disable dynamic block imitation features to",
            "provide better compatibility with some platforms. Note that this",
            "feature should get enabled automatically, if needed.")
        .define("general.forceCompatibilityMode", false);

    public static final BooleanValue BG_IMITATION = COMMON
        .comment("Variants will imitate the properties of their background blocks,",
            "such as the ability to fall like sand or sustain leaves. Can be",
            "configured on an individual basis in the ore presets.")
        .define("blocks.bgImitation", true);

    public static final BooleanValue BG_DUPLICATION = COMMON
        .comment("Whether to suppress the ability for background blocks to duplicate",
            "themselves. Enabling this may improve gameplay balance by not letting",
            "ores spread in the world unexpectedly. Can be configured on an",
            "individual basis in the ore presets.")
        .define("blocks.bgDuplication", true);

    public static final BooleanValue FURNACE_RECIPES = COMMON
        .define("blocks.enableFurnaceRecipes", true);

    public static final BooleanValue ASSETS_FROM_RP = CLIENT
        .comment("Attempts to generate any new ore sprites from the topmost resource",
            "pack. Not an ideal solution for many resource packs.")
        .define("resources.assetsFromRP", true);

    public static final BooleanValue OVERLAY_MODEL_SHADE = CLIENT
        .comment("Indicates whether to enable shading in generated block models.")
        .define("models.overlayShade", true);

    public static final BooleanValue SHADE_MODIFIER = CLIENT
        .comment("Whether textures should use variable opacity to push and pull the",
            "background sprite.")
        .define("textures.shadeModifier", true);

    public static final BooleanValue OVERLAY_TRANSPARENCY = CLIENT
        .comment("Whether textures should be rendered with support for a full range",
            "of opacity. You may want to disable this if you're using shaders.")
        .define("models.overlayTransparency", true);

    public static final DoubleValue OVERLAY_SCALE = CLIENT
        .comment("How much larger the overlay model is than the background model.",
            "Lower values may look better, but cause z-fighting.")
        .defineInRange("models.overlayScale", 1.001, 1.0, 2.0);

    public static final EnumValue<ModelSettings.Type> MODEL_TYPE = CLIENT
        .comment("The default type of model to generate for all ores.")
        .defineEnum("models.type", ModelSettings.Type.SINGLE);

    public static final BooleanValue AUTO_REFRESH = CLIENT
        .comment("Whether to automatically reload resources after enabling the",
            "generated resource pack. This may result in fewer missing",
            "textures in rare cases.")
        .define("blocks.autoRefresh", false);

    public static final ConfigValue<String> FORMATTERS_FILE = CLIENT
        .comment("Item display formatters moved to a dedicated TOML file.",
            "This entry is informational only.",
            "Edit " + ItemFormatterTomlConfig.FILE_NAME + " to customize item formatters.")
        .define("items.formattersFile", ItemFormatterTomlConfig.FILE_NAME);

    public static final BooleanValue VARIANTS_DROP = COMMON
        .comment("Whether ore variants will drop instead of original counterparts.")
        .define("blocks.variantsDrop", false);

    public static final BooleanValue VARIANTS_SILK_TOUCH = COMMON
        .comment("Whether ore variants will drop when using silk touch.")
        .define("blocks.variantsDropWithSilkTouch", true);

    public static final BooleanValue MAP_INFESTED_VARIANTS = COMMON
        .comment("Whether to allow silverfish to enter into any infested variants.")
        .define("blocks.mapInfestedVariants", true);

    public static final BooleanValue COPY_TAGS = COMMON
        .comment("Whether to copy any tags at all. Globally toggles the copy feature.")
        .define("tags.copyTags", true);

    public static final BooleanValue COPY_BLOCK_TAGS = COMMON
        .comment("Whether to copy any block tags at all for ore variants.")
        .define("tags.copyBlockTags", true);

    public static final BooleanValue COPY_ITEM_TAGS = COMMON
        .comment("Whether to copy any item tags at all for ore variants.")
        .define("tags.copyItemTags", true);

    public static final BooleanValue COPY_BG_TAGS = COMMON
        .comment("Whether tags should be copied from background blocks.")
        .define("tags.copyBgTags", false);

    public static final BooleanValue COPY_FG_TAGS = COMMON
        .comment("Whether tags should be copied from foreground blocks.")
        .define("tags.copyFgTags", true);

    public static final BooleanValue COPY_DENSE_TAGS = COMMON
        .comment("Whether regular tags should be copied for dense variant blocks and items.")
        .define("tags.copyDenseTags", true);

    public static final BooleanValue DENSE_ORES = COMMON
        .comment("Adds a dense variant of every ore. Drops 1-3x each original drop.")
        .define("denseOres.enabled", false);

    public static final DoubleValue DENSE_CHANCE = COMMON
        .comment("The 0-1 chance that dense ores will spawn instead of regular variants.")
        .defineInRange("denseOres.chance", 0.09, 0.0, 1.0);

    public static final IntValue DENSE_SMELT_MULTIPLIER = COMMON
        .comment("The number of items to yield when smelting dense ores.")
        .defineInRange("denseOres.smeltingMultiplier", 2, 0, Integer.MAX_VALUE);

    public static final IntValue DENSE_DROP_MULTIPLIER = COMMON
        .comment("The maximum multiple of items to drop when mining dense ores.")
        .defineInRange("denseOres.dropMultiplier", 3, 1, Integer.MAX_VALUE);

    public static final IntValue DENSE_DROP_MULTIPLIER_MIN = COMMON
        .comment("The minimum multiple of items to drop when mining dense ores.")
        .defineInRange("denseOres.dropMultiplierMin", 1, 1, Integer.MAX_VALUE);

    public static final BooleanValue RANDOM_DROP_COUNT = COMMON
        .comment("If true, dense variants drop a random number between min and max multipliers.")
        .define("denseOres.randomDropCount", true);

    public static final BooleanValue BIOME_SPECIFIC = COMMON
        .comment("Whether ores should spawn according to specific biomes vs. anywhere.")
        .define("worldGen.biomeSpecific", true);

    public static final BooleanValue AUTO_DISABLE_ORES = COMMON
        .comment("Whether vanilla spawning of ores should be blocked.")
        .define("worldGen.autoDisableOres", true);

    public static final BooleanValue AUTO_DISABLE_STONE = COMMON
        .comment("Whether vanilla spawning of stone variants should be blocked.")
        .define("worldGen.autoDisableStone", true);

    public static final BooleanValue ENABLE_OSV_ORES = COMMON
        .comment("Whether to spawn custom ore variants.")
        .define("worldGen.enableOSVOres", true);

    public static final BooleanValue ENABLE_OSV_STONE = COMMON
        .comment("Whether to spawn stone types with custom variables.")
        .define("worldGen.enableOSVStone", true);

    public static final BooleanValue HIGH_ACCURACY = COMMON
        .comment("Whether to keep worldgen using vanilla-compatible datapack behavior.",
            "When disabled, OSV may simplify some foreign settings.")
        .define("worldGen.highAccuracy", false);

    public static final ConfigValue<List<String>> DISABLED_FEATURES = COMMON
        .comment("Add configured feature IDs to disable spawning for those features.",
            "Use '/osv debug features' in game to list available IDs.")
        .define("worldGen.disabledFeatures", List.of("create:zinc_ore"), Objects::nonNull);

    public static final BooleanValue DEBUG_STARTUP = COMMON
        .comment("Enable extra startup diagnostics and snapshots in logs.")
        .define("general.debugStartup", false);

    private static final Map<String, List<Map<String, Object>>> FORMATTERS = ItemFormatterTomlConfig.load();

    private static final Lazy<StateMap<List<Component>>> DEFAULT_COMPONENTS = Lazy.of(() -> {
        try {
            final StateMap<List<Component>> map = new StateMap<>();
            for (final Map.Entry<String, List<Map<String, Object>>> formatters : FORMATTERS.entrySet()) {
                final List<Component> components = new ArrayList<>();
                for (final Map<String, Object> formatter : formatters.getValue()) {
                    components.add(ComponentReader.fromRaw(formatter));
                }
                map.put(formatters.getKey(), components);
            }
            return map;
        } catch (final RuntimeException e) {
            return StateMap.singletonList("", Component.literal("{bg} (Invalid Config)"));
        }
    });

    public static void register() {
        final ModContainer ctx = ModLoadingContext.get().getActiveContainer();
        ctx.registerConfig(ModConfig.Type.COMMON, COMMON.build(), COMMON_CFG.getFile().getName());
        ctx.registerConfig(ModConfig.Type.CLIENT, CLIENT.build(), CLIENT_CFG.getFile().getName());
    }

    private static CommentedFileConfig readConfig(final boolean client) {
        return TomlFileConfig.load(client);
    }

    public static File getCommon() {
        return COMMON_CFG.getFile();
    }

    public static File getClient() {
        return CLIENT_CFG.getFile();
    }

    public static List<String> blockEntries() {
        return getListValue(BLOCK_ENTRIES, COMMON_CFG, "blockRegistry.values", Collections.singletonList("all all"));
    }

    public static void setBlockEntries(final List<String> entries) {
        BLOCK_ENTRIES.set(entries);
    }

    public static boolean checkForDuplicates() {
        return getBooleanValue(CHECK_FOR_DUPLICATES, COMMON_CFG, "blockRegistry.checkForDuplicates", true);
    }

    public static Map<String, List<String>> propertyGroups() {
        return PROPERTY_GROUPS;
    }

    public static Map<String, List<String>> blockGroups() {
        return BLOCK_GROUPS;
    }

    public static Map<String, Boolean> enabledMods() {
        return ENABLED_MODS;
    }

    public static double overlayScale() {
        return getDoubleValue(OVERLAY_SCALE, CLIENT_CFG, "models.overlayScale", 1.001);
    }

    public static ModelSettings.Type modelType() {
        return getEnumValue(MODEL_TYPE, ModelSettings.Type.class, CLIENT_CFG, "models.type", ModelSettings.Type.SINGLE);
    }

    public static StateMap<List<Component>> getItemFormatters() {
        return DEFAULT_COMPONENTS.get();
    }

    public static PresetUpdatePreference updatePresets() {
        return getEnumValue(
            UPDATE_PRESETS,
            PresetUpdatePreference.class,
            COMMON_CFG,
            "general.updatePresets",
            PresetUpdatePreference.MOD_UPDATED
        );
    }

    public static boolean forceCompatibilityMode() {
        return getBooleanValue(FORCE_COMPATIBILITY_MODE, COMMON_CFG, "general.forceCompatibilityMode", false);
    }

    public static boolean bgImitation() {
        return getBooleanValue(BG_IMITATION, COMMON_CFG, "blocks.bgImitation", true);
    }

    public static boolean bgDuplication() {
        return getBooleanValue(BG_DUPLICATION, COMMON_CFG, "blocks.bgDuplication", true);
    }

    public static boolean furnaceRecipes() {
        return getBooleanValue(FURNACE_RECIPES, COMMON_CFG, "blocks.enableFurnaceRecipes", true);
    }

    public static boolean assetsFromRP() {
        return getBooleanValue(ASSETS_FROM_RP, CLIENT_CFG, "resources.assetsFromRP", true);
    }

    public static boolean overlayShade() {
        return getBooleanValue(OVERLAY_MODEL_SHADE, CLIENT_CFG, "models.overlayShade", true);
    }

    public static boolean shadeModifier() {
        return getBooleanValue(SHADE_MODIFIER, CLIENT_CFG, "textures.shadeModifier", true);
    }

    public static boolean overlayTransparency() {
        return getBooleanValue(OVERLAY_TRANSPARENCY, CLIENT_CFG, "models.overlayTransparency", true);
    }

    public static boolean autoRefresh() {
        return getBooleanValue(AUTO_REFRESH, CLIENT_CFG, "blocks.autoRefresh", false);
    }

    public static boolean variantsDrop() {
        return getBooleanValue(VARIANTS_DROP, COMMON_CFG, "blocks.variantsDrop", false);
    }

    public static boolean variantsSilkTouch() {
        return getBooleanValue(VARIANTS_SILK_TOUCH, COMMON_CFG, "blocks.variantsDropWithSilkTouch", true);
    }

    public static boolean mapInfestedVariants() {
        return getBooleanValue(MAP_INFESTED_VARIANTS, COMMON_CFG, "blocks.mapInfestedVariants", true);
    }

    public static boolean copyTags() {
        return getBooleanValue(COPY_TAGS, COMMON_CFG, "tags.copyTags", true);
    }

    public static boolean copyBlockTags() {
        return getBooleanValue(COPY_BLOCK_TAGS, COMMON_CFG, "tags.copyBlockTags", true);
    }

    public static boolean copyItemTags() {
        return getBooleanValue(COPY_ITEM_TAGS, COMMON_CFG, "tags.copyItemTags", true);
    }

    public static boolean copyBgTags() {
        return getBooleanValue(COPY_BG_TAGS, COMMON_CFG, "tags.copyBgTags", false);
    }

    public static boolean copyFgTags() {
        return getBooleanValue(COPY_FG_TAGS, COMMON_CFG, "tags.copyFgTags", true);
    }

    public static boolean copyDenseTags() {
        return getBooleanValue(COPY_DENSE_TAGS, COMMON_CFG, "tags.copyDenseTags", true);
    }

    public static boolean denseOres() {
        return getBooleanValue(DENSE_ORES, COMMON_CFG, "denseOres.enabled", false);
    }

    public static double denseChance() {
        return getDoubleValue(DENSE_CHANCE, COMMON_CFG, "denseOres.chance", 0.09);
    }

    public static int denseSmeltMultiplier() {
        return getIntValue(DENSE_SMELT_MULTIPLIER, COMMON_CFG, "denseOres.smeltingMultiplier", 2);
    }

    public static int denseDropMultiplier() {
        return getIntValue(DENSE_DROP_MULTIPLIER, COMMON_CFG, "denseOres.dropMultiplier", 3);
    }

    public static int denseDropMultiplierMin() {
        return getIntValue(DENSE_DROP_MULTIPLIER_MIN, COMMON_CFG, "denseOres.dropMultiplierMin", 1);
    }

    public static boolean randomDropCount() {
        return getBooleanValue(RANDOM_DROP_COUNT, COMMON_CFG, "denseOres.randomDropCount", true);
    }

    public static boolean biomeSpecific() {
        return getBooleanValue(BIOME_SPECIFIC, COMMON_CFG, "worldGen.biomeSpecific", true);
    }

    public static boolean autoDisableOres() {
        return getBooleanValue(AUTO_DISABLE_ORES, COMMON_CFG, "worldGen.autoDisableOres", true);
    }

    public static boolean autoDisableStone() {
        return getBooleanValue(AUTO_DISABLE_STONE, COMMON_CFG, "worldGen.autoDisableStone", true);
    }

    public static boolean enableOSVOres() {
        return getBooleanValue(ENABLE_OSV_ORES, COMMON_CFG, "worldGen.enableOSVOres", true);
    }

    public static boolean enableOSVStone() {
        return getBooleanValue(ENABLE_OSV_STONE, COMMON_CFG, "worldGen.enableOSVStone", true);
    }

    public static boolean highAccuracy() {
        return getBooleanValue(HIGH_ACCURACY, COMMON_CFG, "worldGen.highAccuracy", false);
    }

    public static List<String> disabledFeatures() {
        return getListValue(DISABLED_FEATURES, COMMON_CFG, "worldGen.disabledFeatures", List.of("create:zinc_ore"));
    }

    public static boolean debugStartup() {
        return getBooleanValue(DEBUG_STARTUP, COMMON_CFG, "general.debugStartup", false);
    }

    private static boolean getBooleanValue(
        final BooleanValue value,
        final CommentedFileConfig cfg,
        final String path,
        final boolean fallback
    ) {
        try {
            return value.get();
        } catch (final IllegalStateException ignored) {
            final Object raw = cfg.getRaw(split(path));
            return raw instanceof Boolean b ? b : fallback;
        }
    }

    private static int getIntValue(
        final IntValue value,
        final CommentedFileConfig cfg,
        final String path,
        final int fallback
    ) {
        try {
            return value.get();
        } catch (final IllegalStateException ignored) {
            final Object raw = cfg.getRaw(split(path));
            return raw instanceof Number number ? number.intValue() : fallback;
        }
    }

    private static double getDoubleValue(
        final DoubleValue value,
        final CommentedFileConfig cfg,
        final String path,
        final double fallback
    ) {
        try {
            return value.get();
        } catch (final IllegalStateException ignored) {
            final Object raw = cfg.getRaw(split(path));
            return raw instanceof Number number ? number.doubleValue() : fallback;
        }
    }

    private static <E extends Enum<E>> E getEnumValue(
        final EnumValue<E> value,
        final Class<E> type,
        final CommentedFileConfig cfg,
        final String path,
        final E fallback
    ) {
        try {
            return value.get();
        } catch (final IllegalStateException ignored) {
            final Object raw = cfg.getRaw(split(path));
            if (type.isInstance(raw)) {
                return type.cast(raw);
            }
            if (raw instanceof String name) {
                try {
                    return Enum.valueOf(type, name);
                } catch (final IllegalArgumentException ignoredName) {
                    return fallback;
                }
            }
            return fallback;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> getListValue(
        final ConfigValue<List<String>> value,
        final CommentedFileConfig cfg,
        final String path,
        final List<String> fallback
    ) {
        try {
            return value.get();
        } catch (final IllegalStateException ignored) {
            final Object raw = cfg.getRaw(split(path));
            if (raw instanceof List<?> list) {
                return list.stream().map(String::valueOf).toList();
            }
            return fallback;
        }
    }

    private static List<String> split(final String path) {
        return Arrays.asList(path.split("\\."));
    }
}
