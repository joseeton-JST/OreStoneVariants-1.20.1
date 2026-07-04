package io.github.joseetoon.genlib.event.lifecycle;

import io.github.joseetoon.genlib.event.LibEvent;

public class CheckErrorsEvent {
    public static final LibEvent<Runnable> EVENT =
        LibEvent.create(callbacks -> () -> callbacks.forEach(Runnable::run));
}
