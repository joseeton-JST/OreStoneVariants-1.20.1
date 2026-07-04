package io.github.joseetoon.genlib.event.world;

import net.minecraft.world.level.LevelAccessor;
import io.github.joseetoon.genlib.event.LibEvent;

import java.util.function.Consumer;

public class CommonWorldEvent {

    public static final LibEvent<Consumer<LevelAccessor>> LOAD =
        LibEvent.create(consumers -> level -> consumers.forEach(consumer -> consumer.accept(level)));

    public static final LibEvent<Consumer<LevelAccessor>> UNLOAD =
        LibEvent.create(consumers -> level -> consumers.forEach(consumer -> consumer.accept(level)));
}
