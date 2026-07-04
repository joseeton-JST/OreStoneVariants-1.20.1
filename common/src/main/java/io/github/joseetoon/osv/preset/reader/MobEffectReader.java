package io.github.joseetoon.osv.preset.reader;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;

public final class MobEffectReader {

    public static final Codec<Pair<MobEffectInstance, Float>> CODEC =
        BuiltInRegistries.MOB_EFFECT.holderByNameCodec().xmap(
            effect -> Pair.of(new MobEffectInstance(effect), 1.0F),
            pair -> pair.getFirst().getEffect()
        );

    private MobEffectReader() {}
}
