package io.github.joseetoon.osv.io;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.util.function.Consumer;

public class GeneratedResourceFinder implements RepositorySource {

    @Override
    public void loadPacks(Consumer<Pack> packs) {
        final String name = ModFolders.RESOURCE_DIR.getName();
        final Pack pack = Pack.readMetaAndCreate(
            name, Component.literal(name), true,
            id -> ResourceHelper.RESOURCES,
            PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
        if (pack != null) {
            packs.accept(pack);
        }
    }
}
