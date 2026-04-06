package io.github.joseetoon.osv.exception;

import org.jetbrains.annotations.NotNull;
import io.github.joseetoon.genlib.exception.FormattedException;

public abstract class PresetLoadException extends FormattedException {

    public PresetLoadException(final String msg) {
        super(msg);
    }

    public PresetLoadException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    @Override
    public @NotNull String getCategory() {
        return "osv.errorMenu.presets";
    }
}
