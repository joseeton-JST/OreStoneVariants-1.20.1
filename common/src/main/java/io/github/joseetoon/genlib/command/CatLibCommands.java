package io.github.joseetoon.genlib.command;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.MutableComponent;
import io.github.joseetoon.genlib.client.gui.LibErrorMenu;
import io.github.joseetoon.genlib.event.error.LibErrorContext;
import io.github.joseetoon.genlib.util.LibReference;

public class CatLibCommands {

    public static final LibCommandBuilder ERROR_MENU =
        LibCommandBuilder.named("errors")
            .description("Opens the error menu.")
            .side(CommandSide.CLIENT)
            .mod(LibReference.MOD_DESCRIPTOR)
            .generate((builder, utl) ->
                builder.executes(utl.wrap(CatLibCommands::displayErrors)));

    @Environment(EnvType.CLIENT)
    private static void displayErrors(final CommandContextWrapper ctx) {
        if (!LibErrorContext.hasErrors()) {
            ctx.sendMessage(Component.translatable("catlib.errorText.noErrors")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
        } else {
            ctx.setScreen(new LibErrorMenu(null).loadImmediately());
        }
    }
}
