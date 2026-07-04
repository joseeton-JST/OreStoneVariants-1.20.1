package io.github.joseetoon.genlib.util;

import io.github.joseetoon.genlib.data.JsonType;
import io.github.joseetoon.genlib.data.ModDescriptor;
import io.github.joseetoon.genlib.versioning.Version;

import java.util.Arrays;
import java.util.List;

public class LibReference {

    public static final String MOD_ID = "genlib";
    public static final String MOD_NAME = "GenLib";
    public static final Version MOD_VERSION = Version.parse("1.2.22");

    public static final ModDescriptor MOD_DESCRIPTOR =
        ModDescriptor.builder().modId(MOD_ID).name(MOD_NAME).version(MOD_VERSION)
            .configFolder(McUtils.getConfigDir()).build();

    /** @deprecated Use {@link JsonType} */
    @Deprecated
    public static final List<String> JSON_EXTENSIONS = Arrays.asList("json", "mcmeta");

    /** @deprecated Use {@link JsonType} */
    @Deprecated
    public static final List<String> HJSON_EXTENSIONS = Arrays.asList("hjson", "cave");
}
