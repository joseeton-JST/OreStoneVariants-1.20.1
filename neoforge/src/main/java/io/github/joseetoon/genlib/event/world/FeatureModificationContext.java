package io.github.joseetoon.genlib.event.world;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import personthecat.overwritevalidator.annotations.InheritMissingMembers;
import personthecat.overwritevalidator.annotations.Overwrite;
import personthecat.overwritevalidator.annotations.OverwriteClass;

import java.util.List;
import java.util.function.Predicate;

@OverwriteClass
@InheritMissingMembers
public class FeatureModificationContext {

    private final Holder<Biome> biomeHolder;
    private final ModifiableBiomeInfo.BiomeInfo.Builder builder;

    public FeatureModificationContext(final Holder<Biome> biomeHolder,
                                      final ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        this.biomeHolder = biomeHolder;
        this.builder = builder;
    }

    @Overwrite
    public Biome getBiome() {
        return this.biomeHolder.value();
    }

    @Overwrite
    public Holder<Biome> getBiomeHolder() {
        return this.biomeHolder;
    }

    @Overwrite
    public ResourceLocation getName() {
        return this.biomeHolder.unwrapKey()
            .map(ResourceKey::location)
            .orElse(ResourceLocation.fromNamespaceAndPath("unknown", "unknown"));
    }

    @Overwrite
    public RegistryAccess getRegistryAccess() {
        return null; // Not directly available in BiomeModifier context
    }

    @Overwrite
    public void addFeature(final GenerationStep.Decoration step, final PlacedFeature feature) {
        this.builder.getGenerationSettings().addFeature(step, Holder.direct(feature));
    }

    @Overwrite
    public void addCarver(final GenerationStep.Carving step, final ConfiguredWorldCarver<?> carver) {
        this.builder.getGenerationSettings().addCarver(step, Holder.direct(carver));
    }

    @Overwrite
    public boolean removeFeature(final ResourceLocation cfId) {
        boolean removed = false;
        for (final GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
            removed |= this.removeFeature(step, cfId);
        }
        return removed;
    }

    @Overwrite
    public boolean removeFeature(final GenerationStep.Decoration step, final ResourceLocation cfId) {
        final List<Holder<PlacedFeature>> features =
            this.builder.getGenerationSettings().getFeatures(step);
        final int before = features.size();
        features.removeIf(pfHolder -> pfHolder.value().feature()
            .unwrapKey()
            .map(k -> k.location().equals(cfId))
            .orElse(false));
        return features.size() < before;
    }

    @Overwrite
    public boolean removeCarver(final ResourceLocation id) {
        boolean removed = false;
        for (final GenerationStep.Carving step : GenerationStep.Carving.values()) {
            removed |= this.removeCarver(step, id);
        }
        return removed;
    }

    @Overwrite
    public boolean removeCarver(final GenerationStep.Carving step, final ResourceLocation id) {
        final List<Holder<ConfiguredWorldCarver<?>>> carvers =
            this.builder.getGenerationSettings().getCarvers(step);
        final int before = carvers.size();
        carvers.removeIf(carverHolder -> carverHolder.unwrapKey()
            .map(k -> k.location().equals(id))
            .orElse(false));
        return carvers.size() < before;
    }

    @Overwrite
    public boolean removeFeature(final Predicate<PlacedFeature> predicate) {
        boolean removed = false;
        for (final GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
            removed |= this.removeFeature(step, predicate);
        }
        return removed;
    }

    @Overwrite
    public boolean removeFeature(final GenerationStep.Decoration step, final Predicate<PlacedFeature> predicate) {
        final List<Holder<PlacedFeature>> features =
            this.builder.getGenerationSettings().getFeatures(step);
        final int before = features.size();
        features.removeIf(pfHolder -> predicate.test(pfHolder.value()));
        return features.size() < before;
    }
}
