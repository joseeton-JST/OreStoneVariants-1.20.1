package io.github.joseetoon.genlib.command.function;

import io.github.joseetoon.genlib.command.CommandContextWrapper;

public interface CommandFunction {
    void execute(final CommandContextWrapper wrapper) throws Throwable;
}
