package dev.mending.core.api.util;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Tells Bedrock players from Java players by their UUID, without needing Floodgate at compile or run time.
 * Floodgate builds these UUIDs from a zero upper half and the Xbox id, which no Java account has.
 * A Bedrock player who linked a Java account plays under that account and counts as a Java player.
 */
public final class Bedrock {

    private Bedrock() {
        throw new UnsupportedOperationException("This is a static utility class");
    }

    /**
     * @return true if this is a UUID Floodgate gives a Bedrock player
     */
    public static boolean isBedrock(@NotNull UUID uuid) {
        return Objects.requireNonNull(uuid, "uuid").getMostSignificantBits() == 0L;
    }

    /**
     * @return true if this player joined through Floodgate, also while offline
     */
    public static boolean isBedrock(@NotNull OfflinePlayer player) {
        return isBedrock(Objects.requireNonNull(player, "player").getUniqueId());
    }
}
