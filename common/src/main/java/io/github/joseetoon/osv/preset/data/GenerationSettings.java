package io.github.joseetoon.osv.preset.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Value;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static io.github.joseetoon.genlib.serialization.CodecUtils.easyList;

@Value
public class GenerationSettings {

    boolean enabled;
    @Nullable List<PlacedFeatureSettings<?, ?>> features;

    private static final Codec<List<PlacedFeatureSettings<?, ?>>> FEATURES_CODEC =
        easyList(PlacedFeatureSettings.CODEC);

    private static final Codec<GenerationSettings> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.optionalFieldOf("enabled", false).forGetter(GenerationSettings::isEnabled),
        FEATURES_CODEC.optionalFieldOf("features").forGetter(gen -> Optional.ofNullable(gen.features))
    ).apply(instance, (enabled, features) -> new GenerationSettings(enabled, features.orElse(null))));

    public static final GenerationSettings EMPTY = new GenerationSettings(false, null);

    public static final Codec<GenerationSettings> CODEC =
        Codec.either(Codec.unit(Unit.INSTANCE), Codec.either(FEATURES_CODEC, OBJECT_CODEC)).xmap(
            either -> either.map(
                unit -> EMPTY,
                value -> value.map(features -> new GenerationSettings(false, features), g -> g)
            ),
            gen -> gen.features == null
                ? Either.left(Unit.INSTANCE)
                : Either.right(Either.right(gen))
        );
}
