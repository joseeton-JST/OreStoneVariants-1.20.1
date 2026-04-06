package io.github.joseetoon.osv.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import io.github.joseetoon.osv.block.OreVariant;
import io.github.joseetoon.osv.config.VariantDescriptor;

@Environment(EnvType.CLIENT)
public class VariantRenderDispatcher {

    @ExpectPlatform
    public static void setupRenderLayer(final VariantDescriptor descriptor, final OreVariant variant) {
        throw new AssertionError();
    }
}
