package io.github.joseetoon.genlib.util;

import lombok.experimental.UtilityClass;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;
import io.github.joseetoon.genlib.event.registry.RegistryHandle;
import io.github.joseetoon.genlib.exception.MissingOverrideException;
import io.github.joseetoon.genlib.exception.RegistryLookupException;
import personthecat.overwritevalidator.annotations.OverwriteTarget;
import personthecat.overwritevalidator.annotations.PlatformMustOverwrite;

import java.util.Optional;

@UtilityClass
@OverwriteTarget(required = true)
public class RegistryUtils {

    /**
     * Acquires a handle on the registry corresponding to this key. In particular, this
     * is useful for two reason:
     *
     * <ul>
     *   <li>To dynamically acquire a registry from either {@link Registry} or {@link BuiltInRegistries}</li>
     *   <li>On Forge, to acquire either of the above or the preferred Forge registry.</li>
     * </ul>
     *
     * @throws RegistryLookupException If the expected registry does not exist.
     * @param key The key of the registry being returned.
     * @param <T> The type of object contained within the registry.
     * @return A platform-agnostic representation of this registry.
     */
    public static <T> RegistryHandle<T> getHandle(final ResourceKey<Registry<T>> key) {
        return tryGetHandle(key).orElseThrow(() -> new RegistryLookupException(key));
    }

    /**
     * Attempts to acquire a handle on the corresponding registry for this key. Or else,
     * returns {@link Optional#empty}.
     *
     * @param key The key of the registry being returned.
     * @param <T> The type of object contained within the registry.
     * @return A platform-agnostic representation of this registry, or else {@link Optional#empty}.
     */
    @PlatformMustOverwrite
    public static <T> Optional<RegistryHandle<T>> tryGetHandle(final ResourceKey<Registry<T>> key) {
        throw new MissingOverrideException();
    }

    /**
     * Acquires a handle on a registry when given the element type. For example, when
     * given <code>Biome.class</code>, will return {@link BuiltInRegistries#BIOME}.
     * On the Forge platform, this method will return the equivalent Forge registry.
     *
     * @throws net.minecraft.core.Registry if the expected registry is not found.
     * @param clazz The element type contained within the registry.
     * @param <T>   The type token of this element.
     * @return A handle on the expected registry, guaranteed.
     */
    @NotNull
    @PlatformMustOverwrite
    public static <T> RegistryHandle<T> getByType(final Class<T> clazz) {
        throw new MissingOverrideException();
    }
}
