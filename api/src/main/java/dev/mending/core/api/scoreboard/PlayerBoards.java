package dev.mending.core.api.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * One scoreboard for every player who needs one, so each can see other sidebars and team names. All plugins
 * share these boards, a player can only look at one anyway. A board goes when its player quits.
 * Get it from {@code MendingCore.boards()}. Main thread only.
 */
public final class PlayerBoards {

    private record Entry(Scoreboard board, Sidebar sidebar) {
    }

    private final Map<UUID, Entry> entries = new HashMap<>();

    @ApiStatus.Internal
    public PlayerBoards() {
    }

    /**
     * Returns the player's own board and makes sure they look at it. Sending a board they already see
     * would make the sidebar flicker, so that only happens when needed.
     */
    public @NotNull Scoreboard board(@NotNull Player player) {
        final Scoreboard board = entry(player).board();
        if (player.getScoreboard() != board) {
            player.setScoreboard(board);
        }
        return board;
    }

    /**
     * Returns the sidebar on the player's own board and makes sure they look at that board.
     */
    public @NotNull Sidebar sidebar(@NotNull Player player) {
        board(player);
        return entry(player).sidebar();
    }

    /**
     * @return the player's board if they have one; does not create one
     */
    public @NotNull Optional<Scoreboard> existing(@NotNull UUID player) {
        ensureMainThread();
        final Entry entry = this.entries.get(player);
        return entry == null ? Optional.empty() : Optional.of(entry.board());
    }

    /**
     * @return whether this is the board of a player
     */
    public boolean owns(@NotNull Scoreboard board) {
        ensureMainThread();
        for (Entry entry : this.entries.values()) {
            if (entry.board() == board) {
                return true;
            }
        }
        return false;
    }

    /**
     * Puts the player back on the server's main scoreboard and forgets their board.
     */
    public void release(@NotNull Player player) {
        ensureMainThread();
        final Entry entry = this.entries.remove(player.getUniqueId());
        if (entry != null && player.getScoreboard() == entry.board()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    @ApiStatus.Internal
    public void handleQuit(@NotNull Player player) {
        this.entries.remove(player.getUniqueId());
    }

    @ApiStatus.Internal
    public void close() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            release(player);
        }
        this.entries.clear();
    }

    private Entry entry(Player player) {
        ensureMainThread();
        return this.entries.computeIfAbsent(player.getUniqueId(), id -> {
            final Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            return new Entry(board, new Sidebar(board));
        });
    }

    private static void ensureMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Scoreboards must only be used on the main thread");
        }
    }
}
