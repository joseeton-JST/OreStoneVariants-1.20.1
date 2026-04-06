package io.github.joseetoon.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.osv.preset.reader.FoodPropertiesReader;
import io.github.joseetoon.osv.preset.reader.StateMapReader;
import io.github.joseetoon.osv.util.StateMap;

import java.util.List;

import static io.github.joseetoon.genlib.serialization.CodecUtils.codecOf;
import static io.github.joseetoon.genlib.serialization.CodecUtils.ofEnum;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.nullable;

@Value
@FieldNameConstants
public class ItemSettings implements DynamicSerializable<ItemSettings> {

    @Nullable Boolean isFireResistant;
    @Nullable Integer maxStackSize;
    @Nullable Rarity rarity;
    @Nullable ResourceLocation craftRemainingItem;
    @Nullable SoundEvent eatingSound;
    @Nullable FoodProperties foodProperties;
    @Nullable StateMap<String> variants;
    @Nullable StateMap<List<Component>> formatters;

    private static final Codec<SoundEvent> SOUND_CODEC =
        ResourceLocation.CODEC.xmap(BuiltInRegistries.SOUND_EVENT::get, BuiltInRegistries.SOUND_EVENT::getKey);

    public static final Codec<ItemSettings> CODEC = codecOf(
        nullable(Codec.BOOL, Fields.isFireResistant, ItemSettings::getIsFireResistant),
        nullable(Codec.intRange(0, Integer.MAX_VALUE), Fields.maxStackSize, ItemSettings::getMaxStackSize),
        nullable(ofEnum(Rarity.class), Fields.rarity, ItemSettings::getRarity),
        nullable(ResourceLocation.CODEC, Fields.craftRemainingItem, ItemSettings::getCraftRemainingItem),
        nullable(SOUND_CODEC, Fields.eatingSound, ItemSettings::getEatingSound),
        nullable(FoodPropertiesReader.CODEC, Fields.foodProperties, ItemSettings::getFoodProperties),
        nullable(StateMapReader.STRING, Fields.variants, ItemSettings::getVariants),
        nullable(StateMapReader.COMPONENTS, Fields.formatters, ItemSettings::getFormatters),
        ItemSettings::new
    );

    public static final ItemSettings EMPTY = new ItemSettings(false, null, null,  null, null, null, null, null);

    @Override
    public Codec<ItemSettings> codec() {
        return CODEC;
    }
}
