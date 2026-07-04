package io.github.joseetoon.genlib.exception;

import net.minecraft.resources.ResourceKey;

public class RegistryLookupException extends RuntimeException {
    public RegistryLookupException(final ResourceKey<?> key) {
        super("No registry was found for key: " + key);
    }
}
