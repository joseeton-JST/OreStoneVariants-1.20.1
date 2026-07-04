package io.github.joseetoon.genlib.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.utils.FakeCommentedConfig;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import io.github.joseetoon.genlib.util.Shorthand;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static io.github.joseetoon.genlib.util.HjsonUtils.readJson;
import static io.github.joseetoon.genlib.util.HjsonUtils.writeJson;

/**
 * Contains the necessary procedures for handling a Forge-friendly configuration
 * based on Hjson, a human-friendly variant of JSON.
 * <p>
 *   One of the benefits of using Hjson over the Forge-preferred TOML is that string arrays
 *   in Hjson do not require double quotes. This provides a setup that a bit more similar
 *   to the older config formats used prior to MC 1.13.
 * </p>
 */
public class HjsonFileConfig implements CommentedFileConfig  {

    /** The main file which stores all data represented by this config. */
    private final File file;

    /** The primary collection of values. */
    private final Map<String, Object> map;

    /** Comments are stored separately to be consistent with CommentedFileConfig's spec. */
    private final Map<String, String> comments;

    /** Data about this config's state for concurrency. */
    private volatile boolean writing, closed;

    /** Constructs a new instance solely from the path to this config file. */
    public HjsonFileConfig(String path) {
        this(new File(path));
    }

    /** Constructs a new instance from the object representing this config file. */
    public HjsonFileConfig(File file) {
        this(file, loadContainer(file));
    }

    /** Constructs a new instance from an existing JSON object. */
    public HjsonFileConfig(File file, JsonObject json) {
        this(file, getContainer(file, json));
    }

    /** Constructs a new instance with data that have been previously loaded. */
    private HjsonFileConfig(File file, Container container) {
        this.file = file;
        this.map = container.map;
        this.comments = container.comments;
    }

    /** Converts all of this config's data into an Hjson object. */
    private JsonObject toHjson() {
        JsonObject json = new JsonObject();
        for (String key : map.keySet()) {
            final JsonValue value = toHjson(map.get(key));
            Shorthand.getOptional(comments, key).ifPresent(value::setComment);
            json.set(key, value);
        }
        return json;
    }

    /** Converts a raw value into an Hjson value. */
    private static JsonValue toHjson(Object o) {
        return o instanceof HjsonFileConfig
            ? ((HjsonFileConfig) o).toHjson()
            : JsonValue.valueOf(o);
    }

    /** Reads the json from the disk and parses its data into an ElectronWill-friendly format. */
    private static Container loadContainer(File file) {
        return getContainer(file, readJson(file).orElse(new JsonObject()));
    }

    /** Converts the input Hjson data into an ElectronWill-friendly format. */
    private static Container getContainer(File file, JsonObject json) {
        final Container container = new Container();
        for (JsonObject.Member member : json) {
            put(file, container, member.getName(), member.getValue());
        }
        return container;
    }

    /** Puts the JsonObject's raw value and comments into the container. */
    private static void put(File file, Container container, String key, JsonValue value) {
        container.map.put(key, toRaw(file, value));
        container.comments.put(key, value.getBOLComment());
    }

    /** Converts an Hjson value into its raw counterpart. */
    private static Object toRaw(File file, JsonValue value) {
        return value.isObject()
            ? toConfig(file, value.asObject())
            : value.asRaw();
    }

    /** Converts the input Hjson object into a configuration. */
    private static HjsonFileConfig toConfig(File file, JsonObject object) {
        return new HjsonFileConfig(file, getContainer(file, object));
    }

    private HjsonFileConfig newSubConfig() {
        return new HjsonFileConfig(this.file, new Container());
    }

    /** Returns the second-to-last object in the path, or null if any parent node is missing. */
    private HjsonFileConfig getLastConfig(List<String> path) {
        HjsonFileConfig config = this;
        for (int i = 0; i < path.size() - 1; i++) {
            final Object next = config.map.get(path.get(i));
            if (!(next instanceof HjsonFileConfig child)) {
                return null;
            }
            config = child;
        }
        return config;
    }

    /** Returns the second-to-last object in the path, creating missing parent sub-configs. */
    private HjsonFileConfig getOrCreateLastConfig(List<String> path) {
        HjsonFileConfig config = this;
        for (int i = 0; i < path.size() - 1; i++) {
            final String key = path.get(i);
            final Object next = config.map.get(key);
            if (next instanceof HjsonFileConfig child) {
                config = child;
                continue;
            }
            final HjsonFileConfig child = config.newSubConfig();
            config.map.put(key, child);
            config = child;
        }
        return config;
    }

