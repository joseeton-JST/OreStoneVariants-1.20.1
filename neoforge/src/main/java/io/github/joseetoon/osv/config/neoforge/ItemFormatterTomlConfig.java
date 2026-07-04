package io.github.joseetoon.osv.config.neoforge;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import io.github.joseetoon.genlib.util.McUtils;
import io.github.joseetoon.osv.preset.reader.ComponentReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ItemFormatterTomlConfig {

    static final String FILE_NAME = "osv-client-formatters.toml";
    private static final String CLIENT_FILE_NAME = "osv-client.toml";
    private static final List<String> LEGACY_FORMATTERS_PATH = List.of("items", "formatters");
    private static final List<String> INFO_PATH = List.of("items", "formattersFile");
    private static final String FORMATTER_ENTRY = "formatter";
    private static final String WHEN_KEY = "when";

    private ItemFormatterTomlConfig() {}

    static LinkedHashMap<String, List<Map<String, Object>>> load() {
        return load(McUtils.getConfigDir().toPath());
    }

    static LinkedHashMap<String, List<Map<String, Object>>> load(final Path configDir) {
        migrateLegacyClientToml(configDir);

        final Path file = configDir.resolve(FILE_NAME);
        if (!Files.exists(file)) {
            final LinkedHashMap<String, List<Map<String, Object>>> defaults = defaults();
            save(configDir, defaults);
            return copy(defaults);
        }

        try (CommentedFileConfig cfg = open(file)) {
            cfg.load();
            final LinkedHashMap<String, List<Map<String, Object>>> loaded = readFormatterEntries(cfg);
            if (!loaded.isEmpty()) {
                return loaded;
            }
        }

        final LinkedHashMap<String, List<Map<String, Object>>> defaults = defaults();
        save(configDir, defaults);
        return copy(defaults);
    }

    static void save(final Path configDir, final LinkedHashMap<String, List<Map<String, Object>>> formatters) {
        try {
            Files.createDirectories(configDir);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to create config directory " + configDir, e);
        }

        try (CommentedFileConfig cfg = open(configDir.resolve(FILE_NAME))) {
            final List<CommentedConfig> entries = new ArrayList<>();
            for (final Map.Entry<String, List<Map<String, Object>>> stateEntry : formatters.entrySet()) {
                for (final Map<String, Object> formatter : stateEntry.getValue()) {
                    final CommentedConfig entry = CommentedConfig.inMemory();
                    entry.set(WHEN_KEY, stateEntry.getKey());
                    for (final Map.Entry<String, Object> formatterEntry : formatter.entrySet()) {
                        if (!WHEN_KEY.equals(formatterEntry.getKey())) {
                            entry.set(formatterEntry.getKey(), formatterEntry.getValue());
                        }
                    }
                    entries.add(entry);
                }
            }
            cfg.clear();
            cfg.set(FORMATTER_ENTRY, entries);
            cfg.save();
        }
    }

    private static void migrateLegacyClientToml(final Path configDir) {
        final Path formatterFile = configDir.resolve(FILE_NAME);
        if (Files.exists(formatterFile)) {
            return;
        }

        final Path clientFile = configDir.resolve(CLIENT_FILE_NAME);
        if (!Files.exists(clientFile)) {
            return;
        }

        try (CommentedFileConfig clientCfg = open(clientFile)) {
            clientCfg.load();
            final Object raw = clientCfg.getRaw(LEGACY_FORMATTERS_PATH);
            if (!(raw instanceof UnmodifiableConfig legacyConfig)) {
                return;
            }

            final LinkedHashMap<String, List<Map<String, Object>>> migrated = readLegacyFormatterMap(legacyConfig);
            if (migrated.isEmpty()) {
                return;
            }

            save(configDir, migrated);
            clientCfg.remove(LEGACY_FORMATTERS_PATH);
            clientCfg.set(INFO_PATH, FILE_NAME);
            clientCfg.setComment(INFO_PATH, "Item display formatters moved to osv-client-formatters.toml.");
            clientCfg.save();
        }
    }

    private static LinkedHashMap<String, List<Map<String, Object>>> readLegacyFormatterMap(
        final UnmodifiableConfig legacyConfig
    ) {
        final LinkedHashMap<String, List<Map<String, Object>>> loaded = new LinkedHashMap<>();
        for (final Map.Entry<String, Object> entry : legacyConfig.valueMap().entrySet()) {
            final List<Map<String, Object>> formatters = readFormatterList(entry.getValue());
            if (!formatters.isEmpty()) {
                loaded.put(entry.getKey(), formatters);
            }
        }
        return loaded;
    }

    private static LinkedHashMap<String, List<Map<String, Object>>> readFormatterEntries(final CommentedFileConfig cfg) {
        final LinkedHashMap<String, List<Map<String, Object>>> loaded = new LinkedHashMap<>();
        final Object raw = cfg.getRaw(List.of(FORMATTER_ENTRY));
        if (!(raw instanceof List<?> entries)) {
            return loaded;
        }

        for (final Object entry : entries) {
            final LinkedHashMap<String, Object> formatter = toLinkedMap(entry);
            if (formatter == null) {
                continue;
            }

            final Object when = formatter.remove(WHEN_KEY);
            final String key = when == null ? "" : String.valueOf(when);
            loaded.computeIfAbsent(key, ignored -> new ArrayList<>()).add(formatter);
        }
        return loaded;
    }

    private static List<Map<String, Object>> readFormatterList(final Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }

        final List<Map<String, Object>> formatters = new ArrayList<>();
        for (final Object entry : list) {
            final LinkedHashMap<String, Object> formatter = toLinkedMap(entry);
            if (formatter != null) {
                formatters.add(formatter);
            }
        }
        return formatters;
    }

    private static LinkedHashMap<String, Object> toLinkedMap(final Object raw) {
        final Map<String, Object> source;
        if (raw instanceof UnmodifiableConfig config) {
            source = config.valueMap();
        } else if (raw instanceof Map<?, ?> map) {
            final LinkedHashMap<String, Object> converted = new LinkedHashMap<>();
            for (final Map.Entry<?, ?> entry : map.entrySet()) {
                converted.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            source = converted;
        } else {
            return null;
        }

        final LinkedHashMap<String, Object> linked = new LinkedHashMap<>();
        for (final Map.Entry<String, Object> entry : source.entrySet()) {
            linked.put(entry.getKey(), entry.getValue());
        }
        return linked;
    }

    private static LinkedHashMap<String, List<Map<String, Object>>> defaults() {
        final LinkedHashMap<String, List<Map<String, Object>>> defaults = new LinkedHashMap<>();
        defaults.put("dense=true", List.of(copyFormatter(ComponentReader.DEFAULT_DENSE)));
        defaults.put("", List.of(copyFormatter(ComponentReader.DEFAULT_NORMAL)));
        return defaults;
    }

    private static Map<String, Object> copyFormatter(final Map<String, Object> source) {
        return new LinkedHashMap<>(source);
    }

    private static LinkedHashMap<String, List<Map<String, Object>>> copy(
        final LinkedHashMap<String, List<Map<String, Object>>> source
    ) {
        final LinkedHashMap<String, List<Map<String, Object>>> copy = new LinkedHashMap<>();
        for (final Map.Entry<String, List<Map<String, Object>>> entry : source.entrySet()) {
            final List<Map<String, Object>> formatters = new ArrayList<>();
            for (final Map<String, Object> formatter : entry.getValue()) {
                formatters.add(new LinkedHashMap<>(formatter));
            }
            copy.put(entry.getKey(), formatters);
        }
        return copy;
    }

    private static CommentedFileConfig open(final Path file) {
        return CommentedFileConfig.builder(file, TomlFormat.instance()).sync().build();
    }
}
