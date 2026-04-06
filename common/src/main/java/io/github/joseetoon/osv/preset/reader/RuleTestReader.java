package io.github.joseetoon.osv.preset.reader;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import io.github.joseetoon.osv.world.rule.BlockSetRuleTest;

import static io.github.joseetoon.genlib.serialization.CodecUtils.asParent;
import static io.github.joseetoon.genlib.serialization.CodecUtils.simpleEither;

public class RuleTestReader {

    // RuleTest.CODEC is remapped to f_74307_ in the SRG production jar but Loom fails to
    // remap our reference to it. Use reflection trying both Mojang (dev) and SRG (prod) names.
    private static final Codec<RuleTest> RULE_TEST_CODEC = findRuleTestCodec();

    @SuppressWarnings("unchecked")
    private static Codec<RuleTest> findRuleTestCodec() {
        for (final String name : new String[] { "CODEC", "f_74307_" }) {
            try {
                final java.lang.reflect.Field f = RuleTest.class.getDeclaredField(name);
                f.setAccessible(true);
                return (Codec<RuleTest>) f.get(null);
            } catch (final NoSuchFieldException ignored) {
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Could not find RuleTest.CODEC (tried 'CODEC' and 'f_74307_')");
    }

    public static final Codec<RuleTest> CODEC = simpleEither(asParent(BlockSetRuleTest.CODEC), RULE_TEST_CODEC)
        .withEncoder(rule -> rule instanceof BlockSetRuleTest ? asParent(BlockSetRuleTest.CODEC) : RULE_TEST_CODEC);
}
