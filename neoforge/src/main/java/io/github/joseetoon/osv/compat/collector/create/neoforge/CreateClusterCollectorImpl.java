package io.github.joseetoon.osv.compat.collector.create.neoforge;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import io.github.joseetoon.osv.compat.collector.create.CreateClusterCollector;
import io.github.joseetoon.osv.preset.data.ClusterSettings.ClusterSettingsBuilder;

import java.util.Optional;

// Create mod support disabled: no compatible Create version available for 1.20.1
public class CreateClusterCollectorImpl extends CreateClusterCollector {

    public static Optional<CreateClusterCollector> getInstance() {
        return Optional.empty();
    }

    @Override
    public boolean isFeatureConfigSupported(final FeatureConfiguration config) {
        return false;
    }

    @Override
    public boolean featureContainsBlock(final FeatureConfiguration config, final BlockState state) {
        return false;
    }

    @Override
    public void collectFeatureConfig(final ClusterSettingsBuilder builder, final FeatureConfiguration config) {
    }
}
