package io.github.joseetoon.osv.client.model;

import net.minecraft.resources.ResourceLocation;
import io.github.joseetoon.osv.client.texture.TextureHandler;
import io.github.joseetoon.osv.config.VariantDescriptor;
import io.github.joseetoon.osv.util.JsonCompat;
import org.hjson.JsonObject;

public class SingleLayerModelGenerator implements ModelGenerator {

    public JsonObject generateBlock(final VariantDescriptor cfg, final ModelWrapper model, final ResourceLocation overlay) {
        final JsonObject generated = model.getModel().shallowCopy().asObject();
        final JsonObject newTextures = new JsonObject();
        for (final JsonObject.Member member : JsonCompat.getAsserted(generated, "textures").asObject()) {
            final String texture = member.getValue().asString();
            if (texture.startsWith("#")) {
                newTextures.add(member.getName(), texture);
            } else {
                final ResourceLocation bg = new ResourceLocation(texture);
                ResourceLocation id = TextureHandler.generateSingleLayer(bg, overlay);
                if (id != null) {
                    newTextures.add(member.getName(), id.toString());
                }
            }
        }
        return generated.set("textures", newTextures);
    }
}
