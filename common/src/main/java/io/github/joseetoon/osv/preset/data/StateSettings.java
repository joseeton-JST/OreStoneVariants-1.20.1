package io.github.joseetoon.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.osv.preset.reader.StateMapReader;
import io.github.joseetoon.osv.util.StateMap;

import java.util.Set;

import static io.github.joseetoon.genlib.serialization.CodecUtils.codecOf;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.nullable;

@Value
@FieldNameConstants
public class StateSettings implements DynamicSerializable<StateSettings> {

    @Nullable StateMap<MapColor> materialColor;
    @Nullable StateMap<Integer> lightEmission;
    @Nullable StateMap<Set<EntityType<?>>> isValidSpawn;
    @Nullable StateMap<Boolean> isRedstoneConductor;
    @Nullable StateMap<Boolean> isSuffocating;
    @Nullable StateMap<Boolean> isViewBlocking;
    @Nullable StateMap<Boolean> hasPostProcess;
    @Nullable StateMap<Boolean> emissiveRendering;

    public static final Codec<StateSettings> CODEC = codecOf(
        nullable(StateMapReader.COLORS, Fields.materialColor, s -> s.materialColor),
        nullable(StateMapReader.INT, Fields.lightEmission, s -> s.lightEmission),
        nullable(StateMapReader.ENTITIES, Fields.isValidSpawn, s -> s.isValidSpawn),
        nullable(StateMapReader.BOOL, Fields.isRedstoneConductor, s -> s.isRedstoneConductor),
        nullable(StateMapReader.BOOL, Fields.isSuffocating, s -> s.isSuffocating),
        nullable(StateMapReader.BOOL, Fields.isViewBlocking, s -> s.isViewBlocking),
        nullable(StateMapReader.BOOL, Fields.hasPostProcess, s -> s.hasPostProcess),
        nullable(StateMapReader.BOOL, Fields.emissiveRendering, s -> s.emissiveRendering),
        StateSettings::new
    );

    public static final StateSettings EMPTY =
        new StateSettings(null, null, null, null, null, null, null, null);

    @Override
    public Codec<StateSettings> codec() {
        return CODEC;
    }
}
