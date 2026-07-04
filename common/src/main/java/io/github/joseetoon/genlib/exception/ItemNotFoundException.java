package io.github.joseetoon.genlib.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(final String id) {
        super("There is no item named " + id);
    }
}
