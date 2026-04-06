package io.github.joseetoon.osv.compat.transformer;

import io.github.joseetoon.genlib.util.JsonTransformer;

public class CommonConfigTransformers {
    public static final JsonTransformer.ObjectResolver BLOCK_REGISTRY = JsonTransformer.root().freeze();
    public static final JsonTransformer.ObjectResolver WORLD_GEN = JsonTransformer.root().freeze();
    public static final JsonTransformer.ObjectResolver MOD_SUPPORT = JsonTransformer.root().freeze();
}
