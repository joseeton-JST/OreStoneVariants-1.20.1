package io.github.joseetoon.osv.item.neoforge;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import io.github.joseetoon.osv.ModRegistries;
import io.github.joseetoon.osv.block.AdditionalProperties;
import io.github.joseetoon.osv.item.VariantItem;
import io.github.joseetoon.osv.util.Reference;

public class ModCreativeTabs {

    // Registries.CREATIVE_MODE_TAB and ResourceKey.createRegistryKey are not remapped
    // correctly by Loom in this project. Use the ResourceLocation overload of
    // DeferredRegister.create â€” ResourceLocation constructors are never SRG-remapped.
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.<CreativeModeTab>create(
            ResourceLocation.fromNamespaceAndPath("minecraft", "creative_mode_tab"), Reference.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VARIANT_TAB = TABS.register("variants", () ->
        build(icon(title(CreativeModeTab.builder(), translatable("itemGroup.osv.variants")), () -> {
            final var variant = ModRegistries.VARIANTS
                .findByValue(v -> v.getFg().equals(Blocks.COAL_ORE) && v.getBg().equals(Blocks.DIORITE))
                .orElseGet(() -> ModRegistries.VARIANTS.iterator().next());
            return new ItemStack(variant);
        })));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DENSE_VARIANT_TAB = TABS.register("dense_variants", () ->
        build(icon(title(CreativeModeTab.builder(), translatable("itemGroup.osv.dense_variants")), () -> {
            final VariantItem item = ModRegistries.ITEMS
                .findByValue(v -> v.getFg().equals(Blocks.DIAMOND_ORE) && v.getBg().equals(Blocks.GRANITE))
                .orElseGet(ModCreativeTabs::firstDense);
            return new ItemStack(item);
        })));

    // CreativeModeTab$Builder.build() is remapped to m_257652_ in the SRG production jar
    // but Loom fails to remap it. Try Mojang name first (dev), then SRG name (prod).
    private static CreativeModeTab build(final CreativeModeTab.Builder builder) {
        for (final String name : new String[] { "build", "m_257652_" }) {
            try {
                final java.lang.reflect.Method m = CreativeModeTab.Builder.class.getMethod(name);
                return (CreativeModeTab) m.invoke(builder);
            } catch (final NoSuchMethodException ignored) {
            } catch (final java.lang.reflect.InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Could not find CreativeModeTab$Builder.build() (tried 'build' and 'm_257652_')");
    }

    // CreativeModeTab$Builder.icon(Supplier) is remapped to m_257737_ in the SRG production jar
    // but Loom fails to remap it. Try Mojang name first (dev), then SRG name (prod).
    private static CreativeModeTab.Builder icon(final CreativeModeTab.Builder builder, final java.util.function.Supplier<ItemStack> icon) {
        for (final String name : new String[] { "icon", "m_257737_" }) {
            try {
                final java.lang.reflect.Method m = CreativeModeTab.Builder.class.getMethod(name, java.util.function.Supplier.class);
                return (CreativeModeTab.Builder) m.invoke(builder, icon);
            } catch (final NoSuchMethodException ignored) {
            } catch (final java.lang.reflect.InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Could not find CreativeModeTab$Builder.icon(Supplier) (tried 'icon' and 'm_257737_')");
    }

    // CreativeModeTab$Builder.title(Component) is remapped to m_257941_ in the SRG production jar
    // but Loom fails to remap it. Try Mojang name first (dev), then SRG name (prod).
    private static CreativeModeTab.Builder title(final CreativeModeTab.Builder builder, final net.minecraft.network.chat.Component title) {
        for (final String name : new String[] { "title", "m_257941_" }) {
            try {
                final java.lang.reflect.Method m = CreativeModeTab.Builder.class.getMethod(name, net.minecraft.network.chat.Component.class);
                return (CreativeModeTab.Builder) m.invoke(builder, title);
            } catch (final NoSuchMethodException ignored) {
            } catch (final java.lang.reflect.InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Could not find CreativeModeTab$Builder.title(Component) (tried 'title' and 'm_257941_')");
    }

    // translatable(String) is remapped to m_237115_ in the SRG production jar
    // but Loom fails to remap it in this new file. Try Mojang name first (dev), then SRG name (prod).
    private static MutableComponent translatable(final String key) {
        for (final String name : new String[] { "translatable", "m_237115_" }) {
            try {
                final java.lang.reflect.Method m = Component.class.getMethod(name, String.class);
                return (MutableComponent) m.invoke(null, key);
            } catch (final NoSuchMethodException ignored) {
            } catch (final java.lang.reflect.InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Could not find translatable(String) (tried 'translatable' and 'm_237115_')");
    }

    private static VariantItem firstDense() {
        for (final VariantItem item : ModRegistries.ITEMS) {
            if (AdditionalProperties.isDense(item.getState())) {
                return item;
            }
        }
        return ModRegistries.ITEMS.iterator().next();
    }
}
