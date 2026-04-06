package io.github.joseetoon.osv.exception;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.exception.FormattedException;

public class CompatOutOfDateException extends FormattedException {

    private final String mod;

    public CompatOutOfDateException(final String mod, final Throwable cause) {
        super(cause);
        this.mod = mod;
    }

    @Override
    public @NotNull String getCategory() {
        return "osv.errorMenu.compat";
    }

    @Override
    public @Nullable Component getTooltip() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.outOfDate", this.mod);
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return net.minecraft.network.chat.Component.literal(this.mod);
    }
}
