package io.github.joseetoon.genlib.util;

import lombok.experimental.UtilityClass;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;
import io.github.joseetoon.genlib.event.registry.ForgeRegistryHandle;
import io.github.joseetoon.genlib.event.registry.MojangRegistryHandle;
import io.github.joseetoon.genlib.event.registry.RegistryHandle;
import io.github.joseetoon.genlib.exception.MissingElementException;
import io.github.joseetoon.genlib.exception.RegistryLookupException;
import personthecat.overwritevalidator.annotations.Inherit;
import personthecat.overwritevalidator.annotations.InheritMissingMembers;
import personthecat.overwritevalidator.annotations.Overwrite;
import personthecat.overwritevalidator.annotations.OverwriteClass;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
@OverwriteClass
@InheritMissingMembers
public class RegistryUtils {

    private static final Map<Class<?>, RegistryHandle<?>> REGISTRY_BY_TYPE = new ConcurrentHashMap<>();

    @Inherit
    public static <T> RegistryHandle<T> getHandle(final ResourceKey<Registry<T>> key) {
        return tryGetHandle(key).orElseThrow(() -> new RegistryLookupException(key));
    }

    @Overwrite
    @SuppressWarnings("unchecked")
    public static <T> Optional<RegistryHandle<T>> tryGetHandle(final ResourceKey<Registry<T>> key) {
        return BuiltInRegistries.REGISTRY.getOptional(key.location())
            .map(registry -> (RegistryHandle<T>) new MojangRegistryHandle<>((Registry<?>) registry));
    }

    @NotNull
    @Overwrite
    @SuppressWarnings("unchecked")
    public static <T> RegistryHandle<T> getByType(final Class<T> clazz) {
        return (RegistryHandle<T>) REGISTRY_BY_TYPE.computeIfAbsent(clazz, c -> {
            final RegistryHandle<?> builtin = findRegistry(clazz);
            if (builtin != null) return builtin;
            throw new MissingElementException("No registry for type: " + clazz.getSimpleName());
        });
    }

    private static RegistryHandle<?> findRegistry(final Class<?> clazz) {
        for (final Registry<?> r : BuiltInRegistries.REGISTRY) {
            final var iterator = r.iterator();
            if (iterator.hasNext() && clazz.isInstance(iterator.next())) {
                return new MojangRegistryHandle<>(r);
            }
        }
        return null;
    }
}
