package io.github.joseetoon.osv.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;
import io.github.joseetoon.osv.ModRegistries;
import io.github.joseetoon.osv.exception.InvalidBlockEntryException;
import io.github.joseetoon.osv.init.PresetLoadingContext;
import io.github.joseetoon.osv.preset.OrePreset;
import io.github.joseetoon.osv.util.Group;
import io.github.joseetoon.osv.util.RlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockEntry {

    @EqualsAndHashCode.Exclude String raw;
    String foreground;
    String background;

    public BlockEntry(final String foreground, final String background) {
        this.raw = foreground + " " + background;
        this.foreground = foreground;
        this.background = background;
    }

    public static BlockEntry create(final String raw) throws InvalidBlockEntryException {
        final String[] split = ArrayUtils.removeAllOccurrences(raw.split(",\\s*|\\s+"), "");
        if (split.length != 2) throw new InvalidBlockEntryException(raw);
        return new BlockEntry(raw, split[0], split[1]);
    }

    public List<VariantDescriptor> resolve() {
        final Group oreGroup = this.getOreGroup();
        final Group blockGroup = this.getBlockGroup();

        final List<VariantDescriptor> descriptors = new ArrayList<>();
        for (final String path : oreGroup.getEntries()) {
            final Optional<OrePreset> ore = PresetLoadingContext.loadOre(path);
            if (ore.isEmpty() || DefaultOres.canIgnoreEntry(ore.get().getMod())) {
                continue;
            }
            for (final ResourceLocation id : blockGroup.ids()) {
                if (Cfg.modEnabled(RlUtils.ns(id))) {
                    descriptors.add(new VariantDescriptor(this, path, ore.get(), id));
                }
            }
        }
        return descriptors;
    }

    public List<BlockEntry> deconstruct() {
        final Group oreGroup = this.getOreGroup();
        final Group blockGroup = this.getBlockGroup();

        final List<BlockEntry> entries = new ArrayList<>();
        for (final String path : oreGroup.getEntries()) {
            for (final ResourceLocation id : blockGroup.ids()) {
                entries.add(new BlockEntry(path, id.toString()));
            }
        }
        return entries;
    }

    public Group getOreGroup() {
        return ModRegistries.PROPERTY_GROUPS.getOptional(this.foreground)
            .orElseGet(() -> Group.synthetic(this.foreground));
    }

    public Group getBlockGroup() {
        return ModRegistries.BLOCK_GROUPS.getOptional(this.background)
            .orElseGet(() -> Group.synthetic(this.background));
    }

    @Override
    public String toString() {
        return "BlockEntry[ " + this.foreground + " -> " + this.background + " ]";
    }
}
