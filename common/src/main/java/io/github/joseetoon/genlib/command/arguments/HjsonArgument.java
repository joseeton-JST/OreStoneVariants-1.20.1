package io.github.joseetoon.genlib.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import org.hjson.JsonObject;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.command.CommandUtils;
import io.github.joseetoon.genlib.data.JsonType;
import io.github.joseetoon.genlib.data.Lazy;
import io.github.joseetoon.genlib.util.LibReference;
import io.github.joseetoon.genlib.util.McUtils;
import io.github.joseetoon.genlib.util.PathUtils;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static io.github.joseetoon.genlib.exception.Exceptions.cmdSyntax;
import static io.github.joseetoon.genlib.util.HjsonUtils.readSuppressing;
import static io.github.joseetoon.genlib.util.PathUtils.extension;

@SuppressWarnings("unused")
public class HjsonArgument implements ArgumentType<HjsonArgument.Result> {

    public static void register() {
        // Argument type synchronization registration removed in 1.20.1
    }

    private final FileArgument getter;

    public HjsonArgument(final File dir) {
        this(new FileArgument(dir));
    }

    public HjsonArgument(final File dir, final boolean recursive) {
        this(new FileArgument(dir, recursive));
    }

    public HjsonArgument(final File dir, @Nullable final File preferred, final boolean recursive) {
        this(new FileArgument(dir, preferred, recursive));
    }

    protected HjsonArgument(final FileArgument getter) {
        this.getter = getter;
    }

    @Override
    public Result parse(final StringReader reader) throws CommandSyntaxException {
        final File f = this.getter.parse(reader);
        final String ext = extension(f);
        if (f.exists() && !(f.isDirectory() || JsonType.isSupported(ext))) {
            throw cmdSyntax(reader, "Unsupported format");
        }
        return new Result(getter.dir, f);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> ctx, final SuggestionsBuilder builder) {
        final Stream<String> neighbors = CommandUtils.getLastArg(ctx, HjsonArgument.class, Result.class)
            .map(result -> this.getter.suggestPaths(ctx, result.file))
            .orElseGet(() -> this.getter.suggestPaths(ctx));
        return SharedSuggestionProvider.suggest(neighbors, builder);
    }

    public static class Result {

        private final File root;
        public final File file;
        public final Lazy<JsonObject> json;

        private Result(final File root, final File file) {
            this.root = root;
            this.file = file;
            this.json = Lazy.of(() -> {
                synchronized(this) {
                    return readSuppressing(file).orElseGet(JsonObject::new);
                }
            });
        }

        public Stream<String> getNeighbors() {
            return PathUtils.getSimpleContents(root, file);
        }
    }
}