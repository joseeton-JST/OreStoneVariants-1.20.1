package io.github.joseetoon.osv.mixin.forge;

import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.joseetoon.osv.preset.reader.forge.LootTableReaderImpl;

@Mixin(ReloadableServerResources.class)
public class ServerResourcesMixin {

    @Final
    @Shadow
    private LootDataManager lootData;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onLoad(final RegistryAccess.Frozen registries, final FeatureFlagSet featureFlags,
                        final Commands.CommandSelection envType, final int permissionsLevel, final CallbackInfo ci) {
        LootTableReaderImpl.updateTables(this.lootData);
    }
}
