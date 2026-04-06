package io.github.joseetoon.osv.exception;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import io.github.joseetoon.genlib.exception.FormattedException;
import io.github.joseetoon.osv.config.BlockEntry;
import io.github.joseetoon.osv.config.VariantDescriptor;

import java.util.Map;
import java.util.Set;

public class DuplicateBlockEntryException extends FormattedException {

    private final Map<VariantDescriptor, Set<BlockEntry>> duplicates;

    public DuplicateBlockEntryException(final Map<VariantDescriptor, Set<BlockEntry>> duplicates) {
        super("Discovered " + duplicates.size() + " duplicate block entries");
        this.duplicates = duplicates;
    }

    @Override
    public @NotNull String getCategory() {
        return "osv.errorMenu.blockList";
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return net.minecraft.network.chat.Component.translatable("osv.errorText.duplicateEntries");
    }

    @Override
    public @NotNull Component getDetailMessage() {
        final MutableComponent newLine = net.minecraft.network.chat.Component.literal("\n");
        final MutableComponent component = net.minecraft.network.chat.Component.literal("");

        component.append(net.minecraft.network.chat.Component.translatable("osv.errorText.variantsDuplicated")
            .setStyle(Style.EMPTY.applyFormats(ChatFormatting.BOLD, ChatFormatting.UNDERLINE)));
        component.append(newLine);
        component.append(newLine);

        for (final Map.Entry<VariantDescriptor, Set<BlockEntry>> kv : this.duplicates.entrySet()) {
            component.append(net.minecraft.network.chat.Component.literal(" * ").withStyle(Style.EMPTY.withBold(true)));
            component.append(net.minecraft.network.chat.Component.literal(kv.getKey().getId().toString())
                .withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)));
            component.append(newLine);

            for (final BlockEntry entry : kv.getValue()) {
                component.append(net.minecraft.network.chat.Component.literal("   - ").withStyle(Style.EMPTY.withBold(true)));
                component.append(net.minecraft.network.chat.Component.literal(entry.getRaw()).withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                component.append(newLine);
            }
            component.append(newLine);
        }
        return component;
    }
}
