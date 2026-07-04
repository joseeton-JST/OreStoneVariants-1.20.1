package io.github.joseetoon.genlib.serialization;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

public class EasyStateCodec implements Codec<BlockState> {

    public static final EasyStateCodec INSTANCE = new EasyStateCodec();

    EasyStateCodec() {}

    @Override
    public <T> DataResult<T> encode(final BlockState input, final DynamicOps<T> ops, final T prefix) {
        return DataResult.success(ops.createString(BlockStateParser.serialize(input)));
    }

    @Override
    public <T> DataResult<Pair<BlockState, T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getStringValue(input).flatMap(id -> {
            try {
                final BlockState state = BlockStateParser.parseForBlock(
                    BuiltInRegistries.BLOCK.asLookup(), new StringReader(id), false).blockState();
                return DataResult.success(Pair.of(state, input));
            } catch (final CommandSyntaxException e) {
                return DataResult.error(() -> e.getMessage());
            }
        });
    }

    @Override
    public String toString() {
        return "EasyStateCodec";
    }
}
