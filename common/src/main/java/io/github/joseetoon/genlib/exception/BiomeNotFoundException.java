package io.github.joseetoon.genlib.exception;

public class BiomeNotFoundException extends RuntimeException {
    public BiomeNotFoundException(final String name) {
        super("There is no biome named " + name);
    }
}
