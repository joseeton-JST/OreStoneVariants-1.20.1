package io.github.joseetoon.osv.mixin;

import net.minecraft.util.valueproviders.UniformInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UniformInt.class)
public interface UniformIntAccessor {

    @Accessor
    int getMinInclusive();

    @Accessor
    int getMaxInclusive();
}
