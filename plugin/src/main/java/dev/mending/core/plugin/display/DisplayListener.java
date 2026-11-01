package dev.mending.core.plugin.display;

import dev.mending.core.api.display.BossBars;
import dev.mending.core.api.scoreboard.PlayerBoards;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;

/**
 * Forgets the boards and bars of players who quit, and takes the bars of plugins that go away.
 */
public final class DisplayListener implements Listener {

    private final PlayerBoards boards;
    private final BossBars bossBars;

    public DisplayListener(PlayerBoards boards, BossBars bossBars) {
        this.boards = boards;
        this.bossBars = bossBars;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        this.boards.handleQuit(event.getPlayer());
        this.bossBars.handleQuit(event.getPlayer());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        this.bossBars.hideAll(event.getPlugin());
    }
}
