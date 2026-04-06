package io.github.joseetoon.osv.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import io.github.joseetoon.osv.ModRegistries;
import io.github.joseetoon.osv.preset.OrePreset;
import io.github.joseetoon.osv.util.Reference;

import java.util.concurrent.CompletableFuture;

import static io.github.joseetoon.genlib.exception.Exceptions.cmdSyntax;

public class OrePresetArgument implements ArgumentType<OrePreset> {

    public static void register() {
        // TODO: Register argument type via ArgumentTypeInfos on 1.20.1
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> ctx, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ModRegistries.ORE_PRESETS.keySet(), builder);
    }

    @Override
    public OrePreset parse(final StringReader reader) throws CommandSyntaxException {
        final OrePreset preset = ModRegistries.ORE_PRESETS.get(reader.readString());
        if (preset == null) throw cmdSyntax(reader, "No such preset");
        return preset;
    }
}
