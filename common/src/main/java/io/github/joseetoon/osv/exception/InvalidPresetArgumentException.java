package io.github.joseetoon.osv.exception;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.util.PathUtils;

import java.io.File;

public class InvalidPresetArgumentException extends PresetLoadException {

    private final String msg;
    @Nullable private final String name;

    public InvalidPresetArgumentException(final File root, final File file, final String msg) {
        super(PathUtils.getRelativePath(root, file));
        this.msg = msg;
        this.name = file.getName();
    }

    public InvalidPresetArgumentException(final File root, final File file, final Throwable cause) {
        super(PathUtils.getRelativePath(root, file), cause);
        this.msg = cause.getMessage();
        this.name = file.getName();
    }

    public InvalidPresetArgumentException(final String msg, final Throwable cause) {
        super(msg, cause);
        this.msg = msg;
        this.name = null;
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return net.minecraft.network.chat.Component.literal(this.getMessage());
    }

    @Override
    public @NotNull Component getTitleMessage() {
        if (this.name != null) {
            return net.minecraft.network.chat.Component.translatable("osv.errorText.invalidArgument", this.name);
        }
        return net.minecraft.network.chat.Component.translatable("osv.errorText.unknownInvalidArgument");
    }

    @Override
    public @Nullable Component getTooltip() {
        return net.minecraft.network.chat.Component.literal(this.msg);
    }
}
