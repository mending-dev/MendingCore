---
description: What happens when a GUI is created, opened, refreshed, resized and closed.
---

# Lifecycle

## Overview

```
new Gui(...)            creates the inventory and slot arrays, renders nothing
        │
open(player)            first time: render() + paginations, then opens the inventory
        │
InventoryOpenEvent      onOpen(player)
        │
clicks                  icon / input slot handlers
refresh()               clear icons ► render() ► paginations
        │
InventoryCloseEvent     onClose(player) ► refund input slots (last viewer)
```

## Creation

The constructor validates the rows (1 to 6) and creates the Bukkit inventory with the `Gui` as holder. Nothing is drawn yet, so creating a GUI is cheap.

## First render

The first render happens lazily, on the first call of `open(Player)` or `getInventory()`:

1. `render()` runs (your override or the lambda from `onRender`).
2. All [paginations](pagination.md) render their current page.

The first render does **not** clear anything. Icons you set with `set(...)` before opening are kept.

## open(Player)

* Must be called on the main thread.
* Renders if this is the first time, then calls `player.openInventory(...)`.
* If another plugin cancels the open event, `onOpen` is not called.

## onOpen(Player)

Called after the inventory was opened, for every viewer. It is **not** called while the GUI is reopening because of a title or size change.

## refresh()

```
clear all icons ► render() ► paginations render
```

* Input slots are **not** cleared. Their content stays.
* Everything you placed with `set` outside `render()` is removed. Put all icons in `render()`.
* Must be called on the main thread.

## Title and size changes

`title(Component)` and `rows(int)` create a new inventory and reopen it for all viewers, because the client cannot rename or resize an open window.

* `onClose` and `onOpen` are suppressed during the reopen, and input slot contents are not refunded.
* The player sees a short flicker and the cursor position resets.
* Shrinking with `rows(int)` drops icons and input slots beyond the new size. Input contents are refunded to a viewer.

{% hint style="warning" %}
Do not use `title(...)` for animations. Each call reopens the inventory. Use `refresh()` or `set(...)` for changes inside the window.
{% endhint %}

## onClose(Player)

Called when a player closes the GUI, including on disconnect and when MendingCore is disabled.

Order inside the close event:

1. `onClose(player)` runs.
2. If this player is the last viewer, every input slot is refunded (unless it has `refundOnClose(false)` or you cleared it in `onClose`).

So if you process the content of an input slot yourself in `onClose`, call `slot.clear()` or `slot.take()` afterwards, otherwise it is also refunded.

## Multiple viewers

One `Gui` instance can be shown to several players. They share content and state. Input slot contents are refunded when the **last** viewer closes. For per-player content create one instance per player, see [Best practices](best-practices.md).

## Thread rules

| Method | Main thread enforced |
| --- | --- |
| `open`, `refresh`, `title`, `rows` | Yes, throws `IllegalStateException` |
| `set`, `fill*`, `input`, `add`, `clear` | Not checked, but must still only be called from the main thread |
