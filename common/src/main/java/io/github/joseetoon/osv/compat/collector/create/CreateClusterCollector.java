package io.github.joseetoon.osv.compat.collector.create;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.joseetoon.osv.compat.collector.FeatureCollector;
import io.github.joseetoon.osv.preset.data.ClusterSettings;
import io.github.joseetoon.osv.preset.data.ClusterSettings.ClusterSettingsBuilder;

import java.util.Optional;

public abstract class CreateClusterCollector extends FeatureCollector<ClusterSettings, ClusterSettingsBuilder> {

    protected CreateClusterCollector() {
        super(ClusterSettings::builder, ClusterSettingsBuilder::build);
    }

    @ExpectPlatform
    public static Optional<CreateClusterCollector> getInstance() {
        throw new AssertionError();
    }
}
