package io.github.joseetoon.osv.world.interceptor;

import lombok.extern.log4j.Log4j2;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.osv.config.Cfg;
import io.github.joseetoon.osv.util.unsafe.UnsafeUtils;

import java.net.URL;
import java.security.CodeSource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class InterceptorDispatcher {

    private static final Map<Integer, LevelAccessor> INTERCEPTORS = new ConcurrentHashMap<>();
    private static final int REGION_ID = Integer.MIN_VALUE;
    private static final String[] CRITICAL_CLASSES = {
        "io.github.joseetoon.osv.world.interceptor.InterceptorDispatcher",
        "io.github.joseetoon.osv.world.interceptor.InterceptorHandle",
        "io.github.joseetoon.osv.world.interceptor.WorldGenRegionInterceptor",
        "io.github.joseetoon.osv.world.interceptor.WorldGenTickInterceptor",
        "io.github.joseetoon.osv.world.interceptor.ServerLevelInterceptor",
        "io.github.joseetoon.osv.world.interceptor.ServerTickInterceptor",
        "io.github.joseetoon.osv.world.interceptor.ClientLevelInterceptor"
    };
    public static final boolean COMPATIBILITY_MODE = Cfg.forceCompatibilityMode() || !UnsafeUtils.isAvailable();

    public static <L extends LevelAccessor> L intercept(
            final L level, final BlockState state, final Block expected, final @Nullable BlockPos pos) {
        if (COMPATIBILITY_MODE) {
            return level;
        }
        final L interceptor = get(level);
        if (interceptor instanceof InterceptorAccessor) {
            if (interceptor instanceof WorldGenRegionInterceptor) {
                ((WorldGenRegionInterceptor) interceptor).prime((WorldGenRegion) level);
            }
            ((InterceptorAccessor) interceptor).intercept(state, expected, pos);
        }
        return interceptor;
    }

    @SuppressWarnings("unchecked")
    private static <L extends LevelAccessor> L get(final L level) {
        final int key = level instanceof WorldGenRegion ? REGION_ID : System.identityHashCode(level);
        final LevelAccessor existing = INTERCEPTORS.get(key);
        if (existing != null) {
            return (L) existing;
        }
        final LevelAccessor created = create(level);
        final LevelAccessor raced = INTERCEPTORS.putIfAbsent(key, created);
        return (L) (raced != null ? raced : created);
    }

    private static LevelAccessor create(final LevelAccessor level) {
        try {
            if (level instanceof WorldGenRegion) {
                log.debug("Creating region interceptor for type: {} in thread: {}",
                    level.getClass(), Thread.currentThread());
                return WorldGenRegionInterceptor.create((WorldGenRegion) level);
            } else if (level instanceof ServerLevel) {
                log.debug("Creating server interceptor for type: {} in thread: {}",
                    level.getClass(), Thread.currentThread());
                return ServerLevelInterceptor.create((ServerLevel) level);
            } else if (level.isClientSide()) {
                log.debug("Creating client interceptor for type: {} in thread: {}",
                    level.getClass(), Thread.currentThread());
                return ClientLevelInterceptor.create((ClientLevel) level);
            }
        } catch (final Throwable t) {
            throw new IllegalStateException("Failed to create OSV interceptor for " + level.getClass().getName(), t);
        }
        throw new IllegalStateException("No OSV interceptor mapping for " + level.getClass().getName());
    }

    public static void verifyRuntimeIntegrity() {
        final ClassLoader loader = InterceptorDispatcher.class.getClassLoader();
        for (final String name : CRITICAL_CLASSES) {
            try {
                Class.forName(name, false, loader);
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException("Missing critical OSV interceptor class: " + name, e);
            }
        }
    }

    public static void debugSelfTest() {
        verifyRuntimeIntegrity();
        if (!log.isDebugEnabled()) {
            return;
        }
        final StringBuilder sb = new StringBuilder("OSV interceptor self-test classes loaded from: ");
        for (final String name : CRITICAL_CLASSES) {
            try {
                final Class<?> cls = Class.forName(name, false, InterceptorDispatcher.class.getClassLoader());
                final CodeSource source = cls.getProtectionDomain() != null
                    ? cls.getProtectionDomain().getCodeSource()
                    : null;
                final URL location = source != null ? source.getLocation() : null;
                sb.append('[')
                    .append(name)
                    .append(" -> ")
                    .append(Objects.toString(location, "unknown"))
                    .append(']');
            } catch (final Throwable t) {
                sb.append('[').append(name).append(" -> error: ").append(t.getClass().getSimpleName()).append(']');
            }
        }
        log.debug(sb.toString());
    }

    public static void unloadAll() {
        INTERCEPTORS.clear();
    }
}
