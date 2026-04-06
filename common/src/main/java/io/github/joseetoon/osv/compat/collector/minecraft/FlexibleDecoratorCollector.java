package io.github.joseetoon.osv.compat.collector.minecraft;

import io.github.joseetoon.osv.compat.collector.DecoratorCollector;
import io.github.joseetoon.osv.preset.data.FlexibleDecoratorSettings;
import io.github.joseetoon.osv.preset.data.FlexibleDecoratorSettings.FlexibleDecoratorSettingsBuilder;

/**
 * [1.20.1 Migration] Decorator detection is no longer supported.
 * The FeatureDecorator API was removed in 1.18.
 */
public class FlexibleDecoratorCollector extends DecoratorCollector<FlexibleDecoratorSettings, FlexibleDecoratorSettingsBuilder> {

    public FlexibleDecoratorCollector() {
        super(FlexibleDecoratorSettings::builder, FlexibleDecoratorSettingsBuilder::build);
    }
}
