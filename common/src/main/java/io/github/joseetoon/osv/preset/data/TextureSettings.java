package io.github.joseetoon.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.osv.client.texture.Modifier;
import io.github.joseetoon.osv.preset.reader.StateMapReader;
import io.github.joseetoon.osv.util.StateMap;

import java.util.List;

import static io.github.joseetoon.genlib.serialization.CodecUtils.codecOf;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.defaulted;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.nullable;

@Value
@FieldNameConstants
public class TextureSettings implements DynamicSerializable<TextureSettings> {

    boolean shade;
    @Nullable Float threshold;
    @Nullable ResourceLocation background;
    @Nullable StateMap<List<ResourceLocation>> original;
    @Nullable StateMap<List<ResourceLocation>> overlay;
    @Nullable StateMap<List<Modifier>> modifiers;

    public static final Codec<TextureSettings> CODEC = codecOf(
        defaulted(Codec.BOOL, Fields.shade, true, TextureSettings::isShade),
        nullable(Codec.FLOAT, Fields.threshold, TextureSettings::getThreshold),
        nullable(ResourceLocation.CODEC, Fields.background, TextureSettings::getBackground),
        nullable(StateMapReader.IDS, Fields.original, TextureSettings::getOriginal),
        nullable(StateMapReader.IDS, Fields.overlay, TextureSettings::getOverlay),
        nullable(StateMapReader.MODIFIERS, Fields.modifiers,  TextureSettings::getModifiers),
        TextureSettings::new
    );

    public static final TextureSettings EMPTY = new TextureSettings(true, null, null, null, null, null);

    @Override
    public Codec<TextureSettings> codec() {
        return CODEC;
    }
}
