package io.github.joseetoon.osv.command;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.apache.commons.lang3.mutable.MutableInt;
import io.github.joseetoon.genlib.command.CommandContextWrapper;
import io.github.joseetoon.genlib.command.CommandSide;
import io.github.joseetoon.genlib.command.annotations.ModCommand;
import io.github.joseetoon.genlib.command.annotations.Node;
import io.github.joseetoon.genlib.command.annotations.Node.ListInfo;
import io.github.joseetoon.genlib.command.annotations.Node.StringValue;
import io.github.joseetoon.genlib.command.arguments.ArgumentSuppliers;
import io.github.joseetoon.genlib.io.FileIO;
import io.github.joseetoon.genlib.util.GenericArrayLinter;
import io.github.joseetoon.genlib.util.ResourceArrayLinter;
import io.github.joseetoon.genlib.event.registry.CommonRegistries;
import io.github.joseetoon.genlib.data.JsonPath;
import io.github.joseetoon.genlib.util.HjsonUtils;
import io.github.joseetoon.genlib.util.FeatureSupport;
import io.github.joseetoon.genlib.util.Shorthand;
import io.github.joseetoon.osv.ModRegistries;
import io.github.joseetoon.osv.client.model.ModelHandler;
import io.github.joseetoon.osv.client.texture.TextureHandler;
import io.github.joseetoon.osv.command.argument.BackgroundArgument;
import io.github.joseetoon.osv.command.argument.BlockGroupArgument;
import io.github.joseetoon.osv.command.argument.CompatBlockStateArgument;
import io.github.joseetoon.osv.command.argument.OrePresetArgument;
import io.github.joseetoon.osv.command.argument.PropertyArgument;
import io.github.joseetoon.osv.command.argument.PropertyGroupArgument;
import io.github.joseetoon.osv.compat.OverlayCompat;
import io.github.joseetoon.osv.config.BlockEntry;
import io.github.joseetoon.osv.config.BlockList;
import io.github.joseetoon.osv.config.Cfg;
import io.github.joseetoon.osv.config.DefaultOres;
import io.github.joseetoon.osv.config.DefaultStones;
import io.github.joseetoon.osv.config.VariantDescriptor;
import io.github.joseetoon.osv.init.PresetLoadingContext;
import io.github.joseetoon.osv.io.ModFolders;
import io.github.joseetoon.osv.io.ResourceHelper;
import io.github.joseetoon.osv.preset.OrePreset;
import io.github.joseetoon.osv.preset.data.OreSettings;
import io.github.joseetoon.osv.preset.generator.PresetGenerator;
import io.github.joseetoon.osv.preset.writer.PresetWriter;
import io.github.joseetoon.osv.util.Group;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CommandOsv {

    private static final int WARN_BACKUPS = 10;

    @ModCommand(
        description = {
            "Removes all of the unnecessary loot tables from your ore presets.",
            "This will allow the variants to pull directly from their background blocks."
        })
    private void optimizeLoot(final CommandContextWrapper ctx) {
        final MutableInt count = new MutableInt(0);
        FileIO.listFilesRecursive(ModFolders.ORE_DIR, PresetLoadingContext::isPreset).forEach(preset ->
            HjsonUtils.updateJson(preset, json -> {
                final JsonValue loot = json.get(OreSettings.Fields.loot);
                if (loot != null && loot.isString() && ResourceLocation.tryParse(loot.asString()) != null) {
                    json.remove(OreSettings.Fields.loot);
                    count.increment();
                }
            }).unwrap()
        );
        ctx.sendMessage("Updated {} presets", count.getValue());
    }

    @ModCommand(
        linter = GenericArrayLinter.class,
        description = {
            "Updates the block list to simplify your entries as much possible. The",
            "updated list will contain the same variants, but will usually be shorter."
        })
    private void optimizeEntries(final CommandContextWrapper ctx) {
        final List<String> raw = BlockList.intoRaw(BlockList.optimize(BlockList.get()));
        updateEntries(raw);
        ctx.generateMessage("Updated block list:\n")
            .append(ctx.lintMessage(Arrays.toString(raw.toArray())))
            .sendMessage();
    }

    @ModCommand(
        side = CommandSide.CLIENT,
        description = "Updates any overlays in the given path to use the new layout from 7.0.",
        branch = @Node(name = "path", descriptor = ArgumentSuppliers.File.class))
    private void upgradeOverlays(final CommandContextWrapper ctx, final File path) {
        if (WARN_BACKUPS <= FileIO.backup(ctx.getBackupsFolder(), path)) {
            ctx.sendError("> {} backups detected. Consider cleaning these out.", WARN_BACKUPS);
        }
        ctx.sendMessage("Renamed {} overlays.", OverlayCompat.renameOverlays(path));
    }

    @ModCommand(
        side = CommandSide.CLIENT,
        description = {
            "Backs up and regenerates the OSV resource pack. Resources will be ",
            "refreshed immediately."
        })
    private void regenerate(final CommandContextWrapper ctx) {
        ctx.sendMessage("Creating backup of /resources");
        final File assets = ResourceHelper.file("assets");
        if (WARN_BACKUPS <= FileIO.backup(ctx.getBackupsFolder(), assets, false)) {
            ctx.sendError("> {} backups detected. Consider cleaning these out.", WARN_BACKUPS);
            ctx.sendMessage("Truncating old backups of /resources");
            FileIO.truncateBackups(ctx.getBackupsFolder(), assets, WARN_BACKUPS);
        }
        ModRegistries.resetAll();
        TextureHandler.clearOverlayCache();
        FileIO.mkdirsOrThrow(assets);
        ctx.sendMessage("Reloading texture paths.");
        ModRegistries.ORE_PRESETS.forEach(OrePreset::reloadTextures);
        ctx.sendMessage("Saving updated presets.");
        PresetWriter.savePresets(ctx.getServer().registryAccess());
        ctx.sendMessage("Generating textures.");
        ModelHandler.generateOverlayModel();
        ModRegistries.ORE_PRESETS.forEach(TextureHandler::generateOverlays);
        ctx.sendMessage("Generating models.");
        ModRegistries.BLOCK_LIST.forEach(l -> l.forEach(ModelHandler::generateModels));
        ctx.sendMessage("Scheduling refresh.");
        Minecraft.getInstance().reloadResourcePacks()
            .thenRun(() -> ctx.sendMessage("New resources generated successfully."));
    }

    @ModCommand(
        side = CommandSide.CLIENT,
        description = {
            "Reloads ore and stone presets. Only affects world generation and model",
            "assets. Requires world restart"
        })
    private void reload(final CommandContextWrapper ctx) {
        ModRegistries.resetAll();
        Minecraft.getInstance().reloadResourcePacks()
            .thenRun(() -> ctx.sendMessage("OSV resources reloaded successfully."));
    }

    @ModCommand(
        description = "Displays a list of all current biome features.",
        linter = ResourceArrayLinter.class,
        branch = @Node(name = "feature", registry = Feature.class))
    private void debugFeatures(final CommandContextWrapper ctx, final Feature<?> feature) {
        ctx.sendLintedMessage(Arrays.toString(FeatureSupport.getIds(feature).toArray()));
    }

    @ModCommand(
        description = "Displays all resource locations for variants matching the given preset.",
        linter = ResourceArrayLinter.class,
        branch = @Node(name = "group", type = PropertyArgument.class))
    private void getProperties(final CommandContextWrapper ctx, final Group group) {
        final List<ResourceLocation> matching = new ArrayList<>();
        ModRegistries.VARIANTS.forEach((id, variant) -> {
            if (group.getEntries().contains(variant.getPreset().getName())) {
                matching.add(id);
            }
        });
        ctx.sendLintedMessage(Arrays.toString(matching.toArray()));
    }

    @ModCommand(
        description = "Displays all resource locations for variants matching the given background.",
        linter = ResourceArrayLinter.class,
        branch = @Node(name = "group", type = BackgroundArgument.class))
    private void getBlocks(final CommandContextWrapper ctx, final Group group) {
        final List<ResourceLocation> matching = new ArrayList<>();
        ModRegistries.VARIANTS.forEach((id, variant) -> {
            if (group.ids().contains(CommonRegistries.BLOCKS.getKey(variant.getBg()))) {
                matching.add(id);
            }
        });
        ctx.sendLintedMessage(Arrays.toString(matching.toArray()));
    }

    @ModCommand(
        description = "Places any number of new variants on the block list.",
        linter = GenericArrayLinter.class,
        branch = {
            @Node(name = "ore", type = PropertyArgument.class, intoList = @ListInfo),
            @Node(name = "in"),
            @Node(name = "bg", type = BackgroundArgument.class, intoList = @ListInfo)
        })
    private void put(final CommandContextWrapper ctx, final List<Group> ore, final List<Group> bg) {
        final Set<BlockEntry> entries = BlockList.get();
        entries.addAll(BlockList.create(ore, bg));
        if (Cfg.checkForDuplicates() && checkDuplicates(ctx, BlockList.resolve(entries))) {
            return;
        }
        final List<String> raw = BlockList.intoRaw(BlockList.optimize(entries));
        updateEntries(raw);
        ctx.generateMessage("Updated block list:\n")
            .append(ctx.lintMessage(Arrays.toString(raw.toArray())))
            .append("\nRestart to see changes in game.")
            .sendMessage();
    }

    @ModCommand(
        description = "Adds any number of properties into an existing or new group.",
        linter = GenericArrayLinter.class,
        branch = {
            @Node(name = "entry", type = OrePresetArgument.class, intoList = @ListInfo),
            @Node(name = "in"),
            @Node(name = "group", type = PropertyGroupArgument.class)
        })
    private void groupProperties(final CommandContextWrapper ctx, final List<OrePreset> entry, final Group group) {
        final Set<String> output = new HashSet<>(group.getEntries());
        entry.forEach(preset -> output.add(preset.getName()));
        group(ctx, new Group(group.getName(), output), true);
    }

    @ModCommand(
        description = "Adds any number of blocks into an existing or new group.",
        linter = GenericArrayLinter.class,
        branch = {
            @Node(name = "entry", type = CompatBlockStateArgument.class, intoList = @ListInfo),
            @Node(name = "in"),
            @Node(name = "group", type = BlockGroupArgument.class)
        })
    private void groupBlocks(final CommandContextWrapper ctx, final List<BlockState> entry, final Group group) {
        final Set<String> output = new HashSet<>(group.getEntries());
        entry.forEach(state -> {
            final ResourceLocation id = Objects.requireNonNull(CommonRegistries.BLOCKS.getKey(state.getBlock()));
            output.add(id.toString());
        });
        group(ctx, new Group(group.getName(), output), false);
    }

    @ModCommand(
        description = "Generates diagnostic data on any block into an ore or stone preset file.",
        branch = {
            @Node(name = "option", enumValue = PresetGenerator.Option.class),
            @Node(name = "blocks", type = CompatBlockStateArgument.class, intoList = @ListInfo)
        })
    private void generate(
            final CommandContextWrapper ctx,
            final PresetGenerator.Option option,
            final List<BlockState> blocks) {
        if (blocks.isEmpty()) {
            ctx.sendError("Nothing to generate");
            return;
        }
        final BlockPos pos = BlockPos.containing(ctx.getPos());
        blocks.forEach(block -> PresetGenerator.generate(option, ctx.getLevel(), pos, block));
        ctx.sendMessage("Successfully generated {} file(s).", blocks.size());
    }

    @ModCommand(
        description = "Variant of generate accepting a single block and a filename",
        branch = {
            @Node(name = "option", enumValue = PresetGenerator.Option.class),
            @Node(name = "block", type = CompatBlockStateArgument.class),
            @Node(name = "as"),
            @Node(name = "name", stringValue = @StringValue)
        })
    private void generateAs(
            final CommandContextWrapper ctx,
            final PresetGenerator.Option option,
            final BlockState block,
            final String name) {
        final BlockPos pos = BlockPos.containing(ctx.getPos());
        PresetGenerator.generateAs(option, ctx.getLevel(), pos, block, name);
        ctx.sendMessage("Successfully generated {}.xjs.", name);
    }

    @ModCommand(
        description = "Displays the current contents of the block list.",
        linter = GenericArrayLinter.class)
    private void listEntries(final CommandContextWrapper ctx) {
        ctx.sendLintedMessage(Arrays.toString(Cfg.blockEntries().toArray()));
    }

    @ModCommand(
        description = "Displays the current contents of a property group.",
        linter = GenericArrayLinter.class,
        branch = @Node(name = "group", type = PropertyGroupArgument.class))
    private void listProperties(final CommandContextWrapper ctx, final Group group) {
        ctx.sendLintedMessage(Arrays.toString(group.getEntries().toArray()));
    }

    @ModCommand(
        description = "Displays the current contents of a block group.",
        linter = GenericArrayLinter.class,
        branch = @Node(name = "group", type = BlockGroupArgument.class))
    private void listBlocks(final CommandContextWrapper ctx, final Group group) {
        ctx.sendLintedMessage(Arrays.toString(group.getEntries().toArray()));
    }

    @ModCommand(description = "Clears all entries from the block list.")
    private void clearEntries(final CommandContextWrapper ctx) {
        updateEntries(Collections.emptyList());
        ctx.sendMessage("Successfully cleared values. Restart to see changes.");
    }

    @ModCommand(
        description = "Clears all entries from the given property group.",
        branch = @Node(name = "group", type = PropertyGroupArgument.class))
    private void clearProperties(final CommandContextWrapper ctx, final Group group) {
        if (group.getName().equals(Group.ALL)) {
            clearAllGroups(true);
        } else if (group.getName().equals(Group.DEFAULT)) {
            clearDefaultGroups(true);
        } else {
            updateGroup(new Group(group.getName(), Collections.emptySet()), true);
        }
        ctx.sendMessage("Successfully cleared values. Restart to see changes.");
    }

    @ModCommand(
        description = "Clears all entries from the given block group.",
        branch = @Node(name = "group", type = BlockGroupArgument.class))
    private void clearBlocks(final CommandContextWrapper ctx, final Group group) {
        if (group.getName().equals(Group.ALL)) {
            clearAllGroups(false);
        } else if (group.getName().equals(Group.DEFAULT)) {
            clearDefaultGroups(false);
        } else {
            updateGroup(new Group(group.getName(), Collections.emptySet()), false);
        }
        ctx.sendMessage("Successfully cleared values. Restart to see changes.");
    }

    @ModCommand(
        description = "Removes any number of new variants on the block list.",
        linter = GenericArrayLinter.class,
        branch = {
            @Node(name = "ore", type = PropertyArgument.class, intoList = @ListInfo),
            @Node(name = "from"),
            @Node(name = "bg", type = BackgroundArgument.class, intoList = @ListInfo)
        })
    private void removeEntries(final CommandContextWrapper ctx, final List<Group> ore, final List<Group> bg) {
        final Set<BlockEntry> possible = BlockList.deconstruct(BlockList.get());
        possible.removeAll(BlockList.deconstruct(BlockList.create(ore, bg)));
        final List<String> raw = BlockList.intoRaw(BlockList.optimize(possible));
        updateEntries(raw);
        ctx.generateMessage("Updated block list:\n")
            .append(ctx.lintMessage(Arrays.toString(raw.toArray())))
            .append("\nRestart to see changes in game.")
            .sendMessage();
    }

    @ModCommand(
        description = "Deletes all entries from the given property group",
        branch = @Node(name = "group", type = PropertyGroupArgument.class))
    private void removeProperties(final CommandContextWrapper ctx, final Group group) {
        if (group.getName().equals(Group.ALL)) {
            deleteAllGroups(true);
        } else if (group.getName().equals(Group.DEFAULT)) {
            deleteDefaultGroups(true);
        } else {
            deleteGroup(group, true);
        }
        ctx.sendMessage("Successfully deleted values. Restart to see changes.");
    }

    @ModCommand(
        description = "Deletes all entries from the given block group",
        branch = @Node(name = "group", type = BlockGroupArgument.class))
    private void removeBlocks(final CommandContextWrapper ctx, final Group group) {
        if (group.getName().equals(Group.ALL)) {
            deleteAllGroups(false);
        } else if (group.getName().equals(Group.DEFAULT)) {
            deleteDefaultGroups(false);
        } else {
            deleteGroup(group,  false);
        }
        ctx.sendMessage("Successfully deleted values. Restart to see changes.");
    }

    private static boolean checkDuplicates(
            final CommandContextWrapper ctx, final Map<BlockEntry, List<VariantDescriptor>> entries) {
        final Map<VariantDescriptor, Set<BlockEntry>> duplicates = BlockList.getDuplicates(entries);
        if (!duplicates.isEmpty()) {
            ctx.sendError("Refusing to update block list. Found {} duplicates.", duplicates.size());
            for (final Map.Entry<VariantDescriptor, Set<BlockEntry>> duplicate : duplicates.entrySet()) {
                final Set<String> values = Shorthand.map(duplicate.getValue(), BlockEntry::getRaw);
                ctx.sendError("Found {} in {}", duplicate.getKey().getId(), Arrays.toString(values.toArray()));
            }
            return true;
        }
        return false;
    }

    private static void updateEntries(final List<String> entries) {
        Cfg.setBlockEntries(entries);
        HjsonUtils.updateJson(Cfg.getCommon(), json ->
            JsonPath.objectOnly("blockRegistry.values").setValue(json, HjsonUtils.anyArray(entries))
        ).expect("Could not update config file.");
        ModRegistries.BLOCK_LIST.reset();
    }

    private static void group(final CommandContextWrapper ctx, final Group updated, final boolean ore) {
        updateGroup(updated, ore);
        ctx.generateMessage("Updated group list:\n")
            .append(ctx.lintMessage(Arrays.toString(updated.getEntries().toArray())))
            .append("\nRestart to see changes.")
            .sendMessage();
    }

    private static void updateGroup(final Group group, final boolean ore) {
        if (ore) {
            Cfg.propertyGroups().put(group.getName(), new ArrayList<>(group.getEntries()));
            ModRegistries.PROPERTY_GROUPS.reset();
        } else {
            Cfg.blockGroups().put(group.getName(), new ArrayList<>(group.getEntries()));
            ModRegistries.BLOCK_GROUPS.reset();
        }
        final String path = "blockRegistry." + (ore ? "propertyGroups." : "blockGroups.") + group.getName();
        HjsonUtils.updateJson(Cfg.getCommon(), json ->
            JsonPath.objectOnly(path).setValue(json, HjsonUtils.anyArray(group.getEntries()))
        ).expect("Could not update config file.");
    }

    private static void clearAllGroups(final boolean ore) {
        if (ore) {
            for (final Map.Entry<String, List<String>> group : Cfg.propertyGroups().entrySet()) {
                group.setValue(Collections.emptyList());
            }
            ModRegistries.PROPERTY_GROUPS.reset();
        } else {
            for (final Map.Entry<String, List<String>> group : Cfg.blockGroups().entrySet()) {
                group.setValue(Collections.emptyList());
            }
            ModRegistries.BLOCK_GROUPS.reset();
        }
        HjsonUtils.updateJson(Cfg.getCommon(), json -> {
            final String path = ore ? "propertyGroups" : "blockGroups";
            final JsonObject blockRegistry = HjsonUtils.getObjectOrNew(json, "blockRegistry");
            final JsonObject groups = HjsonUtils.getObjectOrNew(blockRegistry, path);
            for (final String name : groups.names()) {
                groups.set(name, new JsonArray());
            }
        }).expect("Could not update config file.");
    }

    private static void clearDefaultGroups(final boolean ore) {
        if (ore) {
            for (final String group : DefaultOres.NAMES) {
                Cfg.propertyGroups().put(group, Collections.emptyList());
            }
            ModRegistries.PROPERTY_GROUPS.reset();
        } else {
            for (final String group : DefaultStones.NAMES) {
                Cfg.blockGroups().put(group, Collections.emptyList());
            }
            ModRegistries.BLOCK_GROUPS.reset();
        }
        HjsonUtils.updateJson(Cfg.getCommon(), json -> {
            final String path = ore ? "propertyGroups" : "blockGroups";
            final String[] names = ore ? DefaultOres.NAMES : DefaultStones.NAMES;
            final JsonObject blockRegistry = HjsonUtils.getObjectOrNew(json, "blockRegistry");
            final JsonObject groups = HjsonUtils.getObjectOrNew(blockRegistry, path);
            for (final String name : names) {
                groups.set(name, new JsonArray());
            }
        }).expect("Could not update config file.");
    }

    private static void deleteGroup(final Group group, final boolean ore) {
        if (ore) {
            Cfg.propertyGroups().remove(group.getName());
            ModRegistries.PROPERTY_GROUPS.reset();
        } else {
            Cfg.blockGroups().remove(group.getName());
            ModRegistries.BLOCK_GROUPS.reset();
        }
        final String path = "blockRegistry." + (ore ? "propertyGroups." : "blockGroups.") + group.getName();
        HjsonUtils.updateJson(Cfg.getCommon(), json ->
            JsonPath.objectOnly(path).setValue(json, null)
        ).expect("Could not update config file.");
    }

    private static void deleteAllGroups(final boolean ore) {
        if (ore) {
            Cfg.propertyGroups().clear();
            ModRegistries.PROPERTY_GROUPS.reset();
        } else {
            Cfg.blockGroups().clear();
            ModRegistries.BLOCK_GROUPS.reset();
        }
        final String path = "blockRegistry." + (ore ? "propertyGroups" : "blockGroups");
        HjsonUtils.updateJson(Cfg.getCommon(), json ->
            JsonPath.objectOnly(path).setValue(json, new JsonObject())
        ).expect("Could not update config file.");
    }

    private static void deleteDefaultGroups(final boolean ore) {
        if (ore) {
            for (final String group : DefaultOres.NAMES) {
                Cfg.propertyGroups().remove(group);
            }
            ModRegistries.PROPERTY_GROUPS.reset();
        } else {
            for (final String group : DefaultStones.NAMES) {
                Cfg.blockGroups().remove(group);
            }
            ModRegistries.BLOCK_GROUPS.reset();
        }
        HjsonUtils.updateJson(Cfg.getCommon(), json -> {
            final String path = ore ? "propertyGroups" : "blockGroups";
            final String[] names = ore ? DefaultOres.NAMES : DefaultStones.NAMES;
            final JsonObject blockRegistry = HjsonUtils.getObjectOrNew(json, "blockRegistry");
            final JsonObject groups = HjsonUtils.getObjectOrNew(blockRegistry, path);
            for (final String name : names) {
                groups.remove(name);
            }
        }).expect("Could not update config file.");
    }
}
