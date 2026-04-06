package io.github.joseetoon.osv.preset.data;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import io.github.joseetoon.osv.world.decorator.DecoratorProvider;

import java.util.Collections;
import java.util.List;

public class EmptyDecoratorSettings implements DecoratorProvider<EmptyDecoratorSettings> {

    public static final EmptyDecoratorSettings INSTANCE = new EmptyDecoratorSettings();

    public static final Codec<EmptyDecoratorSettings> CODEC = Codec.unit(INSTANCE);

    private EmptyDecoratorSettings() {};

    @Override
    public List<PlacementModifier> buildPlacementModifiers() {
        return Collections.emptyList();
    }

    @Override
    public Codec<EmptyDecoratorSettings> codec() {
        return CODEC;
    }
}
