package io.github.joseetoon.osv.exception;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.exception.FormattedException;

public class CompatibilityModeException extends FormattedException {

    public CompatibilityModeException() {
        super("Running in compatibility mode. Some things may not work right.");
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.compatibilityMode");
    }

    @Override
    public @Nullable Component getTooltip() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.interceptorUnavailable");
    }

    @Override
    public @NotNull Component getDetailMessage() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.interceptorUnavailable");
    }
}
