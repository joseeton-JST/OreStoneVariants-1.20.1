package io.github.joseetoon.genlib.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public final class ClientScreenSetter {

    private ClientScreenSetter() {}

    public static void setScreen(final Object screen) {
        Minecraft.getInstance().setScreen((Screen) screen);
    }
}
