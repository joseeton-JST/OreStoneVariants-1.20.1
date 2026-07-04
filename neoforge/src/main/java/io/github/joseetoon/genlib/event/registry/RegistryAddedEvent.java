package io.github.joseetoon.genlib.event.registry;

import lombok.extern.log4j.Log4j2;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import io.github.joseetoon.genlib.event.LibEvent;
import io.github.joseetoon.genlib.util.RegistryUtils;
import personthecat.overwritevalidator.annotations.Overwrite;
import personthecat.overwritevalidator.annotations.OverwriteClass;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@OverwriteClass
public class RegistryAddedEvent {

    private static final Map<ResourceKey<?>, LibEvent<RegistryAddedCallback<?>>> DYNAMIC_EVENT_MAP = new ConcurrentHashMap<>();

    @Overwrite
    @SuppressWarnings("unchecked")
    public static <T> LibEvent<RegistryAddedCallback<T>> get(final ResourceKey<Registry<T>> key) {
        final RegistryHandle<T> handle = RegistryUtils.getHandle(key);
        if (handle instanceof MojangRegistryHandle) {
            final Registry<T> registry = ((MojangRegistryHandle<T>) handle).getRegistry();
            if (registry instanceof RegistryEventAccessor<?>) {
                return ((RegistryEventAccessor<T>) registry).getRegistryAddedEvent();
            }
            log.debug("Mojang registry {} does not expose RegistryEventAccessor. Using dynamic fallback.", key);
        }
        // For Forge registries and Mojang registries without mixin accessors, use dynamic fallback.
        return (LibEvent<RegistryAddedCallback<T>>) (Object) DYNAMIC_EVENT_MAP
            .computeIfAbsent(key, k -> (LibEvent<RegistryAddedCallback<?>>) (Object) newEvent());
    }

    @Overwrite
    public static <T> void withRetroactive(final ResourceKey<Registry<T>> key, final RegistryAddedCallback<T> f) {
        get(key).register(f);
        runRetroactively(key, f);
    }

    @Overwrite
    public static <T> void withDynamic(final ResourceKey<Registry<T>> key, final RegistryAddedCallback<T> f) {
        get(key).register(f);
        runDynamically(key, f);
    }

    @Overwrite
    public static <T> void exhaustive(final ResourceKey<Registry<T>> key, final RegistryAddedCallback<T> f) {
        get(key).register(f);
        runRetroactively(key, f);
        runDynamically(key, f);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onRegistryAccess(final RegistryAccess registries) {
        DYNAMIC_EVENT_MAP.forEach((key, event) -> {
            try {
                final RegistryHandle<Object> registry =
                    new MojangRegistryHandle(registries.registryOrThrow((ResourceKey) key));
                registry.forEach((id, t) ->
                    ((LibEvent<RegistryAddedCallback<Object>>) (Object) event)
                        .invoker().onRegistryAdded(registry, id, t));
            } catch (final Exception e) {
                log.debug("Registry {} not available in this registry access: {}", key, e.getMessage());
            }
        });
    }

    private static <T> void runRetroactively(final ResourceKey<Registry<T>> key, final RegistryAddedCallback<T> f) {
        final RegistryHandle<T> handle = RegistryUtils.getHandle(key);
        handle.forEach((id, t) -> f.onRegistryAdded(handle, id, t));
    }

    @SuppressWarnings("unchecked")
    private static <T> void runDynamically(final ResourceKey<Registry<T>> key, final RegistryAddedCallback<T> f) {
        DYNAMIC_EVENT_MAP.computeIfAbsent(key,
            k -> (LibEvent<RegistryAddedCallback<?>>) (Object) newEvent()).register(f);
    }

    private static <T> LibEvent<RegistryAddedCallback<T>> newEvent() {
        return LibEvent.nonRecursive(callbacks -> (h, id, t) -> callbacks.forEach(c -> c.onRegistryAdded(h, id, t)));
    }
}
