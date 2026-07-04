package io.github.joseetoon.genlib.event.world;

import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

@Getter
public class RegistrySet {
    private final RegistryAccess registries;
    private final Registry<ConfiguredWorldCarver<?>> carvers;
    private final Registry<ConfiguredFeature<?, ?>> features;
    private final Registry<PlacedFeature> placedFeatures;

    public RegistrySet(final RegistryAccess registries) {
        this.registries = registries;
        this.carvers = registries.registryOrThrow(Registries.CONFIGURED_CARVER);
        this.features = registries.registryOrThrow(Registries.CONFIGURED_FEATURE);
        this.placedFeatures = registries.registryOrThrow(Registries.PLACED_FEATURE);
    }
}
