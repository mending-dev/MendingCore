---
description: Complete method reference of the Gui class.
---

# Gui reference

`dev.mending.core.api.gui.Gui implements InventoryHolder`

Constants: `Gui.COLUMNS = 9`, `Gui.MAX_ROWS = 6`.

All methods that return `Gui` are fluent and return `this`.

## Creating

| Signature | Description |
| --- | --- |
| `Gui(Component title, int rows)` | Creates a GUI. `rows` must be 1 to 6, otherwise `IllegalArgumentException`. |
| `Gui(String title, int rows)` | Same, the title is parsed as MiniMessage. |
| `static Gui create(Component title, int rows)` | Factory, same as the constructor. |
| `static Gui create(Component title, int rows, Consumer<Gui> renderer)` | Factory with a render function. |

## Hooks (override or configure)

| Signature | Description |
| --- | --- |
| `protected void render()` | Place icons here. Runs on the first open and on every `refresh()`. The default calls the lambda from `onRender`. |
| `Gui onRender(Consumer<Gui> renderer)` | Sets the render function, alternative to overriding `render()`. |
| `protected void onOpen(Player player)` | After the GUI was opened for a player. |
| `protected void onClose(Player player)` | When a player closed the GUI, before input slots are refunded. |

## Opening and closing

| Signature | Description |
| --- | --- |
| `void open(Player player)` | Renders if needed and opens the GUI. Main thread only. |
| `void refresh()` | Clears all icons, runs `render()` and the paginations. Input slots keep their content. Main thread only. |
| `void close(Player player)` | Closes the GUI for that player if they are viewing it. |
| `void closeAll()` | Closes the GUI for all viewers. |
| `List<Player> viewers()` | Current viewers as a new list. |

## Title and size

| Signature | Description |
| --- | --- |
| `Component title()` | Current title. |
| `Gui title(Component title)` | Changes the title. Reopens the inventory for all viewers. |
| `int rows()` | Number of rows. |
| `int size()` | Number of slots (`rows * 9`). |
| `Gui rows(int rows)` | Changes the size. Reopens for all viewers, drops icons and input slots beyond the new size. |

See [Lifecycle](lifecycle.md) for the reopen behaviour.

## Icons

| Signature | Description |
| --- | --- |
| `Gui set(int slot, GuiIcon icon)` | Sets an icon. `null` removes it. Throws `IndexOutOfBoundsException` for a bad slot and `IllegalStateException` if the slot is an input slot. |
| `Gui set(int row, int column, GuiIcon icon)` | Same with 1-based row and column. |
| `Gui remove(int slot)` | Same as `set(slot, null)`. |
| `GuiIcon icon(int slot)` | The icon at that slot, or `null`. |
| `boolean add(GuiIcon icon)` | Puts the icon into the first slot without icon and input slot. Returns `false` if the GUI is full. |
| `Gui clear()` | Removes all icons. Input slots are untouched. |

## Filling

All `fill*` methods **overwrite** existing icons and **skip** input slots, except `fillEmpty`, which only fills free slots.

| Signature | Description |
| --- | --- |
| `Gui fill(GuiIcon icon)` | Every slot. |
| `Gui fillEmpty(GuiIcon icon)` | Only slots without icon and input slot. |
| `Gui fillSlots(GuiIcon icon, int... slots)` | The listed slots. |
| `Gui fillRange(GuiIcon icon, int from, int to)` | From slot to slot, both inclusive. |
| `Gui fillRows(GuiIcon icon, int... rows)` | 1-based rows. A row beyond `rows()` throws `IllegalArgumentException`. |
| `Gui fillColumns(GuiIcon icon, int... columns)` | 1-based columns. |
| `Gui fillBorder(GuiIcon icon)` | The outer frame. |

## Input slots

| Signature | Description |
| --- | --- |
| `InputSlot input(int slot)` | Returns the input slot, creating it if needed. An icon at that slot is removed. |
| `InputSlot getInput(int slot)` | The input slot or `null`. |

See [Input slots](input-slots.md).

## Pagination

| Signature | Description |
| --- | --- |
| `<T> Pagination<T> pagination(int[] slots, Function<? super T, GuiIcon> mapper)` | Creates a pagination over the given slots. It renders automatically after `render()`. |

See [Pagination](pagination.md).

## Settings

| Signature | Description |
| --- | --- |
| `Gui lockPlayerInventory(boolean lock)` | If `true`, clicks in the player's own inventory are cancelled while the GUI is open. Default `false`. Shift-clicks into input slots keep working. |
| `boolean isPlayerInventoryLocked()` | Current setting. |

## InventoryHolder

| Signature | Description |
| --- | --- |
| `Inventory getInventory()` | The Bukkit inventory. Triggers the first render if it has not happened yet. |

{% hint style="warning" %}
Do not modify the returned inventory directly. Changes made behind the GUI's back are not tracked and are overwritten by `refresh()`. Use `set(...)` and input slots.
{% endhint %}

## Internal methods

`handleClick`, `handleDrag`, `handleOpen` and `handleClose` are public only for the MendingCore listener and are annotated `@ApiStatus.Internal`. Never call them.
