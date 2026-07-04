package io.github.joseetoon.genlib.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.experimental.UtilityClass;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import io.github.joseetoon.genlib.data.ModDescriptor;
import io.github.joseetoon.genlib.util.McUtils;
import personthecat.overwritevalidator.annotations.Inherit;
import personthecat.overwritevalidator.annotations.InheritMissingMembers;
import personthecat.overwritevalidator.annotations.Overwrite;
import personthecat.overwritevalidator.annotations.OverwriteClass;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
@OverwriteClass
@InheritMissingMembers
public class LibCommandRegistrar {

    private static final Map<LiteralArgumentBuilder<CommandSourceStack>, CommandSide> COMMANDS = new ConcurrentHashMap<>();

    @Inherit
    public static void registerCommands(final ModDescriptor mod, final boolean libCommands, final Class<?>... types) {
        final CommandRegistrationContext ctx = CommandRegistrationContext.forMod(mod);
        CommandClassEvaluator.getBuilders(mod, types).forEach(ctx::addCommand);
        if (libCommands) ctx.addLibCommands();
        ctx.registerAll();
    }

    @Inherit
    public static void registerCommand(final LiteralArgumentBuilder<CommandSourceStack> cmd) {
        registerCommand(cmd, CommandSide.EITHER);
    }

    @Overwrite
    @SuppressWarnings("unused")
    public static void registerCommand(final LiteralArgumentBuilder<CommandSourceStack> cmd, final CommandSide side) {
        COMMANDS.put(cmd, side);
    }

    public static void copyInto(final Commands manager) {
        final boolean dedicated = McUtils.isDedicatedServer();
        COMMANDS.forEach((cmd, side) -> {
            if (side.canRegister(dedicated)) manager.getDispatcher().register(cmd);
        });
    }
}
