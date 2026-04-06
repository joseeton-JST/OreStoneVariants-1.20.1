package io.github.joseetoon.osv.world.placement;

import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

public interface NoneDecoratorProvider<T> extends PlacementProvider<T> {

    @Override
    default List<PlacementModifier> createModifiers() {
        return List.of();
    }
}
