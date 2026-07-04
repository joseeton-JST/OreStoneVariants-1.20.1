package io.github.joseetoon.genlib.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import io.github.joseetoon.genlib.exception.Exceptions;
import io.github.joseetoon.genlib.util.LibReference;
import io.github.joseetoon.genlib.util.McUtils;
import io.github.joseetoon.genlib.util.Shorthand;
import personthecat.fresult.Void;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class EnumArgument<E extends Enum<E>> implements ArgumentType<E> {

    public static void register() {
        // Argument type synchronization registration removed in 1.20.1
    }

    private final Class<E> enumClass;

    private EnumArgument(final Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    public static <E extends Enum<E>> EnumArgument<?> of(final Class<E> enumClass) {
        return new EnumArgument<>(enumClass);
    }

    @Override
    public E parse(final StringReader reader) throws CommandSyntaxException {
        return Shorthand.getEnumConstant(reader.readUnquotedString(), this.enumClass)
            .orElseThrow(() -> Exceptions.cmdSyntax(reader, "No such value"));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> ctx, final SuggestionsBuilder sb) {
        return SharedSuggestionProvider.suggest(
            Stream.of(this.enumClass.getEnumConstants()).map(e -> e.toString().toLowerCase()), sb);
    }
}
