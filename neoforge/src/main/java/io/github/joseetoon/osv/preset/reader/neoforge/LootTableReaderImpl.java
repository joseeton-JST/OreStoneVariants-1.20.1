package io.github.joseetoon.osv.preset.reader.neoforge;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;

public class LootTableReaderImpl {

    private static final Gson GSON = new Gson();

    public static LootTable loadWithHooks(final ResourceLocation id, final JsonElement gson) {
        return LootDataType.TABLE.deserialize(id, JsonOps.INSTANCE, GSON.fromJson(gson, JsonElement.class)).orElse(null);
    }
}
