package io.github.joseetoon.genlib.exception;

import static io.github.joseetoon.genlib.util.Shorthand.f;

public class BiomeTypeNotFoundException extends RuntimeException {
    public BiomeTypeNotFoundException(final String name) {
        super(f("There is no biome category named: {}", name));
    }
}
