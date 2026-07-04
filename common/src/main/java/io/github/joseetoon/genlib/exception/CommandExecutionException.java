package io.github.joseetoon.genlib.exception;

public class CommandExecutionException extends RuntimeException {
    public CommandExecutionException(final String msg) {
        super(msg);
    }

    public CommandExecutionException(final Throwable e) {
        super(e);
    }
}
