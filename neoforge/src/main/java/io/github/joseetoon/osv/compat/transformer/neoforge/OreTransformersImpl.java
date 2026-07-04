package io.github.joseetoon.osv.compat.transformer.neoforge;

import io.github.joseetoon.genlib.util.JsonTransformer;
import io.github.joseetoon.genlib.util.JsonTransformer.ObjectResolver;

public class OreTransformersImpl {

    public static ObjectResolver createPlatform() {
        return JsonTransformer.root()
            .relocate("block.level", "forge.harvestLevel")
            .relocate("block.tool", "forge.harvestTool")
            .freeze();
    }
}
