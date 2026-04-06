package io.github.joseetoon.osv.util;

import io.github.joseetoon.genlib.util.HjsonUtils;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

public final class JsonCompat {

    public static Optional<JsonValue> getOptional(final JsonObject object, final String key) {
        return Optional.ofNullable(object.get(key));
    }

    public static <T> Optional<T> getOptional(
            final JsonObject object, final String key, final Function<JsonValue, T> mapper) {
        final JsonValue value = object.get(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.apply(value));
    }

    public static JsonValue getAsserted(final JsonObject object, final String key) {
        final JsonValue value = object.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required key: " + key);
        }
        return value;
    }

    public static Iterable<JsonValue> intoArray(final JsonValue value) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value.isArray()) {
            final JsonArray array = value.asArray();
            return array;
        }
        return Collections.singletonList(value);
    }

    public static boolean matches(final JsonValue left, final JsonValue right) {
        return left != null && left.equals(right);
    }

    public static void setDefaults(final JsonObject target, final JsonObject defaults) {
        HjsonUtils.setRecursivelyIfAbsent(target, defaults);
    }

    private JsonCompat() {}
}
