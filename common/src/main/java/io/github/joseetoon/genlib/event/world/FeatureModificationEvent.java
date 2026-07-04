package io.github.joseetoon.genlib.event.world;

import io.github.joseetoon.genlib.event.LibEvent;

import java.util.function.Consumer;

public class FeatureModificationEvent {
    public static final LibEvent<Consumer<FeatureModificationContext>> EVENT =
        LibEvent.create(callbacks -> ctx -> callbacks.forEach(c -> c.accept(ctx)));
}
