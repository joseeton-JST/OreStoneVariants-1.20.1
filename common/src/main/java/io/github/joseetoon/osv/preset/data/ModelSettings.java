package io.github.joseetoon.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import static io.github.joseetoon.genlib.serialization.CodecUtils.codecOf;
import static io.github.joseetoon.genlib.serialization.CodecUtils.ofEnum;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.defaultGet;

@Value
@FieldNameConstants
public class ModelSettings implements DynamicSerializable<ModelSettings> {

    private static final Type DEFAULT_TYPE = Type.SINGLE;
    private static final boolean DEFAULT_SHADE = true;

    Type type;
    boolean shade;

    public static final Codec<ModelSettings> CODEC = codecOf(
        defaultGet(Type.CODEC, Fields.type, () -> DEFAULT_TYPE, ModelSettings::getType),
        defaultGet(Codec.BOOL, Fields.shade, () -> DEFAULT_SHADE, ModelSettings::isShade),
        ModelSettings::new
    );

    public static final ModelSettings EMPTY = new ModelSettings(DEFAULT_TYPE, DEFAULT_SHADE);

    @Override
    public Codec<ModelSettings> codec() {
        return CODEC;
    }

    public enum Type {
        SINGLE,
        OVERLAY;

        public static final Codec<Type> CODEC = ofEnum(Type.class);
    }
}
