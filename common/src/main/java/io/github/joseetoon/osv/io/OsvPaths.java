package io.github.joseetoon.osv.io;

import net.minecraft.resources.ResourceLocation;
import io.github.joseetoon.genlib.util.PathUtils;
import io.github.joseetoon.osv.util.Reference;

public class OsvPaths {

    public static String toOsvTexturePath(final String path) {
        final String subbed = PathUtils.namespaceToSub(path);
        return PathUtils.asTexturePath(Reference.MOD_ID, subbed);
    }

    public static ResourceLocation toOsvTextureId(final ResourceLocation id) {
        final String subbed = PathUtils.namespaceToSub(id);
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, subbed);
    }

    public static String fromForeign(final ResourceLocation id, final String prefix) {
        final String subbed = PathUtils.namespaceToSub(id);
        return PathUtils.prependFilename(subbed, prefix + "_");
    }
}
