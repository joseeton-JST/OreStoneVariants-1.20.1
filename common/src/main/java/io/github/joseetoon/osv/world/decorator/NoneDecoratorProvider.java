package io.github.joseetoon.osv.world.decorator;

import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.Collections;
import java.util.List;

public interface NoneDecoratorProvider<T> extends DecoratorProvider<T> {

    @Override
    default List<PlacementModifier> buildPlacementModifiers() {
        return Collections.emptyList();
    }
}
