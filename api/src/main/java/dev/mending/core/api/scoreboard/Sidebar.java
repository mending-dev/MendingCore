package dev.mending.core.api.scoreboard;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The sidebar of a scoreboard. It only writes what changed, so updating it every second does not flicker,
 * and lines can have any formatting and be identical. Lines beyond {@value #MAX_LINES} are cut off.
 *
 * One sidebar per scoreboard. For a player's own board use {@code MendingCore.boards().sidebar(player)}.
 * Main thread only.
 */
public final class Sidebar {

    /** The most lines the client shows. */
    public static final int MAX_LINES = 15;

    private static final String OBJECTIVE = "mc_sidebar";
    private static final String TEAM_PREFIX = "mc_line_";
    private static final String MARKERS = "0123456789abcdef";

    private final Scoreboard board;
    private Component title = Component.empty();
    private List<Component> lines = List.of();

    public Sidebar(@NotNull Scoreboard board) {
        this.board = Objects.requireNonNull(board, "board");
    }

    public @NotNull Component title() {
        return this.title;
    }

    public @NotNull List<Component> lines() {
        return this.lines;
    }

    public @NotNull Sidebar title(@NotNull Component title) {
        return update(title, this.lines);
    }

    /**
     * Same with the title in MiniMessage format.
     */
    public @NotNull Sidebar title(@NotNull String title) {
        return title(MiniMessage.miniMessage().deserialize(title));
    }

    public @NotNull Sidebar lines(@NotNull List<? extends Component> lines) {
        return update(this.title, lines);
    }

    public @NotNull Sidebar lines(@NotNull Component... lines) {
        return lines(Arrays.asList(lines));
    }

    /**
     * Same with each line in MiniMessage format. Never put player-controlled text into them.
     */
    public @NotNull Sidebar lines(@NotNull String... lines) {
        return lines(Arrays.stream(lines).map(MiniMessage.miniMessage()::deserialize).toList());
    }

    /**
     * Shows this title and these lines, writing only what changed.
     */
    public @NotNull Sidebar update(@NotNull Component title, @NotNull List<? extends Component> lines) {
        ensureMainThread();
        write(title, lines);
        return this;
    }

    /**
     * Takes the sidebar off the scoreboard. It comes back with the next update.
     */
    public void clear() {
        ensureMainThread();
        remove();
    }

    void write(Component title, List<? extends Component> lines) {
        Objects.requireNonNull(title, "title");
        final List<Component> shown = List.copyOf(lines.size() > MAX_LINES ? lines.subList(0, MAX_LINES) : lines);

        Objective objective = this.board.getObjective(OBJECTIVE);
        final boolean created = objective == null;
        if (created) {
            objective = this.board.registerNewObjective(OBJECTIVE, Criteria.DUMMY, title);
            // The order values are not for reading.
            objective.numberFormat(NumberFormat.blank());
        }
        if (objective.getDisplaySlot() != DisplaySlot.SIDEBAR) {
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        if (!created && this.title.equals(title) && this.lines.equals(shown)) {
            return;
        }
        if (!created && !this.title.equals(title)) {
            objective.displayName(title);
        }

        final SidebarDiff diff = SidebarDiff.between(created ? List.of() : this.lines, shown);
        for (int index : diff.rewritten()) {
            team(index).prefix(shown.get(index));
        }
        for (int index : diff.rescored()) {
            objective.getScore(marker(index)).setScore(shown.size() - index);
        }
        for (int index : diff.removed()) {
            removeLine(index);
        }
        this.title = title;
        this.lines = shown;
    }

    void remove() {
        for (int index = 0; index < MAX_LINES; index++) {
            removeLine(index);
        }
        final Objective objective = this.board.getObjective(OBJECTIVE);
        if (objective != null) {
            objective.unregister();
        }
        this.title = Component.empty();
        this.lines = List.of();
    }

    private Team team(int index) {
        Team team = this.board.getTeam(TEAM_PREFIX + index);
        if (team == null) {
            team = this.board.registerNewTeam(TEAM_PREFIX + index);
            team.addEntry(marker(index));
        }
        return team;
    }

    private void removeLine(int index) {
        this.board.resetScores(marker(index));
        final Team team = this.board.getTeam(TEAM_PREFIX + index);
        if (team != null) {
            team.unregister();
        }
    }

    /** An invisible score holder, the text of a line is its team's prefix. */
    private static String marker(int index) {
        return "§" + MARKERS.charAt(index);
    }

    private static void ensureMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Sidebars must only be used on the main thread");
        }
    }
}
