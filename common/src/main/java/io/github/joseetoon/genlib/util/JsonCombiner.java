package io.github.joseetoon.genlib.util;

import org.hjson.JsonValue;
import io.github.joseetoon.genlib.command.arguments.HjsonArgument;
import io.github.joseetoon.genlib.data.JsonPath;

import static io.github.joseetoon.genlib.exception.Exceptions.runEx;

/** Used for merging JsonObject paths between json files. */
public class JsonCombiner {

    /**
     * Responsible for loading all necessary data for merging JsonObjects.
     *
     * @param from The complete path to the value being copied.
     * @param to The name of the preset being written into.
     * @param path The path of the JSON data being merged.
     */
    public static void combine(final HjsonArgument.Result from, final HjsonArgument.Result to, final JsonPath path) {
        JsonValue fromValue = HjsonUtils.getLastContainer(from.json.get(), path);
        if (fromValue.isObject()) {
            final String key = path.get(path.size() - 1).left()
                .orElseThrow(() -> runEx("Expected an object at end of path."));
            fromValue = fromValue.asObject().get(key);
        } else if (fromValue.isArray()) {
            final int index = path.get(path.size() - 1).right()
                .orElseThrow(() -> runEx("Expected an array at end of path."));
            fromValue = fromValue.asArray().get(index);
        }
        HjsonUtils.setValueFromPath(to.json.get(), path, fromValue);
        HjsonUtils.writeJson(to.json.get(), to.file);
    }
}