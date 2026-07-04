package io.github.joseetoon.genlib.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import io.github.joseetoon.genlib.event.error.LibErrorContext;

@Environment(EnvType.CLIENT)
public final class ClientErrorBroadcaster {

    private ClientErrorBroadcaster() {}

    public static void broadcastErrors() {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            LibErrorContext.broadcastErrors(player);
        }
    }
}
