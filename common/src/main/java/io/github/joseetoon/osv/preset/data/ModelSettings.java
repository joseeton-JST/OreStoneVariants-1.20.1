package io.github.joseetoon.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import io.github.joseetoon.osv.client.model.ModelGenerator;
import io.github.joseetoon.osv.client.model.OverlayModelGenerator;
import io.github.joseetoon.osv.client.model.SingleLayerModelGenerator;
import io.github.joseetoon.osv.config.Cfg;

import static io.github.joseetoon.genlib.serialization.CodecUtils.codecOf;
import static io.github.joseetoon.genlib.serialization.CodecUtils.ofEnum;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.defaultGet;

@Value
@FieldNameConstants
public class ModelSettings implements DynamicSerializable<ModelSettings> {

    Type type;
    boolean shade;

    public static final Codec<ModelSettings> CODEC = codecOf(
        defaultGet(Type.CODEC, Fields.type, Cfg::modelType, ModelSettings::getType),
        defaultGet(Codec.BOOL, Fields.shade, Cfg::overlayShade, ModelSettings::isShade),
        ModelSettings::new
    );

    public static final ModelSettings EMPTY = new ModelSettings(Cfg.modelType(), Cfg.overlayShade());

    @Override
    public Codec<ModelSettings> codec() {
        return CODEC;
    }

    @Getter
    @AllArgsConstructor
    public enum Type {
        SINGLE(new SingleLayerModelGenerator()),
        OVERLAY(new OverlayModelGenerator());

        ModelGenerator generator;

        public static final Codec<Type> CODEC = ofEnum(Type.class);
    }
}
