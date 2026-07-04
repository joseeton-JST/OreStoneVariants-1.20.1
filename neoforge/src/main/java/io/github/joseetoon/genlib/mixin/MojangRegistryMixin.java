package io.github.joseetoon.genlib.mixin;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import io.github.joseetoon.genlib.event.LibEvent;
import io.github.joseetoon.genlib.event.registry.MojangRegistryHandle;
import io.github.joseetoon.genlib.event.registry.RegistryAddedCallback;
import io.github.joseetoon.genlib.event.registry.RegistryEventAccessor;

@Mixin(MappedRegistry.class)
public abstract class MojangRegistryMixin<T> implements RegistryEventAccessor<T> {

    @Nullable
    private LibEvent<RegistryAddedCallback<T>> registryAddedEvent = null;

    @Inject(method = "register", at = @At("RETURN"))
    private <V extends T> void onRegister(
            final ResourceKey<T> key, final V v, final RegistrationInfo registrationInfo,
            final CallbackInfoReturnable<?> ci) {
        this.fireRegistryAdded(key, v);
    }

    private <V extends T> void fireRegistryAdded(final ResourceKey<T> key, final V v) {
        if (this.registryAddedEvent != null) {
            this.registryAddedEvent.invoker()
                .onRegistryAdded(new MojangRegistryHandle<>((Registry<T>) (Object) this),
                    key.location(), v);
        }
    }

    @NotNull
    @Override
    public LibEvent<RegistryAddedCallback<T>> getRegistryAddedEvent() {
        if (this.registryAddedEvent == null) {
            this.registryAddedEvent = LibEvent.create(callbacks -> (handle, id, t) ->
                callbacks.forEach(c -> c.onRegistryAdded(handle, id, t)));
        }
        return this.registryAddedEvent;
    }
}
