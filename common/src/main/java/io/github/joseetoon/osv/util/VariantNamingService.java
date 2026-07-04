package io.github.joseetoon.osv.util;

import net.minecraft.resources.ResourceLocation;

public class VariantNamingService {

    private static final String MINECRAFT = "minecraft";
    private static final String STONE = "stone";

    public static ResourceLocation create(final String foreground, final ResourceLocation background) {
        final String fgFormat = formatFg(foreground);
        final String bgFormat = formatBg(background);
        if (bgFormat.isEmpty()) {
            return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, fgFormat);
        }
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, fgFormat + "_" + bgFormat);
    }

    public static String formatFg(final String foreground) {
        return formatFg(ResourceLocation.parse(foreground));
    }

    public static String formatFg(final ResourceLocation foreground) {
        if (MINECRAFT.equals(RlUtils.ns(foreground))) {
            return RlUtils.path(foreground);
        }
        return RlUtils.ns(foreground) + "_" + RlUtils.path(foreground);
    }

    public static String formatBg(final ResourceLocation id) {
        if (MINECRAFT.equals(RlUtils.ns(id))) {
            return STONE.equals(RlUtils.path(id)) ? "" : RlUtils.path(id);
        }
        if (STONE.equals(RlUtils.path(id))) {
            return RlUtils.ns(id);
        }
        return RlUtils.ns(id) + "_" + RlUtils.path(id);
    }
}
