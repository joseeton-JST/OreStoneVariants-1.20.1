package io.github.joseetoon.genlib.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import io.github.joseetoon.genlib.data.MultiValueMap;

import java.util.ArrayList;
import java.util.List;

public class CategorizedList extends ObjectSelectionList<CategorizedList.ListEntry> {

    private final List<ButtonEntry> buttons = new ArrayList<>();
    private final int rowLeft;
    private final int rowWidth;

    public CategorizedList(Screen parent, int x0, int x1, MultiValueMap<String, AbstractWidget> widgets) {
        super(Minecraft.getInstance(), x1 - x0, parent.height - 85, 35, 22);
        this.rowLeft = x0;
        this.rowWidth = x1 - x0;
        this.setX(x0);
        this.setY(35);
        widgets.forEach((category, ws) -> {
            if (category != null && !category.isEmpty()) {
                this.addEntry(new Category(Component.translatable(category)));
            }
            ws.forEach(this::addButton);
        });
    }

    public static Button createButton(Component display, Button.OnPress onPress) {
        return Button.builder(display, onPress).size(0, 20).build();
    }

    private void addButton(AbstractWidget widget) {
        final ButtonEntry entry = new ButtonEntry(widget);
        this.addEntry(entry);
        this.buttons.add(entry);
    }

    public int numButtons() { return this.buttons.size(); }
    public void selectButton(int button) { this.buttons.get(button).widget.active = false; }
    public void deselectAll() { this.buttons.forEach(b -> b.widget.active = true); }

    @Override
    public int getRowLeft() {
        return this.rowLeft;
    }

    @Override
    public int getRowWidth() {
        return this.rowWidth;
    }

    @Override
    protected void renderListBackground(final GuiGraphics graphics) {
    }

    public abstract static class ListEntry extends ObjectSelectionList.Entry<ListEntry> {}

    public static class Category extends ListEntry {
        private final Component label;
        public Category(Component label) { this.label = label; }

        @Override
        public Component getNarration() { return this.label; }

        @Override
        public void render(GuiGraphics graphics, int idx, int top, int left, int w, int h, int mx, int my, boolean hover, float partial) {
            graphics.drawCenteredString(Minecraft.getInstance().font, this.label, left + w / 2, top + (h - 8) / 2, 0xFFFFFF);
        }
    }

    public static class ButtonEntry extends ListEntry {
        public final AbstractWidget widget;
        public ButtonEntry(AbstractWidget widget) { this.widget = widget; }

        @Override
        public Component getNarration() { return this.widget.getMessage(); }

        @Override
        public void render(GuiGraphics graphics, int idx, int top, int left, int w, int h, int mx, int my, boolean hover, float partial) {
            this.widget.setPosition(left, top);
            this.widget.render(graphics, mx, my, partial);
        }
    }
}
