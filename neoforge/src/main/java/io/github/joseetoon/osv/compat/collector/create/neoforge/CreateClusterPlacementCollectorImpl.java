package io.github.joseetoon.osv.compat.collector.create.neoforge;

import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import io.github.joseetoon.osv.compat.collector.create.CreateClusterPlacementCollector;
import io.github.joseetoon.osv.preset.data.FlexiblePlacementSettings.FlexiblePlacementSettingsBuilder;
import io.github.joseetoon.osv.world.providers.SimpleCount;
import io.github.joseetoon.osv.world.providers.SimpleHeight;

import java.lang.reflect.Method;
import java.util.Optional;

public class CreateClusterPlacementCollectorImpl extends CreateClusterPlacementCollector {

    private static final CreateClusterPlacementCollector INSTANCE = new CreateClusterPlacementCollectorImpl();

    private CreateClusterPlacementCollectorImpl() {}

    public static Optional<CreateClusterPlacementCollector> getInstance() {
        return Optional.of(INSTANCE);
    }

    @Override
    public boolean isPlacementSupported(final PlacementModifier modifier) {
        final String className = modifier.getClass().getName();
        return className.equals("com.simibubi.create.foundation.worldgen.ConfigDrivenDecorator")
            || className.equals("com.simibubi.create.foundation.worldgen.ConfigDrivenPlacement");
    }

    @Override
    public void collectPlacement(final FlexiblePlacementSettingsBuilder builder, final PlacementModifier modifier) {
        if (!this.isPlacementSupported(modifier)) {
            return;
        }

        try {
            final Class<?> type = modifier.getClass();
            final Method getFrequency = type.getMethod("getFrequency");
            final Method getMinY = type.getMethod("getMinY");
            final Method getMaxY = type.getMethod("getMaxY");

            final Number frequency = (Number) getFrequency.invoke(modifier);
            final Number minY = (Number) getMinY.invoke(modifier);
            final Number maxY = (Number) getMaxY.invoke(modifier);

            builder.count(new SimpleCount(0, Math.max(0, frequency.intValue())));
            builder.height(new SimpleHeight(minY.intValue(), maxY.intValue()));
        } catch (final ReflectiveOperationException | ClassCastException ignored) {
            // Incompatible Create API variant; skip this modifier.
        }
    }
}
