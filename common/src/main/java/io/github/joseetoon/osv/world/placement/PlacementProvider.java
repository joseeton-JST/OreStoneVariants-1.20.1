package io.github.joseetoon.osv.world.placement;

import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import io.github.joseetoon.osv.preset.data.DynamicSerializable;

import java.util.List;

public interface PlacementProvider<T> extends DynamicSerializable<T> {
    List<PlacementModifier> createModifiers();
}
