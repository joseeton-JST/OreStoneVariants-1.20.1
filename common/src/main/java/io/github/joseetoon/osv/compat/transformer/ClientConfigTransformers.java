package io.github.joseetoon.osv.compat.transformer;

import io.github.joseetoon.genlib.util.JsonTransformer;

public class ClientConfigTransformers {
    public static final JsonTransformer.ObjectResolver ROOT = JsonTransformer.root().freeze();
    public static final JsonTransformer.ObjectResolver RESOURCES = JsonTransformer.root().freeze();
}
