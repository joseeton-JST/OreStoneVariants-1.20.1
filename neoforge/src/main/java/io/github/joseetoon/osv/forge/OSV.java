package io.github.joseetoon.osv.forge;

import lombok.extern.log4j.Log4j2;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import io.github.joseetoon.genlib.CatLib;
import io.github.joseetoon.genlib.command.CommandRegistrationContext;
import io.github.joseetoon.genlib.command.DefaultLibCommands;
import io.github.joseetoon.genlib.command.LibCommandRegistrar;
import io.github.joseetoon.genlib.event.error.LibErrorContext;
import io.github.joseetoon.genlib.event.lifecycle.CheckErrorsEvent;
import io.github.joseetoon.genlib.event.world.FeatureModificationEvent;
import io.github.joseetoon.genlib.util.McUtils;
import io.github.joseetoon.osv.ModRegistries;
import io.github.joseetoon.osv.client.VariantColorizer;
import io.github.joseetoon.osv.client.model.ModelHandler;
import io.github.joseetoon.osv.command.CommandOsv;
import io.github.joseetoon.osv.command.argument.BackgroundArgument;
import io.github.joseetoon.osv.command.argument.BlockGroupArgument;
import io.github.joseetoon.osv.command.argument.OrePresetArgument;
import io.github.joseetoon.osv.command.argument.PropertyArgument;
import io.github.joseetoon.osv.command.argument.PropertyGroupArgument;
import io.github.joseetoon.osv.config.Cfg;
import io.github.joseetoon.osv.config.OsvTrackers;
import io.github.joseetoon.osv.exception.CompatibilityModeException;
import io.github.joseetoon.osv.exception.ConfigFileNotLoadedException;
import io.github.joseetoon.osv.exception.JarFilesNotCopiedException;
import io.github.joseetoon.osv.init.VariantLoadingContext;
import io.github.joseetoon.osv.io.JarFiles;
import io.github.joseetoon.osv.io.ModFolders;
import io.github.joseetoon.osv.io.ResourceHelper;
import io.github.joseetoon.osv.item.DenseVariantTab;
import io.github.joseetoon.osv.item.VariantTab;
import io.github.joseetoon.osv.item.neoforge.ModCreativeTabs;
import io.github.joseetoon.osv.preset.writer.PresetWriter;
import io.github.joseetoon.osv.recipe.RecipeHelper;
import io.github.joseetoon.osv.tag.TagHelper;
import io.github.joseetoon.osv.tag.neoforge.OsvTagPackResources;
import io.github.joseetoon.osv.util.Reference;
import io.github.joseetoon.osv.util.neoforge.StartupSnapshot;
import io.github.joseetoon.osv.world.OreGen;
import io.github.joseetoon.osv.world.carver.GiantClusterCarver;
import io.github.joseetoon.osv.world.carver.GiantSphereCarver;
import io.github.joseetoon.osv.world.decorator.FlexibleVariantDecorator;
import io.github.joseetoon.osv.world.feature.ClusterFeature;
import io.github.joseetoon.osv.world.feature.PassiveOreSwapFeature;
import io.github.joseetoon.osv.world.feature.SphereFeature;
import io.github.joseetoon.osv.world.interceptor.InterceptorDispatcher;

import java.util.Optional;

import static io.github.joseetoon.genlib.event.error.LibErrorContext.apply;

@Log4j2
@Mod(Reference.MOD_ID)
public class OSV {

