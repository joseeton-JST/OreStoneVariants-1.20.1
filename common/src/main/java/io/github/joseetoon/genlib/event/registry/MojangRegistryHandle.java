package io.github.joseetoon.genlib.event.registry;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class MojangRegistryHandle<T> implements RegistryHandle<T> {

    private final Registry<T> registry;

    public MojangRegistryHandle(final Registry<T> registry) {
        this.registry = registry;
    }

    public Registry<T> getRegistry() {
        return this.registry;
    }

    @Nullable
    @Override
    public ResourceLocation getKey(final T t) {
        return this.registry.getKey(t);
    }

    @Nullable
    @Override
    public T lookup(final ResourceLocation id) {
        return this.registry.get(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends T> V register(final ResourceLocation id, final V v) {
        return (V) ((MappedRegistry<T>) this.registry)
            .register(ResourceKey.create(this.registry.key(), id), v, RegistrationInfo.BUILT_IN)
            .value();
    }

    @Override
    public void forEach(final BiConsumer<ResourceLocation, T> f) {
        for (final Map.Entry<ResourceKey<T>, T> entry : new HashSet<>(this.registry.entrySet())) {
            f.accept(entry.getKey().location(), entry.getValue());
        }
    }

    @Override
    public boolean isRegistered(final ResourceLocation id) {
        return this.registry.containsKey(id);
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return this.registry.keySet();
    }

    @Override
    public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        return this.registry.entrySet();
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this.registry.iterator();
    }

    @Override
    public Stream<T> stream() {
        return this.registry.stream();
    }
}
