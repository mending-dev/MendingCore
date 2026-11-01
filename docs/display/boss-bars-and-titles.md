---
description: BossBars and Titles, boss bars by id that do not blink and titles per player.
---

# Boss bars and titles

## BossBars

`MendingCore.get().bossBars()` keeps the boss bars of every player by id. Showing a bar again changes it in place, so a countdown does not blink.

```java
private static final NamespacedKey COUNTDOWN = new NamespacedKey(plugin, "countdown");

BossBars bars = MendingCore.get().bossBars();
bars.show(player, COUNTDOWN, Component.text("Starts in " + seconds), seconds / 30f,
    BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
```

The id carries the plugin's namespace, so plugins cannot take each other's bars.

| Signature | Description |
| --- | --- |
| `show(Player, NamespacedKey, Component, float, Color, Overlay)` | Shows a bar, or changes the one with this id. Progress is moved into 0 to 1. |
| `show(Iterable<Player>, NamespacedKey, Function<Player, Component>, float, Color, Overlay)` | The same for several players, with the name rendered for each. |
| `progress(Player, NamespacedKey, float)` | Changes the progress. Nothing without such a bar. |
| `name(Player, NamespacedKey, Component)` | Changes the name. Nothing without such a bar. |
| `isShown(Player, NamespacedKey)` | Whether the player has it. |
| `hide(Player, NamespacedKey)` | Takes it from the player. |
| `hide(NamespacedKey)` | Takes it from everybody. |
| `hideAll(Player)` | Takes every bar from the player, of every plugin. |
| `hideAll(Plugin)` | Takes a plugin's bars from everybody. |

You do not have to clean up: a player's bars go when they quit, a plugin's when it is disabled. After a rejoin a player has no bars, show them again. Main thread only, `IllegalStateException` otherwise.

## Titles

`dev.mending.core.api.display.Titles` is a static helper around Paper's titles.

| Signature | Description |
| --- | --- |
| `Title.Times times(int fadeIn, int stay, int fadeOut)` | Times in ticks. |
| `DEFAULT_TIMES` | 10, 70 and 20 ticks, as in vanilla. |
| `show(Player, Component, Component)` | Title and subtitle with the default times. |
| `show(Player, Component, Component, Title.Times)` | With your times. |
| `show(Player, String, String)` | Both in MiniMessage format. |
| `show(Iterable<Player>, Function<Player, Component>, Function<Player, Component>, Title.Times)` | For several players, rendered for each. |
| `clear(Player)` | Takes the title away. |

```java
Titles.show(players, this::winnerTitle, this::winnerSubtitle, Titles.times(0, 60, 10));
```

Never put player-controlled text into the `String` variant, use `Component.text(...)`.
