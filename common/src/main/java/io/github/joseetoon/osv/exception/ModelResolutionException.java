package io.github.joseetoon.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.exception.FormattedException;

public class ModelResolutionException extends FormattedException {

    private final ResourceLocation model;

    public ModelResolutionException(final ResourceLocation model) {
        super("Could not resolve required model: " + model);
        this.model = model;
    }

    @Override
    public @NotNull String getCategory() {
        return "osv.errorMenu.models";
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return net.minecraft.network.chat.Component.literal(this.model.toString());
    }

    @Override
    public @NotNull Component getTitleMessage() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.couldNotReadModel", this.model.toString());
    }

    @Override
    public @Nullable Component getTooltip() {
        return net.minecraft.network.chat.Component.literal(this.getLocalizedMessage());
    }
}
