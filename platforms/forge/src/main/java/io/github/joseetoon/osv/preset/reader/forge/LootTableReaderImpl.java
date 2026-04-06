package io.github.joseetoon.osv.preset.reader.forge;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.common.ForgeHooks;

import java.util.concurrent.atomic.AtomicReference;

public class LootTableReaderImpl {

    private static final AtomicReference<LootDataManager> TABLES = new AtomicReference<>();
    private static final Gson GSON = Deserializers.createLootTableSerializer().create();

    public static LootTable loadWithHooks(final ResourceLocation id, final JsonElement gson) {
        return ForgeHooks.loadLootTable(GSON, id, gson, true);
    }

    public static synchronized void updateTables(final LootDataManager tables) {
        TABLES.set(tables);
    }
}
