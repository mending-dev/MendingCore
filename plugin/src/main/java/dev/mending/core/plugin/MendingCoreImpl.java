package dev.mending.core.plugin;

import dev.mending.core.api.MendingCore;
import dev.mending.core.api.display.BossBars;
import dev.mending.core.api.gui.Gui;
import dev.mending.core.api.scoreboard.PlayerBoards;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link MendingCore}. Internal: never referenced from the API module.
 */
final class MendingCoreImpl implements MendingCore {

    private final MendingCorePlugin plugin;
    private final PlayerBoards boards = new PlayerBoards();
    private final BossBars bossBars = new BossBars();

    MendingCoreImpl(@NotNull MendingCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String version() {
        return this.plugin.getPluginMeta().getVersion();
    }

    @Override
    public @NotNull Plugin plugin() {
        return this.plugin;
    }

    @Override
    public @NotNull PlayerBoards boards() {
        return this.boards;
    }

    @Override
    public @NotNull BossBars bossBars() {
        return this.bossBars;
    }

    /**
     * Releases all resources. Called once from {@link MendingCorePlugin#onDisable()}.
     */
    void close() {
        // Closing fires InventoryCloseEvent, so input slot contents are refunded before shutdown.
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof Gui) {
                player.closeInventory();
            }
        }
        this.bossBars.close();
        this.boards.close();
    }
}