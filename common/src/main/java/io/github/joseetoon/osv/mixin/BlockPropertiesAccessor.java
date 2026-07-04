package io.github.joseetoon.osv.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;
import java.util.function.ToIntFunction;

@Mixin(BlockBehaviour.Properties.class)
public interface BlockPropertiesAccessor {

    @Accessor
    Function<BlockState, MapColor> getMapColor();

    @Accessor
    void setMapColor(final Function<BlockState, MapColor> getter);

    @Accessor
    boolean getHasCollision();

    @Accessor
    void setHasCollision(final boolean b);

    @Accessor
    SoundType getSoundType();

    @Accessor
    void setSoundType(final SoundType type);

    @Accessor
    ToIntFunction<BlockState> getLightEmission();

    @Accessor
    void setLightEmission(final ToIntFunction<BlockState> light);

    @Accessor
    float getExplosionResistance();

    @Accessor
    void setExplosionResistance(final float resistance);

    @Accessor
    float getDestroyTime();

    @Accessor
    void setDestroyTime(final float time);

    @Accessor
    boolean getRequiresCorrectToolForDrops();

    @Accessor
    void setRequiresCorrectToolForDrops(final boolean b);

    @Accessor
    boolean getIsRandomlyTicking();

    @Accessor
    void setIsRandomlyTicking(final boolean b);

    @Accessor
    float getFriction();

    @Accessor
    void setFriction(final float friction);

    @Accessor
    float getSpeedFactor();

    @Accessor
    void setSpeedFactor(final float factor);

    @Accessor
    float getJumpFactor();

    @Accessor
    void setJumpFactor(final float factor);

    @Accessor
    ResourceKey<LootTable> getDrops();

    @Accessor
    void setDrops(final ResourceKey<LootTable> id);

    @Accessor
    boolean getCanOcclude();

    @Accessor
    void setCanOcclude(final boolean b);

    @Accessor
    boolean getIsAir();

    @Accessor
    void setIsAir(final boolean b);

    @Accessor
    BlockBehaviour.StateArgumentPredicate<EntityType<?>> getIsValidSpawn();

    @Accessor
    void setIsValidSpawn(final BlockBehaviour.StateArgumentPredicate<EntityType<?>> predicate);

    @Accessor
    BlockBehaviour.StatePredicate getIsRedstoneConductor();

    @Accessor
    void setIsRedstoneConductor(final BlockBehaviour.StatePredicate predicate);

    @Accessor
    BlockBehaviour.StatePredicate getIsSuffocating();

    @Accessor
    void setIsSuffocating(final BlockBehaviour.StatePredicate predicate);

    @Accessor
    BlockBehaviour.StatePredicate getIsViewBlocking();

    @Accessor
    void setIsViewBlocking(final BlockBehaviour.StatePredicate predicate);

    @Accessor
    BlockBehaviour.StatePredicate getHasPostProcess();

    @Accessor
    void setHasPostProcess(final BlockBehaviour.StatePredicate predicate);

    @Accessor
    BlockBehaviour.StatePredicate getEmissiveRendering();

    @Accessor
    void setEmissiveRendering(final BlockBehaviour.StatePredicate predicate);

    @Accessor
    boolean getDynamicShape();

    @Accessor
    void setDynamicShape(final boolean b);

    @Accessor
    PushReaction getPushReaction();

    @Accessor
    void setPushReaction(final PushReaction reaction);

    @Accessor
    boolean getReplaceable();

    @Accessor
    void setReplaceable(final boolean b);

    @Accessor
    boolean getLiquid();

    @Accessor
    void setLiquid(final boolean b);
}
