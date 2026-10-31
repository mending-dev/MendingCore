---
description: Where the costs are in the GUI system and how to avoid performance problems.
---

# Performance

The GUI system is built so the common path is cheap:

* Icons and input slots are stored in **arrays** indexed by slot, so a click is an O(1) lookup with no boxing.
* One listener serves all GUIs, and its holder check uses `getHolder(false)`, which avoids block-state snapshots.
* Nothing is rendered until the first open, and creating a GUI allocates only an inventory and two arrays.

What is left are the costs **you** can influence.

## What costs what

| Operation | Cost | Notes |
| --- | --- | --- |
| Click on an icon | O(1) | Plus your handler. |
| `set(slot, icon)` | One `Inventory.setItem` | The item is copied into the server's internal representation. |
| `refresh()` | Clears icons, runs `render()`, runs paginations | Proportional to the number of slots and to what `render()` does. |
| `fill*` | One `set` per slot | Up to 54. |
| `add(icon)` | Scans from slot 0 | O(n) per call. |
| `ItemBuilder.build()` | Clones the item, copies the meta | Cheap, but not free. |
| `ItemBuilder.name(String)` | Parses MiniMessage | The most expensive part of building an icon. |
| `Skulls.ofTexture` | Creates a profile | Cache it. |
| `title(...)`, `rows(...)` | New inventory, reopen for viewers | Visible to the player. |
| `viewers()` | Allocates a list | Avoid in loops. |
| `Slots.*` | Allocates an array | Cache in a `static final`. |

## Tips

### 1. Cache constant icons

Fillers, back buttons and close buttons never change. Create them once.

```java
public final class Icons {
    public static final GuiIcon FILLER =
        GuiIcon.of(ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()));

    private Icons() {}
}
```

The icon's item is never modified, and handlers get the `GuiClick`, so one instance can be used by every GUI and every player. `Inventory.setItem` copies the item, so sharing is safe.

### 2. Cache parsed text

`name(String)` and `lore(String...)` parse MiniMessage every time. For text that does not change, build the `Component` once and pass it:

```java
private static final Component TITLE = MiniMessage.miniMessage().deserialize("<gradient:red:gold>Shop");
```

### 3. Refresh less, update less

`refresh()` rebuilds everything. If only one icon changes (a counter, a timer), replace just that icon:

```java
set(13, counterIcon(count));   // instead of refresh()
```

Do not refresh every tick. For countdowns, update once per second at most, and only while someone is viewing: `if (gui.viewers().isEmpty()) return;` (or keep your own viewer flag set in `onOpen`/`onClose`).

Changes made within one tick are sent to the player together by the server, so several `set` calls in one tick are not several packets, but each call still costs server time.

### 4. Keep the mapper of a Pagination cheap

The mapper runs for each visible item on every refresh. A page of 28 items means 28 calls per page flip. Do not query databases, read files or compute heavy values there. Precompute data, or cache icons per item:

```java
private final Map<Warp, GuiIcon> iconCache = new HashMap<>();

private final Pagination<Warp> page = pagination(CONTENT, warp ->
    iconCache.computeIfAbsent(warp, this::createIcon));
```

### 5. Hand big lists over once

`setItems` copies the list (`List.copyOf`). Calling it with a large list on every refresh costs O(n) each time. Keep the list in a field and call `setItems` only when the data changes. For a filtered view, keep the filtered result cached until the filter changes.

### 6. Avoid `add` in loops

`add` scans from slot 0 for each call. For many icons use explicit slots or a `Pagination`.

```java
// O(n²)
for (GuiIcon icon : icons) gui.add(icon);

// O(n)
int slot = 0;
for (GuiIcon icon : icons) gui.set(slot++, icon);
```

### 7. Share static menus

A menu without player-specific content (server selector, rules) can be one shared `Gui` instance for all players: one inventory, one render. Players with different needs get their own instance, see [Best practices](best-practices.md).

### 8. Never block the main thread

Database queries, HTTP and file access belong into async tasks. Load the data first, then build and open the GUI on the main thread, see [Pagination](pagination.md).

### 9. Do not create GUIs off the main thread

`Gui` creates a Bukkit inventory and must be used on the main thread. Open, refresh, title and rows enforce it and throw `IllegalStateException`.

### 10. Heads: use textures, not lookups

`Skulls.ofTexture(...)` and `ofUrl(...)` never touch the network. `Skulls.of(player)` is cheap but the client resolves the skin lazily, so the first display can show the default skin for a moment.

### 11. Clean up

Remove GUIs you keep in maps in `onClose`, and cancel repeating tasks. Nothing in the library holds references to your GUIs, so they are collected as soon as you let go.

## Pitfalls

| Pitfall | Effect | Fix |
| --- | --- | --- |
| Building icons in every `render()` | Garbage and CPU per refresh | Cache constants, build dynamic icons only when needed |
| Refreshing every tick | Wasted server time | Update single slots, throttle |
| Database call in the mapper | Lag spikes on page flip | Async preload |
| `title()` for animation | Inventory reopens each time | Animate with icons instead |
| Per-player GUIs for static menus | Memory and render cost | Share one instance |
| Heavy `onChange` handlers on input slots | Runs on every click | Keep light, defer heavy work |
| Holding closed GUIs in a map | Memory leak | Remove in `onClose` |

## Measuring

If a menu feels slow, profile with Spark (`/spark profiler`) and look for your `render()`, mapper and handlers. The framework's own methods are rarely the problem.
