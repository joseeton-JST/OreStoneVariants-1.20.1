package io.github.joseetoon.osv.world.decorator;

import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import io.github.joseetoon.osv.preset.data.DynamicSerializable;

import java.util.List;

public interface DecoratorProvider<T> extends DynamicSerializable<T> {
    List<PlacementModifier> buildPlacementModifiers();
}
