package io.github.joseetoon.genlib.data;

import com.mojang.serialization.Codec;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.genlib.event.registry.DynamicRegistries;
import io.github.joseetoon.genlib.serialization.CodecUtils;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.function.Predicate;

import static io.github.joseetoon.genlib.serialization.CodecUtils.codecOf;
import static io.github.joseetoon.genlib.serialization.CodecUtils.simpleEither;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.defaultGet;
import static io.github.joseetoon.genlib.serialization.FieldDescriptor.defaulted;

@Getter
@Builder
@NotThreadSafe
@FieldNameConstants
@AllArgsConstructor
@RequiredArgsConstructor
public class BiomePredicate implements Predicate<Biome> {

    @With private final boolean blacklist;

    @NotNull private final List<ResourceLocation> names;
    @NotNull private final List<String> mods;
    @NotNull private final List<TagKey<Biome>> tags;

    @Nullable private Set<Biome> compiled;

    private int registryTracker;

    public static final BiomePredicate ALL_BIOMES = builder().build();

    private static final Codec<List<TagKey<Biome>>> BIOME_TAGS_CODEC =
        ResourceLocation.CODEC.xmap(
            rl -> TagKey.create(Registries.BIOME, rl),
            TagKey::location
        ).listOf();

    private static final Codec<BiomePredicate> OBJECT_CODEC = codecOf(
        defaulted(Codec.BOOL, Fields.blacklist, false, BiomePredicate::isBlacklist),
        defaultGet(CodecUtils.ID_LIST, Fields.names, Collections::emptyList, BiomePredicate::getNames),
        defaultGet(CodecUtils.STRING_LIST, Fields.mods, Collections::emptyList, BiomePredicate::getMods),
        defaultGet(BIOME_TAGS_CODEC, Fields.tags, Collections::emptyList, BiomePredicate::getTags),
        BiomePredicate::new
    );

    private static final Codec<BiomePredicate> ID_CODEC =
        CodecUtils.ID_LIST.xmap(ids -> builder().names(ids).build(), BiomePredicate::getNames);

    public static final Codec<BiomePredicate> CODEC = simpleEither(ID_CODEC, OBJECT_CODEC)
        .withEncoder(bp -> bp.isNamesOnly() ? ID_CODEC : OBJECT_CODEC);

    @Override
    public boolean test(final Biome biome) {
        return this.getCompiled().contains(biome);
    }

    public boolean test(final Holder<Biome> holder) {
        if (this.names.isEmpty() && this.mods.isEmpty() && this.tags.isEmpty()) {
            return !this.blacklist;
        }
        boolean rawMatch = false;
        final ResourceLocation id = holder.unwrapKey().map(k -> k.location()).orElse(null);
        if (id != null) {
            rawMatch |= this.names.contains(id);
            rawMatch |= this.mods.contains(id.getNamespace());
        }
        rawMatch |= this.tags.stream().anyMatch(holder::is);
        return rawMatch != this.blacklist;
    }

    @NotNull
    @SuppressWarnings("UnusedReturnValue")
    public synchronized Set<Biome> compile() {
        final Set<Biome> all = new HashSet<>();
        DynamicRegistries.BIOMES.forEach(all::add);

        if (this.isEmpty()) {
            return new InfinitySet<>(all);
        }
        final Set<Biome> matching = new HashSet<>();
        DynamicRegistries.BIOMES.forEach((id, biome) -> {
            if (this.matches(biome, id)) {
                matching.add(biome);
            }
        });
        this.registryTracker = DynamicRegistries.BIOMES.getId();
        return this.compiled = new InvertibleSet<>(matching, this.blacklist).optimize(all);
    }

    @NotNull
    public Set<Biome> getCompiled() {
        if (this.compiled == null || this.registryTracker != DynamicRegistries.BIOMES.getId()) {
            return this.compile();
        }
        return this.compiled;
    }

    public boolean isEmpty() {
        return this.names.isEmpty() && this.mods.isEmpty() && this.tags.isEmpty();
    }

    public boolean matches(final Biome biome, final ResourceLocation id) {
        if (this.isEmpty()) return true;
        return this.names.contains(id) || this.mods.contains(id.getNamespace());
    }

    public boolean matchesName(final ResourceLocation id) {
        return this.names.isEmpty() || this.names.contains(id);
    }

    public boolean matchesMod(final ResourceLocation id) {
        return this.mods.isEmpty() || this.mods.contains(id.getNamespace());
    }

    public boolean isNamesOnly() {
        return !this.blacklist && this.mods.isEmpty() && this.tags.isEmpty();
    }

    @Override
    public int hashCode() {
        return this.getCompiled().hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof BiomePredicate) {
            return this.getCompiled().equals(((BiomePredicate) o).getCompiled());
        }
        return false;
    }

    public static class BiomePredicateBuilder {
        @SuppressWarnings("ConstantConditions")
        public BiomePredicate build() {
            if (this.names == null) this.names = Collections.emptyList();
            if (this.mods == null) this.mods = Collections.emptyList();
            if (this.tags == null) this.tags = Collections.emptyList();
            return new BiomePredicate(this.blacklist, this.names, this.mods, this.tags);
        }
    }
}
