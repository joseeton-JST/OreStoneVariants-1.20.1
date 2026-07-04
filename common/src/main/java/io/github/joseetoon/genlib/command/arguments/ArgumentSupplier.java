package io.github.joseetoon.genlib.command.arguments;

import java.util.function.Supplier;

public interface ArgumentSupplier<T> extends Supplier<ArgumentDescriptor<T>> {}
