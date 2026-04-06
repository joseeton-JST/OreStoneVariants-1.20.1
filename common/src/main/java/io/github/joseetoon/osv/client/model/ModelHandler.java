package io.github.joseetoon.osv.client.model;

import lombok.extern.log4j.Log4j2;
import io.github.joseetoon.genlib.event.error.LibErrorContext;
import io.github.joseetoon.genlib.exception.FormattedIOException;
import io.github.joseetoon.genlib.exception.GenericFormattedException;
import io.github.joseetoon.genlib.io.FileIO;
import io.github.joseetoon.genlib.util.PathUtils;
import personthecat.fresult.Result;
import io.github.joseetoon.osv.client.ClientResourceHelper;
import io.github.joseetoon.osv.client.texture.TextureHandler;
import io.github.joseetoon.osv.config.Cfg;
import io.github.joseetoon.osv.config.VariantDescriptor;
import io.github.joseetoon.osv.io.ModFolders;
import io.github.joseetoon.osv.io.ResourceHelper;
import io.github.joseetoon.osv.preset.data.ModelSettings;
import io.github.joseetoon.osv.util.Reference;
import org.hjson.JsonObject;
import org.hjson.Stringify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Log4j2
public class ModelHandler {

    private static final String OVERLAY_TEMPLATE_PATH = "assets/osv/overlay_template.txt";
    private static final String OVERLAY_MODEL_PATH = "assets/osv/models/block/overlay.json";
    private static final int MAX_BACKUPS = 10;
    private static final int WARN_BACKUPS = 8;
    private static final String FALLBACK_OVERLAY_TEMPLATE = """
        {
          "parent": "block/block",
          "elements": [
            {
              "from": [ {min}, {min}, {min} ],
              "to": [ {max}, {max}, {max} ],
              "shade": {shade},
              "faces": {
                "down": { "texture": "#overlay", "cullface": "down" },
                "up": { "texture": "#overlay", "cullface": "up" },
                "north": { "texture": "#overlay", "cullface": "north" },
                "south": { "texture": "#overlay", "cullface": "south" },
                "west": { "texture": "#overlay", "cullface": "west" },
                "east": { "texture": "#overlay", "cullface": "east" }
              }
            }
          ]
        }
        """;

    /**
     * Moves existing resources to the backup directory. Deletes any folders more
     * than 10 versions old.
     */
    public static void primeForRegen() {
        final File assets = new File(ModFolders.RESOURCE_DIR, "assets");
        if (assets.exists()) {
            final File backups = Reference.MOD.getBackupFolder();
            Result.suppress(() -> FileIO.backup(backups, assets, false))
                .ifErr(e -> {
                    log.error("Setting up model regen", e);
                    LibErrorContext.error(Reference.MOD,
                        new FormattedIOException(assets, e, "Error creating backups for model regen"));
                })
                .ifOk(count -> warnBackups(backups, assets, count));
        }
    }

    /**
     * Handles the edge case where a user has updated their settings many times
     * without deleting old backups. Will inform the user if too many backups
     * are present in the backup directory.
     *
     * @param backups The directory where backups are being stored.
     * @param assets  The assets file, recently backed up.
     * @param count   The number of backups mode of this file.
     */
    private static void warnBackups(final File backups, final File assets, final int count) {
        if (count >= MAX_BACKUPS) {
            FileIO.truncateBackups(backups, assets, 10);
            log.warn("Assets have been backed up > {} times. Deleting extras...", MAX_BACKUPS);
        } else if (count >= WARN_BACKUPS) {
            log.warn("Assets have been backed up > {} times. They will be deleted soon.", WARN_BACKUPS);
        }
    }

    /**
     * Constructs the overlay model to be used by any ore variant that needs it. The
     * generated file can be edited by the user, but it will come with defaults which
     * are ultimately defined in the config file.
     */
    public static void generateOverlayModel() {
        if (!ClientResourceHelper.hasResource(OVERLAY_MODEL_PATH)) {
            final double offset = ((Cfg.overlayScale() * 16.0) - 16.0) / 2.0;
            final String overlay = loadOverlayTemplate()
                .replace("{min}", String.valueOf(0.0 - offset))
                .replace("{max}", String.valueOf(16.0 + offset))
                .replace("{shade}", String.valueOf(Cfg.overlayShade()));
            writeModel(OVERLAY_MODEL_PATH, overlay);
        }
    }

    private static String loadOverlayTemplate() {
        return ResourceHelper.getResource(OVERLAY_TEMPLATE_PATH)
            .map(ModelHandler::readUtf8)
            .orElseGet(() -> {
                log.warn("Could not find {} in runtime resources; using fallback template.", OVERLAY_TEMPLATE_PATH);
                return FALLBACK_OVERLAY_TEMPLATE;
            });
    }

    private static String readUtf8(final InputStream is) {
        try (is) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            log.warn("Could not read {}; using fallback template.", OVERLAY_TEMPLATE_PATH, e);
            return FALLBACK_OVERLAY_TEMPLATE;
        }
    }

    /**
     * Generates <b>every</b> model required for this block entry, including its block
     * state model, block models, and item models. In addition, this method is ultimately
     * responsible for generating overlays for this ore type (if applicable), as well as
     * any single layer textures needed.
     *
     * @param variant Information about a single foreground / background pair.
     */
    public static void generateModels(final VariantDescriptor variant) {
        final String blockStatePath = PathUtils.asBlockStatePath(variant.getId());
        if (shouldGenerate(blockStatePath)) {
            if (!TextureHandler.overlaysGenerated(variant.getForeground())) {
                TextureHandler.generateOverlays(variant.getForeground());
            }
            runGenerator(blockStatePath, variant);
        }
    }

    private static boolean shouldGenerate(final String blockStatePath) {
        if (!ClientResourceHelper.hasResource(blockStatePath)) {
            return true;
        }
        final File existing = ResourceHelper.file(blockStatePath);
        if (!existing.isFile()) {
            return false;
        }
        if (!isStrictJson(existing)) {
            log.info("Detected non-JSON model data at {}. Regenerating.", blockStatePath);
            return true;
        }
        return false;
    }

    private static boolean isStrictJson(final File file) {
        try {
            final String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            com.google.gson.JsonParser.parseString(text);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Executes the generator for this block entry after all checks have passed.
     *
     * @param blockStatePath The generated path to this variant's block state JSON.
     * @param variant Information about a single foreground / background pair.
     */
    private static void runGenerator(final String blockStatePath, final VariantDescriptor variant) {
        final ModelSettings cfg = variant.getForeground().getModel();
        final ModelGenerator gen = cfg.getType().getGenerator();

        try {
            gen.generateModels(variant, blockStatePath, ModelHandler::writeModel);
        } catch (final RuntimeException e) {
            LibErrorContext.error(Reference.MOD, new GenericFormattedException(e));
        }
    }

    /**
     * Writes a new JSON object to the /resources directory as a formatted string.
     *
     * @param concretePath The raw, relative path to this model.
     * @param json The JSON object being serialized.
     */
    private static void writeModel(final String concretePath, final JsonObject json) {
        writeModel(concretePath, json.toString(Stringify.FORMATTED));
    }

    /**
     * Writes a string to the /resources directory.
     *
     * @param concretePath The raw, relative path to this file.
     * @param json Any string data (which will always be a JSON).
     */
    private static void writeModel(final String concretePath, final String json) {
        ResourceHelper.writeResource(concretePath, json).ifErr(e ->
            LibErrorContext.error(Reference.MOD, new GenericFormattedException(e)));
    }
}
