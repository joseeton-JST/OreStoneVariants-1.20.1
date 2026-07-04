package io.github.joseetoon.genlib.util;

import com.mojang.brigadier.StringReader;
import lombok.experimental.UtilityClass;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import io.github.joseetoon.genlib.event.registry.DynamicRegistries;
import io.github.joseetoon.genlib.event.registry.RegistryHandle;
import personthecat.fresult.Result;
import personthecat.overwritevalidator.annotations.Inherit;
import personthecat.overwritevalidator.annotations.InheritMissingMembers;
import personthecat.overwritevalidator.annotations.Overwrite;
import personthecat.overwritevalidator.annotations.OverwriteClass;

import java.io.File;
import java.util.Optional;

import static io.github.joseetoon.genlib.exception.Exceptions.noBiomeNamed;
import static io.github.joseetoon.genlib.exception.Exceptions.noBlockNamed;
import static io.github.joseetoon.genlib.exception.Exceptions.noItemNamed;

@UtilityClass
@OverwriteClass
@InheritMissingMembers
@SuppressWarnings("unused")
public class McUtils {

    private static final RegistryAccess.Frozen BUILTIN_REGISTRIES =
        RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    @Overwrite
    public static File getConfigDir() {
        return FMLPaths.CONFIGDIR.get().toFile();
    }

    @Overwrite
    public static String getPlatform() {
        return "forge";
    }

    @Overwrite
    public static boolean isModLoaded(final String id) {
        return ModList.get().isLoaded(id);
    }

    @Overwrite
    public static boolean isDedicatedServer() {
        return Dist.DEDICATED_SERVER == FMLEnvironment.dist;
    }

    @Inherit
    public static boolean isClientSide() {
        return !isDedicatedServer();
    }

    @Overwrite
    public static Optional<Block> getBlock(final ResourceLocation id) {
        return BuiltInRegistries.BLOCK.getOptional(id);
    }

    @Overwrite
    public static Iterable<Block> getAllBlocks() {
        return BuiltInRegistries.BLOCK;
    }

    @NotNull
    @Inherit
    public static Block assertBlock(final ResourceLocation id) {
        return getBlock(id).orElseThrow(() -> noBlockNamed(id.toString()));
    }

    @NotNull
    @Inherit
    public static BlockState assertBlockState(final ResourceLocation id) {
        return assertBlock(id).defaultBlockState();
    }

    @Inherit
    public static Optional<BlockState> getBlockState(final ResourceLocation id) {
        return getBlock(id).map(Block::defaultBlockState);
    }

    @NotNull
    @Inherit
    public static BlockState assertParseBlockState(final String state) {
        return parseBlockState(state).orElseThrow(() -> noBlockNamed(state));
    }

    @Inherit
    public static Optional<BlockState> parseBlockState(final String state) {
        return Result.suppress(() ->
            BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), new StringReader(state), true).blockState()
        ).get(Result::IGNORE);
    }

    @Overwrite
    public static Optional<Item> getItem(final ResourceLocation id) {
        return BuiltInRegistries.ITEM.getOptional(id);
    }

    @Overwrite
    public static Iterable<Item> getAllItems() {
        return BuiltInRegistries.ITEM;
    }

    @NotNull
    @Inherit
    public static Item assertItem(final ResourceLocation id) {
        return getItem(id).orElseThrow(() -> noItemNamed(id.toString()));
    }

    @NotNull
    @Inherit
    public static Item assertParseItem(final String item) {
        return parseItem(item).orElseThrow(() -> noItemNamed(item));
    }

    @Inherit
    public static Optional<Item> parseItem(final String item) {
        return Result.suppress(() ->
            new ItemParser(BUILTIN_REGISTRIES).parse(new StringReader(item)).item().value()
        ).get(Result::IGNORE);
    }

    @NotNull
    @Inherit
    public static Biome assertBiome(final ResourceLocation id) {
        return getBiome(id).orElseThrow(() -> noBiomeNamed(id.toString()));
    }

    @Inherit
    public static Optional<Biome> getBiome(final ResourceLocation id) {
        return Optional.ofNullable(DynamicRegistries.BIOMES.lookup(id));
    }

    @Inherit
    public static RegistryHandle<Biome> getAllBiomes() {
        return DynamicRegistries.BIOMES;
    }
}
