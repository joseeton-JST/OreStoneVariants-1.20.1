package io.github.joseetoon.genlib.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.event.error.LibErrorContext;
import io.github.joseetoon.genlib.exception.FormattedException;

import java.util.ArrayList;
import java.util.List;

public class LibErrorMenu extends LibMenu {

    private static final int PAD        = 8;
    private static final int TITLE_H    = 28;
    private static final int BUTTONS_H  = 42;
    private static final int LINE_H     = 9; // default MC font line height

    private static final int COL_TITLE   = 0xFFFF5555;
    private static final int COL_LABEL   = 0xFFFF9955;
    private static final int COL_MESSAGE = 0xFFFFFFFF;
    private static final int COL_CAUSE   = 0xFFAAAAAA;

    private final List<FormattedException> errors = new ArrayList<>();
    private int scrollOffset = 0;
    private int totalHeight  = 0;

    public LibErrorMenu(@Nullable Screen parent) {
        super(parent, Component.translatable("catlib.errorMenu.numErrors", LibErrorContext.numMods()));
        LibErrorContext.getCommon().forEach((mod, list) -> this.errors.addAll(list));
        LibErrorContext.getFatal().forEach((mod, list) -> this.errors.addAll(list));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        this.renderBackground(g, mouseX, mouseY, partial);

        final int listTop    = TITLE_H + 2;
        final int listBottom = this.height - BUTTONS_H;
        final int listW      = this.width - PAD * 2 - 6; // 6px reserved for scrollbar

        // ── Header ────────────────────────────────────────────────────────────────
        g.fill(0, 0, this.width, TITLE_H, 0xDD000000);
        g.drawCenteredString(this.font, this.title, this.width / 2, (TITLE_H - LINE_H) / 2, COL_TITLE);

        // ── Clipped content area ───────────────────────────────────────────────
        g.enableScissor(PAD, listTop, this.width - PAD, listBottom);

        int y = listTop - this.scrollOffset + 4;

        for (int i = 0; i < this.errors.size(); i++) {
            final FormattedException error = this.errors.get(i);

            // entry separator
            if (i > 0) {
                g.fill(PAD, y - 2, PAD + listW, y - 1, 0x50FF5555);
            }

            // — Title (e.g. "coal_ore.hjson") ————————————————————————————————————
            g.drawString(this.font, error.getTitleMessage(), PAD + 2, y, COL_LABEL, false);
            y += LINE_H + 2;

            // — Display message ———————————————————————————————————————————————————
            final Component display = error.getDisplayMessage();
            g.drawWordWrap(this.font, display, PAD + 2, y, listW, COL_MESSAGE);
            y += this.font.split(display, listW).size() * LINE_H + 2;

            // — Cause / tooltip ———————————————————————————————————————————————————
            final Component tooltip = error.getTooltip();
            if (tooltip != null) {
                final String raw = tooltip.getString();
                final String truncated = raw.length() > 400 ? raw.substring(0, 400) + " [...]" : raw;
                final Component cause = Component.literal(truncated);
                g.drawWordWrap(this.font, cause, PAD + 2, y, listW, COL_CAUSE);
                y += this.font.split(cause, listW).size() * LINE_H + 2;
            }

            y += PAD;
        }

        if (this.errors.isEmpty()) {
            g.drawCenteredString(this.font,
                Component.literal("No error details available."), this.width / 2, (listTop + listBottom) / 2, COL_CAUSE);
        }

        this.totalHeight = y + this.scrollOffset - listTop;
        g.disableScissor();

        // ── Scrollbar ─────────────────────────────────────────────────────────
        final int visible = listBottom - listTop;
        if (this.totalHeight > visible) {
            final int barH    = Math.max(20, visible * visible / this.totalHeight);
            final int maxScr  = this.totalHeight - visible;
            final int barY    = listTop + (maxScr > 0
                ? (int) ((long) this.scrollOffset * (visible - barH) / maxScr) : 0);
            final int barX    = this.width - PAD + 1;
            g.fill(barX, listTop,  barX + 3, listBottom, 0x30FFFFFF);
            g.fill(barX, barY,     barX + 3, barY + barH, 0xBBFFFFFF);
        }

        // ── Buttons ───────────────────────────────────────────────────────────
        if (this.previous != null) this.previous.render(g, mouseX, mouseY, partial);
        if (this.cancel   != null) this.cancel.render(g, mouseX, mouseY, partial);
        if (this.next     != null) this.next.render(g, mouseX, mouseY, partial);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        final int visible  = this.height - BUTTONS_H - TITLE_H - 2;
        final int maxScroll = Math.max(0, this.totalHeight - visible);
        this.scrollOffset  = (int) Math.max(0, Math.min(maxScroll, this.scrollOffset - deltaY * LINE_H * 3));
        return true;
    }

    public LibErrorMenu loadImmediately() { return this; }
    public boolean hasPreviousError() { return false; }
    public boolean hasNextError() { return false; }
    public Screen previousError() { return this; }
    public Screen nextError() { return this; }
}
