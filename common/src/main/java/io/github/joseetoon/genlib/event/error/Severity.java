package io.github.joseetoon.genlib.event.error;

public enum Severity {
    WARN,
    ERROR,
    FATAL;

    public boolean isAtLeast(final Severity rhs) {
        return this.ordinal() >= rhs.ordinal();
    }
}
