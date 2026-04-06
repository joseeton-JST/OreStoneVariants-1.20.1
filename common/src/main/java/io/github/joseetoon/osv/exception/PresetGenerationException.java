package io.github.joseetoon.osv.exception;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class PresetGenerationException extends PresetSyntaxException {

    public PresetGenerationException(final File root, final File file, final String text, final Throwable cause) {
        super(root, file, text, cause);
    }

    @Override
    public @NotNull Component getTitleMessage() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.presetNotGenerated", this.name);
    }

    @Override
    public @Nullable Component getTooltip() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.xPresetNotGenerated");
    }
}
