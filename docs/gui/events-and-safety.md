---
description: Exactly which inventory events are cancelled, which are allowed and why.
---

# Events and safety

## The listener

MendingCore registers one `GuiListener`. It forwards events to a GUI when the inventory's holder is a `Gui`:

| Event | Priority | Cancelled events | Goes to |
| --- | --- | --- | --- |
| `InventoryClickEvent` | `NORMAL` | ignored | `Gui.handleClick` |
| `InventoryDragEvent` | `NORMAL` | ignored | `Gui.handleDrag` |
| `InventoryOpenEvent` | `MONITOR` | ignored | `Gui.handleOpen` → `onOpen` |
| `InventoryCloseEvent` | `MONITOR` | n/a | `Gui.handleClose` → `onClose` |

If another plugin cancels a click before MendingCore, the GUI does not process it. The listener uses `getHolder(false)`, which avoids block-state snapshots and keeps the check cheap even though it runs for every inventory on the server.

## Click handling

Top = the GUI part of the window, bottom = the player's own inventory.

| Situation | Behaviour |
| --- | --- |
| Click on an icon or empty GUI slot | **Cancelled.** Icon handler runs (not for double clicks). |
| Click on an input slot | **Cancelled.** Handled by the [input slot](input-slots.md) logic. |
| Double click in the top part | Cancelled, no handler. |
| Click outside the window | **Not** cancelled. Vanilla behaviour, the cursor item is dropped. |
| Click in the bottom part | Allowed, unless `lockPlayerInventory(true)`. |
| Shift-click in the bottom part | Cancelled. The item is moved into input slots that accept it, otherwise nothing happens. |
| Double click in the bottom part (collect to cursor) | **Cancelled.** |
| Click by a non-player | Cancelled. |
| Click while the GUI's size changed (stale inventory) | Cancelled. |

## Drag handling

A drag that touches **any** top slot is cancelled. A drag only inside the player's inventory is allowed.

## Exploits this closes

| Exploit | How it is prevented |
| --- | --- |
| Taking icons with double click (collect to cursor) | Collecting is cancelled while a GUI is open. |
| Moving items into the GUI with shift-click | Shift-clicks are cancelled and routed to input slots only. |
| Dragging items over the GUI | Drags touching the top are cancelled. |
| Hotbar swap, offhand swap, drop key on GUI slots | Every click on a top slot is cancelled first. |
| Creative middle-click or creative clicks | Cancelled like any top click. |
| Pulling icons out via `fixed = false` | The flag no longer exists. |
| Losing items put into a slot | Input slots track content and refund on close. |
| Double refund / loss when resizing | Close and open hooks are suppressed while the GUI reopens. |

## What the library does not protect you from

* **Fast repeated clicks.** Handlers can run many times per second. Make them idempotent, see [Icons and clicks](icons-and-clicks.md).
* **Other plugins** modifying the same inventory or opening menus on top.
* **Your own handlers** giving items without checking inventory space. Use `InventoryUtils.giveOrDrop`.
* **State changes between render and click.** An icon rendered when a player had enough money can be clicked later when they do not. Check at click time.

## Closing and disconnects

`InventoryCloseEvent` also fires on disconnect and when MendingCore is disabled (it closes all GUIs). So `onClose` and input slot refunds always run.

## Cross-plugin behaviour

A `Gui` from any plugin is recognised by the single listener in MendingCore because all plugins share the API classes. You never need to register your own inventory listeners for a `Gui`.
