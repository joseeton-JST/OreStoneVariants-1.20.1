package io.github.joseetoon.osv.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(BlockBehaviour.class)
public interface BlockBehaviourInvoker {

    @Invoker("isRandomlyTicking")
    boolean invokeIsRandomlyTicking(BlockState state);

    @Invoker("getDrops")
    List<ItemStack> invokeGetDrops(BlockState state, LootParams.Builder builder);

    @Invoker("propagatesSkylightDown")
    boolean invokePropagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos);

    @Invoker("updateShape")
    BlockState invokeUpdateShape(BlockState state, Direction dir, BlockState facing, LevelAccessor level, BlockPos from, BlockPos to);

    @Invoker("neighborChanged")
    void invokeNeighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos at, boolean bl);

    @Invoker("onPlace")
    void invokeOnPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean bl);

    @Invoker("onRemove")
    void invokeOnRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean bl);

    @Invoker("useWithoutItem")
    InteractionResult invokeUseWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit);

    @Invoker("useItemOn")
    ItemInteractionResult invokeUseItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit);

    @Invoker("triggerEvent")
    boolean invokeTriggerEvent(BlockState state, Level level, BlockPos pos, int i, int j);

    @Invoker("getRenderShape")
    RenderShape invokeGetRenderShape(BlockState state);

    @Invoker("useShapeForLightOcclusion")
    boolean invokeUseShapeForLightOcclusion(BlockState state);

    @Invoker("getFluidState")
    FluidState invokeGetFluidState(BlockState state);

    @Invoker("isSignalSource")
    boolean invokeIsSignalSource(BlockState state);

    @Invoker("hasAnalogOutputSignal")
    boolean invokeHasAnalogOutputSignal(BlockState state);

    @Invoker("rotate")
    BlockState invokeRotate(BlockState state, Rotation rotation);

    @Invoker("mirror")
    BlockState invokeMirror(BlockState state, Mirror mirror);

    @Invoker("getOcclusionShape")
    VoxelShape invokeGetOcclusionShape(BlockState state, BlockGetter getter, BlockPos pos);

    @Invoker("getShape")
    VoxelShape invokeGetShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx);

    @Invoker("getBlockSupportShape")
    VoxelShape invokeGetBlockSupportShape(BlockState state, BlockGetter getter, BlockPos pos);

    @Invoker("getInteractionShape")
    VoxelShape invokeGetInteractionShape(BlockState state, BlockGetter getter, BlockPos pos);

    @Invoker("getLightBlock")
    int invokeGetLightBlock(BlockState state, BlockGetter getter, BlockPos pos);

    @Invoker("getMenuProvider")
    MenuProvider invokeGetMenuProvider(BlockState state, Level level, BlockPos pos);

    @Invoker("canSurvive")
    boolean invokeCanSurvive(BlockState state, LevelReader level, BlockPos pos);

    @Invoker("getShadeBrightness")
    float invokeGetShadeBrightness(BlockState state, BlockGetter getter, BlockPos pos);

    @Invoker("getAnalogOutputSignal")
    int invokeGetAnalogOutputSignal(BlockState state, Level level, BlockPos pos);

    @Invoker("getCollisionShape")
    VoxelShape invokeGetCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx);

    @Invoker("getVisualShape")
    VoxelShape invokeGetVisualShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx);

    @Invoker("getDestroyProgress")
    float invokeGetDestroyProgress(BlockState state, Player player, BlockGetter getter, BlockPos pos);

    @Invoker("spawnAfterBreak")
    void invokeSpawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience);

    @Invoker("attack")
    void invokeAttack(BlockState state, Level level, BlockPos pos, Player player);

    @Invoker("getSignal")
    int invokeGetSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction dir);

    @Invoker("entityInside")
    void invokeEntityInside(BlockState state, Level level, BlockPos pos, Entity entity);

    @Invoker("getDirectSignal")
    int invokeGetDirectSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction dir);

    @Invoker("onProjectileHit")
    void invokeOnProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile);

    @Invoker("tick")
    void invokeTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand);

    @Invoker("randomTick")
    void invokeRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand);
}
