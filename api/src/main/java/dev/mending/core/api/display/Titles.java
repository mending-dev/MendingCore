package dev.mending.core.api.display;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.Function;

/**
 * Shows titles, with short ways to give the times and to render the text per player.
 */
public final class Titles {

    private static final Duration TICK = Duration.ofMillis(50);

    /** Half a second in, three and a half seconds on screen, one second out: the times of vanilla's title command. */
    public static final Title.Times DEFAULT_TIMES = times(10, 70, 20);

    private Titles() {
    }

    /**
     * @return the times of a title, in ticks
     */
    public static @NotNull Title.Times times(int fadeIn, int stay, int fadeOut) {
        return Title.Times.times(TICK.multipliedBy(fadeIn), TICK.multipliedBy(stay), TICK.multipliedBy(fadeOut));
    }

    public static void show(@NotNull Player player, @NotNull Component title, @NotNull Component subtitle) {
        show(player, title, subtitle, DEFAULT_TIMES);
    }

    public static void show(@NotNull Player player, @NotNull Component title, @NotNull Component subtitle, @NotNull Title.Times times) {
        player.showTitle(Title.title(title, subtitle, times));
    }

    /**
     * Same with both texts in MiniMessage format. Never put player-controlled text into them.
     */
    public static void show(@NotNull Player player, @NotNull String title, @NotNull String subtitle) {
        show(player, MiniMessage.miniMessage().deserialize(title), MiniMessage.miniMessage().deserialize(subtitle));
    }

    /**
     * Shows a title to several players, rendered for each of them, for example in their language.
     */
    public static void show(@NotNull Iterable<? extends Player> players,
                            @NotNull Function<? super Player, ? extends Component> title,
                            @NotNull Function<? super Player, ? extends Component> subtitle,
                            @NotNull Title.Times times) {
        for (Player player : players) {
            show(player, title.apply(player), subtitle.apply(player), times);
        }
    }

    public static void clear(@NotNull Player player) {
        player.clearTitle();
    }
}
