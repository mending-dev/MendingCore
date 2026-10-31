---
description: GuiIcon, GuiClick and how click types are handled.
---

# Icons and clicks

## GuiIcon

`dev.mending.core.api.gui.GuiIcon` is an item plus optional click handlers. An icon can **never** be taken out of the GUI.

### Creating

| Signature | Description |
| --- | --- |
| `static GuiIcon of(ItemStack item)` | From an item (copied). |
| `static GuiIcon of(ItemBuilder builder)` | From a builder (`build()` is called once). |
| `static GuiIcon of(Material material)` | A plain item. |

### Handlers

| Signature | Description |
| --- | --- |
| `GuiIcon onClick(Consumer<GuiClick> handler)` | Handler for every click type without a specific handler. |
| `GuiIcon onClick(ClickType type, Consumer<GuiClick> handler)` | Handler for one click type. |
| `GuiIcon onLeftClick(Consumer<GuiClick> handler)` | Shortcut for `ClickType.LEFT`. |
| `GuiIcon onRightClick(Consumer<GuiClick> handler)` | Shortcut for `ClickType.RIGHT`. |
| `ItemStack itemStack()` | A copy of the displayed item. |

A handler registered for a specific click type **wins** over the general `onClick`.

```java
GuiIcon.of(Material.CHEST)
    .onLeftClick(click -> open(click))
    .onRightClick(click -> preview(click))
    .onClick(ClickType.SHIFT_LEFT, click -> quickOpen(click));
```

{% hint style="warning" %}
The general `onClick(handler)` fires for **every** click type except double-click: left, right, shift, middle, number keys, drop, offhand swap. For anything with consequences (buying, deleting, teleporting) use `onLeftClick` or a specific type, so a stray drop key or number key does not trigger it.
{% endhint %}

### Sharing icons

The item of a `GuiIcon` is never changed after creation, and handlers receive the `GuiClick`, so one icon instance can be used in many slots and many GUIs. Keep rarely changing icons (fillers, back buttons) in `static final` constants. See [Performance](performance.md).

## GuiClick

`dev.mending.core.api.gui.GuiClick` is a record passed to every handler.

| Member | Description |
| --- | --- |
| `Gui gui()` | The GUI the click happened in. |
| `Player player()` | The clicking player. |
| `InventoryClickEvent event()` | The raw event. It is **already cancelled**. |
| `int slot()` | The clicked slot. |
| `ClickType type()` | The click type. |
| `boolean isLeftClick()` | Left, shift-left, window-border left or double click. |
| `boolean isRightClick()` | Right, shift-right or window-border right. |
| `boolean isShiftClick()` | Shift-left or shift-right. |
| `void refresh()` | Same as `gui().refresh()`. |
| `void close()` | Closes the GUI on the **next tick**. |
| `void open(Gui other)` | Opens another GUI on the **next tick**. |

### Why next tick?

Closing or opening an inventory from inside the click event that is currently being processed can corrupt the cursor item or cause glitches. `close()` and `open(Gui)` schedule the action one tick later, which is always safe. Use them instead of `player.closeInventory()` and `other.open(player)` inside handlers.

### The event is cancelled

Do not call `event().setCancelled(false)`. To let players move items use an [input slot](input-slots.md).

## Click types

What a player does in the top (GUI) part of the window:

| Click type | Triggered by | Icon handler |
| --- | --- | --- |
| `LEFT` | Left click | yes |
| `RIGHT` | Right click | yes |
| `SHIFT_LEFT`, `SHIFT_RIGHT` | Shift + click | yes |
| `MIDDLE` | Middle mouse button | yes |
| `NUMBER_KEY` | 1-9 over a slot | yes |
| `DROP`, `CONTROL_DROP` | Q, Ctrl+Q | yes |
| `SWAP_OFFHAND` | F | yes |
| `CREATIVE` | Creative-mode click | yes |
| `DOUBLE_CLICK` | Third event of a double click | **no** |

`DOUBLE_CLICK` is ignored on purpose. A double click produces two `LEFT` clicks followed by a `DOUBLE_CLICK`, so a handler would otherwise fire three times.

### Reading the button inside a handler

```java
GuiIcon.of(Material.DIAMOND).onClick(click -> {
    if (click.type() == ClickType.LEFT) {
        buy(click.player(), 1);
    } else if (click.type() == ClickType.SHIFT_LEFT) {
        buy(click.player(), 64);
    }
    // everything else is ignored
});
```

## Guarding actions

The library does not rate-limit clicks. Players can click very fast, so handlers with consequences must be **idempotent**: re-check the condition when the click happens and change state before doing the action.

```java
.onLeftClick(click -> {
    if (!economy.has(click.player(), price)) return;   // check at click time
    economy.withdraw(click.player(), price);
    InventoryUtils.giveOrDrop(click.player(), reward);
    click.refresh();
})
```
