package io.github.joseetoon.genlib.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import net.minecraft.commands.SharedSuggestionProvider;
import org.hjson.JsonObject;
import io.github.joseetoon.genlib.command.CommandUtils;
import io.github.joseetoon.genlib.data.JsonPath;
import io.github.joseetoon.genlib.util.HjsonUtils;
import io.github.joseetoon.genlib.util.LibReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.github.joseetoon.genlib.exception.Exceptions.cmdSyntax;

@SuppressWarnings("unused")
public class PathArgument implements ArgumentType<JsonPath> {

    public static void register() {
        // Argument type synchronization registration removed in 1.20.1
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> ctx, final SuggestionsBuilder builder) {
        final Optional<JsonObject> json = CommandUtils.getLastArg(ctx, HjsonArgument.class, HjsonArgument.Result.class)
            .map(arg -> arg.json.get());
        if (!json.isPresent()) {
            return Suggestions.empty();
        }
        final JsonPath path = CommandUtils.getLastArg(ctx, PathArgument.class, JsonPath.class)
            .orElseGet(() -> new JsonPath(Collections.emptyList()));
        return SharedSuggestionProvider.suggest(HjsonUtils.getPaths(json.get(), path), builder);
    }

    @Override
    public JsonPath parse(final StringReader reader) throws CommandSyntaxException {
        return JsonPath.parse(reader);
    }
}