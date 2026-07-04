package io.github.joseetoon.genlib.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import io.github.joseetoon.genlib.client.gui.ErrorDetailPage;

@Environment(EnvType.CLIENT)
public final class ClientErrorScreens {

    private ClientErrorScreens() {}

    public static Object createDetailsScreen(final Object parent, final Component title, final Component details) {
        return new ErrorDetailPage((Screen) parent, title, details);
    }
}
