package io.github.joseetoon.genlib.config;

import com.electronwill.nightconfig.core.Config;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("unused")
public class DynamicCategory<T> implements Map<String, T> {
    private final Map<String, Entry<String, T>> map = new HashMap<>();
    private final Config cfg;
    private final String path;
    private final T def;
    private volatile transient Values values;
    private volatile transient EntrySet entries;

    public DynamicCategory(final Class<T> t, final Config cfg, final Builder builder, final String path, final Map<String, T> defaults, final T def) {
        this.cfg = cfg;
        this.path = path;
        this.def = def;
        final Map<String, T> resolvedDefaults = new LinkedHashMap<>(defaults);

        if (cfg.contains(path)) {
            resolvedDefaults.putAll(coerce(t, path, ((Config) cfg.get(path)).valueMap()));
        }
        resolvedDefaults.forEach((key, value) -> {
            final ConfigValue<T> forge = builder.define(path + "." + key, value);
            this.map.put(key, new ForgeValue<>(t, key, forge, cfg, path + "." + key, value));
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> coerce(final Class<T> t, final String path, final Map<String, Object> map) {
        map.forEach((k, v) -> {
            if (!t.isInstance(v)) {
                throw new UnsupportedOperationException("Expected a " + t.getSimpleName() + " in config @ " + path + "." + k);
            }
        });
        return (Map<String, T>) map;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        if (value == null) {
            return false;
        } if (value instanceof Entry) {
            return this.map.containsValue(value);
        }
        for (final T t : this.values()) {
            if (value.equals(t)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public T get(final Object key) {
        final Entry<String, T> entry = this.map.get(key);
        if (entry == null) {
            return this.def;
        }
        final T t = entry.getValue();
        return t == null ? this.def : t;
    }

    @Nullable
    @Override
    public T put(final String key, final T value) {
        final Entry<String, T> original = this.map.get(key);
        if (original != null) {
            final T ret = original.getValue();
            original.setValue(value);
            return ret;
        }
        final DynamicValue<T> dynamic = new DynamicValue<>(this.cfg, this.path, key);
        dynamic.setValue(value);
        this.map.put(key, dynamic);
        return null;
    }

    @Override
    public T remove(final Object key) {
        final Map.Entry<String, T> entry = this.map.remove(key);
        return entry != null ? entry.getValue() : null;
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends T> m) {
        for (final Entry<? extends String, ? extends T> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return this.map.keySet();
    }

    @NotNull
    @Override
    public Collection<T> values() {
        if (this.values == null) this.values = new Values();
        return this.values;
    }

    @NotNull
    @Override
    public Set<Entry<String, T>> entrySet() {
        if (this.entries == null) this.entries = new EntrySet();
        return this.entries;
    }

    private static class ForgeValue<T> implements Entry<String, T> {

        final Class<T> type;
        final String key;
        final ConfigValue<T> value;
        final Config cfg;
        final String qualified;
        final T fallback;

        ForgeValue(final Class<T> type, final String key, final ConfigValue<T> value, final Config cfg, final String qualified, final T fallback) {
            this.type = type;
            this.key = key;
            this.value = value;
            this.cfg = cfg;
            this.qualified = qualified;
            this.fallback = fallback;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public T getValue() {
            try {
                final T loaded = this.value.get();
                return loaded != null ? loaded : this.readRawValue();
            } catch (final IllegalStateException | NullPointerException ignored) {
                return this.readRawValue();
            }
        }

        @Override
        public T setValue(final T t) {
            final T original = this.getValue();
            try {
                this.value.set(t);
            } catch (final IllegalStateException | NullPointerException ignored) {
                this.cfg.set(this.qualified, t);
            }
            return original;
        }

        @SuppressWarnings("unchecked")
        private T readRawValue() {
            if (!this.cfg.contains(this.qualified)) {
                return this.fallback;
            }
            final Object raw = this.cfg.get(this.qualified);
            if (raw == null) {
                return this.fallback;
            }
            if (this.type.isInstance(raw)) {
                return (T) raw;
            }
            return this.fallback;
        }
    }

    private static class DynamicValue<T> implements Entry<String, T> {
        final Config cfg;
        final String key;
        final String qualified;
        volatile T cache;

        DynamicValue(final Config cfg, final String path, final String key) {
            this.cfg = cfg;
            this.key = key;
            this.qualified = path + "." + key;
            this.cache = cfg.contains(this.qualified) ? cfg.get(this.qualified) : null;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public T getValue() {
            return this.cache;
        }

        @Override
        public T setValue(final T t) {
            final T original = this.cache;
            this.cfg.set(this.qualified, t);
            this.cache = t;
            return original;
        }
    }

    private class Values extends AbstractCollection<T> {

        @Override
        public final int size() {
            return DynamicCategory.this.size();
        }

        @Override
        public final void clear() {
            DynamicCategory.this.clear();
        }

        @NotNull
        @Override
        public final Iterator<T> iterator() {
            return new ValueIterator();
        }

        @Override
        public final boolean contains(final Object o) {
            return containsValue(o);
        }
    }

    private class ValueIterator implements Iterator<T> {

        final Iterator<Entry<String, T>> wrapped = DynamicCategory.this.map.values().iterator();

        @Override
        public boolean hasNext() {
            return this.wrapped.hasNext();
        }

        @Override
        public T next() {
            return this.wrapped.next().getValue();
        }

        @Override
        public void remove() {
            this.wrapped.remove();
        }
    }

    private class EntrySet extends AbstractSet<Entry<String, T>> {

        @NotNull
        @Override
        public Iterator<Entry<String, T>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return DynamicCategory.this.size();
        }
    }

    private class EntryIterator implements Iterator<Entry<String, T>> {

        final Iterator<Entry<String, Entry<String, T>>> wrapped = DynamicCategory.this.map.entrySet().iterator();

        @Override
        public boolean hasNext() {
            return this.wrapped.hasNext();
        }

        @Override
        public Entry<String, T> next() {
            return this.wrapped.next().getValue();
        }

        @Override
        public void remove() {
            this.wrapped.remove();
        }
    }
}
