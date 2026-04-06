package io.github.joseetoon.osv.client.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;

public class VariantColorizerImpl {

    public static ItemColors getItemColors() {
        return Minecraft.getInstance().getItemColors();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static BlockColor getColor(final BlockColors registry, final Block block) {
        try {
            final Field field = BlockColors.class.getDeclaredField("blockColors");
            field.setAccessible(true);
            final Map<Object, BlockColor> colors = (Map<Object, BlockColor>) field.get(registry);
            return colors.get(block.builtInRegistryHolder());
        } catch (final ReflectiveOperationException ignored) {
            return null;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static ItemColor getColor(final ItemColors registry, final Item item) {
        try {
            final Field field = ItemColors.class.getDeclaredField("itemColors");
            field.setAccessible(true);
            final Map<Object, ItemColor> colors = (Map<Object, ItemColor>) field.get(registry);
            return colors.get(item.builtInRegistryHolder());
        } catch (final ReflectiveOperationException ignored) {
            return null;
        }
    }
}
