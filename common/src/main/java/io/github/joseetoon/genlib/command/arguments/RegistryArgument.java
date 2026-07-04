package io.github.joseetoon.genlib.command.arguments;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import io.github.joseetoon.genlib.data.Lazy;
import io.github.joseetoon.genlib.event.registry.CommonRegistries;
import io.github.joseetoon.genlib.event.registry.RegistryHandle;
import io.github.joseetoon.genlib.util.LibReference;
import io.github.joseetoon.genlib.util.RegistryUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.joseetoon.genlib.exception.Exceptions.cmdSyntax;

public class RegistryArgument<T> implements ArgumentType<T> {

    public static void register() {
        // Argument type synchronization registration removed in 1.20.1
    }

    private static final Map<Class<?>, RegistryArgument<?>> ARGUMENTS_BY_TYPE = new ConcurrentHashMap<>();

    private final RegistryHandle<T> handle;
    private final Lazy<List<String>> suggestions;

    public RegistryArgument(final RegistryHandle<T> handle) {
        this.handle = handle;
        this.suggestions = Lazy.of(() -> computeSuggestions(handle));
    }

    @SuppressWarnings("unchecked")
    public static <T> RegistryArgument<T> getOrThrow(final Class<T> clazz) {
        return (RegistryArgument<T>) ARGUMENTS_BY_TYPE.computeIfAbsent(clazz, c ->
            new RegistryArgument<>(RegistryUtils.getByType(c)));
    }

    @Override
    public T parse(final StringReader reader) throws CommandSyntaxException {
        final ResourceLocation id = ResourceLocation.read(reader);
        if (!this.handle.isRegistered(id)) {
            throw cmdSyntax(reader, "Feature not found");
        }
        return this.handle.lookup(id);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> ctx, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(this.suggestions.get(), builder);
    }

    private static List<String> computeSuggestions(final RegistryHandle<?> handle) {
        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        handle.forEach((id, value) -> {
            if ("minecraft".equals(id.getNamespace())) builder.add(id.getPath());
            builder.add(id.toString());
        });
        return builder.build();
    }
}
