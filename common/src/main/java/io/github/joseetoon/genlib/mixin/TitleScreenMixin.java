package io.github.joseetoon.genlib.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.joseetoon.genlib.client.gui.LibErrorMenu;
import io.github.joseetoon.genlib.event.error.LibErrorContext;
import io.github.joseetoon.genlib.event.lifecycle.CheckErrorsEvent;
import io.github.joseetoon.genlib.event.lifecycle.GameReadyEvent;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    private static final AtomicBoolean INIT_COMPLETE = new AtomicBoolean(false);

    protected TitleScreenMixin(final Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void postInit(final CallbackInfo ci) {
        if (INIT_COMPLETE.compareAndSet(false, true)) {
            CheckErrorsEvent.EVENT.invoker().run();
            if (LibErrorContext.hasErrors()) {
                LibErrorContext.logClientErrors();
                Minecraft.getInstance().setScreen(new LibErrorMenu(this));
            } else {
                GameReadyEvent.CLIENT.invoker().run();
            }
        }
    }
}
