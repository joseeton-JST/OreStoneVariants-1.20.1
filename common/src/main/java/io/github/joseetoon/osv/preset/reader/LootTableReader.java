package io.github.joseetoon.osv.preset.reader;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.util.LibStringUtils;
import personthecat.fresult.Result;
import io.github.joseetoon.osv.exception.InvalidPresetArgumentException;
import io.github.joseetoon.osv.util.Reference;

public class LootTableReader {

    public static Result<LootTable, InvalidPresetArgumentException> read(final Dynamic<?> config) {
        try {
            final String path = "dynamic_loot/" + LibStringUtils.randId(16);
            final ResourceLocation id = new ResourceLocation(Reference.MOD_ID, path);
            final JsonElement gson = config.convert(JsonOps.INSTANCE).getValue();
            final LootTable table = loadWithHooks(id, gson);

            return table != null ? Result.ok(table) : Result.err(
                new InvalidPresetArgumentException("Reading custom loot", new JsonParseException("Unknown error")));

        } catch (final RuntimeException e) {
            return Result.err(new InvalidPresetArgumentException("Reading loot table", e));
        }
    }

    @Nullable
    @ExpectPlatform
    private static LootTable loadWithHooks(final ResourceLocation id, final JsonElement gson) {
        throw new AssertionError();
    }
}
