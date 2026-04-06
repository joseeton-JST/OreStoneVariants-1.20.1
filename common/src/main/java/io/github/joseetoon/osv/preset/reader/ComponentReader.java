package io.github.joseetoon.osv.preset.reader;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentReader {

    public static final Codec<Component> CODEC =
        Codec.STRING.xmap(ComponentReader::translateAny, Component::getString);

    public static final Map<String, Object> DEFAULT_DENSE =
        ImmutableMap.<String, Object>builder()
            .put("text", "{osv.denseKey} {fg} ({bg})")
            .build();

    public static final Map<String, Object> DEFAULT_NORMAL =
        ImmutableMap.<String, Object>builder()
            .put("text", "{fg} ({bg})")
            .build();

    private static final Pattern KEY_PATTERN = Pattern.compile("(?<!\\\\)\\{([^}]+)}");

    public static Component fromRaw(final Map<?, ?> raw) {
        // Store the raw text as a literal so that {fg}/{bg} placeholders survive
        // until VariantItem.createDisplay() can replace them with real translation keys.
        // translateAny() is applied there after substitution.
        final Object text = raw.get("text");
        return net.minecraft.network.chat.Component.literal(text == null ? "" : text.toString());
    }

    public static MutableComponent translateAny(final String text) {
        final MutableComponent component = net.minecraft.network.chat.Component.literal("");
        if (text.isEmpty()) return component;

        final Matcher matcher = KEY_PATTERN.matcher(text);
        int end = 0;

        while (matcher.find()) {
            final String key = matcher.group(1);
            component.append(net.minecraft.network.chat.Component.literal(text.substring(end, matcher.start())));
            component.append(net.minecraft.network.chat.Component.translatable(key));
            end = matcher.end();
        }

        if (end == 0) return net.minecraft.network.chat.Component.literal(text);

        return component.append(net.minecraft.network.chat.Component.literal(text.substring(end)));
    }

    public static String unescape(final String text) {
        return text.replace("\\{", "{");
    }
}
