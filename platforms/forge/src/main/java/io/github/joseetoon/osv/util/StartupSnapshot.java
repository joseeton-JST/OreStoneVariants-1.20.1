package io.github.joseetoon.osv.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.fml.ModList;
import io.github.joseetoon.genlib.util.McUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tracks what the mod list looked like on the last successful startup.
 *
 * When debugStartup is enabled in osv-common.toml:
 *   - At launch: reads the saved snapshot and logs added/removed/updated mods.
 *   - After FMLLoadCompleteEvent: saves the current mod list as the new snapshot.
 *
 * The snapshot is only saved after a crash-free startup, so it always reflects
 * the last known-good state.
 */
@Log4j2
public final class StartupSnapshot {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private StartupSnapshot() {}

    private static File getSnapshotFile() {
        return new File(McUtils.getConfigDir(), "osv/debug/startup_snapshot.json");
    }

    /** Reads the last snapshot, compares with the current mod list, and logs differences. */
    public static void logDiff() {
        final File file = getSnapshotFile();
        if (!file.exists()) {
            log.info("[OSV Debug] No previous startup snapshot found. One will be saved after this run completes successfully.");
            return;
        }
        try {
            final String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            final JsonObject snapshot = JsonParser.parseString(content).getAsJsonObject();

            final String timestamp = snapshot.has("timestamp") ? snapshot.get("timestamp").getAsString() : "unknown";
            log.info("[OSV Debug] Comparing against last successful startup: {}", timestamp);

            final Map<String, String> previous = readModMap(snapshot);
            final Map<String, String> current = currentModMap();

            boolean anyChange = false;
            for (final Map.Entry<String, String> entry : current.entrySet()) {
                if (!previous.containsKey(entry.getKey())) {
                    log.info("[OSV Debug]  + ADDED   {} ({})", entry.getKey(), entry.getValue());
                    anyChange = true;
                } else if (!previous.get(entry.getKey()).equals(entry.getValue())) {
                    log.info("[OSV Debug]  ~ UPDATED {} ({} -> {})", entry.getKey(), previous.get(entry.getKey()), entry.getValue());
                    anyChange = true;
                }
            }
            for (final String id : previous.keySet()) {
                if (!current.containsKey(id)) {
                    log.info("[OSV Debug]  - REMOVED {} ({})", id, previous.get(id));
                    anyChange = true;
                }
            }
            if (!anyChange) {
                log.info("[OSV Debug] Mod list is identical to last successful startup.");
            }
        } catch (final Exception e) {
            log.warn("[OSV Debug] Could not read startup snapshot: {}", e.getMessage());
        }
    }

    /** Saves the current mod list as the new snapshot. Call only after a successful load. */
    public static void save() {
        final File file = getSnapshotFile();
        try {
            file.getParentFile().mkdirs();

            final JsonObject root = new JsonObject();
            root.addProperty("timestamp", LocalDateTime.now().format(FMT));
            root.addProperty("osvVersion", getOsvVersion());

            final JsonArray mods = new JsonArray();
            for (final Map.Entry<String, String> entry : currentModMap().entrySet()) {
                final JsonObject mod = new JsonObject();
                mod.addProperty("id", entry.getKey());
                mod.addProperty("version", entry.getValue());
                mods.add(mod);
            }
            root.add("mods", mods);

            Files.write(file.toPath(), GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
            log.info("[OSV Debug] Startup snapshot saved ({} mods).", mods.size());
        } catch (final IOException e) {
            log.warn("[OSV Debug] Could not save startup snapshot: {}", e.getMessage());
        }
    }

    private static Map<String, String> readModMap(final JsonObject snapshot) {
        final Map<String, String> map = new LinkedHashMap<>();
        if (snapshot.has("mods") && snapshot.get("mods").isJsonArray()) {
            for (final JsonElement el : snapshot.getAsJsonArray("mods")) {
                if (el.isJsonObject()) {
                    final JsonObject mod = el.getAsJsonObject();
                    map.put(mod.get("id").getAsString(), mod.get("version").getAsString());
                }
            }
        }
        return map;
    }

    private static Map<String, String> currentModMap() {
        final Map<String, String> map = new LinkedHashMap<>();
        ModList.get().getMods().forEach(info ->
            map.put(info.getModId(), info.getVersion().toString()));
        return map;
    }

    private static String getOsvVersion() {
        return ModList.get().getMods().stream()
            .filter(info -> Reference.MOD_ID.equals(info.getModId()))
            .map(info -> info.getVersion().toString())
            .findFirst().orElse("unknown");
    }
}
