---
description: Symptoms, causes and fixes for common GUI problems.
---

# Troubleshooting

## Icons disappear after `refresh()`

`refresh()` clears **all** icons and then runs `render()`. Icons set anywhere else are gone. Put every icon in `render()`.

## Items put into the GUI are lost

Players cannot put items into normal icon slots. If you need that, use an [input slot](input-slots.md). If items vanish from an input slot on close, check that `refundOnClose` is still `true` and that your `onClose` did not call `clear()` or `take()` on them.

## `IllegalStateException: Slot N is an input slot`

You called `set(N, icon)` on a slot that is an input slot. Use another slot, or build the icon first and create the input slot at a different position. Note that `input(N)` removes an icon that was there.

## `IndexOutOfBoundsException` in `set`

The slot is outside the GUI. A 3-row GUI has slots 0 to 26. Use `Slots.slot(row, column)` to avoid mistakes.

## `IllegalArgumentException: row N does not exist`

`fillRows` was called with a row beyond `rows()`.

## A handler runs more often than expected

* A general `onClick` fires for every click type (left, right, shift, middle, number keys, drop, ...). Use `onLeftClick` or a specific type.
* Players click fast. Make handlers idempotent and check conditions at click time.
* Double clicks do **not** trigger handlers (they are ignored on purpose).

## The title does not change

`title(Component)` reopens the inventory for all viewers. If you only change a field that `render()` uses for the title, nothing happens: the title is not part of `render()`.

## The GUI flickers when I change the title or size

That is the reopen. Do not use `title` or `rows` for animation. Change icons instead.

## `IllegalStateException: GUIs must only be used on the main thread`

`open`, `refresh`, `title` or `rows` was called from an async task. Move the call into `Bukkit.getScheduler().runTask(plugin, () -> ...)`.

## Pagination shows nothing

* `setItems(...)` was never called or got an empty collection.
* The slot array is wrong (for example `Slots.inner(2)` is empty because inner needs at least 3 rows).
* The slots are input slots (rendering throws).
* `render()` runs after a manual `set` on the same slots? Paginations render **after** `render()`, so they win.

## A second player sees changes the first player made

One `Gui` instance is shared by all viewers. Create one instance per player if state should be private, see [Best practices](best-practices.md).

## Clicks do nothing at all

* MendingCore is not enabled, so no listener is registered. Check the server log and your `paper-plugin.yml` dependency.
* Another plugin cancels inventory click events before MendingCore (it ignores cancelled events).
* You modified `getInventory()` directly and expect handlers for those items. Items must be placed with `set`.

## `onOpen` / `onClose` are not called

* `onOpen` is skipped when another plugin cancels the open event, and while the GUI reopens for a title or size change.
* `onClose` is skipped during that reopen as well.

## Items given from a GUI vanish or are not delivered

Use `InventoryUtils.giveOrDrop(player, item)`. `PlayerInventory.addItem` returns what did not fit, and ignoring that return value loses items.

## I cannot drop the item on my cursor by clicking outside

Clicking outside the window is allowed and not cancelled. If it does not work, another plugin interferes.

## Unit tests fail with `NullPointerException` in `Gui`

`Gui` creates a Bukkit inventory, which needs a running server (or a mock). Keep logic in separate classes and test those.

## Compiler: `Reference to runTask is ambiguous`

Use a lambda instead of a method reference with `BukkitScheduler`:

```java
Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
```
