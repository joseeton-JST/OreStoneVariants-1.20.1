package io.github.joseetoon.genlib;

import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import io.github.joseetoon.genlib.command.CatLibCommands;
import io.github.joseetoon.genlib.command.CommandRegistrationContext;
import io.github.joseetoon.genlib.command.DefaultLibCommands;
import io.github.joseetoon.genlib.command.arguments.EnumArgument;
import io.github.joseetoon.genlib.command.arguments.FileArgument;
import io.github.joseetoon.genlib.command.arguments.HjsonArgument;
import io.github.joseetoon.genlib.command.arguments.PathArgument;
import io.github.joseetoon.genlib.command.arguments.RegistryArgument;
import io.github.joseetoon.genlib.config.LibConfig;
import io.github.joseetoon.genlib.event.error.LibErrorContext;
import io.github.joseetoon.genlib.event.lifecycle.GameReadyEvent;
import io.github.joseetoon.genlib.event.player.CommonPlayerEvent;
import io.github.joseetoon.genlib.event.registry.DynamicRegistries;
import io.github.joseetoon.genlib.event.registry.RegistryAccessEvent;
import io.github.joseetoon.genlib.event.registry.RegistryAddedEvent;
import io.github.joseetoon.genlib.event.world.CatLibBiomeModifier;
import io.github.joseetoon.genlib.event.world.CommonWorldEvent;
import io.github.joseetoon.genlib.util.LibReference;
import io.github.joseetoon.genlib.util.McUtils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public final class CatLib {

    private static final int DEBUG_SAMPLE_RATE = 16;
    private static final AtomicBoolean BOOTSTRAPPED = new AtomicBoolean();
    private static final AtomicInteger DEBUG_SERVER_STARTING_CALLS = new AtomicInteger();
    private static final AtomicInteger DEBUG_TAGS_UPDATED_CALLS = new AtomicInteger();

    private CatLib() {
    }

    public static void bootstrap(final ModContainer container) {
        if (!BOOTSTRAPPED.compareAndSet(false, true)) {
            return;
        }

        final boolean debug = LibConfig.debugStartup();
        final long ctorStart = debug ? System.nanoTime() : 0L;
        final CatLib instance = new CatLib();
        final IEventBus modBus = container.getEventBus();

        LibConfig.register(container);
        modBus.addListener(instance::setup);
        modBus.addListener(instance::registerBiomeModifier);

        NeoForge.EVENT_BUS.addListener(instance::onServerStarting);
        NeoForge.EVENT_BUS.addListener(instance::onTagsUpdated);
        NeoForge.EVENT_BUS.addListener(instance::onLevelLoad);
        NeoForge.EVENT_BUS.addListener(instance::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(instance::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(instance::onPlayerLogout);

        if (debug && log.isDebugEnabled()) {
            final long elapsedMicros = (System.nanoTime() - ctorStart) / 1_000L;
            log.debug("GenLib bootstrap constructor completed in {}us", elapsedMicros);
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        EnumArgument.register();
        FileArgument.register();
        HjsonArgument.register();
        PathArgument.register();
        RegistryArgument.register();

        final CommandRegistrationContext ctx = CommandRegistrationContext.forMod(LibReference.MOD_DESCRIPTOR);
        if (LibConfig.enableGlobalLibCommands()) {
            ctx.addAllCommands(DefaultLibCommands.createAll(LibReference.MOD_DESCRIPTOR, true));
        }
        ctx.addCommand(CatLibCommands.ERROR_MENU).registerAll();
    }

    private void registerBiomeModifier(final RegisterEvent event) {
        event.register(
            NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS,
            ResourceLocation.fromNamespaceAndPath(LibReference.MOD_ID, "catlib_modifier"),
            () -> CatLibBiomeModifier.CODEC
        );
    }

    private void onServerStarting(final ServerStartingEvent event) {
        final boolean debug = LibConfig.debugStartup() && log.isDebugEnabled();
        final long start = debug ? System.nanoTime() : 0L;
        final var registryAccess = event.getServer().registryAccess();
        DynamicRegistries.updateRegistries(registryAccess);
        RegistryAddedEvent.onRegistryAccess(registryAccess);
        RegistryAccessEvent.EVENT.invoker().accept(registryAccess);
        GameReadyEvent.SERVER.invoker().run();
        LibErrorContext.outputServerErrors(true);
        if (debug) {
            final int sample = DEBUG_SERVER_STARTING_CALLS.incrementAndGet();
            if (sample % DEBUG_SAMPLE_RATE == 1) {
                log.debug("GenLib onServerStarting sample={} time={}us", sample, (System.nanoTime() - start) / 1_000L);
            }
        }
    }

    private void onTagsUpdated(final TagsUpdatedEvent event) {
        final boolean debug = LibConfig.debugStartup() && log.isDebugEnabled();
        final long start = debug ? System.nanoTime() : 0L;
        final var registryAccess = event.getRegistryAccess();
        if (registryAccess != null) {
            DynamicRegistries.updateRegistries(registryAccess);
            RegistryAddedEvent.onRegistryAccess(registryAccess);
            RegistryAccessEvent.EVENT.invoker().accept(registryAccess);
        }
        if (debug) {
            final int sample = DEBUG_TAGS_UPDATED_CALLS.incrementAndGet();
            if (sample % DEBUG_SAMPLE_RATE == 1) {
                log.debug(
                    "GenLib onTagsUpdated sample={} hasRegistryAccess={} time={}us",
                    sample, registryAccess != null, (System.nanoTime() - start) / 1_000L
                );
            }
        }
    }

    private void onLevelLoad(final LevelEvent.Load event) {
        CommonWorldEvent.LOAD.invoker().accept(event.getLevel());
    }

    private void onLevelUnload(final LevelEvent.Unload event) {
        CommonWorldEvent.UNLOAD.invoker().accept(event.getLevel());
    }

    private void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (McUtils.isClientSide() && LibErrorContext.hasErrors()) {
            LibErrorContext.broadcastErrors(event.getEntity());
        }
        CommonPlayerEvent.LOGIN.invoker().accept(event.getEntity(), event.getEntity().getServer());
    }

    private void onPlayerLogout(final PlayerEvent.PlayerLoggedOutEvent event) {
        CommonPlayerEvent.LOGOUT.invoker().accept(event.getEntity(), event.getEntity().getServer());
    }
}
