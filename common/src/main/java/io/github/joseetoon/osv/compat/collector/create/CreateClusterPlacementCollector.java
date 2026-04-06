package io.github.joseetoon.osv.compat.collector.create;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.joseetoon.osv.compat.collector.PlacementCollector;
import io.github.joseetoon.osv.preset.data.FlexiblePlacementSettings;
import io.github.joseetoon.osv.preset.data.FlexiblePlacementSettings.FlexiblePlacementSettingsBuilder;

import java.util.Optional;

public abstract class CreateClusterPlacementCollector extends PlacementCollector<FlexiblePlacementSettings, FlexiblePlacementSettingsBuilder> {

    protected CreateClusterPlacementCollector() {
        super(FlexiblePlacementSettings::builder, FlexiblePlacementSettingsBuilder::build);
    }

    @ExpectPlatform
    public static Optional<CreateClusterPlacementCollector> getInstance() {
        throw new AssertionError();
    }
}
