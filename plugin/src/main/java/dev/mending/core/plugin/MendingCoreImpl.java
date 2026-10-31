package dev.mending.core.plugin;

import dev.mending.core.api.MendingCore;
import dev.mending.core.api.gui.Gui;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link MendingCore}. Internal: never referenced from the API module.
 */
final class MendingCoreImpl implements MendingCore {

    private final MendingCorePlugin plugin;

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
    }
}