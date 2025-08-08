package dev.mending.core.paper.api.language;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Lang {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static Component format(String string) {
        return MINI_MESSAGE.deserialize(string);
    }

    public static String toString(Component component) {
        if (component == null) {
            return "";
        }
        return MINI_MESSAGE.serialize(component);
    }

    public static Component get(@Nonnull String key) {
        return MINI_MESSAGE.deserialize("<lang:" + key + ">");
    }

    public static String formatLocalDateTime(@Nonnull LocalDateTime dateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern); // For example: HH:mm, dd.MM.yyyy
        return dateTime.format(formatter);
    }

    public static String capitialize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

}
