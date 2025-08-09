package dev.mending.core.paper.api.language;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Lang {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static Component deserialize(String string) {
        return MINI_MESSAGE.deserialize(string)
                .applyFallbackStyle(Style.style(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(false)));
    }

    public static String serialize(Component component) {
        String serialized = MINI_MESSAGE.serialize(component);
        if (serialized.startsWith("<!italic>")) {
            serialized = serialized.substring("<!italic>".length());
        }
        return serialized;
    }

    public static Component get(@Nonnull String key) {
        return MINI_MESSAGE.deserialize("<lang:" + key + ">");
    }

    public static TextReplacementConfig replace(@Nonnull String string, @Nonnull String replacement) {
        return TextReplacementConfig.builder().matchLiteral(string).replacement(replacement).build();
    }

    public static String formatLocalDateTime(@Nonnull LocalDateTime dateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    public static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

}
