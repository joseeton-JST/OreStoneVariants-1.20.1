package io.github.joseetoon.osv;

import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import io.github.joseetoon.osv.io.ModFolders;
import io.github.joseetoon.osv.io.ResourceHelper;
import io.github.joseetoon.osv.tag.forge.OsvTagPackResources;
import io.github.joseetoon.genlib.command.CommandRegistrationContext;
import io.github.joseetoon.genlib.event.error.LibErrorContext;
import io.github.joseetoon.genlib.event.lifecycle.CheckErrorsEvent;
import io.github.joseetoon.genlib.event.world.FeatureModificationEvent;
import io.github.joseetoon.genlib.util.McUtils;
import io.github.joseetoon.osv.client.VariantColorizer;
import io.github.joseetoon.osv.client.model.ModelHandler;
import io.github.joseetoon.osv.command.CommandOsv;
import io.github.joseetoon.osv.command.argument.*;
import io.github.joseetoon.osv.config.Cfg;
import io.github.joseetoon.osv.config.OsvTrackers;
import io.github.joseetoon.osv.exception.CompatibilityModeException;
import io.github.joseetoon.osv.exception.ConfigFileNotLoadedException;
import io.github.joseetoon.osv.exception.JarFilesNotCopiedException;
import io.github.joseetoon.osv.init.VariantLoadingContext;
import io.github.joseetoon.osv.io.JarFiles;
import io.github.joseetoon.osv.item.VariantItem;
import io.github.joseetoon.osv.item.VariantTab;
import io.github.joseetoon.osv.item.DenseVariantTab;
import io.github.joseetoon.osv.item.forge.ModCreativeTabs;
import io.github.joseetoon.osv.preset.writer.PresetWriter;
import io.github.joseetoon.osv.recipe.RecipeHelper;
import io.github.joseetoon.osv.tag.TagHelper;
import io.github.joseetoon.osv.util.Reference;
import io.github.joseetoon.osv.util.StartupSnapshot;
import io.github.joseetoon.osv.world.OreGen;
import io.github.joseetoon.osv.world.carver.GiantClusterCarver;
import io.github.joseetoon.osv.world.carver.GiantSphereCarver;
import io.github.joseetoon.osv.world.decorator.FlexibleVariantDecorator;
import io.github.joseetoon.osv.world.feature.ClusterFeature;
import io.github.joseetoon.osv.world.feature.PassiveOreSwapFeature;
import io.github.joseetoon.osv.world.feature.SphereFeature;
import io.github.joseetoon.osv.world.interceptor.InterceptorDispatcher;

import static io.github.joseetoon.genlib.event.error.LibErrorContext.apply;

@Log4j2
@Mod(Reference.MOD_ID)
public class OSV {

    // Use ResourceLocation overloads to avoid SRG-remapping failures on vanilla Registries fields.
    private static final DeferredRegister<net.minecraft.world.level.levelgen.feature.Feature<?>> FEATURES =
        DeferredRegister.<net.minecraft.world.level.levelgen.feature.Feature<?>>create(
            new ResourceLocation("minecraft", "worldgen/feature"), Reference.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.level.levelgen.carver.WorldCarver<?>> CARVERS =
        DeferredRegister.<net.minecraft.world.level.levelgen.carver.WorldCarver<?>>create(
            new ResourceLocation("minecraft", "worldgen/carver"), Reference.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.level.levelgen.placement.PlacementModifierType<?>> PLACEMENT_MODIFIERS =
        DeferredRegister.<net.minecraft.world.level.levelgen.placement.PlacementModifierType<?>>create(
            new ResourceLocation("minecraft", "worldgen/placement_modifier_type"), Reference.MOD_ID);

    static {
        FEATURES.register("cluster", () -> ClusterFeature.INSTANCE);
        FEATURES.register("passive_ore_swap", () -> PassiveOreSwapFeature.INSTANCE);
        FEATURES.register("sphere", () -> SphereFeature.INSTANCE);
        CARVERS.register("giant_cluster", () -> GiantClusterCarver.INSTANCE);
        CARVERS.register("giant_sphere", () -> GiantSphereCarver.INSTANCE);
        PLACEMENT_MODIFIERS.register("flexible_decorator",
            () -> (net.minecraft.world.level.levelgen.placement.PlacementModifierType<FlexibleVariantDecorator>) () -> FlexibleVariantDecorator.CODEC);
    }

    public OSV() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus eventBus = MinecraftForge.EVENT_BUS;

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

        eventBus.addListener(EventPriority.LOWEST,
            (ServerStartingEvent e) -> this.serverStarting(e.getServer()));

        // After tags are baked into the registry, cache which tags each variant's
        // fg/bg block belongs to. OsvTagPackResources uses this cache the next time
        // data packs reload (including a /reload command) to emit the correct tag JSONs.
        eventBus.addListener(EventPriority.HIGHEST,
            (TagsUpdatedEvent e) -> TagHelper.injectTags());

        // Register the virtual data pack that emits OSV's dynamic tag JSON files.
        modBus.addListener(this::addPackFinders);
        // Use LOWEST priority so all other mods' DeferredRegisters have already
        // added their blocks before OSV creates variants (fixes intrusive holder crash)
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
        e |= apply(Reference.MOD, JarFiles::copyFiles, JarFilesNotCopiedException::new);
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

        if (InterceptorDispatcher.COMPATIBILITY_MODE) {
            LibErrorContext.warn(Reference.MOD, new CompatibilityModeException());
        }
        CommandRegistrationContext.forMod(Reference.MOD)
            .addLibCommands().addAllCommands(CommandOsv.class).registerAll();

        return true;
    }

    private void initClient(final IEventBus modBus) {
        if (OsvTrackers.MODEL_CACHE.isUpdated()) {
            log.info("Model settings were updated. Resources will be regenerated.");
            ModelHandler.primeForRegen();
        }
        modBus.addListener(EventPriority.LOWEST, (FMLLoadCompleteEvent e) -> {
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
                final Pack pack = Pack.readMetaAndCreate(
                    "osv_tags",
                    Component.literal("OSV Dynamic Tags"),
                    false,
                    OsvTagPackResources::new,
                    PackType.SERVER_DATA,
                    Pack.Position.TOP,
                    PackSource.BUILT_IN
                );
                if (pack != null) packConsumer.accept(pack);
            });
        } else if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addRepositorySource(packConsumer -> {
                final String name = ModFolders.RESOURCE_DIR.getName();
                final Pack pack = Pack.readMetaAndCreate(
                    name,
                    Component.literal(name),
                    true,
                    id -> ResourceHelper.RESOURCES,
                    PackType.CLIENT_RESOURCES,
                    Pack.Position.TOP,
                    PackSource.BUILT_IN
                );
                if (pack != null) packConsumer.accept(pack);
            });
        }
    }

    private void serverStarting(final MinecraftServer server) {
        RecipeHelper.injectRecipes(server.getRecipeManager());
        PresetWriter.savePresets();
    }

    private void serverStopping() {
        InterceptorDispatcher.unloadAll();
        OreGen.onWorldClosed();
    }

    private void onRegister(final RegisterEvent event) {
        // Trigger variant loading during BLOCK registry event so intrusive holders
        // are registered through Forge's NamespacedWrapper (not MappedRegistry directly),
        // which properly binds them before GameData.freezeData checks.
        // Use ForgeRegistries.Keys.BLOCKS (Forge constant) instead of Registries.BLOCK
        // (vanilla field) to avoid SRG-remapping failures.
        event.register(ForgeRegistries.Keys.BLOCKS, helper ->
            VariantLoadingContext.startLoading(helper::register));
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
