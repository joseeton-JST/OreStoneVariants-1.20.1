package io.github.joseetoon.osv;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExpectPlatformImplPresenceTest {

    private static final List<String> IMPL_CLASSES = List.of(
        "io.github.joseetoon.osv.block.neoforge.OreVariantImpl",
        "io.github.joseetoon.osv.client.neoforge.VariantColorizerImpl",
        "io.github.joseetoon.osv.client.neoforge.VariantRenderDispatcherImpl",
        "io.github.joseetoon.osv.client.model.neoforge.OverlayModelGeneratorImpl",
        "io.github.joseetoon.osv.compat.collector.create.neoforge.CreateClusterCollectorImpl",
        "io.github.joseetoon.osv.compat.collector.create.neoforge.CreateClusterDecoratorCollectorImpl",
        "io.github.joseetoon.osv.compat.collector.create.neoforge.CreateClusterPlacementCollectorImpl",
        "io.github.joseetoon.osv.compat.transformer.neoforge.OreTransformersImpl",
        "io.github.joseetoon.osv.config.neoforge.CfgImpl",
        "io.github.joseetoon.osv.item.neoforge.DenseVariantTabImpl",
        "io.github.joseetoon.osv.item.neoforge.ModCreativeTabs",
        "io.github.joseetoon.osv.item.neoforge.VariantTabImpl",
        "io.github.joseetoon.osv.preset.data.neoforge.PlatformBlockSettingsImpl",
        "io.github.joseetoon.osv.preset.reader.neoforge.LootTableReaderImpl",
        "io.github.joseetoon.osv.tag.neoforge.OsvTagPackResources",
        "io.github.joseetoon.osv.tag.neoforge.TagHelperTagUpdateContextImpl",
        "io.github.joseetoon.osv.util.neoforge.StartupSnapshot",
        "io.github.joseetoon.osv.util.unsafe.neoforge.UnsafeUtilsImpl"
    );

    @Test
    void allCriticalPlatformImplementationsAreLoadable() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (final String className : IMPL_CLASSES) {
            assertNotNull(loader.getResource(className.replace('.', '/') + ".class"), className);
        }
    }
}
