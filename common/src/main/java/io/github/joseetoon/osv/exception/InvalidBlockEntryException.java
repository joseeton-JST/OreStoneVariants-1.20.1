package io.github.joseetoon.osv.exception;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.exception.FormattedException;

public class InvalidBlockEntryException extends FormattedException {

    private final String raw;

    public InvalidBlockEntryException(final String raw) {
        super("Could not parse block entry: " + raw);
        this.raw = raw;
    }

    @Override
    public @NotNull String getCategory() {
        return "osv.errorMenu.blockList";
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return net.minecraft.network.chat.Component.literal(this.raw);
    }

    @Override
    public @NotNull Component getTitleMessage() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.couldNotParse", this.raw);
    }

    @Override
    public @Nullable Component getTooltip() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.invalidFormat");
    }

    @Override
    public @NotNull Component getDetailMessage() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.blockListTutorial");
    }
}
