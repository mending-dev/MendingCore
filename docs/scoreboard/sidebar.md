---
description: PlayerBoards and Sidebar, a scoreboard per player and sidebars that do not flicker.
---

# Scoreboards and sidebars

```java
MendingCore.get().boards().sidebar(player)
    .title("<gold>My Server")
    .lines("<gray>Online: <white>" + online, "", "<yellow>mending.dev");
```

## PlayerBoards

`MendingCore.get().boards()` gives every player their own scoreboard on request, so each can see other lines and team names. All plugins share these boards, because a player can only look at one.

| Signature | Description |
| --- | --- |
| `Scoreboard board(Player)` | The player's own board, created if needed. Makes sure they look at it, without sending it again if they already do. |
| `Sidebar sidebar(Player)` | The sidebar on that board. |
| `Optional<Scoreboard> existing(UUID)` | The board if there is one. Creates nothing. |
| `boolean owns(Scoreboard)` | Whether it is the board of a player. |
| `void release(Player)` | Back to the server's main scoreboard, the board is forgotten. |

A board is forgotten when its player quits, and everybody goes back to the main scoreboard when MendingCore is disabled.

The board is a normal `Scoreboard`: add teams and objectives of your own. Names starting with `mc_` belong to the sidebar.

{% hint style="warning" %}
Call `player.setScoreboard(...)` only when the board really changes. Sending a board the player already sees makes the sidebar flicker.

A player looks at one board only. If another plugin manages scoreboards of its own, using `board(...)` or `sidebar(...)` takes the player off its board, and the two plugins keep replacing each other. Use that plugin's way of showing lines on such a server.
{% endhint %}

## Sidebar

| Signature | Description |
| --- | --- |
| `Sidebar(Scoreboard)` | A sidebar on any scoreboard. One per scoreboard. |
| `update(Component title, List<? extends Component> lines)` | Shows both, writing only what changed. |
| `title(Component)`, `title(String)` | Changes the title. |
| `lines(List)`, `lines(Component...)`, `lines(String...)` | Changes the lines. |
| `Component title()`, `List<Component> lines()` | What is shown. |
| `void clear()` | Takes the sidebar off the board. The next update brings it back. |

`Sidebar.MAX_LINES` is 15, the most the client shows. Further lines are cut off. `String` variants parse MiniMessage, so never put player-controlled text into them, use `Component.text(...)`.

## Performance

Only changed lines are sent, so calling `update` with the same content costs a comparison and nothing else, not even an allocation for the changes. When the number of lines changes, every line's order value is written again, because it depends on the count.

## Per viewer

Every player has a board of their own, so render the lines for each player, for example in their language:

```java
for (Player player : Bukkit.getOnlinePlayers()) {
    MendingCore.get().boards().sidebar(player).update(title(player), lines(player));
}
```

All methods run on the main thread only and throw `IllegalStateException` otherwise.
