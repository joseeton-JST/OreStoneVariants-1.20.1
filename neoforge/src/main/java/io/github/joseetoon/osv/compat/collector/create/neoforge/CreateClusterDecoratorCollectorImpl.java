package io.github.joseetoon.osv.compat.collector.create.neoforge;

import io.github.joseetoon.osv.compat.collector.create.CreateClusterDecoratorCollector;

import java.util.Optional;

/**
 * [1.20.1 Migration] Create mod compat decorator collection is no longer supported.
 * The FeatureDecorator API was removed in 1.18.
 */
public class CreateClusterDecoratorCollectorImpl extends CreateClusterDecoratorCollector {

    private static final CreateClusterDecoratorCollector INSTANCE = new CreateClusterDecoratorCollectorImpl();

    private CreateClusterDecoratorCollectorImpl() {}

    public static Optional<CreateClusterDecoratorCollector> getInstance() {
        return Optional.of(INSTANCE);
    }
}
