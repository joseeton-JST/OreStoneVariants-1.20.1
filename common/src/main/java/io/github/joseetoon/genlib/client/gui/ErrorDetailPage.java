package io.github.joseetoon.genlib.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Stub: GUI rendering API changed in 1.20.1 (GuiGraphics replaced PoseStack).
 * This class is kept for binary compatibility only.
 */
public class ErrorDetailPage extends SimpleTextPage {

    public ErrorDetailPage(@Nullable Screen parent, Component title, Component details) {
        super(parent, title, details);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partial) {
        super.render(graphics, x, y, partial);
    }
}
