package io.github.joseetoon.genlib.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Stub: GUI rendering API changed in 1.20.1 (GuiGraphics replaced PoseStack).
 * This class is kept for binary compatibility but not fully functional.
 */
public class LibMenu extends Screen {

    protected static final float G = 32.0F;
    protected static final int Y0 = 35;
    protected static final int Y1 = 50;

    @Nullable protected Screen parent;
    protected Button previous;
    protected Button cancel;
    protected Button next;

    protected LibMenu(@Nullable Screen parent, Component title) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.previous = Button.builder(CommonComponents.GUI_BACK, b -> this.onPrevious())
            .pos(this.width / 2 - 60 - 120 - 10, this.height - 35).size(120, 20).build();
        this.cancel = Button.builder(CommonComponents.GUI_CANCEL, b -> this.onClose())
            .pos(this.width / 2 - 60, this.height - 35).size(120, 20).build();
        this.next = Button.builder(CommonComponents.GUI_PROCEED, b -> this.onNext())
            .pos(this.width / 2 - 60 + 120 + 10, this.height - 35).size(120, 20).build();

        this.addRenderableWidget(this.previous);
        this.addRenderableWidget(this.cancel);
        this.addRenderableWidget(this.next);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partial) {
        this.renderBackground(graphics, x, y, partial);
        super.render(graphics, x, y, partial);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    }

    protected void renderMenu(GuiGraphics graphics, int x, int y, float partial) {}

    protected void renderDetails(GuiGraphics graphics, int x, int y, float partial) {}

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    protected void onPrevious() {}

    protected void onNext() {}
}
