package io.github.joseetoon.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.data.Range;

import static io.github.joseetoon.genlib.serialization.CodecUtils.codecOf;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.defaultGet;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.defaulted;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.nullable;

@Value
@FieldNameConstants
public class VariantSettings implements DynamicSerializable<VariantSettings> {

    private static final boolean DEFAULT_BG_IMITATION = true;
    private static final boolean DEFAULT_BG_DUPLICATION = true;

    @Nullable ResourceLocation original;
    @Nullable Range xp;
    @Nullable String translationKey;
    boolean copyTags;
    boolean canBeDense;
    boolean bgImitation;
    boolean bgDuplication;

    public static final Codec<VariantSettings> CODEC = codecOf(
        nullable(ResourceLocation.CODEC, Fields.original, o -> o.original),
        nullable(Range.CODEC, Fields.xp, o -> o.xp),
        nullable(Codec.STRING, Fields.translationKey, o -> o.translationKey),
        defaulted(Codec.BOOL, Fields.copyTags, true, o -> o.copyTags),
        defaulted(Codec.BOOL, Fields.canBeDense, true, o -> o.canBeDense),
        defaultGet(Codec.BOOL, Fields.bgImitation, () -> DEFAULT_BG_IMITATION, o -> o.bgImitation),
        defaultGet(Codec.BOOL, Fields.bgDuplication, () -> DEFAULT_BG_DUPLICATION, o -> o.bgDuplication),
        VariantSettings::new
    );

    public static final VariantSettings EMPTY =
        new VariantSettings(null, null, null, true, true, DEFAULT_BG_IMITATION, DEFAULT_BG_DUPLICATION);

    public static VariantSettings withOriginal(final ResourceLocation id) {
        return new VariantSettings(id, null, null, true, true, DEFAULT_BG_IMITATION, DEFAULT_BG_DUPLICATION);
    }

    @Override
    public Codec<VariantSettings> codec() {
        return CODEC;
    }
}