    /** Returns the last element of the input array. */
    private static String endOfPath(List<String> path) {
        return path.get(path.size() - 1);
    }

    @Override
    public String setComment(List<String> path, String comment) {
        return getOrCreateLastConfig(path).comments.put(endOfPath(path), comment);
    }

    @Override
    public String removeComment(List<String> path) {
        final HjsonFileConfig config = getLastConfig(path);
        return config != null ? config.comments.remove(endOfPath(path)) : null;
    }

    @Override
    public void clearComments() {
        comments.clear();
        for (Object o : map.values()) {
            if (o instanceof HjsonFileConfig) {
                ((HjsonFileConfig) o).clearComments();
            }
        }
    }

    @Override
    public String getComment(List<String> path) {
        final HjsonFileConfig config = getLastConfig(path);
        return config != null ? config.comments.get(endOfPath(path)) : null;
    }

    @Override
    public boolean containsComment(List<String> path) {
        final HjsonFileConfig config = getLastConfig(path);
        return config != null && config.comments.containsKey(endOfPath(path));
    }

    @Override
    public Map<String, String> commentMap() {
        return comments;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T set(List<String> path, Object value) {
        return (T) getOrCreateLastConfig(path).map.put(endOfPath(path), value);
    }

    @Override
    public boolean add(List<String> path, Object value) {
        return add(getOrCreateLastConfig(path), endOfPath(path), value);
    }

    /** Adds a value directly to the config, if it does not already exist. */
    private static boolean add(HjsonFileConfig config, String key, Object value) {
        if (!config.map.containsKey(key)) {
            config.map.put(key, value);
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T remove(List<String> path) {
        final HjsonFileConfig config = getLastConfig(path);
        return config != null ? (T) config.map.remove(endOfPath(path)) : null;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getRaw(List<String> path) {
        final HjsonFileConfig config = getLastConfig(path);
        return config != null ? (T) config.map.get(endOfPath(path)) : null;
    }

    @Override
    public boolean contains(List<String> path) {
        HjsonFileConfig config = this;
        final int lastIndex = path.size() - 1;
        for (int i = 0; i < lastIndex; i++) {
            final String s = path.get(i);
            if (!config.map.containsKey(s)) {
                return false;
            }
            config = (HjsonFileConfig) config.map.get(s);
        }
        return config.map.containsKey(path.get(lastIndex));
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Map<String, Object> valueMap() {
        return map;
    }

    @Override
    public Set<? extends CommentedConfig.Entry> entrySet() {
        return new FakeCommentedConfig(this).entrySet();
    }

    /** No proper implementation. Doing so is still too much effort at this time. */
    @Override public ConfigFormat<CommentedFileConfig> configFormat() {
        return null;
    }

    @Override
    public HjsonFileConfig createSubConfig() {
        return newSubConfig();
    }

    @Override
    public <R> R bulkCommentedUpdate(final Function<? super CommentedConfig, R> action) {
        synchronized (this) {
            return action.apply(this);
        }
    }

    @Override
    public <R> R bulkCommentedRead(final Function<? super UnmodifiableCommentedConfig, R> action) {
        synchronized (this) {
            return action.apply(this);
        }
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public Path getNioPath() {
        return file.toPath();
    }

    @Override
    public synchronized void save() {
        if (closed) {
            throw new IllegalStateException("Cannot save a closed file config.");
        }
        writing = true;
        writeJson(toHjson(), file).expect("Error writing to config file.");
        writing = false;
    }

    @Override
    public void load() {
        if (!writing) {
            synchronized (this) {
                if (closed) {
                    throw new IllegalStateException("Cannot (re)load a closed file config");
                }
                map.clear();
                comments.clear();
                final Container container = loadContainer(file);
                map.putAll(container.map);
                comments.putAll(container.comments);
            }
        }
    }

    @Override
    public void close() {
        closed = true;
    }

    /** A DTO holding both the value map and comments. */
    private static class Container {
        final Map<String, Object> map;
        final Map<String, String> comments;

        Container(Map<String, Object> map, Map<String, String> comments) {
            this.map = map;
            this.comments = comments;
        }

        Container() {
            this(new LinkedHashMap<>(), new HashMap<>());
        }
    }
}
