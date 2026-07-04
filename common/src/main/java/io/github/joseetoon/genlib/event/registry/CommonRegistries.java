package io.github.joseetoon.genlib.event.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import io.github.joseetoon.genlib.util.RegistryUtils;

@SuppressWarnings("unused")
public class CommonRegistries {
    public static final RegistryHandle<Block> BLOCKS = RegistryUtils.getHandle(Registries.BLOCK);
    public static final RegistryHandle<Fluid> FLUIDS = RegistryUtils.getHandle(Registries.FLUID);
    public static final RegistryHandle<Item> ITEMS = RegistryUtils.getHandle(Registries.ITEM);
    public static final RegistryHandle<EntityType<?>> ENTITIES = RegistryUtils.getHandle(Registries.ENTITY_TYPE);
}
