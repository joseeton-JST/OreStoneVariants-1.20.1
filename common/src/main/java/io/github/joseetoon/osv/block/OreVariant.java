package io.github.joseetoon.osv.block;

import dev.architectury.injectables.annotations.ExpectPlatform;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.util.Shorthand;
import io.github.joseetoon.osv.ModRegistries;
import io.github.joseetoon.osv.config.Cfg;
import io.github.joseetoon.osv.item.VariantItem;
import io.github.joseetoon.osv.mixin.BlockBehaviourInvoker;
import io.github.joseetoon.osv.mixin.UseOnContextAccessor;
import io.github.joseetoon.osv.preset.OrePreset;
import io.github.joseetoon.osv.world.interceptor.InterceptorAccessor;
import io.github.joseetoon.osv.world.interceptor.InterceptorDispatcher;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class OreVariant extends SharedStateBlock {

    protected final OrePreset preset;
    protected final Block bg;
    protected final Block fg;

    private final Map<BlockState, Item> itemMap = new HashMap<>();
    private final Predicate<BlockState> shouldTick;

    public OreVariant(final OrePreset preset, final Properties properties, final StateConfig config) {
        super(properties, config);
        this.preset = preset;
        this.bg = config.bg;
        this.fg = config.fg;
        this.shouldTick = this.optimizeTickBehavior();
    }

    @ExpectPlatform
    @SuppressWarnings("unused")
    public static OreVariant createPlatformVariant(final OrePreset preset, final Properties properties, final StateConfig config) {
        throw new AssertionError();
    }

    public OrePreset getPreset() {
        return this.preset;
    }

    public Block getBg() {
        return this.bg;
    }

    public Block getFg() {
        return this.fg;
    }

    @Contract("null -> null; !null -> !null")
    public BlockState asBg(final @Nullable BlockState me) {
        return copyInto(this.bg.defaultBlockState(), me);
    }

    @Contract("null -> null; !null -> !null")
    public BlockState asFg(final @Nullable BlockState me) {
        return copyInto(this.fg.defaultBlockState(), me);
    }

    @Override
    public Item asItem() {
        return this.asItem(this.defaultBlockState());
    }

    public Item asItem(final BlockState me) {
        return this.itemMap.computeIfAbsent(me, s -> {
            final Item item = ModRegistries.ITEMS.findByValue(i -> s.equals(i.getState())).orElse(null);
            return item != null ? item : this.fg.asItem();
        });
    }

    protected <L extends LevelAccessor> L prime(final L level, final BlockState actual, final Block in) {
        return InterceptorDispatcher.intercept(level, actual, in, null);
    }

    // Todo: the interceptor needs to *just* take the ore variant and dynamically match bg / fg
    protected <L extends LevelAccessor> L primeRestricted(final L level, final BlockState actual, final Block in, final BlockPos pos) {
        if (this.preset.getVariant().isBgDuplication()) return this.prime(level, actual, in);
        return InterceptorDispatcher.intercept(level, actual, in, pos);
    }

    private BlockBehaviourInvoker bgi() {
        return (BlockBehaviourInvoker) this.bg;
    }

    private BlockBehaviourInvoker fgi() {
        return (BlockBehaviourInvoker) this.fg;
    }

    private Predicate<BlockState> optimizeTickBehavior() {
        if (this.isRandomlyTicking) {
            return s -> true;
        }
        final Object2BooleanMap<BlockState> tickingStates = new Object2BooleanOpenHashMap<>();
        int numTicking = 0;
        for (final BlockState state : this.stateDefinition.getPossibleStates()) {
            final boolean ticking =
                this.bgi().invokeIsRandomlyTicking(this.asBg(state)) || this.fgi().invokeIsRandomlyTicking(this.asFg(state));
            tickingStates.put(state, ticking);
            if (ticking) numTicking++;
        }
        if (numTicking == 0) {
            return s -> false;
        } else if (numTicking == this.stateDefinition.getPossibleStates().size()) {
            return s -> true;
        }
        return tickingStates::getBoolean;
    }

    @Override
    public boolean isRandomlyTicking(final BlockState state) {
        return this.shouldTick.test(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(final BlockState state, final LootParams.Builder builder) {
        final List<ItemStack> drops;
        final LootTable table;
        if (this.preset.hasLootId()) {
            drops = super.getDrops(state, builder);
        } else if ((table = this.preset.getCustomLoot()) != null) {
            drops = table.getRandomItems(builder
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .create(LootContextParamSets.BLOCK));
        } else {
            drops = this.fgi().invokeGetDrops(this.asFg(state), builder);
        }
        return this.filterDrops(this.updateCount(drops, state, builder), state, builder);
    }

    private List<ItemStack> updateCount(final List<ItemStack> drops, final BlockState state, final LootParams.Builder builder) {
        final RandomSource rand = builder.getLevel().getRandom();
        for (final ItemStack drop : drops) {
            drop.setCount(this.getCount(rand, drop, state));
        }
        return drops;
    }

    private int getCount(final RandomSource rand, final ItemStack drop, final BlockState state) {
        int i = drop.getCount();
        if (AdditionalProperties.isDense(state)) {
            int m = Cfg.denseDropMultiplier();
            if (Cfg.randomDropCount()) {
                m = Math.max(1, m <= 1 ? 1 : (1 + rand.nextInt(m)));
            }
            i *= Math.max(m, Cfg.denseDropMultiplierMin());
        }
        return i;
    }

    private List<ItemStack> filterDrops(final List<ItemStack> drops, final BlockState state, final LootParams.Builder builder) {
        final List<ItemStack> clone = new ArrayList<>();
        for (final ItemStack drop : drops) {
            final ItemStack fgStack = new ItemStack(this.fg);
            if (ItemStack.isSameItem(drop, fgStack)) {
                if (Cfg.variantsSilkTouch() && this.hasSilkTouch(builder)) {
                    clone.add(new ItemStack(this.asItem(state)));
                } else if (Cfg.variantsDrop()) {
                    final ItemStack noDense = new ItemStack(this.asItem(AdditionalProperties.nonDense(state)));
                    noDense.setCount(drop.getCount());
                    clone.add(noDense);
                } else {
                    fgStack.setCount(drop.getCount());
                    clone.add(fgStack);
                }
            } else {
                clone.add(drop);
            }
        }
        return AdditionalProperties.isDense(state) ? this.updateDense(clone) : clone;
    }

    private boolean hasSilkTouch(final LootParams.Builder builder) {
        final ItemStack tool = builder.getOptionalParameter(LootContextParams.TOOL);
        if (tool == null) return false;
        return EnchantmentHelper.getItemEnchantmentLevel(
            builder.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH),
            tool
        ) > 0;
    }

    private List<ItemStack> updateDense(final List<ItemStack> drops) {
        final List<ItemStack> clone = new ArrayList<>();
        int denseCount = 0;
        for (final ItemStack drop : drops) {
            final Item item = drop.getItem();
            final boolean dense = item instanceof VariantItem
                && AdditionalProperties.isDense(((VariantItem) item).getState());
            if (dense) {
                if (denseCount == 0) {
                    clone.add(drop);
                }
                denseCount++;
            } else {
                clone.add(drop);
            }
        }
        return clone;
    }

    @Override
    public boolean propagatesSkylightDown(final BlockState state, final BlockGetter getter, final BlockPos pos) {
        return this.bgi().invokePropagatesSkylightDown(state, getter, pos);
    }

    @Override
    public void destroy(final LevelAccessor level, final BlockPos pos, final BlockState state) {
        final LevelAccessor interceptor = this.primeRestricted(level, state, this.bg, pos);
        try {
            this.bg.destroy(interceptor, pos, state);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    public void wasExploded(final Level level, final BlockPos pos, final Explosion explosion) {
        final Level interceptor = this.primeRestricted(level, this.defaultBlockState(), this.bg, pos);
        try {
            this.bg.wasExploded(interceptor, pos, explosion);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    public void stepOn(final Level level, final BlockPos pos, final BlockState state, final Entity entity) {
        Level interceptor = this.primeRestricted(level, state, this.bg, pos);
        try {
            this.bg.stepOn(interceptor, pos, this.asBg(state), entity);

            interceptor = this.prime(level, state, this.fg);
            this.fg.stepOn(interceptor, pos, this.asFg(state), entity);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext ctx) {
        final Level level = ctx.getLevel();
        Level interceptor = this.primeRestricted(level, this.defaultBlockState(), this.bg, ctx.getClickedPos());
        try {
            ((UseOnContextAccessor) ctx).setLevel(interceptor);
            final BlockState bgState = this.bg.getStateForPlacement(ctx);
            if (bgState == null) return null;

            this.prime(level, this.defaultBlockState(), this.fg);
            final BlockState fgState = this.fg.getStateForPlacement(ctx);
            if (fgState == null) return null;

            return copyInto(this.defaultBlockState(), fgState, bgState);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity entity, ItemStack stack) {
        // It's generally unsafe to defer this method, but these two are fine.
        if (this.bg instanceof BeehiveBlock || this.bg instanceof IceBlock) {
            try {
                this.bg.playerDestroy(level, player, pos, state, entity, stack);
            } catch (final RuntimeException ignored) {
                super.playerDestroy(level, player, pos, state, entity, stack);
            }
        } else {
            super.playerDestroy(level, player, pos, state, entity, stack);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        this.bg.setPlacedBy(level, pos, this.asBg(state), entity, stack);
    }

    @Override
    public void fallOn(final Level level, final BlockState state, final BlockPos pos, final Entity entity, final float f) {
        final Level interceptor = this.primeRestricted(level, state, this.bg, pos);
        try {
            this.bg.fallOn(interceptor, this.asBg(state), pos, entity, f);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    public void updateEntityAfterFallOn(final BlockGetter getter, final Entity entity) {
        this.bg.updateEntityAfterFallOn(getter, entity);
    }

    @Override
    public BlockState playerWillDestroy(final Level level, final BlockPos pos, final BlockState state, final Player player) {
        final Level interceptor = this.primeRestricted(level, state, this.bg, pos);
        try {
            this.bg.playerWillDestroy(interceptor, pos, this.asBg(state), player);
            return super.playerWillDestroy(level, pos, state, player);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    public boolean dropFromExplosion(final Explosion explosion) {
        return this.bg.dropFromExplosion(explosion);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> list, TooltipFlag flag) {
        final int size = list.size();
        this.bg.appendHoverText(stack, ctx, list, flag);

        if (size == list.size()) {
            this.fg.appendHoverText(stack, ctx, list, flag);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction dir, BlockState facing, LevelAccessor level, BlockPos from, BlockPos to) {
        final LevelAccessor interceptor = this.primeRestricted(level, state, this.bg, from);
        try {
            if (facing.getBlock() instanceof OreVariant v && v.bg.equals(this.bg)) {
                facing = this.asBg(facing);
            }
            return copyInto(state, this.bgi().invokeUpdateShape(this.asBg(state), dir, facing, level, from, to));
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block facing, BlockPos at, boolean bl) {
        final Level interceptor = this.primeRestricted(level, state, this.bg, pos);
        try {
            if (facing instanceof OreVariant v && v.bg.equals(this.bg)) {
                facing = this.bg;
            }
            this.bgi().invokeNeighborChanged(this.asBg(state), level, pos, facing, at, bl);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(final BlockState state, final Level level, final BlockPos pos, final BlockState old, final boolean bl) {
        Level interceptor = this.primeRestricted(level, state, this.bg, pos);
        try {
            final boolean same = this == old.getBlock();
            final BlockState bgOld = same ? this.asBg(old) : old;
            this.bgi().invokeOnPlace(this.asBg(state), interceptor, pos, bgOld, bl);

            interceptor = this.prime(level, state, this.fg);
            final BlockState fgOld = same ? this.asFg(old) : old;
            this.fgi().invokeOnPlace(this.asFg(state), interceptor, pos, fgOld, bl);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(final BlockState state, final Level level, final BlockPos pos, final BlockState newState, final boolean bl) {
        Level interceptor = this.primeRestricted(level, state, this.bg, pos);
        try {
            final boolean same = this == newState.getBlock();
            final BlockState bgOld = same ? this.asBg(newState) : newState;
            this.bgi().invokeOnRemove(this.asBg(state), interceptor, pos, bgOld, bl);

            interceptor = this.prime(level, state, this.fg);
            final BlockState fgOld = same ? this.asFg(newState) : newState;
            this.fgi().invokeOnRemove(this.asFg(state), interceptor, pos, fgOld, bl);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        Level interceptor = this.primeRestricted(level, state, this.bg, pos);
        try {
            final InteractionResult bgr = this.bgi().invokeUseWithoutItem(this.asBg(state), interceptor, pos, player, hit);

            interceptor = this.prime(level, state, this.fg);
            final InteractionResult fgr = this.fgi().invokeUseWithoutItem(this.asFg(state), interceptor, pos, player, hit);

            return bgr == InteractionResult.FAIL ? InteractionResult.FAIL : fgr;
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Level interceptor = this.primeRestricted(level, state, this.bg, pos);
        try {
            final ItemInteractionResult bgr = this.bgi().invokeUseItemOn(stack, this.asBg(state), interceptor, pos, player, hand, hit);

            interceptor = this.prime(level, state, this.fg);
            final ItemInteractionResult fgr = this.fgi().invokeUseItemOn(stack, this.asFg(state), interceptor, pos, player, hand, hit);
            return bgr == ItemInteractionResult.FAIL ? ItemInteractionResult.FAIL : fgr;
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean triggerEvent(final BlockState state, final Level level, final BlockPos pos, final int i, final int j) {
        Level interceptor = this.primeRestricted(level, state, this.bg, pos);
        try {
            final boolean bge = this.bgi().invokeTriggerEvent(this.asBg(state), level, pos, i, j);

            interceptor = this.prime(level, state, this.fg);
            final boolean fge = this.fgi().invokeTriggerEvent(this.asFg(state), level, pos, i, j);
            return bge || fge;
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(final BlockState state) {
        return this.bgi().invokeGetRenderShape(this.asBg(state));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean useShapeForLightOcclusion(final BlockState state) {
        final Boolean b = this.onPreInit(cfg ->
            ((BlockBehaviourInvoker) cfg.bg).invokeUseShapeForLightOcclusion(copyInto(cfg.bg.defaultBlockState(), state)));
        return b != null ? b : false;
    }

    @SuppressWarnings("deprecation")
    public PushReaction getPistonPushReaction(final BlockState state) {
        // There's a special exemption in PistonBlock.
        if (this.bg.equals(Blocks.OBSIDIAN)
                || this.bg.equals(Blocks.CRYING_OBSIDIAN)
                || this.bg.equals(Blocks.RESPAWN_ANCHOR)) {
            return PushReaction.BLOCK;
        }
        return this.asBg(state).getPistonPushReaction();
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(final BlockState state) {
        return this.bgi().invokeGetFluidState(this.asBg(state));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSignalSource(final BlockState state) {
        return this.bgi().invokeIsSignalSource(this.asBg(state));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasAnalogOutputSignal(final BlockState state) {
        return this.bgi().invokeHasAnalogOutputSignal(this.asBg(state));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(final BlockState state, final Rotation rotation) {
        return copyInto(state, this.bgi().invokeRotate(this.asBg(state), rotation));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(final BlockState state, final Mirror mirror) {
        return copyInto(state, this.bgi().invokeMirror(this.asBg(state), mirror));
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public VoxelShape getOcclusionShape(final BlockState state, final BlockGetter getter, final BlockPos pos) {
        return this.bgi().invokeGetOcclusionShape(this.asBg(state), getter, pos);
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(final BlockState state, final BlockGetter getter, final BlockPos pos, final CollisionContext ctx) {
        return this.bgi().invokeGetShape(this.asBg(state), getter, pos, ctx);
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public VoxelShape getBlockSupportShape(final BlockState state, final BlockGetter getter, final BlockPos pos) {
        return this.bgi().invokeGetBlockSupportShape(this.asBg(state), getter, pos);
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public VoxelShape getInteractionShape(final BlockState state, final BlockGetter getter, final BlockPos pos) {
        return this.bgi().invokeGetInteractionShape(this.asBg(state), getter, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightBlock(final BlockState state, final BlockGetter getter, final BlockPos pos) {
        return this.bgi().invokeGetLightBlock(this.asBg(state), getter, pos);
    }

    @Nullable
    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public MenuProvider getMenuProvider(final BlockState state, final Level level, final BlockPos pos) {
        return this.bgi().invokeGetMenuProvider(this.asBg(state), level, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        return this.bgi().invokeCanSurvive(this.asBg(state), level, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getShadeBrightness(final BlockState state, final BlockGetter getter, final BlockPos pos) {
        return this.bgi().invokeGetShadeBrightness(this.asBg(state), getter, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getAnalogOutputSignal(final BlockState state, final Level level, final BlockPos pos) {
        return this.bgi().invokeGetAnalogOutputSignal(this.asBg(state), level, pos);
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        return this.bgi().invokeGetCollisionShape(this.asBg(state), getter, pos, ctx);
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public VoxelShape getVisualShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        return this.bgi().invokeGetVisualShape(this.asBg(state), getter, pos, ctx);
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getDestroyProgress(final BlockState state, final Player player, final BlockGetter getter, final BlockPos pos) {
        return this.fgi().invokeGetDestroyProgress(this.asFg(state), player, getter, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void spawnAfterBreak(
            final BlockState state, final ServerLevel server, final BlockPos pos, final ItemStack stack,
            final boolean dropExperience) {
        ServerLevel interceptor = this.primeRestricted(server, state, this.bg, pos);
        try {
            this.bgi().invokeSpawnAfterBreak(this.asBg(state), interceptor, pos, stack, dropExperience);

            interceptor = this.prime(server, state, this.fg);
            this.fgi().invokeSpawnAfterBreak(this.asFg(state), interceptor, pos, stack, dropExperience);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void attack(final BlockState state, final Level level, final BlockPos pos, final Player player) {
        Level interceptor = this.primeRestricted(level, state, this.bg, pos);
        try {
            this.bgi().invokeAttack(this.asBg(state), interceptor, pos, player);

            interceptor = this.prime(level, state, this.fg);
            this.fgi().invokeAttack(this.asFg(state), interceptor, pos, player);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getSignal(final BlockState state, final BlockGetter getter, final BlockPos pos, final Direction dir) {
        final int bgs = this.bgi().invokeGetSignal(this.asBg(state), getter, pos, dir);
        final int fgs = this.fgi().invokeGetSignal(this.asFg(state), getter, pos, dir);
        return Math.max(bgs, fgs);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void entityInside(final BlockState state, final Level level, final BlockPos pos, final Entity entity) {
        Level interceptor = this.primeRestricted(level, state, this.bg, pos);
        try {
            this.bgi().invokeEntityInside(this.asBg(state), interceptor, pos, entity);

            interceptor = this.prime(level, state, this.fg);
            this.fgi().invokeEntityInside(this.asFg(state), interceptor, pos, entity);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getDirectSignal(final BlockState state, final BlockGetter getter, final BlockPos pos, final Direction dir) {
        final int bgs = this.bgi().invokeGetDirectSignal(this.asBg(state), getter, pos, dir);
        final int fgs = this.fgi().invokeGetDirectSignal(this.asFg(state), getter, pos, dir);
        return Math.max(bgs, fgs);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onProjectileHit(final Level level, final BlockState state, final BlockHitResult hit, final Projectile projectile) {
        this.bgi().invokeOnProjectileHit(level, this.asBg(state), hit, projectile);
        this.fgi().invokeOnProjectileHit(level, this.asFg(state), hit, projectile);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(final BlockState state, final ServerLevel server, final BlockPos pos, final RandomSource rand) {
        ServerLevel interceptor = this.primeRestricted(server, state, this.bg, pos);
        try {
            this.bgi().invokeTick(this.asBg(state), interceptor, pos, rand);

            interceptor = this.prime(server, state, this.fg);
            this.fgi().invokeTick(this.asFg(state), interceptor, pos, rand);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(final BlockState state, final ServerLevel server, final BlockPos pos, final RandomSource rand) {
        ServerLevel interceptor = this.primeRestricted(server, state, this.bg, pos);
        try {
            this.bgi().invokeRandomTick(this.asBg(state), interceptor, pos, rand);

            interceptor = this.prime(server, state, this.fg);
            this.fgi().invokeRandomTick(this.asFg(state), interceptor, pos, rand);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void animateTick(final BlockState state, final Level level, final BlockPos pos, final RandomSource rand) {
        Level interceptor = this.primeRestricted(level, state, this.bg, pos);
        try {
            this.bg.animateTick(this.asBg(state), interceptor, pos, rand);

            interceptor = this.prime(level, state, this.fg);
            this.fg.animateTick(this.asFg(state), interceptor, pos, rand);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }
}
