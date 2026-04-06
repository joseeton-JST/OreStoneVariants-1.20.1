package io.github.joseetoon.osv.preset.reader;

import net.minecraft.world.level.levelgen.VerticalAnchor;

public interface ValueHeightAccessor extends CommonHeightAccessor {
    VerticalAnchor getValue();

    @Override
    default VerticalAnchor getMinInclusive() {
        return this.getValue();
    }

    @Override
    default VerticalAnchor getMaxInclusive() {
        return this.getValue();
    }
}
