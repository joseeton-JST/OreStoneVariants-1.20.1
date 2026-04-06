package io.github.joseetoon.osv.item.forge;

import net.minecraft.world.item.CreativeModeTab;

public class VariantTabImpl {

    public static CreativeModeTab createInstance() {
        return ModCreativeTabs.VARIANT_TAB.get();
    }
}
