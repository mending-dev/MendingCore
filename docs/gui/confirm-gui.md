---
description: ConfirmGui, a yes/no menu where text and items stay yours.
---

# ConfirmGui

```java
ConfirmGui.create(Component.text("Delete this home?"),
        ItemBuilder.of(Material.LIME_CONCRETE).name("<green>Delete"),
        ItemBuilder.of(Material.RED_CONCRETE).name("<red>Keep"))
    .filler(Icons.FILLER)
    .onConfirm(click -> homes.delete(click.player(), "base"))
    .onCancel(click -> click.open(parent))
    .open(player);
```

Title and items are always yours. The library has no built-in text and no built-in look.

## Creating

| Signature | Description |
| --- | --- |
| `static ConfirmGui create(Component title, ItemBuilder confirm, ItemBuilder cancel)` | A dialog with 3 rows. |
| `ConfirmGui(Component title, int rows, ItemStack confirm, ItemStack cancel)` | Any size, also for subclasses. |

## Options

| Signature | Description |
| --- | --- |
| `onConfirm(Consumer<GuiClick>)` | What the confirm button does. |
| `onCancel(Consumer<GuiClick>)` | What the cancel button does. |
| `filler(GuiIcon)` | Fills every other slot. Default: nothing. |
| `confirmSlot(int)` | Moves the button. Default: middle row, column 4. |
| `cancelSlot(int)` | Moves the button. Default: middle row, column 6. |
| `autoClose(boolean)` | Whether a button closes the menu before its action runs. Default: `true`. |

Everything of `Gui` works too: `onRender(...)`, `set(...)` and `title(...)`. The player's inventory is locked, undo that with `lockPlayerInventory(false)`.

## More icons

Add a description or other icons with `onRender`, or subclass it and call `super.render()`. Your icons are placed after the buttons and can replace them.

```java
ConfirmGui gui = ConfirmGui.create(title, yes, no);
gui.onRender(g -> g.set(1, 5, GuiIcon.of(summary(player))));
```

## Behaviour

* Both buttons react to a **left click**, so a stray number or drop key does nothing.
* With `autoClose`, one decision per opening counts. A second fast click does not run the action again.
* The action runs right after the close is scheduled. `click.open(other)` inside it opens the next menu as usual.
* With `autoClose(false)` the menu stays open and your action decides. Fast clicks can then run it several times, so [check at click time](icons-and-clicks.md#guarding-actions).
* Check conditions in the action, not when the dialog opens: what was true then may not be any more.

A subclass overriding `onOpen` or `onClose` must call `super`.
