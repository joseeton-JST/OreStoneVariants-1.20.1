package io.github.joseetoon.osv.preset.data;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import io.github.joseetoon.osv.world.placement.PlacementProvider;

import java.util.List;

public class EmptyPlacementSettings implements PlacementProvider<EmptyPlacementSettings> {

    public static final EmptyPlacementSettings INSTANCE = new EmptyPlacementSettings();

    public static final Codec<EmptyPlacementSettings> CODEC = Codec.unit(INSTANCE);

    private EmptyPlacementSettings() {};

    @Override
    public List<PlacementModifier> createModifiers() {
        return List.of();
    }

    @Override
    public Codec<EmptyPlacementSettings> codec() {
        return CODEC;
    }
}
