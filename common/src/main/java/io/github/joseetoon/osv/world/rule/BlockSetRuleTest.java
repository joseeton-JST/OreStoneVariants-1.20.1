package io.github.joseetoon.osv.world.rule;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import io.github.joseetoon.genlib.data.InvertibleSet;
import io.github.joseetoon.genlib.serialization.EasyStateCodec;
import io.github.joseetoon.osv.util.Reference;

import java.util.Collections;
import java.util.Set;

import static io.github.joseetoon.genlib.serialization.CodecUtils.easySet;

public class BlockSetRuleTest extends RuleTest {

    public static final Codec<BlockSetRuleTest> CODEC = easySet(EasyStateCodec.INSTANCE)
        .xmap(BlockSetRuleTest::new, rule -> rule.blocks);

    // BuiltInRegistries.RULE_TEST is remapped to f_256978_ and Registry.register to m_122965_
    // in the SRG production jar, but Loom fails to remap our calls. Use reflection as a fallback.
    public static final RuleTestType<BlockSetRuleTest> INSTANCE = registerRuleTestType();

    @SuppressWarnings("unchecked")
    private static RuleTestType<BlockSetRuleTest> registerRuleTestType() {
        final ResourceLocation id = new ResourceLocation(Reference.MOD_ID, "block_set_rule_test");
        final RuleTestType<BlockSetRuleTest> type = () -> CODEC;
        // Try Mojang name (dev env), then SRG name (prod env)
        for (final String fieldName : new String[] { "RULE_TEST", "f_256978_" }) {
            try {
                final java.lang.reflect.Field f = net.minecraft.core.registries.BuiltInRegistries.class.getDeclaredField(fieldName);
                f.setAccessible(true);
                final Registry<RuleTestType<?>> reg = (Registry<RuleTestType<?>>) f.get(null);
                // Registry.register(Registry, ResourceLocation, Object) — try Mojang then SRG
                for (final String methodName : new String[] { "register", "m_122965_" }) {
                    try {
                        final java.lang.reflect.Method m = Registry.class.getMethod(methodName, Registry.class, ResourceLocation.class, Object.class);
                        return (RuleTestType<BlockSetRuleTest>) m.invoke(null, reg, id, type);
                    } catch (final NoSuchMethodException ignored) {
                    } catch (final java.lang.reflect.InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                throw new RuntimeException("Could not find Registry.register(Registry,ResourceLocation,Object)");
            } catch (final NoSuchFieldException ignored) {
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Could not find BuiltInRegistries.RULE_TEST (tried 'RULE_TEST' and 'f_256978_')");
    }

    // Blocks.STONE (f_50069_) and Block.defaultBlockState() (m_49966_) are not remapped by Loom.
    public static final BlockSetRuleTest STONE_ONLY =
        new BlockSetRuleTest(Collections.singleton(getStoneState()));

    private static BlockState getStoneState() {
        net.minecraft.world.level.block.Block stone = null;
        for (final String fieldName : new String[] { "STONE", "f_50069_" }) {
            try {
                final java.lang.reflect.Field f = Blocks.class.getDeclaredField(fieldName);
                f.setAccessible(true);
                stone = (net.minecraft.world.level.block.Block) f.get(null);
                break;
            } catch (final NoSuchFieldException ignored) {
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        if (stone == null) {
            throw new RuntimeException("Could not find Blocks.STONE (tried 'STONE' and 'f_50069_')");
        }
        for (final String methodName : new String[] { "defaultBlockState", "m_49966_" }) {
            try {
                final java.lang.reflect.Method m = stone.getClass().getMethod(methodName);
                return (BlockState) m.invoke(stone);
            } catch (final NoSuchMethodException ignored) {
            } catch (final java.lang.reflect.InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Could not find Block.defaultBlockState() (tried 'defaultBlockState' and 'm_49966_')");
    }

    private final Set<BlockState> blocks;

    public BlockSetRuleTest(final Set<BlockState> blocks) {
        this.blocks = new InvertibleSet<>(blocks, false).optimize(Collections.emptySet());
    }

    @Override
    public boolean test(final BlockState state, final RandomSource rand) {
        return this.blocks.contains(state);
    }

    @Override
    protected RuleTestType<?> getType() {
        return INSTANCE;
    }
}
