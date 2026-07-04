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

    private static final ResourceLocation TYPE_ID =
        ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block_set_rule_test");

    public static final Codec<BlockSetRuleTest> CODEC = easySet(EasyStateCodec.INSTANCE)
        .xmap(BlockSetRuleTest::new, rule -> rule.blocks);

    private static final RuleTestType<BlockSetRuleTest> FALLBACK_TYPE =
        () -> com.mojang.serialization.MapCodec.assumeMapUnsafe(CODEC);

    private static volatile RuleTestType<BlockSetRuleTest> instance;

    public static void ensureRegistered() {
        getInstance();
    }

    @SuppressWarnings("unchecked")
    private static RuleTestType<BlockSetRuleTest> registerRuleTestType() {
        final RuleTestType<BlockSetRuleTest> type = FALLBACK_TYPE;
        for (final String fieldName : new String[] { "RULE_TEST", "f_256978_" }) {
            try {
                final java.lang.reflect.Field f = net.minecraft.core.registries.BuiltInRegistries.class.getDeclaredField(fieldName);
                f.setAccessible(true);
                final Registry<RuleTestType<?>> reg = (Registry<RuleTestType<?>>) f.get(null);
                for (final String methodName : new String[] { "register", "m_122965_" }) {
                    try {
                        final java.lang.reflect.Method m =
                            Registry.class.getMethod(methodName, Registry.class, ResourceLocation.class, Object.class);
                        return (RuleTestType<BlockSetRuleTest>) m.invoke(null, reg, TYPE_ID, type);
                    } catch (final NoSuchMethodException ignored) {
                    } catch (final java.lang.reflect.InvocationTargetException | IllegalAccessException e) {
                        if (isFrozenRegistry(e)) {
                            return FALLBACK_TYPE;
                        }
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

    private static boolean isFrozenRegistry(final Throwable error) {
        Throwable cause = error;
        while (cause != null) {
            if (cause instanceof IllegalStateException && cause.getMessage() != null
                && cause.getMessage().contains("already frozen")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private static RuleTestType<BlockSetRuleTest> getInstance() {
        RuleTestType<BlockSetRuleTest> current = instance;
        if (current == null) {
            synchronized (BlockSetRuleTest.class) {
                current = instance;
                if (current == null) {
                    current = registerRuleTestType();
                    instance = current;
                }
            }
        }
        return current;
    }

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
        return getInstance();
    }
}
