package dev.mending.core.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Safe string parsing without exceptions in the normal flow and without locale pitfalls.
 * All methods accept null and return an empty result for anything that is not valid.
 */
public final class Parse {

    private Parse() {
        throw new UnsupportedOperationException("This is a static utility class");
    }

    /**
     * Accepts true/false, yes/no (case-insensitive) and 1/0.
     */
    public static @NotNull Optional<Boolean> bool(@Nullable String str) {
        if (str == null) {
            return Optional.empty();
        }
        if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes") || str.equals("1")) {
            return Optional.of(Boolean.TRUE);
        }
        if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("no") || str.equals("0")) {
            return Optional.of(Boolean.FALSE);
        }
        return Optional.empty();
    }

    public static boolean isBoolean(@Nullable String str) {
        return bool(str).isPresent();
    }

    /**
     * Parses an int with an optional sign. Only ASCII digits are accepted; overflow yields empty.
     */
    public static @NotNull OptionalInt integer(@Nullable String str) {
        if (!looksLikeInteger(str, 10)) {
            return OptionalInt.empty();
        }
        final long value = Long.parseLong(str); // at most 10 digits always fit into a long
        return value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE
            ? OptionalInt.of((int) value)
            : OptionalInt.empty();
    }

    public static boolean isInteger(@Nullable String str) {
        return integer(str).isPresent();
    }

    /**
     * Parses a long with an optional sign. Only ASCII digits are accepted; overflow yields empty.
     */
    public static @NotNull OptionalLong longValue(@Nullable String str) {
        if (!looksLikeInteger(str, 19)) {
            return OptionalLong.empty();
        }
        try {
            return OptionalLong.of(Long.parseLong(str));
        } catch (NumberFormatException e) {
            return OptionalLong.empty(); // 19 digits can still overflow
        }
    }

    /**
     * Parses a decimal number such as {@code 12}, {@code -0.5} or {@code 1e3}. NaN, infinity and
     * Java-specific forms like {@code 1d} or hex floats are rejected.
     */
    public static @NotNull OptionalDouble doubleValue(@Nullable String str) {
        if (str == null || str.isEmpty() || str.length() > 64) {
            return OptionalDouble.empty();
        }
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            final boolean allowed = (c >= '0' && c <= '9') || c == '.' || c == '-' || c == '+' || c == 'e' || c == 'E';
            if (!allowed) {
                return OptionalDouble.empty();
            }
        }
        try {
            final double value = Double.parseDouble(str);
            return Double.isFinite(value) ? OptionalDouble.of(value) : OptionalDouble.empty();
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }

    /**
     * Case-insensitive enum lookup without exceptions.
     */
    public static <E extends Enum<E>> @NotNull Optional<E> enumValue(@NotNull Class<E> type, @Nullable String name) {
        if (name == null) {
            return Optional.empty();
        }
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(name)) {
                return Optional.of(constant);
            }
        }
        return Optional.empty();
    }

    private static boolean looksLikeInteger(@Nullable String str, int maxDigits) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        final char first = str.charAt(0);
        final int start = first == '-' || first == '+' ? 1 : 0;
        final int digits = str.length() - start;
        if (digits < 1 || digits > maxDigits) {
            return false;
        }
        for (int i = start; i < str.length(); i++) {
            final char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}