    private static final DeferredRegister<net.minecraft.world.level.levelgen.feature.Feature<?>> FEATURES =
        DeferredRegister.create(ResourceLocation.fromNamespaceAndPath("minecraft", "worldgen/feature"), Reference.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.level.levelgen.carver.WorldCarver<?>> CARVERS =
        DeferredRegister.create(ResourceLocation.fromNamespaceAndPath("minecraft", "worldgen/carver"), Reference.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.level.levelgen.placement.PlacementModifierType<?>> PLACEMENT_MODIFIERS =
        DeferredRegister.create(ResourceLocation.fromNamespaceAndPath("minecraft", "worldgen/placement_modifier_type"), Reference.MOD_ID);

    static {
        FEATURES.register("cluster", () -> ClusterFeature.INSTANCE);
        FEATURES.register("passive_ore_swap", () -> PassiveOreSwapFeature.INSTANCE);
        FEATURES.register("sphere", () -> SphereFeature.INSTANCE);
        CARVERS.register("giant_cluster", () -> GiantClusterCarver.INSTANCE);
        CARVERS.register("giant_sphere", () -> GiantSphereCarver.INSTANCE);
        PLACEMENT_MODIFIERS.register("flexible_decorator", () -> FlexibleVariantDecorator.TYPE);
    }

    public OSV() {
        final var container = ModLoadingContext.get().getActiveContainer();
        final IEventBus modBus = container.getEventBus();
        final IEventBus eventBus = NeoForge.EVENT_BUS;

        CatLib.bootstrap(container);
        if (!this.initCommon()) {
            return;
        }
        if (Cfg.debugStartup()) {
            StartupSnapshot.logDiff();
        }
        if (McUtils.isClientSide()) {
            this.initClient(modBus);
        }

        ModCreativeTabs.TABS.register(modBus);
        FEATURES.register(modBus);
        CARVERS.register(modBus);
        PLACEMENT_MODIFIERS.register(modBus);

        eventBus.addListener(EventPriority.LOWEST, (ServerStartingEvent e) -> this.serverStarting(e.getServer()));
        eventBus.addListener(EventPriority.HIGHEST, (TagsUpdatedEvent e) -> TagHelper.injectTags());
        modBus.addListener(this::addPackFinders);
        modBus.addListener(EventPriority.LOWEST, this::onRegister);
        modBus.addListener(this::buildContents);

        CheckErrorsEvent.EVENT.register(VariantLoadingContext::stopLoading);
        CheckErrorsEvent.EVENT.register(ModRegistries.STONE_PRESETS::load);
        FeatureModificationEvent.EVENT.register(OreGen::setupOreFeatures);
        eventBus.addListener((ServerStoppingEvent e) -> this.serverStopping());
    }

    private boolean initCommon() {
        boolean e;
        e = apply(Reference.MOD, Cfg::register, ConfigFileNotLoadedException::new);
        e |= apply(Reference.MOD, JarFiles::copyPresets, JarFilesNotCopiedException::new);
        if (e) return false;

        BackgroundArgument.register();
        BlockGroupArgument.register();
        OrePresetArgument.register();
        PropertyArgument.register();
        PropertyGroupArgument.register();

        InterceptorDispatcher.verifyRuntimeIntegrity();
        if (Cfg.debugStartup()) {
            InterceptorDispatcher.debugSelfTest();
        }

        if (InterceptorDispatcher.compatibilityMode()) {
            LibErrorContext.warn(Reference.MOD, new CompatibilityModeException());
        }
        CommandRegistrationContext.forMod(Reference.MOD)
            .addLibCommands().addAllCommands(CommandOsv.class).registerAll();
        LibCommandRegistrar.registerCommand(DefaultLibCommands.createCaveTestAlias(Reference.MOD).getCommand());

        return true;
    }

    private void initClient(final IEventBus modBus) {
        modBus.addListener(EventPriority.LOWEST, (FMLLoadCompleteEvent e) -> {
            if (OsvTrackers.modelCache().isUpdated()) {
                log.info("Model settings were updated. Resources will be regenerated.");
                ModelHandler.primeForRegen();
            }
            ModelHandler.generateOverlayModel();
            VariantColorizer.colorizeAll();
            if (Cfg.debugStartup()) {
                StartupSnapshot.save();
            }
        });
    }

    private void addPackFinders(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            event.addRepositorySource(packConsumer -> {
                final PackLocationInfo info = new PackLocationInfo(
                    "osv_tags",
                    Component.literal("OSV Dynamic Tags"),
                    PackSource.BUILT_IN,
                    Optional.empty()
                );
                final Pack pack = Pack.readMetaAndCreate(
                    info,
                    new Pack.ResourcesSupplier() {
                        @Override
                        public PackResources openPrimary(final PackLocationInfo packLocationInfo) {
                            return new OsvTagPackResources(packLocationInfo.id());
                        }

                        @Override
                        public PackResources openFull(final PackLocationInfo packLocationInfo, final Pack.Metadata metadata) {
                            return new OsvTagPackResources(packLocationInfo.id());
                        }
                    },
                    PackType.SERVER_DATA,
                    new PackSelectionConfig(false, Pack.Position.TOP, false)
                );
                if (pack != null) packConsumer.accept(pack);
            });
        } else if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addRepositorySource(packConsumer -> {
                final String name = ModFolders.RESOURCE_DIR.getName();
                final PackLocationInfo info = new PackLocationInfo(
                    name,
                    Component.literal(name),
                    PackSource.BUILT_IN,
                    Optional.empty()
                );
                final Pack pack = Pack.readMetaAndCreate(
                    info,
                    new Pack.ResourcesSupplier() {
                        @Override
                        public PackResources openPrimary(final PackLocationInfo packLocationInfo) {
                            return ResourceHelper.createPackResources(packLocationInfo);
                        }

                        @Override
                        public PackResources openFull(final PackLocationInfo packLocationInfo, final Pack.Metadata metadata) {
                            return ResourceHelper.createPackResources(packLocationInfo);
                        }
                    },
                    PackType.CLIENT_RESOURCES,
                    ResourceHelper.PACK_SELECTION
                );
                if (pack != null) packConsumer.accept(pack);
            });
        }
    }

    private void serverStarting(final MinecraftServer server) {
        RecipeHelper.injectRecipes(server.getRecipeManager());
        PresetWriter.savePresets(server.registryAccess());
    }

    private void serverStopping() {
        InterceptorDispatcher.unloadAll();
        OreGen.onWorldClosed();
    }

    private void onRegister(final RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> VariantLoadingContext.startLoading());
    }

    private void buildContents(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == VariantTab.INSTANCE) {
            ModRegistries.ITEMS.forEach((id, item) -> {
                if (!io.github.joseetoon.osv.block.AdditionalProperties.isDense(item.getState())) {
                    event.accept(item);
                }
            });
        } else if (event.getTab() == DenseVariantTab.INSTANCE) {
            ModRegistries.ITEMS.forEach((id, item) -> {
                if (io.github.joseetoon.osv.block.AdditionalProperties.isDense(item.getState())) {
                    event.accept(item);
                }
            });
        }
    }
}
