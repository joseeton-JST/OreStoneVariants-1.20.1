package io.github.joseetoon.genlib.exception;

public class ModLoadException extends RuntimeException {

    public ModLoadException(final String msg) {
        super(msg);
    }

    public ModLoadException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public ModLoadException(final Throwable cause) {
        super(cause);
    }
}
