package io.github.joseetoon.osv.config.forge;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import lombok.extern.log4j.Log4j2;
import io.github.joseetoon.genlib.util.McUtils;
import io.github.joseetoon.osv.config.ConfigProvider;
import io.github.joseetoon.osv.config.ConfigFile;
import io.github.joseetoon.osv.util.Reference;
import org.hjson.JsonObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Log4j2
public class TomlFileConfig {

    /**
     * Loads or creates a TOML config file. If an old .xjs file exists and the new .toml
     * does not, migrates from .xjs to .toml automatically.
     */
    public static CommentedFileConfig load(final boolean client) {
        final String filename = Reference.MOD_ID + (client ? "-client.toml" : "-common.toml");
        final File tomlFile = new File(McUtils.getConfigDir(), filename);

        // Attempt migration from .xjs to .toml if needed
        migrateFromOldFormat(client, tomlFile);

        // Load or create TOML config
        return loadOrCreate(tomlFile);
    }

    private static CommentedFileConfig loadOrCreate(final File tomlFile) {
        try {
            final CommentedFileConfig cfg = CommentedFileConfig.builder(tomlFile.toPath(), TomlFormat.instance())
                .sync()
                .build();
            if (tomlFile.exists()) {
                cfg.load();
            }
            return cfg;
        } catch (final Exception e) {
            log.error("Error loading TOML config {}: {}", tomlFile.getName(), e.getMessage());
            throw new RuntimeException("Failed to load TOML config: " + tomlFile.getName(), e);
        }
    }

    private static void migrateFromOldFormat(final boolean client, final File newFile) {
        if (newFile.exists()) return; // Already migrated

        final String oldFilename = Reference.MOD_ID + (client ? "-client.xjs" : "-common.xjs");
        final File oldFile = new File(McUtils.getConfigDir(), oldFilename);
        if (!oldFile.exists()) return; // No old file to migrate

        try {
            log.info("Migrating {} to TOML format...", oldFilename);
            final ConfigFile oldConfig = ConfigProvider.tryLoad(oldFile);
            if (oldConfig != null) {
                // Convert HJSON to TOML
                final CommentedFileConfig tomlConfig = CommentedFileConfig.builder(newFile.toPath(), TomlFormat.instance())
                    .sync()
                    .build();
                copyJsonToToml(oldConfig.json, tomlConfig);
                tomlConfig.save();
                log.info("Successfully migrated {} to {}", oldFilename, newFile.getName());

                // Backup old file with .bak suffix
                final File oldBackup = new File(McUtils.getConfigDir(), oldFilename + ".bak");
                Files.copy(oldFile.toPath(), oldBackup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                oldFile.delete();
                log.info("Old config backed up to {}", oldBackup.getName());
            }
        } catch (final Exception e) {
            log.warn("Failed to migrate {} to TOML. Will create new config.", oldFilename);
            log.warn("Migration error: {}", e.getMessage());
        }
    }

    private static void copyJsonToToml(final JsonObject json, final CommentedFileConfig toml) {
        for (final JsonObject.Member member : json) {
            final String key = member.getName();
            final Object value = jsonToNative(member.getValue());
            if (value != null) {
                toml.set(key, value);
            }
        }
    }

    private static Object jsonToNative(final org.hjson.JsonValue value) {
        if (value.isObject()) {
            final JsonObject obj = value.asObject();
            final java.util.LinkedHashMap<String, Object> map = new java.util.LinkedHashMap<>();
            for (final JsonObject.Member member : obj) {
                final Object val = jsonToNative(member.getValue());
                if (val != null) {
                    map.put(member.getName(), val);
                }
            }
            return map;
        } else if (value.isArray()) {
            final java.util.List<Object> list = new java.util.ArrayList<>();
            for (final org.hjson.JsonValue item : value.asArray()) {
                final Object val = jsonToNative(item);
                if (val != null) {
                    list.add(val);
                }
            }
            return list;
        } else {
            return value.asRaw();
        }
    }
}
