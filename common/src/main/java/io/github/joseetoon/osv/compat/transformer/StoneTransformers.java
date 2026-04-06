package io.github.joseetoon.osv.compat.transformer;

import io.github.joseetoon.genlib.util.JsonTransformer;
import io.github.joseetoon.genlib.util.JsonTransformer.ObjectResolver;

public class StoneTransformers {
    public static final ObjectResolver ROOT =
        JsonTransformer.root()
            .relocate("block.location", "stone")
            .freeze();
}
