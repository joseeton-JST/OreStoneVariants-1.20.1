package io.github.joseetoon.osv.world.carver;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderSet;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import io.github.joseetoon.genlib.data.DimensionPredicate;

import static io.github.joseetoon.genlib.serialization.CodecUtils.codecOf;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.field;

public class DimensionLocalCarverConfig extends CarverConfiguration {

    public static final Codec<DimensionLocalCarverConfig> CODEC = codecOf(
        field(DimensionPredicate.CODEC, "dimensions", cfg -> cfg.dimensions),
        field(ConfiguredWorldCarver.DIRECT_CODEC, "delegate", cfg -> cfg.delegate),
        DimensionLocalCarverConfig::new
    );

    public final DimensionPredicate dimensions;
    public final ConfiguredWorldCarver<?> delegate;

    public DimensionLocalCarverConfig(final DimensionPredicate dims, final ConfiguredWorldCarver<?> delegate) {
        super(1.0F, ConstantHeight.ZERO, ConstantFloat.ZERO, VerticalAnchor.BOTTOM, CarverDebugSettings.DEFAULT,
            HolderSet.direct());
        this.dimensions = dims;
        this.delegate = delegate;
    }
}
