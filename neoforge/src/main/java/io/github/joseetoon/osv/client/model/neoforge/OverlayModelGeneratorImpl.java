package io.github.joseetoon.osv.client.model.neoforge;

import lombok.extern.log4j.Log4j2;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import io.github.joseetoon.genlib.event.registry.CommonRegistries;
import io.github.joseetoon.osv.client.model.ModelWrapper;
import io.github.joseetoon.osv.config.Cfg;
import io.github.joseetoon.osv.config.VariantDescriptor;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Log4j2
public class OverlayModelGeneratorImpl {

    private static final String MODEL_TEMPLATE_PATH = "assets/osv/forge_model_template.txt";
    private static final String FALLBACK_MODEL_TEMPLATE = """
        {
          "loader": "forge:multi-layer",
          "parent": "block/block",
          "textures": { "particle": "{particle}" },
          "layers": {
            "{bg_layer}": { "parent": "{bg}" },
            "{fg_layer}": {
              "parent": "osv:block/overlay",
              "textures": { "overlay": "{fg}" }
            }
          }
        }
        """;
    private static final String MODEL_TEMPLATE = loadModelTemplate();

    public static JsonObject platformModel(
            final VariantDescriptor cfg, final ModelWrapper model, final ResourceLocation overlay) {
        final String raw = MODEL_TEMPLATE
            .replace("{bg}", model.getId().toString())
            .replace("{fg}", overlay.toString())
            .replace("{particle}", resolveParticle(model.getModel()).orElseGet(overlay::toString))
            .replace("{bg_layer}", getBgLayer(cfg.getBackground()))
            .replace("{fg_layer}", getFgLayer());

        return JsonValue.readHjson(raw).asObject();
    }

    private static Optional<String> resolveParticle(final JsonObject model) {
        final JsonValue textures = model.get("textures");
        if (textures == null || !textures.isObject()) {
            return Optional.empty();
        }
        final JsonValue particle = textures.asObject().get("particle");
        if (particle == null || !particle.isString()) {
            return Optional.empty();
        }
        return Optional.of(particle.asString());
    }

    private static String getBgLayer(final ResourceLocation id) {
        final Block block = CommonRegistries.BLOCKS.lookup(id);
        if (block == null) return "solid";

        final RenderType layer = ItemBlockRenderTypes.getChunkRenderType(block.defaultBlockState());
        if (layer == RenderType.translucent()) return "translucent";
        if (layer == RenderType.cutout()) return "cutout";
        if (layer == RenderType.cutoutMipped()) return "cutout_mipped";
        if (layer == RenderType.tripwire()) return "tripwire";
        return "solid";
    }

    private static String getFgLayer() {
        return Cfg.overlayTransparency() ? "translucent" : "cutout_mipped";
    }

    private static String loadModelTemplate() {
        try (InputStream is = OverlayModelGeneratorImpl.class.getClassLoader().getResourceAsStream(MODEL_TEMPLATE_PATH)) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (final IOException e) {
            log.warn("Could not read {}; using fallback template.", MODEL_TEMPLATE_PATH, e);
            return FALLBACK_MODEL_TEMPLATE;
        }
        log.warn("Could not find {}; using fallback template.", MODEL_TEMPLATE_PATH);
        return FALLBACK_MODEL_TEMPLATE;
    }
}
