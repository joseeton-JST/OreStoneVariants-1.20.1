package io.github.joseetoon.osv.mixin;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.joseetoon.osv.preset.reader.forge.LootTableReaderImpl;

import java.util.concurrent.Executor;

@Mixin(ReloadableServerResources.class)
public class ServerResourcesMixin {

    @Final
    @Shadow
    private LootDataManager lootData;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onLoad(final LayeredRegistryAccess<RegistryLayer> registries, final net.minecraft.core.HolderLookup.Provider provider, final net.minecraft.commands.Commands.CommandSelection envType, final int permissionsLevel, final Executor executor, final Executor executor2, final CallbackInfo ci) {
        LootTableReaderImpl.updateTables(this.lootData);
    }
}
