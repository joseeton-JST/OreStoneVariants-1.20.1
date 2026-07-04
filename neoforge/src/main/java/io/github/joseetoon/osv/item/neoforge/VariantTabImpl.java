package io.github.joseetoon.osv.item.neoforge;

import net.minecraft.world.item.CreativeModeTab;

public class VariantTabImpl {

    public static CreativeModeTab createInstance() {
        return ModCreativeTabs.VARIANT_TAB.get();
    }
}
