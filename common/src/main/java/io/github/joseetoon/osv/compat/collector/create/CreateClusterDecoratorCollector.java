package io.github.joseetoon.osv.compat.collector.create;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.joseetoon.osv.compat.collector.DecoratorCollector;
import io.github.joseetoon.osv.preset.data.FlexibleDecoratorSettings;
import io.github.joseetoon.osv.preset.data.FlexibleDecoratorSettings.FlexibleDecoratorSettingsBuilder;

import java.util.Optional;

public abstract class CreateClusterDecoratorCollector extends DecoratorCollector<FlexibleDecoratorSettings, FlexibleDecoratorSettingsBuilder> {

    protected CreateClusterDecoratorCollector() {
        super(FlexibleDecoratorSettings::builder, FlexibleDecoratorSettingsBuilder::build);
    }

    @ExpectPlatform
    public static Optional<CreateClusterDecoratorCollector> getInstance() {
        throw new AssertionError();
    }
}
