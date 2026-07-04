package net.minecraftforge.common;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ForgeConfigSpec implements IConfigSpec {

    private final ModConfigSpec delegate;

    public ForgeConfigSpec(final ModConfigSpec delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    @Override
    public void validateSpec(final ModConfig config) {
        this.delegate.validateSpec(config);
    }

    @Override
    public boolean isCorrect(final UnmodifiableCommentedConfig config) {
        return this.delegate.isCorrect(config);
    }

    @Override
    public void correct(final CommentedConfig config) {
        this.delegate.correct(config);
    }

    @Override
    public void acceptConfig(final IConfigSpec.ILoadedConfig config) {
        this.delegate.acceptConfig(config);
    }

    public static class Builder {
        private final ModConfigSpec.Builder delegate = new ModConfigSpec.Builder();

        public Builder comment(final String... comment) {
            this.delegate.comment(comment);
            return this;
        }

        public Builder push(final String path) {
            this.delegate.push(path);
            return this;
        }

        public Builder pop() {
            this.delegate.pop();
            return this;
        }

        public Builder pop(final int count) {
            this.delegate.pop(count);
            return this;
        }

        public <T> ConfigValue<T> define(final String path, final T defaultValue) {
            return new ConfigValue<>(this.delegate.define(path, defaultValue));
        }

        public <T> ConfigValue<T> define(final String path, final T defaultValue, final Predicate<Object> validator) {
            return new ConfigValue<>(this.delegate.define(path, defaultValue, validator));
        }

        public BooleanValue define(final String path, final boolean defaultValue) {
            return new BooleanValue(this.delegate.define(path, defaultValue));
        }

        public DoubleValue defineInRange(final String path, final double defaultValue, final double min, final double max) {
            return new DoubleValue(this.delegate.defineInRange(path, defaultValue, min, max));
        }

        public IntValue defineInRange(final String path, final int defaultValue, final int min, final int max) {
            return new IntValue(this.delegate.defineInRange(path, defaultValue, min, max));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(final String path, final V defaultValue) {
            return new EnumValue<>(this.delegate.defineEnum(path, defaultValue));
        }

        public <T> ConfigValue<List<? extends T>> defineList(
            final String path,
            final List<? extends T> defaultValue,
            final Predicate<Object> elementValidator
        ) {
            return new ConfigValue<>(this.delegate.defineList(path, defaultValue, elementValidator));
        }

        public ForgeConfigSpec build() {
            return new ForgeConfigSpec(this.delegate.build());
        }
    }

    public static class ConfigValue<T> implements Supplier<T> {
        protected final ModConfigSpec.ConfigValue<T> delegate;

        public ConfigValue(final ModConfigSpec.ConfigValue<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get() {
            return this.delegate.get();
        }

        public void set(final T value) {
            this.delegate.set(value);
        }

        public List<String> getPath() {
            return this.delegate.getPath();
        }
    }

    public static class BooleanValue extends ConfigValue<Boolean> {
        public BooleanValue(final ModConfigSpec.BooleanValue delegate) {
            super(delegate);
        }
    }

    public static class DoubleValue extends ConfigValue<Double> {
        public DoubleValue(final ModConfigSpec.DoubleValue delegate) {
            super(delegate);
        }
    }

    public static class IntValue extends ConfigValue<Integer> {
        public IntValue(final ModConfigSpec.IntValue delegate) {
            super(delegate);
        }
    }

    public static class EnumValue<V extends Enum<V>> extends ConfigValue<V> {
        public EnumValue(final ModConfigSpec.EnumValue<V> delegate) {
            super(delegate);
        }
    }
}
