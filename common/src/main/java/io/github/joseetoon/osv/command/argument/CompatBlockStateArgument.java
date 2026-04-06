package io.github.joseetoon.osv.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * 1.20.1-compatible wrapper with a no-arg constructor so GenLib can instantiate
 * it through reflection for @Node(type = ...).
 */
public class CompatBlockStateArgument implements ArgumentType<BlockInput> {

    private static final CommandBuildContext BUILD_CONTEXT = CommandBuildContext.simple(
        RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY),
        FeatureFlags.DEFAULT_FLAGS
    );

    private final BlockStateArgument delegate = BlockStateArgument.block(BUILD_CONTEXT);

    @Override
    public BlockInput parse(final StringReader reader) throws CommandSyntaxException {
        return this.delegate.parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
            final CommandContext<S> context, final SuggestionsBuilder builder) {
        return this.delegate.listSuggestions(context, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return this.delegate.getExamples();
    }
}
