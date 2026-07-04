package io.github.joseetoon.osv.preset.reader;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.FoodProperties.PossibleEffect;

import java.util.ArrayList;
import java.util.List;

import static io.github.joseetoon.genlib.serialization.CodecUtils.dynamic;
import static io.github.joseetoon.genlib.serialization.CodecUtils.easyList;
import static io.github.joseetoon.genlib.serialization.DynamicField.field;

public class FoodPropertiesReader {

    public static final Codec<FoodProperties> CODEC = dynamic(FoodPropertiesBuilder::new, FoodPropertiesBuilder::build).create(
        field(Codec.INT, "nutrition", FoodProperties::nutrition, (f, i) -> f.wrapped.nutrition(i)),
        field(Codec.FLOAT, "saturationModifier", FoodProperties::saturation, (f, m) -> f.wrapped.saturationModifier(m)),
        field(Codec.BOOL, "isMeat", ignored -> false, FoodPropertiesBuilder::setIsMeat),
        field(Codec.BOOL, "canAlwaysEat", FoodProperties::canAlwaysEat, FoodPropertiesBuilder::setCanAlwaysEat),
        field(Codec.BOOL, "fastFood", food -> food.eatSeconds() < 1.6F, FoodPropertiesBuilder::setFastFood),
        field(easyList(MobEffectReader.CODEC), "effects", FoodPropertiesReader::toLegacyEffects, FoodPropertiesBuilder::addEffects)
    );

    private static List<Pair<MobEffectInstance, Float>> toLegacyEffects(final FoodProperties food) {
        final List<Pair<MobEffectInstance, Float>> effects = new ArrayList<>();
        for (final PossibleEffect effect : food.effects()) {
            effects.add(Pair.of(effect.effect(), effect.probability()));
        }
        return effects;
    }

    private static class FoodPropertiesBuilder {
        FoodProperties.Builder wrapped;

        FoodPropertiesBuilder() {
            this.wrapped = new FoodProperties.Builder();
        }

        void setIsMeat(final boolean ignored) {
            // 1.21.x removed the meat flag from FoodProperties.
        }

        void setCanAlwaysEat(final boolean canAlwaysEat) {
            if (canAlwaysEat) {
                this.wrapped.alwaysEdible();
            }
        }

        void setFastFood(final boolean fastFood) {
            if (fastFood) {
                this.wrapped.fast();
            }
        }

        void addEffects(final List<Pair<MobEffectInstance, Float>> effects) {
            effects.forEach(p -> this.wrapped.effect(p.getFirst(), p.getSecond()));
        }

        FoodProperties build() {
            return this.wrapped.build();
        }
    }
}
