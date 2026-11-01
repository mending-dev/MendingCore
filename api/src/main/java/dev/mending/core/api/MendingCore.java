package dev.mending.core.api;

import dev.mending.core.api.display.BossBars;
import dev.mending.core.api.scoreboard.PlayerBoards;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Entry point of the MendingCore API.
 *
 * Other plugins obtain the instance via {@link #get()} (or Bukkit's {@code ServicesManager})
 * and must declare a dependency on {@code MendingCore} in their {@code paper-plugin.yml}.
 */
public interface MendingCore {

    /**
     * Returns the active MendingCore instance.
     *
     * @return the active instance
     * @throws IllegalStateException if MendingCore is not enabled yet
     */
    static @NotNull MendingCore get() {
        return MendingCoreProvider.get();
    }

    /**
     * @return the version of the running MendingCore plugin
     */
    @NotNull String version();

    /**
     * @return the MendingCore plugin instance, e.g. as owner for tasks and listeners
     */
    @NotNull Plugin plugin();

    /**
     * @return the scoreboards of the players, with their sidebars
     */
    @NotNull PlayerBoards boards();

    /**
     * @return the boss bars of the players
     */
    @NotNull BossBars bossBars();
}