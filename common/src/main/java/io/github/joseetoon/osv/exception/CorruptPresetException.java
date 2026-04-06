package io.github.joseetoon.osv.exception;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.util.PathUtils;

import java.io.File;

public class CorruptPresetException extends PresetLoadException {

    private final String name;

    public CorruptPresetException(final File root, final File file, final Throwable cause) {
        super(PathUtils.getRelativePath(root, file), cause);
        this.name = file.getName();
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return net.minecraft.network.chat.Component.literal(this.getMessage());
    }

    @Override
    public @NotNull Component getTitleMessage() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.xPresetCorrupt", this.name);
    }

    @Override
    public @Nullable Component getTooltip() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.presetCorrupt");
    }
}
