package io.github.joseetoon.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;

class ServerTickInterceptor extends LevelTicks<Block> {

    final ThreadLocal<InterceptorHandle> handles;
    final LevelTicks<Block> delegate;

    ServerTickInterceptor(final ServerLevelInterceptor interceptor, final LevelTicks<Block> delegate) {
        super(l -> false, () -> InactiveProfiler.INSTANCE);
        this.handles = interceptor.handles;
        this.delegate = delegate;
    }

    @Override
    public boolean willTickThisTick(final BlockPos pos, final Block block) {
        return this.delegate.willTickThisTick(pos, block);
    }

    @Override
    public boolean hasScheduledTick(final BlockPos pos, final Block block) {
        final InterceptorHandle handle = this.handles.get();
        if (handle != null && handle.isPrimed()) {
            return this.delegate.hasScheduledTick(pos, handle.expose(pos, block));
        }
        return this.delegate.hasScheduledTick(pos, block);
    }

    @Override
    public void schedule(ScheduledTick<Block> tick) {
        final InterceptorHandle handle = this.handles.get();
        if (handle != null && handle.isPrimed()) {
            final Block exposed = handle.expose(tick.pos(), tick.type());
            if (exposed != tick.type()) {
                tick = new ScheduledTick<>(exposed, tick.pos(), tick.triggerTick(), tick.priority(), tick.subTickOrder());
            }
        }
        this.delegate.schedule(tick);
    }

    @Override
    public int count() {
        return this.delegate.count();
    }
}
