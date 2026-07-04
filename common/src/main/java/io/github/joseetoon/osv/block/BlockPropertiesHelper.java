package io.github.joseetoon.osv.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockBehaviour.StateArgumentPredicate;
import net.minecraft.world.level.block.state.BlockBehaviour.StatePredicate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import io.github.joseetoon.osv.mixin.BlockBehaviourAccessor;
import io.github.joseetoon.osv.mixin.BlockPropertiesAccessor;
import io.github.joseetoon.osv.preset.OrePreset;
import io.github.joseetoon.osv.preset.data.BlockSettings;
import io.github.joseetoon.osv.preset.data.StateSettings;
import io.github.joseetoon.osv.preset.data.VariantSettings;

import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class BlockPropertiesHelper {

    public static Properties merge(final OrePreset preset, final Block bg, final Block fg) {
        final Properties merged = Properties.ofFullCopy(fg);
        final Context ctx = new Context(preset, bg, fg);

        final BlockPropertiesAccessor accessor = (BlockPropertiesAccessor) merged;
        accessor.setMapColor(ctx.mapColor());
        accessor.setHasCollision(ctx.hasCollision());
        accessor.setSoundType(ctx.soundType());
        accessor.setExplosionResistance(ctx.explosionResistance());
        accessor.setDestroyTime(ctx.destroyTime());
        accessor.setRequiresCorrectToolForDrops(ctx.requiresCorrectToolForDrops());
        accessor.setIsRandomlyTicking(ctx.isRandomlyTicking());
        accessor.setFriction(ctx.friction());
        accessor.setSpeedFactor(ctx.speedFactor());
        accessor.setJumpFactor(ctx.jumpFactor());
        accessor.setDrops(ctx.drops());
        accessor.setCanOcclude(ctx.canOcclude());
        accessor.setIsAir(ctx.isAir());
        accessor.setDynamicShape(ctx.dynamicShape());
        accessor.setLightEmission(ctx.lightEmission());
        accessor.setIsValidSpawn(ctx.isValidSpawn());
        accessor.setIsRedstoneConductor(ctx.isRedstoneConductor());
        accessor.setIsSuffocating(ctx.isSuffocating());
        accessor.setIsViewBlocking(ctx.isViewBlocking());
        accessor.setHasPostProcess(ctx.hasPostProcess());
        accessor.setEmissiveRendering(ctx.emissiveRendering());
        accessor.setPushReaction(ctx.pushReaction());
        accessor.setReplaceable(ctx.replaceable());
        accessor.setLiquid(ctx.liquid());

        return merged;
    }

    private static class Context {
        final BlockPropertiesAccessor bgp;
        final BlockPropertiesAccessor fgp;
        final VariantSettings ore;
        final BlockSettings block;
        final StateSettings state;
        final OrePreset preset;
        final Block bg;
        final Block fg;

        Context(final OrePreset preset, final Block bg, final Block fg) {
            this.bgp = (BlockPropertiesAccessor) ((BlockBehaviourAccessor) bg).getProperties();
            this.fgp = (BlockPropertiesAccessor) ((BlockBehaviourAccessor) fg).getProperties();
            this.ore = preset.getVariant();
            this.block = preset.getBlock();
            this.state = preset.getState();
            this.preset = preset;
            this.bg = bg;
            this.fg = fg;
        }

        Function<BlockState, MapColor> mapColor() {
            if (this.state.getMaterialColor() != null) {
                // OSV's MaterialColor (StateMap) needs to be mapped to MapColor
                // For now, let's assume the preset's materialColor getter handles the conversion to MapColor if needed.
                // Wait, StateSettings.getMaterialColor() returns a helper that creates a Function.
                // In 1.20, that function should return MapColor.
                return (Function<BlockState, MapColor>) (Object) this.state.getMaterialColor().createFunction();
            } else if (this.ore.isBgImitation()) {
                return getBg(this.bgp.getMapColor());
            }
            return getFg(this.fgp.getMapColor());
        }

        boolean hasCollision() {
            if (this.block.getHasCollision() != null) {
                return this.block.getHasCollision();
            } else if (this.ore.isBgImitation()) {
                return this.bgp.getHasCollision();
            }
            return this.fgp.getHasCollision();
        }

        SoundType soundType() {
            if (this.block.getSoundType() != null) {
                return this.block.getSoundType();
            } else if (this.ore.isBgImitation()) {
                return this.bgp.getSoundType();
            }
            return this.fgp.getSoundType();
        }

        float explosionResistance() {
            if (this.block.getExplosionResistance() != null) {
                return this.block.getExplosionResistance();
            } else if (this.ore.isBgImitation()) {
                return Math.max(this.bgp.getExplosionResistance(), this.fgp.getExplosionResistance());
            }
            return this.fgp.getExplosionResistance();
        }

        float destroyTime() {
            if (this.block.getDestroyTime() != null) {
                return this.block.getDestroyTime();
            } else if (this.ore.isBgImitation()) {
                final float bgTime = this.bgp.getDestroyTime();
                final float fgTime = this.fgp.getDestroyTime();
                return bgTime < 0 ? -1.0F : Math.max(bgTime + fgTime - 1.5F, 0.0F);
            }
            return this.fgp.getDestroyTime();
        }

        boolean requiresCorrectToolForDrops() {
            if (this.block.getRequiresCorrectToolForDrops() != null) {
                return this.block.getRequiresCorrectToolForDrops();
            } else if (this.ore.isBgImitation()) {
                return this.bgp.getRequiresCorrectToolForDrops() && this.fgp.getRequiresCorrectToolForDrops();
            }
            return this.fgp.getRequiresCorrectToolForDrops();
        }

        boolean isRandomlyTicking() {
            if (this.block.getIsRandomlyTicking() != null) {
                return this.block.getIsRandomlyTicking();
            } else if (this.ore.isBgImitation()) {
                return this.bgp.getIsRandomlyTicking() || this.fgp.getIsRandomlyTicking();
            }
            return this.fgp.getIsRandomlyTicking();
        }

        float friction() {
            if (this.block.getFriction() != null) {
                return this.block.getFriction();
            } else if (this.ore.isBgImitation()) {
                final float bgFriction = this.bgp.getFriction();
                final float fgFriction = this.fgp.getFriction();
                return (bgFriction + fgFriction) / 2.0F;
            }
            return this.fgp.getFriction();
        }

        float speedFactor() {
            if (this.block.getSpeedFactor() != null) {
                return this.block.getSpeedFactor();
            } else if (this.ore.isBgImitation()) {
                final float bgFactor = this.bgp.getSpeedFactor();
                final float fgFactor = this.fgp.getSpeedFactor();
                return (bgFactor + fgFactor + fgFactor) / 3.0F;
            }
            return this.fgp.getSpeedFactor();
        }

        float jumpFactor() {
            if (this.block.getJumpFactor() != null) {
                return this.block.getJumpFactor();
            } else if (this.ore.isBgImitation()) {
                final float bgFactor = this.bgp.getJumpFactor();
                final float fgFactor = this.fgp.getJumpFactor();
                return (bgFactor + fgFactor + fgFactor) / 3.0F;
            }
            return this.fgp.getJumpFactor();
        }

        ResourceKey<LootTable> drops() {
            final ResourceLocation loot = this.preset.getLootReference();
            return loot != null ? ResourceKey.create(Registries.LOOT_TABLE, loot) : this.fgp.getDrops();
        }

        boolean canOcclude() {
            if (this.block.getCanOcclude() != null) {
                return this.block.getCanOcclude();
            } else if (this.ore.isBgImitation()) {
                return this.bgp.getCanOcclude() && this.fgp.getCanOcclude();
            }
            return this.fgp.getCanOcclude();
        }

        boolean isAir() {
            if (this.block.getIsAir() != null) {
                return this.block.getIsAir();
            } else if (this.ore.isBgImitation()) {
                return this.bgp.getIsAir() || this.fgp.getIsAir();
            }
            return this.fgp.getIsAir();
        }

        boolean dynamicShape() {
            if (this.block.getDynamicShape() != null) {
                return this.block.getDynamicShape();
            } else if (this.ore.isBgImitation()) {
                return this.bgp.getDynamicShape() || this.fgp.getDynamicShape();
            }
            return this.fgp.getDynamicShape();
        }

        ToIntFunction<BlockState> lightEmission() {
            if (this.state.getLightEmission() != null) {
                return this.state.getLightEmission().createFunction()::apply;
            } else if (this.ore.isBgImitation()) {
                return getMax(this.bgp.getLightEmission(), this.fgp.getLightEmission());
            }
            return getFg(this.fgp.getLightEmission());
        }

        StateArgumentPredicate<EntityType<?>> isValidSpawn() {
            if (this.state.getIsValidSpawn() != null) {
                final Function<BlockState, Set<EntityType<?>>> entities = this.state.getIsValidSpawn().createFunction();
                return (state, getter, pos, entity) -> entities.apply(state).contains(entity);
            } else if (this.ore.isBgImitation()) {
                return checkBoth(this.bgp.getIsValidSpawn(), this.fgp.getIsValidSpawn());
            }
            return checkFg(this.fgp.getIsValidSpawn());
        }

        StatePredicate isRedstoneConductor() {
            if (this.state.getIsRedstoneConductor() != null) {
                final Function<BlockState, Boolean> predicate = this.state.getIsRedstoneConductor().createFunction();
                return (state, getter, pos) -> predicate.apply(state);
            }
            return checkEither(this.bgp.getIsRedstoneConductor(), this.fgp.getIsRedstoneConductor());
        }

        StatePredicate isSuffocating() {
            if (this.state.getIsSuffocating() != null) {
                final Function<BlockState, Boolean> predicate = this.state.getIsSuffocating().createFunction();
                return (state, getter, pos) -> predicate.apply(state);
            } else if (this.ore.isBgImitation()) {
                return checkBg(this.bgp.getIsSuffocating());
            }
            return checkFg(this.fgp.getIsSuffocating());
        }

        StatePredicate isViewBlocking() {
            if (this.state.getIsViewBlocking() != null) {
                final Function<BlockState, Boolean> predicate = this.state.getIsViewBlocking().createFunction();
                return (state, getter, pos) -> predicate.apply(state);
            } else if (this.ore.isBgImitation()) {
                return checkBg(this.bgp.getIsViewBlocking());
            }
            return checkFg(this.fgp.getIsViewBlocking());
        }

        StatePredicate hasPostProcess() {
            if (this.state.getHasPostProcess() != null) {
                final Function<BlockState, Boolean> predicate = this.state.getHasPostProcess().createFunction();
                return (state, getter, pos) -> predicate.apply(state);
            }
            return checkEither(this.bgp.getHasPostProcess(), this.fgp.getHasPostProcess());
        }

        StatePredicate emissiveRendering() {
            if (this.state.getEmissiveRendering() != null) {
                final Function<BlockState, Boolean> predicate = this.state.getEmissiveRendering().createFunction();
                return (state, getter, pos) -> predicate.apply(state);
            }
            return checkEither(this.bgp.getEmissiveRendering(), this.fgp.getEmissiveRendering());
        }

        PushReaction pushReaction() {
            if (this.ore.isBgImitation()) {
                return this.bgp.getPushReaction();
            }
            return this.fgp.getPushReaction();
        }

        boolean replaceable() {
            if (this.ore.isBgImitation()) {
                return this.bgp.getReplaceable();
            }
            return this.fgp.getReplaceable();
        }

        boolean liquid() {
            if (this.ore.isBgImitation()) {
                return this.bgp.getLiquid();
            }
            return this.fgp.getLiquid();
        }

        <T> Function<BlockState, T> getBg(final Function<BlockState, T> bg) {
            return state -> bg.apply(asBg(state));
        }

        <T> Function<BlockState, T> getFg(final Function<BlockState, T> fg) {
            return state -> fg.apply(asFg(state));
        }

        ToIntFunction<BlockState> getMax(final ToIntFunction<BlockState> bg, final ToIntFunction<BlockState> fg) {
            return state -> Math.max(bg.applyAsInt(asBg(state)), fg.applyAsInt(asFg(state)));
        }

        ToIntFunction<BlockState> getFg(final ToIntFunction<BlockState> fg) {
            return state -> fg.applyAsInt(asFg(state));
        }

        StatePredicate checkBg(final StatePredicate bg) {
            return (state, getter, pos) -> bg.test(asBg(state), getter, pos);
        }

        StatePredicate checkFg(final StatePredicate fg) {
            return (state, getter, pos) -> fg.test(asFg(state), getter, pos);
        }

        <T> StateArgumentPredicate<T> checkFg(final StateArgumentPredicate<T> fg) {
            return (state, getter, pos, t) -> fg.test(asFg(state), getter, pos, t);
        }

        StatePredicate checkEither(final StatePredicate bg, final StatePredicate fg) {
            return (state, getter, pos) -> bg.test(asBg(state), getter, pos) || fg.test(asFg(state), getter, pos);
        }

        <T> StateArgumentPredicate<T> checkBoth(final StateArgumentPredicate<T> bg, final StateArgumentPredicate<T> fg) {
            return (state, getter, pos, t) -> bg.test(asBg(state), getter, pos, t) && fg.test(asFg(state), getter, pos, t);
        }

        BlockState asBg(final BlockState actual) {
            return ((OreVariant) actual.getBlock()).asOther(actual, this.bg);
        }

        BlockState asFg(final BlockState actual) {
            return ((OreVariant) actual.getBlock()).asOther(actual, this.fg);
        }
    }
}
