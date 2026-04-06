package io.github.joseetoon.osv.util;

import net.minecraft.resources.ResourceLocation;

/**
 * Safe replacements for ResourceLocation.getNamespace() and getPath().
 *
 * Loom's remapJar fails to remap getNamespace() (SRG: m_135827_) and getPath()
 * (SRG: m_135815_) in this project. Using toString() + substring is always safe
 * because String methods are never SRG-remapped.
 */
public final class RlUtils {

    private RlUtils() {}

    public static String ns(final ResourceLocation id) {
        final String s = id.toString();
        return s.substring(0, s.indexOf(':'));
    }

    public static String path(final ResourceLocation id) {
        final String s = id.toString();
        return s.substring(s.indexOf(':') + 1);
    }
}
