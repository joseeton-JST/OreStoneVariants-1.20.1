package io.github.joseetoon.osv.mixin;

import com.google.common.collect.Sets;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.joseetoon.osv.io.ModFolders;
import io.github.joseetoon.osv.io.ResourceHelper;

import java.util.Set;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {

    @Final
    @Shadow
    @Mutable
    private Set<RepositorySource> sources;

    @Inject(at = @At("RETURN"), method = "<init>([Lnet/minecraft/server/packs/repository/RepositorySource;)V")
    private void addGeneratedResources(RepositorySource[] repositorySources, CallbackInfo ci) {
        this.sources = Sets.newConcurrentHashSet(this.sources);
        this.sources.add(packs -> {
            final String name = ModFolders.RESOURCE_DIR.getName();
            final Pack pack = Pack.readMetaAndCreate(
                name, Component.literal(name), true,
                id -> ResourceHelper.RESOURCES,
                PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
            if (pack != null) {
                packs.accept(pack);
            }
        });
    }
}
