package io.github.joseetoon.genlib.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.neoforged.fml.ModContainer;
import io.github.joseetoon.genlib.event.error.Severity;
import io.github.joseetoon.genlib.util.LibReference;
import io.github.joseetoon.genlib.util.McUtils;
import personthecat.overwritevalidator.annotations.InheritMissingMembers;
import personthecat.overwritevalidator.annotations.Overwrite;
import personthecat.overwritevalidator.annotations.OverwriteClass;

import java.util.Arrays;
import java.util.List;

@OverwriteClass
@InheritMissingMembers
public class LibConfig {

    private static final String FILENAME = LibReference.MOD_ID + ".toml";
    private static final CommentedFileConfig COMMON_CFG = loadConfig();

    static {
        ensureDefaults();
    }

    @Overwrite
    public static boolean enableGlobalLibCommands() {
        return getBoolean("general.enableGlobalLibCommands", false);
    }

    @Overwrite
    public static Severity errorLevel() {
        final String value = getString("general.errorLevel", Severity.ERROR.name());
        try {
            return Severity.valueOf(value);
        } catch (final IllegalArgumentException ignored) {
            return Severity.ERROR;
        }
    }

    @Overwrite
    public static boolean wrapText() {
        return getBoolean("general.wrapText", true);
    }

    @Overwrite
    public static int displayLength() {
        final int value = getInt("general.displayLength", 35);
        return Math.max(0, Math.min(100, value));
    }

    public static boolean debugStartup() {
        return getBoolean("general.debugStartup", false);
    }

    public static void register(final ModContainer ctx) {
        ensureDefaults();
    }

    private static CommentedFileConfig loadConfig() {
        final CommentedFileConfig cfg = CommentedFileConfig.builder(
                McUtils.getConfigDir().toPath().resolve(FILENAME),
                TomlFormat.instance()
            )
            .sync()
            .build();
        if (cfg.getFile().exists()) {
            cfg.load();
        }
        return cfg;
    }

    private static synchronized void ensureDefaults() {
        boolean changed = false;
        changed |= define("general.enableGlobalLibCommands", false,
            "Whether to enable this library's provided commands as regular commands.");
        changed |= define("general.errorLevel", Severity.ERROR.name(),
            "The minimum error level to display in the error menu.");
        changed |= define("general.wrapText", true,
            "Whether to wrap text on the error detail page. Hit W or space to toggle in game.");
        changed |= define("general.displayLength", 35,
            "How many lines for the display command to render in the chat before opening a",
            "dedicated screen. Set this to 0 to always open a screen.");
        changed |= define("general.debugStartup", false,
            "Enables debug timing logs for GenLib startup and registry update hooks.");

        if (changed) {
            COMMON_CFG.save();
        }
    }

    private static boolean define(final String path, final Object value, final String... comments) {
        final List<String> split = split(path);
        boolean changed = false;
        if (!COMMON_CFG.contains(split)) {
            COMMON_CFG.set(split, value);
            changed = true;
        }
        final String comment = String.join("\n", comments);
        final String existing = COMMON_CFG.getComment(split);
        if (existing == null || !existing.equals(comment)) {
            COMMON_CFG.setComment(split, comment);
            changed = true;
        }
        return changed;
    }

    private static boolean getBoolean(final String path, final boolean defaultValue) {
        final Object raw = COMMON_CFG.getRaw(split(path));
        return raw instanceof Boolean value ? value : defaultValue;
    }

    private static int getInt(final String path, final int defaultValue) {
        final Object raw = COMMON_CFG.getRaw(split(path));
        return raw instanceof Number value ? value.intValue() : defaultValue;
    }

    private static String getString(final String path, final String defaultValue) {
        final Object raw = COMMON_CFG.getRaw(split(path));
        return raw instanceof String value ? value : defaultValue;
    }

    private static List<String> split(final String path) {
        return Arrays.asList(path.split("\\."));
    }
}
