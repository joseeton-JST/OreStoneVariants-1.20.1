package io.github.joseetoon.osv.exception;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import io.github.joseetoon.genlib.exception.FormattedException;

public class ConfigFileNotLoadedException extends FormattedException {
    public ConfigFileNotLoadedException(final Throwable cause) {
        super(cause);
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.noConfigFile");
    }
}
