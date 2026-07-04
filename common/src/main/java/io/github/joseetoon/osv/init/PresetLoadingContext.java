package io.github.joseetoon.osv.init;

import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.data.SafeRegistry;
import io.github.joseetoon.genlib.event.error.LibErrorContext;
import io.github.joseetoon.genlib.event.registry.CommonRegistries;
import io.github.joseetoon.genlib.io.FileIO;
import io.github.joseetoon.genlib.util.HjsonUtils;
import io.github.joseetoon.osv.compat.PresetCompat;
import io.github.joseetoon.osv.exception.PresetLoadException;
import io.github.joseetoon.osv.io.JarFiles;
import io.github.joseetoon.osv.io.ModFolders;
import io.github.joseetoon.osv.mixin.BlockBehaviourAccessor;
import io.github.joseetoon.osv.mixin.BlockPropertiesAccessor;
import io.github.joseetoon.osv.preset.OrePreset;
import io.github.joseetoon.osv.preset.StonePreset;
import io.github.joseetoon.osv.util.Reference;
import io.github.joseetoon.osv.util.RlUtils;
import io.github.joseetoon.osv.util.VariantNamingService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static java.util.Optional.empty;
import static io.github.joseetoon.genlib.util.PathUtils.extension;
import static io.github.joseetoon.genlib.util.PathUtils.noExtension;

// Todo: on ore loaded: generate textures, etc
//   figure out if this ore loaded late && generated textures
//     if so, schedule refresh?

@Log4j2
public class PresetLoadingContext {

    private static final SafeRegistry<String, File> ORES =
        SafeRegistry.of(() -> collectPresets(ModFolders.ORE_DIR)).canBeReset(true);

    private static final SafeRegistry<String, File> STONE =
        SafeRegistry.of(() -> collectPresets(ModFolders.STONE_DIR)).canBeReset(true);

    private static final Context CTX = new Context();

    private PresetLoadingContext() {}

    public static void runTransformations() {
        ORES.forEach(file -> HjsonUtils.readSuppressing(file).ifPresent(json ->
            PresetCompat.transformOrePreset(file, json)));
        STONE.forEach(file -> HjsonUtils.readSuppressing(file).ifPresent(json ->
            PresetCompat.transformStonePreset(file, json)));
    }

    private static Map<String, File> collectPresets(final File dir) {
        final Map<String, File> files = new HashMap<>();
        for (final File file : FileIO.listFilesRecursive(dir)) {
            if (isPreset(file)) {
                files.put(noExtension(file), file);
            }
        }
        return files;
    }

    public static boolean isPreset(final File file) {
        return !JarFiles.isSpecialFile(file.getName()) && Reference.VALID_EXTENSIONS.contains(extension(file));
    }

    public static Optional<OrePreset> loadOre(final String path) {
        if (CTX.availableOres.contains(path)) {
            return Optional.ofNullable(CTX.outputOres.get(path));
        }
        synchronized (CTX) {
            final Optional<OrePreset> ore = createOre(path);
            CTX.availableOres.add(path);
            ore.ifPresent(o -> CTX.outputOres.put(path, o));
            return ore;
        }
    }

    private static Optional<OrePreset> createOre(final String path) {
        final ResourceLocation asId = ResourceLocation.parse(path);

        // Skip ores whose source block is not in the registry (mod not installed).
        // OSV's own custom ores (namespace == MOD_ID) are exempt from this check.
        // Use toString().startsWith() instead of asId.getNamespace() (m_135827_) which
        // Loom fails to remap in this file, causing NoSuchMethodError at runtime.
        if (!asId.toString().startsWith(Reference.MOD_ID + ":")
                && CommonRegistries.BLOCKS.lookup(asId) == null) {
            log.debug("Skipping ore preset for {} — block not in registry (mod not installed?)", asId);
            return empty();
        }

        final String normalized = VariantNamingService.formatFg(asId);
        final File file = ORES.get(normalized);

        final Optional<OrePreset> ore;
        if (file != null) {
            ore = createFromFile(file);
        } else {
            generateTemplateFile(asId, normalized);
            ore = Optional.of(OrePreset.createDynamic(asId, normalized));
        }
        ore.ifPresent(o -> onOreCreated(file, path, o));
        return ore;
    }

