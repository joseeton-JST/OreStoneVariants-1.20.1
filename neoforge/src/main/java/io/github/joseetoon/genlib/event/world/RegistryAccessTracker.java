package io.github.joseetoon.genlib.event.world;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public interface RegistryAccessTracker {
    Set<ResourceLocation> getModifiedBiomes();
}
