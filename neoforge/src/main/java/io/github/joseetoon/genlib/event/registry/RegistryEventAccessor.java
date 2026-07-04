package io.github.joseetoon.genlib.event.registry;

import io.github.joseetoon.genlib.event.LibEvent;

public interface RegistryEventAccessor<T> {
    LibEvent<RegistryAddedCallback<T>> getRegistryAddedEvent();
}
