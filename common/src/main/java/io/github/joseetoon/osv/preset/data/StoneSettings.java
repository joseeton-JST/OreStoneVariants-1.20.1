package io.github.joseetoon.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import io.github.joseetoon.genlib.serialization.EasyStateCodec;
import io.github.joseetoon.osv.preset.reader.RuleTestReader;
import io.github.joseetoon.osv.world.rule.BlockSetRuleTest;

import static io.github.joseetoon.genlib.serialization.CodecUtils.codecOf;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.defaulted;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.field;

@Value
@FieldNameConstants
public class StoneSettings {

    BlockState stone;
    RuleTest source;
    GenerationSettings gen;

    public static final Codec<StoneSettings> CODEC = codecOf(
        field(EasyStateCodec.INSTANCE, Fields.stone, StoneSettings::getStone),
        defaulted(RuleTestReader.CODEC, Fields.source, BlockSetRuleTest.STONE_ONLY, StoneSettings::getSource),
        defaulted(GenerationSettings.CODEC, Fields.gen, GenerationSettings.EMPTY, StoneSettings::getGen),
        StoneSettings::new
    );
}
