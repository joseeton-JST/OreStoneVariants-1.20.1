package io.github.joseetoon.genlib.event.lifecycle;

import io.github.joseetoon.genlib.event.LibEvent;
import io.github.joseetoon.genlib.util.McUtils;

public class GameReadyEvent {

    public static final LibEvent<Runnable> CLIENT =
        LibEvent.create(callbacks -> () -> callbacks.forEach(Runnable::run));

    public static final LibEvent<Runnable> SERVER =
        LibEvent.create(callbacks -> () -> callbacks.forEach(Runnable::run));

    public static final LibEvent<Runnable> COMMON =
        McUtils.isClientSide() ? CLIENT : SERVER;
}
