package io.github.joseetoon.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.exception.FormattedException;

public class VariantLoadException extends FormattedException {

    private final ResourceLocation id;

    public VariantLoadException(final ResourceLocation id, final Throwable cause) {
        super("Error loading " + id, cause);
        this.id = id;
    }

    @Override
    public @NotNull String getCategory() {
        return "osv.errorMenu.variants";
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return net.minecraft.network.chat.Component.literal(this.id.getPath());
    }

    @Override
    public @NotNull Component getTitleMessage() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.variantError", this.id.toString());
    }

    @Override
    public @Nullable Component getTooltip() {
        return net.minecraft.network.chat.Component.literal(this.getCause().getMessage());
    }
}
