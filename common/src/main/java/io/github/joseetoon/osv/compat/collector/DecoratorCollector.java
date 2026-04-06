package io.github.joseetoon.osv.compat.collector;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import io.github.joseetoon.osv.world.decorator.DecoratorProvider;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * [1.20.1 Migration] The FeatureDecorator/DecoratorConfiguration API was removed in 1.18.
 * This class is now a stub — decorator settings are no longer auto-detected from vanilla features.
 * All collectors return false from canCollect() and produce default decorator values.
 */
public abstract class DecoratorCollector<Settings extends DecoratorProvider<?>, Builder> {

    private final Supplier<Builder> creator;
    private final Function<Builder, Settings> build;

    public DecoratorCollector(final Supplier<Builder> creator, final Function<Builder, Settings> build) {
        this.creator = creator;
        this.build = build;
    }

    public boolean canCollect(final ConfiguredFeature<?, ?> configured) {
        return false;
    }

    public Settings collect(final ConfiguredFeature<?, ?> configured) {
        return this.build.apply(this.creator.get());
    }
}
