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

            // Wrap to normalize complex values before TOML serialization
            return new TomlConfigWrapper(cfg);
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
                // Normalize complex types before storing in TOML
                toml.set(key, normalizeForToml(value));
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

    /**
     * Normalizes values for TOML serialization.
     * NightConfig's own Config objects (sub-sections) are left as-is so the TOML
     * writer produces proper [table] sections. BUT we MUST recursively normalize
     * values INSIDE those Config objects to catch nested BiMaps, etc.
     */
    public static Object normalizeForToml(Object value) {
        if (value == null) return null;

        // NightConfig's own Config/CommentedConfig — preserve structure but normalize contents
        if (value instanceof CommentedFileConfig) {
            normalizeConfigInPlace((CommentedFileConfig) value);
            return value;
        }
        if (value instanceof com.electronwill.nightconfig.core.Config) {
            normalizeConfigInPlace((com.electronwill.nightconfig.core.Config) value);
            return value;
        }

        // Standard java.util.Map (including BiMap and other custom Map types)
        // Convert to a plain LinkedHashMap that TOML can serialize as an inline table
        if (value instanceof java.util.Map) {
            final java.util.Map<?, ?> map = (java.util.Map<?, ?>) value;
            final java.util.LinkedHashMap<String, Object> normalized = new java.util.LinkedHashMap<>();
            for (final java.util.Map.Entry<?, ?> entry : map.entrySet()) {
                normalized.put(String.valueOf(entry.getKey()), normalizeForToml(entry.getValue()));
            }
            return normalized;
        }

        // Handle Lists — recurse into each element
        if (value instanceof java.util.List) {
            final java.util.List<?> list = (java.util.List<?>) value;
            final java.util.List<Object> normalized = new java.util.ArrayList<>();
            for (final Object item : list) {
                normalized.add(normalizeForToml(item));
            }
            return normalized;
        }

        // Primitives and Strings are fine
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }

        // Enums — serialize as their name string
        if (value instanceof Enum) {
            return ((Enum<?>) value).name();
        }

        // Last resort: toString
        return value.toString();
    }

    /**
     * Recursively normalize all values INSIDE a Config object.
     * This ensures BiMaps and other unsupported types nested within Config sub-sections
     * are converted to standard java.util types before serialization.
     */
    private static void normalizeConfigInPlace(final com.electronwill.nightconfig.core.Config config) {
        final java.util.List<String> keysToUpdate = new java.util.ArrayList<>();

        // First pass: identify which values need normalization
        for (final String key : config.valueMap().keySet()) {
            final Object value = config.getRaw(java.util.Collections.singletonList(key));
            if (value != null && shouldNormalize(value)) {
                keysToUpdate.add(key);
            }
        }

        // Second pass: update values that need normalization
        for (final String key : keysToUpdate) {
            final Object original = config.getRaw(java.util.Collections.singletonList(key));
            final Object normalized = normalizeForToml(original);
            config.set(java.util.Collections.singletonList(key), normalized);
        }
    }

    /**
     * Check if a value should be normalized (is a complex/unsupported type).
     */
    private static boolean shouldNormalize(final Object value) {
        if (value == null) return false;
        if (value instanceof String || value instanceof Number || value instanceof Boolean) return false;
        if (value instanceof Enum) return false;
        if (value instanceof com.electronwill.nightconfig.core.Config) return true; // Need to check inside
        return true; // Everything else needs normalization
    }

    /**
     * Wrapper around CommentedFileConfig that normalizes complex types before save.
     */
    private static class TomlConfigWrapper implements CommentedFileConfig {
        private final CommentedFileConfig delegate;

        TomlConfigWrapper(final CommentedFileConfig delegate) {
            this.delegate = delegate;
        }

        @Override
        public <T> T set(java.util.List<String> path, Object value) {
            return delegate.set(path, normalizeForToml(value));
        }

        @Override
        public <T> T getRaw(java.util.List<String> path) {
            return delegate.getRaw(path);
        }

        @Override
        public boolean contains(java.util.List<String> path) {
            return delegate.contains(path);
        }

        @Override
        public <T> T remove(java.util.List<String> path) {
            return delegate.remove(path);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public java.util.Map<String, Object> valueMap() {
            return delegate.valueMap();
        }

        @Override
        public java.util.Set<? extends com.electronwill.nightconfig.core.CommentedConfig.Entry> entrySet() {
            return delegate.entrySet();
        }

        @Override
        public String setComment(java.util.List<String> path, String comment) {
            return delegate.setComment(path, comment);
        }

        @Override
        public String getComment(java.util.List<String> path) {
            return delegate.getComment(path);
        }

        @Override
        public boolean containsComment(java.util.List<String> path) {
            return delegate.containsComment(path);
        }

        @Override
        public String removeComment(java.util.List<String> path) {
            return delegate.removeComment(path);
        }

        @Override
        public java.util.Map<String, String> commentMap() {
            return delegate.commentMap();
        }

        @Override
        public void clearComments() {
            delegate.clearComments();
        }

        @Override
        public com.electronwill.nightconfig.core.CommentedConfig createSubConfig() {
            return delegate.createSubConfig();
        }

        @Override
        public File getFile() {
            return delegate.getFile();
        }

        @Override
        public java.nio.file.Path getNioPath() {
            return delegate.getNioPath();
        }

        @Override
        public void save() {
            // Normalize all values before saving
            normalizeAllValues(delegate);
            delegate.save();
        }

        @Override
        public void load() {
            delegate.load();
        }

        @Override
        public void close() {
            delegate.close();
        }

        @Override
        public com.electronwill.nightconfig.core.ConfigFormat<CommentedFileConfig> configFormat() {
            @SuppressWarnings("unchecked")
            final com.electronwill.nightconfig.core.ConfigFormat<CommentedFileConfig> fmt =
                (com.electronwill.nightconfig.core.ConfigFormat<CommentedFileConfig>) (Object) delegate.configFormat();
            return fmt;
        }

        @Override
        public boolean add(java.util.List<String> path, Object value) {
            return delegate.add(path, normalizeForToml(value));
        }

        /**
         * Recursively normalize ALL values in the config (including nested ones)
         * to prevent TOML serialization errors from unsupported types.
         */
        private void normalizeAllValues(final CommentedFileConfig config) {
            normalizeAllValuesRecursive(config, new java.util.ArrayList<>());
        }

        private void normalizeAllValuesRecursive(final CommentedFileConfig config, final java.util.List<String> path) {
            final java.util.List<String> keysToNormalize = new java.util.ArrayList<>();

            for (final String key : config.valueMap().keySet()) {
                final java.util.List<String> keyPath = new java.util.ArrayList<>(path);
                keyPath.add(key);
                final Object value = config.getRaw(keyPath);

                // Recurse into nested Config objects
                if (value instanceof CommentedFileConfig) {
                    normalizeAllValuesRecursive((CommentedFileConfig) value, keyPath);
                } else if (value instanceof com.electronwill.nightconfig.core.Config) {
                    // Generic Config (not CommentedFileConfig)
                    normalizeAllValuesRecursive((com.electronwill.nightconfig.core.Config) value, keyPath);
                } else if (value != null && !(value instanceof String || value instanceof Number || value instanceof Boolean)) {
                    // This value needs normalization
                    keysToNormalize.add(key);
                }
            }

            // Normalize all problematic values at this level
            for (final String key : keysToNormalize) {
                final java.util.List<String> keyPath = new java.util.ArrayList<>(path);
                keyPath.add(key);
                final Object original = config.getRaw(keyPath);
                config.set(keyPath, normalizeForToml(original));
            }
        }

        private void normalizeAllValuesRecursive(final com.electronwill.nightconfig.core.Config config,
                                                  final java.util.List<String> path) {
            final java.util.List<String> keysToNormalize = new java.util.ArrayList<>();

            for (final String key : config.valueMap().keySet()) {
                final java.util.List<String> keyPath = new java.util.ArrayList<>(path);
                keyPath.add(key);
                final Object value = config.getRaw(keyPath);

                // Recurse into nested Config objects
                if (value instanceof CommentedFileConfig) {
                    normalizeAllValuesRecursive((CommentedFileConfig) value, keyPath);
                } else if (value instanceof com.electronwill.nightconfig.core.Config) {
                    normalizeAllValuesRecursive((com.electronwill.nightconfig.core.Config) value, keyPath);
                } else if (value != null && !(value instanceof String || value instanceof Number || value instanceof Boolean)) {
                    keysToNormalize.add(key);
                }
            }

            // Normalize all problematic values at this level
            for (final String key : keysToNormalize) {
                final java.util.List<String> keyPath = new java.util.ArrayList<>(path);
                keyPath.add(key);
                final Object original = config.getRaw(keyPath);
                config.set(keyPath, normalizeForToml(original));
            }
        }
    }

}
