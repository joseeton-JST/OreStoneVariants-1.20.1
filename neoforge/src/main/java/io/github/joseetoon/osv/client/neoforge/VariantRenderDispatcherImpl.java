package io.github.joseetoon.osv.client.neoforge;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import io.github.joseetoon.osv.block.OreVariant;
import io.github.joseetoon.osv.config.VariantDescriptor;
import io.github.joseetoon.osv.preset.data.ModelSettings;

public class VariantRenderDispatcherImpl {

    @SuppressWarnings("deprecation")
    public static void setupRenderLayer(final VariantDescriptor descriptor, final OreVariant variant) {
        final RenderType layer = ItemBlockRenderTypes.getChunkRenderType(variant.getBg().defaultBlockState());
        if (descriptor.getForeground().getModel().getType() == ModelSettings.Type.OVERLAY) {
            ItemBlockRenderTypes.setRenderLayer(variant, l -> l == layer || l == RenderType.translucent());
            return;
        }
        ItemBlockRenderTypes.setRenderLayer(variant, layer);
    }
}