    private static void generateTemplateFile(final ResourceLocation id, final String normalized) {
        // Don't generate templates for OSV's own custom ores.
        if (Reference.MOD_ID.equals(RlUtils.ns(id))) return;

        final File output = new File(ModFolders.ORE_DIR, RlUtils.ns(id) + "/" + normalized + ".hjson");
        if (output.exists()) return;

        // Read destroyTime and explosionResistance from the actual block if available.
        final Block block = CommonRegistries.BLOCKS.lookup(id);
        String destroyTime = "3.0";
        String explosionResistance = "3.0";
        if (block != null) {
            final BlockPropertiesAccessor props = (BlockPropertiesAccessor) ((BlockBehaviourAccessor) block).getProperties();
            destroyTime = String.valueOf(props.getDestroyTime());
            explosionResistance = String.valueOf(props.getExplosionResistance());
        }

        final String content = String.format(
            "// Auto-generated template for %s\n" +
            "// This file was created because the ore is in blockRegistry but had no preset.\n" +
            "// Edit and restart the game to apply your changes.\n" +
            "// Fields left as-is are already read automatically from the original block.\n" +
            "{\n" +
            "  variant: {\n" +
            "    original: %s\n" +
            "  }\n" +
            "\n" +
            "  // Block hardness / blast resistance (auto-detected from the block above).\n" +
            "  // Uncomment and edit to override:\n" +
            "  // block: {\n" +
            "  //   destroyTime: %s\n" +
            "  //   explosionResistance: %s\n" +
            "  // }\n" +
            "\n" +
            "  // Smelting recipe. Set to  recipe: none  to disable.\n" +
            "  // result and xp are read from the original recipe automatically if omitted.\n" +
            "  // recipe: {\n" +
            "  //   result: %s\n" +
            "  //   xp: 0.7\n" +
            "  // }\n" +
            "\n" +
            "  // Generation settings.\n" +
            "  // enabled=false keeps passive mode (vanilla/mod worldgen + OSV visual swap only).\n" +
            "  // Set enabled=true only if you want this preset to use custom OSV spawning.\n" +
            "  // Features can still be omitted and auto-resolved from the original ore.\n" +
            "  // gen: {\n" +
            "  //   enabled: true\n" +
            "  //   features: [\n" +
            "  //     {\n" +
            "  //       height: [ 0, 64 ]  // Y range. Supports negatives for deepslate.\n" +
            "  //       size: 9            // Blocks per vein.\n" +
            "  //       count: 4           // Attempts per chunk.\n" +
            "  //     }\n" +
            "  //   ]\n" +
            "  // }\n" +
            "}\n",
            id, id, destroyTime, explosionResistance, RlUtils.path(id).replace("_ore", "")
        );

        try {
            output.getParentFile().mkdirs();
            Files.write(output.toPath(), content.getBytes(StandardCharsets.UTF_8));
            log.info("Generated template preset for {} at {}", id, output.getPath());
        } catch (final IOException e) {
            log.warn("Could not write template preset for {}: {}", id, e.getMessage());
        }
    }

    private static Optional<OrePreset> createFromFile(final File file) {
        try {
            return OrePreset.fromFile(file);
        } catch (final PresetLoadException e) {
            log.error("Error loading ore preset: {}", file.getName(), e);
            LibErrorContext.error(Reference.MOD, e);
        }
        return empty();
    }

    private static void onOreCreated(@Nullable final File file, final String name, final OrePreset ore) {
        final ResourceLocation id = ore.getOreId();
        if (file == null) {
            log.info("Ore preset {} is loading dynamically.", name);
        } else if (id != null) {
            log.info("Ore preset {} is enabled with background {}.", name, id);
        } else {
            log.info("Ore preset {} is enabled.", name);
        }
    }

    public static Map<String, OrePreset> getOres() {
        return ImmutableMap.copyOf(CTX.outputOres);
    }

    public static Map<String, StonePreset> getStones() {
        if (CTX.outputStones.isEmpty()) loadStones();
        return ImmutableMap.copyOf(CTX.outputStones);
    }

    public static void loadStones() {
        synchronized (CTX) {
            STONE.reload().forEach((name, file) -> {
                try {
                    StonePreset.fromFile(file).ifPresent(stone -> CTX.outputStones.put(name, stone));
                } catch (final PresetLoadException e) {
                    log.error("Error loading stone preset: {}", file.getName(), e);
                    LibErrorContext.error(Reference.MOD, e);
                }
            });
        }
    }

    public static void reloadOres() {
        synchronized (CTX) {
            CTX.outputOres.clear();
            ORES.reload().forEach((name, file) -> {
                try {
                    OrePreset.fromFile(file).ifPresent(ore -> CTX.outputOres.put(name, ore));
                } catch (final PresetLoadException ignored) {}
            });
        }
    }

    public static void reloadStones() {
        synchronized (CTX) {
            CTX.outputStones.clear();
            STONE.reload().forEach((name, file) -> {
                try {
                    StonePreset.fromFile(file).ifPresent(stone -> CTX.outputStones.put(name, stone));
                } catch (final PresetLoadException ignored) {}
            });
        }
    }

    private static class Context {
        final Set<String> availableOres = new HashSet<>();
        final Map<String, OrePreset> outputOres = new HashMap<>();
        final Map<String, StonePreset> outputStones = new HashMap<>();
    }
}
