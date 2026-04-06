package io.github.joseetoon.osv.client.blockstate;

import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.util.HjsonUtils;
import io.github.joseetoon.genlib.util.PathUtils;
import io.github.joseetoon.osv.client.ClientResourceHelper;
import io.github.joseetoon.osv.util.JsonCompat;
import io.github.joseetoon.osv.util.StateMap;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
public class BlockStateLoader {

    public static StateMap<List<VariantWrapper>> getModel(final ResourceLocation id) {
        final StateMap<List<VariantWrapper>> map = new StateMap<>();
        final JsonObject variants = loadVariants(id);
        if (variants != null) {
            for (final JsonObject.Member member : variants) {
                final List<VariantWrapper> wrappers = new ArrayList<>();
                for (final JsonValue value : JsonCompat.intoArray(member.getValue())) {
                    if (value.isObject()) {
                        VariantWrapper.tryCreate(value.asObject()).ifPresent(wrappers::add);
                    }
                }
                map.put(member.getName(), wrappers);
            }
        }
        return map;
    }

    @Nullable
    private static JsonObject loadVariants(final ResourceLocation id) {
        final String path = PathUtils.asBlockStatePath(id);
        final Optional<InputStream> resource = ClientResourceHelper.locateResource(path);
        if (resource.isPresent()) {
            try (final InputStream is = resource.get()) {
                final JsonObject def = HjsonUtils.readSuppressing(is).orElse(null);
                if (def == null) {
                    log.warn("Unable to parse block state definition for {}", id);
                    return null;
                }
                final JsonValue variants = def.get("variants");
                if (variants == null) {
                    log.warn("No variants definition in {}. Unable to load block state definition.", id);
                    return null;
                }
                return variants.asObject();
            } catch (final IOException | UnsupportedOperationException ignored) {}
        }
        return null;
    }
}
