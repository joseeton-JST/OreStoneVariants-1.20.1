package io.github.joseetoon.genlib.event.world;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import io.github.joseetoon.genlib.exception.MissingOverrideException;
import personthecat.overwritevalidator.annotations.OverwriteTarget;
import personthecat.overwritevalidator.annotations.PlatformMustOverwrite;

import java.util.function.Predicate;

@OverwriteTarget(required = true)
public class FeatureModificationContext {

    @PlatformMustOverwrite
    public Biome getBiome() {
        throw new MissingOverrideException();
    }

    @PlatformMustOverwrite
    public Holder<Biome> getBiomeHolder() {
        throw new MissingOverrideException();
    }

    @PlatformMustOverwrite
    public ResourceLocation getName() {
        throw new MissingOverrideException();
    }

    @PlatformMustOverwrite
    public RegistryAccess getRegistryAccess() {
        throw new MissingOverrideException();
    }

    @PlatformMustOverwrite
    public void addFeature(final GenerationStep.Decoration step, final PlacedFeature feature) {
        throw new MissingOverrideException();
    }

    @PlatformMustOverwrite
    public void addCarver(final GenerationStep.Carving step, final ConfiguredWorldCarver<?> carver) {
        throw new MissingOverrideException();
    }

    @PlatformMustOverwrite
    public boolean removeFeature(final ResourceLocation id) {
        throw new MissingOverrideException();
    }

    @PlatformMustOverwrite
    public boolean removeFeature(final GenerationStep.Decoration step, final ResourceLocation id) {
        throw new MissingOverrideException();
    }

    @PlatformMustOverwrite
    public boolean removeCarver(final ResourceLocation id) {
        throw new MissingOverrideException();
    }

    @PlatformMustOverwrite
    public boolean removeCarver(final GenerationStep.Carving step, final ResourceLocation id) {
        throw new MissingOverrideException();
    }

    @PlatformMustOverwrite
    public boolean removeFeature(final Predicate<PlacedFeature> predicate) {
        throw new MissingOverrideException();
    }

    @PlatformMustOverwrite
    public boolean removeFeature(final GenerationStep.Decoration step, final Predicate<PlacedFeature> predicate) {
        throw new MissingOverrideException();
    }
}
