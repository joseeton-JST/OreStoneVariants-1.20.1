package io.github.joseetoon.osv.preset.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import io.github.joseetoon.genlib.event.registry.CommonRegistries;
import io.github.joseetoon.genlib.serialization.HjsonOps;
import io.github.joseetoon.genlib.util.HjsonUtils;
import io.github.joseetoon.osv.io.ModFolders;
import io.github.joseetoon.osv.preset.data.GenerationSettings;
import io.github.joseetoon.osv.preset.data.OreSettings;
import io.github.joseetoon.osv.preset.data.StoneSettings;
import io.github.joseetoon.osv.world.rule.BlockSetRuleTest;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.io.File;
import java.util.Objects;

public final class PresetGenerator {

    public enum Option {
        ORE,
        STONE
    }

    public static void generate(final Option option, final LevelAccessor level, final BlockPos pos, final BlockState block) {
        final ResourceLocation id = CommonRegistries.BLOCKS.getKey(block.getBlock());
        final String generatedName = id != null ? id.getPath() : "generated_" + System.currentTimeMillis();
        generateAs(option, level, pos, block, generatedName);
    }

    public static void generateAs(
            final Option option, final LevelAccessor level, final BlockPos pos, final BlockState block, final String name) {
        switch (option) {
            case ORE -> generateOre(block, name);
            case STONE -> generateStone(block, name);
        }
    }

    private static void generateOre(final BlockState block, final String name) {
        final ResourceLocation id = Objects.requireNonNull(CommonRegistries.BLOCKS.getKey(block.getBlock()));
        final JsonValue encoded = OreSettings.CODEC.encodeStart(HjsonOps.INSTANCE, OreSettings.forBlock(id))
            .result().orElseGet(JsonObject::new);
        final File output = new File(ModFolders.ORE_DIR, id.getNamespace() + "/" + name + ".xjs");
        HjsonUtils.writeJson(encoded.asObject(), output).expect("Could not write ore preset: {}", output);
    }

    private static void generateStone(final BlockState block, final String name) {
        final ResourceLocation id = Objects.requireNonNull(CommonRegistries.BLOCKS.getKey(block.getBlock()));
        final StoneSettings settings = new StoneSettings(block, BlockSetRuleTest.STONE_ONLY, GenerationSettings.EMPTY);
        final JsonValue encoded = StoneSettings.CODEC.encodeStart(HjsonOps.INSTANCE, settings)
            .result().orElseGet(JsonObject::new);
        final File output = new File(ModFolders.STONE_DIR, id.getNamespace() + "/" + name + ".xjs");
        HjsonUtils.writeJson(encoded.asObject(), output).expect("Could not write stone preset: {}", output);
    }

    private PresetGenerator() {}
}
