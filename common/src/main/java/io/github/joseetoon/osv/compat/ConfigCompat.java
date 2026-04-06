package io.github.joseetoon.osv.compat;

import io.github.joseetoon.genlib.util.JsonTransformer;
import io.github.joseetoon.osv.compat.transformer.ClientConfigTransformers;
import io.github.joseetoon.osv.compat.transformer.CommonConfigTransformers;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.LinkedHashSet;
import java.util.Set;

public class ConfigCompat {

    private static final String LEGACY_BYG = "byg";
    private static final String MODERN_BWG = "biomeswevegone";

    public static final JsonTransformer.ObjectResolver COMMON_CONFIG_TRANSFORMER =
        JsonTransformer.root()
            .include(CommonConfigTransformers.BLOCK_REGISTRY)
            .include(CommonConfigTransformers.WORLD_GEN)
            .include(CommonConfigTransformers.MOD_SUPPORT)
            .freeze();

    public static final JsonTransformer.ObjectResolver CLIENT_CONFIG_TRANSFORMER =
        JsonTransformer.root()
            .include(ClientConfigTransformers.ROOT)
            .include(ClientConfigTransformers.RESOURCES)
            .freeze();

    public static void transformCommonConfig(final JsonObject config) {
        COMMON_CONFIG_TRANSFORMER.updateAll(config);
        migrateBygToBiomesWeveGone(config);
    }

    public static void transformClientConfig(final JsonObject config) {
        CLIENT_CONFIG_TRANSFORMER.updateAll(config);
    }

    private static void migrateBygToBiomesWeveGone(final JsonObject config) {
        final JsonValue blockRegistryValue = config.get("blockRegistry");
        if (blockRegistryValue == null || !blockRegistryValue.isObject()) {
            return;
        }
        final JsonObject blockRegistry = blockRegistryValue.asObject();
        migrateBlockGroups(blockRegistry);
        migrateBlockEntryValues(blockRegistry);
    }

    private static void migrateBlockGroups(final JsonObject blockRegistry) {
        final JsonValue groupsValue = blockRegistry.get("blockGroups");
        if (groupsValue == null || !groupsValue.isObject()) {
            return;
        }
        final JsonObject groups = groupsValue.asObject();
        final JsonArray oldGroup = getArray(groups, LEGACY_BYG);
        final JsonArray currentGroup = getArray(groups, MODERN_BWG);

        if (oldGroup == null && currentGroup == null) {
            return;
        }

        final Set<String> merged = new LinkedHashSet<>();
        addAll(merged, currentGroup);
        addAll(merged, oldGroup);
        if (!merged.isEmpty()) {
            groups.set(MODERN_BWG, toArray(merged));
        }
        groups.remove(LEGACY_BYG);
    }

    private static void migrateBlockEntryValues(final JsonObject blockRegistry) {
        final JsonValue valuesValue = blockRegistry.get("values");
        if (valuesValue == null || !valuesValue.isArray()) {
            return;
        }
        final JsonArray values = valuesValue.asArray();
        final JsonArray migrated = new JsonArray();
        for (final JsonValue value : values) {
            if (value != null && value.isString()) {
                migrated.add(rewriteLegacyBygToken(value.asString()));
            } else {
                migrated.add(value);
            }
        }
        blockRegistry.set("values", migrated);
    }

    private static void addAll(final Set<String> out, final JsonArray array) {
        if (array == null) {
            return;
        }
        for (final JsonValue value : array) {
            if (value != null && value.isString()) {
                out.add(rewriteLegacyBygToken(value.asString()));
            }
        }
    }

    private static String rewriteLegacyBygToken(final String value) {
        if (value.equals(LEGACY_BYG)) {
            return MODERN_BWG;
        }
        if (value.startsWith(LEGACY_BYG + ":")) {
            return MODERN_BWG + value.substring(LEGACY_BYG.length());
        }
        return value;
    }

    private static JsonArray toArray(final Set<String> values) {
        final JsonArray array = new JsonArray();
        for (final String value : values) {
            array.add(value);
        }
        return array;
    }

    private static JsonArray getArray(final JsonObject object, final String key) {
        final JsonValue value = object.get(key);
        return value != null && value.isArray() ? value.asArray() : null;
    }
}
