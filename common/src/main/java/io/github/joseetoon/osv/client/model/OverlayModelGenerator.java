package io.github.joseetoon.osv.client.model;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;
import io.github.joseetoon.osv.config.VariantDescriptor;
import org.hjson.JsonObject;

public class OverlayModelGenerator implements ModelGenerator {

    public JsonObject generateBlock(final VariantDescriptor cfg, final ModelWrapper model, final ResourceLocation overlay) {
        return platformModel(cfg, model, overlay);
    }

    @ExpectPlatform
    private static JsonObject platformModel(final VariantDescriptor cfg, final ModelWrapper model, final ResourceLocation overlay) {
        throw new AssertionError();
    }
}
