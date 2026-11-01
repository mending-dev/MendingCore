package dev.mending.core.api.display;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The boss bars of every player, by id. Showing a bar again changes it in place, so a countdown does not blink.
 * A player's bars go when they quit, a plugin's when it is disabled. Get it from {@code MendingCore.bossBars()}.
 *
 * Ids carry the plugin's namespace, so plugins cannot take each other's bars. Main thread only.
 */
public final class BossBars {

    private final Map<UUID, Map<NamespacedKey, BossBar>> bars = new HashMap<>();

    @ApiStatus.Internal
    public BossBars() {
    }

    /**
     * Shows a bar, or changes it if this player already has one with this id.
     *
     * @param progress from 0 to 1, values outside are moved into that range
     */
    public void show(@NotNull Player player, @NotNull NamespacedKey id, @NotNull Component name, float progress,
                     @NotNull BossBar.Color color, @NotNull BossBar.Overlay overlay) {
        ensureMainThread();
        Objects.requireNonNull(id, "id");
        final Map<NamespacedKey, BossBar> own = this.bars.computeIfAbsent(player.getUniqueId(), key -> new LinkedHashMap<>());
        final BossBar shown = own.get(id);
        if (shown == null) {
            final BossBar bar = BossBar.bossBar(name, clamp(progress), color, overlay);
            own.put(id, bar);
            player.showBossBar(bar);
            return;
        }
        shown.name(name);
        shown.progress(clamp(progress));
        shown.color(color);
        shown.overlay(overlay);
    }

    /**
     * Shows a bar to several players, with its name rendered for each of them, for example in their language.
     */
    public void show(@NotNull Iterable<? extends Player> players, @NotNull NamespacedKey id,
                     @NotNull Function<? super Player, ? extends Component> name, float progress,
                     @NotNull BossBar.Color color, @NotNull BossBar.Overlay overlay) {
        for (Player player : players) {
            show(player, id, name.apply(player), progress, color, overlay);
        }
    }

    /**
     * Changes the progress of a bar; does nothing if this player has none with this id.
     */
    public void progress(@NotNull Player player, @NotNull NamespacedKey id, float progress) {
        ensureMainThread();
        final BossBar bar = bar(player.getUniqueId(), id);
        if (bar != null) {
            bar.progress(clamp(progress));
        }
    }

    /**
     * Changes the name of a bar; does nothing if this player has none with this id.
     */
    public void name(@NotNull Player player, @NotNull NamespacedKey id, @NotNull Component name) {
        ensureMainThread();
        final BossBar bar = bar(player.getUniqueId(), id);
        if (bar != null) {
            bar.name(name);
        }
    }

    public boolean isShown(@NotNull Player player, @NotNull NamespacedKey id) {
        ensureMainThread();
        return bar(player.getUniqueId(), id) != null;
    }

    public void hide(@NotNull Player player, @NotNull NamespacedKey id) {
        ensureMainThread();
        final Map<NamespacedKey, BossBar> own = this.bars.get(player.getUniqueId());
        if (own == null) {
            return;
        }
        final BossBar bar = own.remove(id);
        if (bar != null) {
            player.hideBossBar(bar);
        }
        if (own.isEmpty()) {
            this.bars.remove(player.getUniqueId());
        }
    }

    /**
     * Takes this bar from everybody who has it.
     */
    public void hide(@NotNull NamespacedKey id) {
        ensureMainThread();
        hideWhere(id::equals);
    }

    /**
     * Takes every bar from a player, of every plugin.
     */
    public void hideAll(@NotNull Player player) {
        ensureMainThread();
        final Map<NamespacedKey, BossBar> own = this.bars.remove(player.getUniqueId());
        if (own != null) {
            own.values().forEach(player::hideBossBar);
        }
    }

    /**
     * Takes the bars of a plugin from everybody.
     */
    public void hideAll(@NotNull Plugin plugin) {
        ensureMainThread();
        final String namespace = plugin.getName().toLowerCase(Locale.ROOT);
        hideWhere(id -> id.getNamespace().equals(namespace));
    }

    @ApiStatus.Internal
    public void handleQuit(@NotNull Player player) {
        this.bars.remove(player.getUniqueId());
    }

    @ApiStatus.Internal
    public void close() {
        hideWhere(id -> true);
    }

    private void hideWhere(Predicate<NamespacedKey> which) {
        final Iterator<Map.Entry<UUID, Map<NamespacedKey, BossBar>>> players = this.bars.entrySet().iterator();
        while (players.hasNext()) {
            final Map.Entry<UUID, Map<NamespacedKey, BossBar>> entry = players.next();
            final Player player = Bukkit.getPlayer(entry.getKey());
            final List<NamespacedKey> ids = new ArrayList<>(entry.getValue().keySet());
            for (NamespacedKey id : ids) {
                if (which.test(id)) {
                    final BossBar bar = entry.getValue().remove(id);
                    if (player != null) {
                        player.hideBossBar(bar);
                    }
                }
            }
            if (entry.getValue().isEmpty()) {
                players.remove();
            }
        }
    }

    private BossBar bar(UUID player, NamespacedKey id) {
        final Map<NamespacedKey, BossBar> own = this.bars.get(player);
        return own == null ? null : own.get(id);
    }

    private static float clamp(float progress) {
        return Math.clamp(progress, 0.0f, 1.0f);
    }

    private static void ensureMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Boss bars must only be used on the main thread");
        }
    }
}
