package io.github.joseetoon.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import io.github.joseetoon.genlib.data.Range;
import io.github.joseetoon.osv.world.decorator.DecoratorProvider;
import io.github.joseetoon.osv.world.decorator.FlexibleVariantDecorator;

import java.util.ArrayList;
import java.util.List;

import static io.github.joseetoon.genlib.serialization.CodecUtils.codecOf;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.defaulted;

@Value
@Builder
@FieldNameConstants
public class FlexibleDecoratorSettings implements DecoratorProvider<FlexibleDecoratorSettings> {

    public static final FlexibleDecoratorSettings DEFAULTS = FlexibleDecoratorSettings.builder().build();

    @Default int spread = 0;
    @Default double chance = 1.0;
    @Default int bias = 0;
    @Default int extraCount = 0;
    @Default double extraChance = 0;
    @Default Range count = new Range(2);
    @Default Range height = new Range(0, 32);

    public static final Codec<FlexibleDecoratorSettings> CODEC = codecOf(
        defaulted(Codec.INT, Fields.spread, 0, FlexibleDecoratorSettings::getSpread),
        defaulted(Codec.DOUBLE, Fields.chance, 1.0, FlexibleDecoratorSettings::getChance),
        defaulted(Codec.INT, Fields.bias, 0, FlexibleDecoratorSettings::getBias),
        defaulted(Codec.INT, Fields.extraCount, 0, FlexibleDecoratorSettings::getExtraCount),
        defaulted(Codec.DOUBLE, Fields.extraChance, 0.0, FlexibleDecoratorSettings::getExtraChance),
        defaulted(Range.CODEC, Fields.count, new Range(2), FlexibleDecoratorSettings::getCount),
        defaulted(Range.CODEC, Fields.height, new Range(0, 32), FlexibleDecoratorSettings::getHeight),
        FlexibleDecoratorSettings::new
    );

    /**
     * In 1.20.1, feature decoration uses PlacedFeature with a list of PlacementModifiers.
     * The old .decorated() chain was removed in 1.18.
     * This method now builds a list of PlacementModifiers.
     */
    public List<PlacementModifier> buildPlacementModifiers() {
        final List<PlacementModifier> modifiers = new ArrayList<>();

        // Add the flexible height/chance decorator
        modifiers.add(new FlexibleVariantDecorator(this.count, this.height, this.chance));

        // Extra chance placement
        if (this.extraChance > 0 && this.extraCount > 0) {
            final int chance = (int) (1.0 / this.extraChance);
            modifiers.add(RarityFilter.onAverageOnceEvery(chance));
        }

        return modifiers;
    }

    @Override
    public Codec<FlexibleDecoratorSettings> codec() {
        return CODEC;
    }
}
