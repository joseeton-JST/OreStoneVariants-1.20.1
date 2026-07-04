package io.github.joseetoon.osv.tag.neoforge;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import io.github.joseetoon.osv.tag.TagHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.github.joseetoon.osv.util.RlUtils;

/**
 * A virtual data pack that emits tag JSON files for every OSV variant block/item.
 *
 * <p>Tag content is derived from the cache built by {@link TagHelper#injectTags()},
 * which runs after {@code TagsUpdatedEvent}. On the first world load the cache is
 * empty (no tags are emitted). After one {@code /reload} the cache is populated and
 * subsequent loads carry all the correct tag entries.
 *
 * <p>Each variant inherits whichever tags its foreground (ore) and/or background
 * (stone) block belongs to, according to the copyFgTags / copyBgTags config options.
 */
public class OsvTagPackResources implements PackResources {

    // Pack format 15 == 1.20.1 server data
    private static final int PACK_FORMAT = 15;
    private static final String BLOCK_TAG_DIR = "tags/blocks";
    private static final String ITEM_TAG_DIR  = "tags/items";

    private final String packId;
    private final PackLocationInfo location;

    public OsvTagPackResources(final String packId) {
        this.packId = packId;
        this.location = new PackLocationInfo(
            packId,
            Component.literal("OSV Dynamic Tags"),
            PackSource.BUILT_IN,
            Optional.empty()
        );
    }

    // -------------------------------------------------------------------------
    // PackResources interface
    // -------------------------------------------------------------------------

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(final String... paths) {
        return null; // no root resources (pack.mcmeta is handled via getMetadataSection)
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(final PackType type, final ResourceLocation location) {
        if (type != PackType.SERVER_DATA) return null;

        final String path = RlUtils.path(location);

        if (path.startsWith(BLOCK_TAG_DIR + "/")) {
            final TagKey<Block> tag = blockTagKey(location);
            final List<ResourceLocation> entries = TagHelper.getCachedBlockTags().get(tag);
            if (entries != null && !entries.isEmpty()) {
                return () -> toStream(buildTagJson(entries));
            }
        } else if (path.startsWith(ITEM_TAG_DIR + "/")) {
            final TagKey<Item> tag = itemTagKey(location);
            final List<ResourceLocation> entries = TagHelper.getCachedItemTags().get(tag);
            if (entries != null && !entries.isEmpty()) {
                return () -> toStream(buildTagJson(entries));
            }
        }
        return null;
    }

    @Override
    public void listResources(final PackType type, final String namespace,
                              final String pathPrefix, final ResourceOutput output) {
        if (type != PackType.SERVER_DATA) return;

        for (final Map.Entry<TagKey<Block>, List<ResourceLocation>> e
                : TagHelper.getCachedBlockTags().entrySet()) {
            if (!RlUtils.ns(e.getKey().location()).equals(namespace)) continue;
            final ResourceLocation loc = blockTagResource(e.getKey());
            if (RlUtils.path(loc).startsWith(pathPrefix)) {
                final List<ResourceLocation> entries = e.getValue();
                output.accept(loc, () -> toStream(buildTagJson(entries)));
            }
        }

        for (final Map.Entry<TagKey<Item>, List<ResourceLocation>> e
                : TagHelper.getCachedItemTags().entrySet()) {
            if (!RlUtils.ns(e.getKey().location()).equals(namespace)) continue;
            final ResourceLocation loc = itemTagResource(e.getKey());
            if (RlUtils.path(loc).startsWith(pathPrefix)) {
                final List<ResourceLocation> entries = e.getValue();
                output.accept(loc, () -> toStream(buildTagJson(entries)));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(final PackType type) {
        if (type != PackType.SERVER_DATA) return Collections.emptySet();
        final Set<String> ns = new HashSet<>();
        TagHelper.getCachedBlockTags().keySet().forEach(t -> ns.add(RlUtils.ns(t.location())));
        TagHelper.getCachedItemTags().keySet().forEach(t -> ns.add(RlUtils.ns(t.location())));
        return ns;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getMetadataSection(final MetadataSectionSerializer<T> deserializer) throws IOException {
        if (deserializer == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(Component.literal("OSV Dynamic Tags"), PACK_FORMAT);
        }
        return null;
    }

    @Override
    public String packId() {
        return this.packId;
    }

    @Override
    public PackLocationInfo location() {
        return this.location;
    }

    @Override
    public void close() {}

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Converts a packed block-tag ResourceLocation to a TagKey<Block>. */
    private static TagKey<Block> blockTagKey(final ResourceLocation loc) {
        // loc.path = "tags/blocks/<path>.json"  â†’  tagPath = "<path>"
        final String tagPath = stripDir(RlUtils.path(loc), BLOCK_TAG_DIR);
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(RlUtils.ns(loc), tagPath));
    }

    /** Converts a packed item-tag ResourceLocation to a TagKey<Item>. */
    private static TagKey<Item> itemTagKey(final ResourceLocation loc) {
        final String tagPath = stripDir(RlUtils.path(loc), ITEM_TAG_DIR);
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(RlUtils.ns(loc), tagPath));
    }

    /** Builds the pack-relative ResourceLocation for a block tag file. */
    private static ResourceLocation blockTagResource(final TagKey<Block> tag) {
        return ResourceLocation.fromNamespaceAndPath(
            RlUtils.ns(tag.location()),
            BLOCK_TAG_DIR + "/" + RlUtils.path(tag.location()) + ".json"
        );
    }

    /** Builds the pack-relative ResourceLocation for an item tag file. */
    private static ResourceLocation itemTagResource(final TagKey<Item> tag) {
        return ResourceLocation.fromNamespaceAndPath(
            RlUtils.ns(tag.location()),
            ITEM_TAG_DIR + "/" + RlUtils.path(tag.location()) + ".json"
        );
    }

    /** Strips "tags/blocks/" (or "tags/items/") prefix and ".json" suffix. */
    private static String stripDir(final String path, final String dir) {
        final String prefix = dir + "/";
        final String stripped = path.startsWith(prefix) ? path.substring(prefix.length()) : path;
        return stripped.endsWith(".json") ? stripped.substring(0, stripped.length() - 5) : stripped;
    }

    /** Generates a minimal tag JSON with replace=false and the given entry list. */
    private static String buildTagJson(final List<ResourceLocation> entries) {
        final JsonObject json = new JsonObject();
        json.addProperty("replace", false);
        final JsonArray values = new JsonArray();
        entries.forEach(e -> values.add(e.toString()));
        json.add("values", values);
        return json.toString();
    }

    private static InputStream toStream(final String json) {
        return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    }
}
