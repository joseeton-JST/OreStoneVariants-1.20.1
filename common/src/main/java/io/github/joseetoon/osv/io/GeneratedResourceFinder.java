package io.github.joseetoon.osv.io;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.util.function.Consumer;

public class GeneratedResourceFinder implements RepositorySource {

    @Override
    public void loadPacks(Consumer<Pack> packs) {
        final String name = ModFolders.RESOURCE_DIR.getName();
        final PackLocationInfo info = new PackLocationInfo(name, Component.literal(name), PackSource.BUILT_IN, java.util.Optional.empty());
        final PackSelectionConfig selection = new PackSelectionConfig(true, Pack.Position.TOP, false);
        final Pack pack = Pack.readMetaAndCreate(
            info,
            new Pack.ResourcesSupplier() {
                @Override
                public net.minecraft.server.packs.PackResources openPrimary(final PackLocationInfo packLocationInfo) {
                    return ResourceHelper.createPackResources(packLocationInfo);
                }

                @Override
                public net.minecraft.server.packs.PackResources openFull(
                    final PackLocationInfo packLocationInfo,
                    final Pack.Metadata metadata
                ) {
                    return ResourceHelper.createPackResources(packLocationInfo);
                }
            },
            PackType.CLIENT_RESOURCES,
            selection);
        if (pack != null) {
            packs.accept(pack);
        }
    }
}
