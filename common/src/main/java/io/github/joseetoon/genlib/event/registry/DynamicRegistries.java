package io.github.joseetoon.genlib.event.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.function.Consumer;

public class DynamicRegistries {

    public static final RegistryHandle<Biome> BIOMES =
        DynamicRegistryHandle.createHandle(Registries.BIOME);

    public static final RegistryHandle<DimensionType> DIMENSION_TYPES =
        DynamicRegistryHandle.createHandle(Registries.DIMENSION_TYPE);

    public static final RegistryHandle<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES =
        DynamicRegistryHandle.createHandle(Registries.CONFIGURED_FEATURE);

    public static void updateRegistries(final RegistryAccess registries) {
        updateRegistry(BIOMES, registries, Registries.BIOME);
        updateRegistry(DIMENSION_TYPES, registries, Registries.DIMENSION_TYPE);
        updateRegistry(CONFIGURED_FEATURES, registries, Registries.CONFIGURED_FEATURE);
    }

    private static <T> void updateRegistry(final RegistryHandle<T> handle,
            final RegistryAccess registries, final ResourceKey<Registry<T>> key) {
        try {
            ((DynamicRegistryHandle<T>) handle)
                .updateRegistry(new MojangRegistryHandle<>(registries.registryOrThrow(key)));
        } catch (final Exception ignored) {
            // Registry may not be present in all contexts (e.g. client-only)
        }
    }

    public static <T> Consumer<Consumer<RegistryHandle<T>>> listen(
            final RegistryHandle<T> handle, final Object mutex) {
        if (handle instanceof DynamicRegistryHandle) {
            return consumer -> ((DynamicRegistryHandle<T>) handle).listen(mutex, consumer);
        }
        return consumer -> {};
    }
}
