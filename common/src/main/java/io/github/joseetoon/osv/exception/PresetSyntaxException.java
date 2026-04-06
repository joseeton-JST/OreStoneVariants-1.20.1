package io.github.joseetoon.osv.exception;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.util.SyntaxLinter;
import io.github.joseetoon.genlib.util.PathUtils;

import java.io.File;

public class PresetSyntaxException extends PresetLoadException {

    protected final String text;
    protected final String name;

    public PresetSyntaxException(final File root, final File file, final String text, final Throwable cause) {
        super(PathUtils.getRelativePath(root, file), cause);
        this.text = text;
        this.name = file.getName();
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return net.minecraft.network.chat.Component.literal(this.getMessage());
    }

    @Override
    public @NotNull Component getTitleMessage() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.presetInvalid", this.name);
    }

    @Override
    public @Nullable Component getTooltip() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.presetInvalid");
    }

    @Override
    public @NotNull Component getDetailMessage() {
        return SyntaxLinter.DEFAULT_LINTER.lint(this.text.replace("\t", "  ").replace("\r", ""));
    }
}
