---
description: InputSlot in depth, letting players put items into a GUI and take them out safely.
---

# Input slots

Everything in a GUI is locked. An `InputSlot` is the one controlled exception: a slot where players can put items in and take items out, with a filter, a stack limit, callbacks and automatic refund.

```java
InputSlot slot = gui.input(13);
```

`Gui.input(int slot)` returns the existing input slot or creates one. If an icon was at that slot it is removed.

## Why not just allow moving icons?

Earlier versions had a `setFixed(false)` flag. It let players pull menu items into their inventory, which duplicates items. The input slot replaces it: the library tracks the content itself and handles every click, so the displayed inventory can never get out of sync.

## Configuration

All configuration methods return the `InputSlot`.

| Method | Description |
| --- | --- |
| `placeholder(ItemStack)` | Item shown while the slot is empty. `null` shows nothing. The placeholder is never content and never refunded. |
| `maxStackSize(int)` | Maximum amount in this slot. `0` (default) uses the item's own max stack size. Negative throws `IllegalArgumentException`. |
| `refundOnClose(boolean)` | Whether the content goes back to the player when the GUI closes. Default `true`. |
| `filter(Predicate<ItemStack>)` | Only matching items can be put in. |
| `canPut(BiPredicate<GuiClick, ItemStack>)` | Like `filter`, but with the click, e.g. for permission checks. |
| `canTake(BiPredicate<GuiClick, ItemStack>)` | Decides whether the current content may be taken out (also applies to swaps and shift-clicks). |
| `onPut(BiConsumer<GuiClick, ItemStack>)` | After items were put in. The item is a copy of the moved amount. |
| `onTake(BiConsumer<GuiClick, ItemStack>)` | After items were taken out. The item is a copy of the moved amount. |
| `onChange(Consumer<GuiClick>)` | After every put or take, after the specific callbacks. |

```java
gui.input(11)
    .placeholder(ItemBuilder.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE).name("<gray>Insert a sword").build())
    .maxStackSize(1)
    .filter(item -> item.getType().name().endsWith("_SWORD"))
    .canPut((click, item) -> click.player().hasPermission("shop.upgrade"))
    .onChange(GuiClick::refresh);
```

## State

| Method | Description |
| --- | --- |
| `int slot()` | The slot index. |
| `boolean isEmpty()` | Whether the slot holds nothing. |
| `ItemStack content()` | A **copy** of the content, or `null` if empty. |
| `void set(ItemStack)` | Replaces the content. `null` or an empty stack clears. |
| `void clear()` | Clears the slot. The content is gone, not refunded. |
| `ItemStack take()` | Removes and returns the content, or `null`. |

Changing the content by code does not call `onPut`, `onTake` or `onChange`.

## Supported clicks

| Click | With empty cursor | With item on cursor |
| --- | --- | --- |
| Left click | Picks up the whole content | Puts the whole stack (up to the limit), merges, or swaps |
| Right click | Picks up half (rounded up) | Puts one item, merges one, or swaps |
| Shift-click on the slot | Moves the content to the player's inventory (what does not fit stays) | n/a |
| Shift-click in the player's inventory | Moves the item into the first input slot that accepts it | n/a |
| Number key (1-9) | Swaps the slot with that hotbar slot | n/a |
| Middle click (creative mode only) | Copies a full stack of the content to the cursor; the content stays | n/a |
| Dragging | n/a | Spreads the cursor over the slots, see below |

Everything else on an input slot is cancelled and does nothing: drop, offhand swap, double click, and the middle click outside creative mode.

### Details

* **Merging** only happens with similar items (same type and meta) and only up to the limit. The rest stays on the cursor.
* **Swapping** happens when the item on the cursor differs from the content. It only works if the cursor stack fits the limit, `canPut` accepts it and `canTake` allows taking the old content. Callbacks run in the order `onTake` (old item), `onPut` (new item), `onChange`.
* **Shift-click into the GUI** tries every input slot in slot order and moves as much as fits. If several input slots accept the item, the first one is filled first.
* **Limits** apply to every way of putting items in.
* **Number keys** check `canTake` for the content and `canPut` for the hotbar item. If the hotbar stack is above the limit and the slot is empty, the limit moves and the rest stays in the hotbar. If the slot holds something, the swap only happens when the whole hotbar stack fits.
* **Middle click** checks `canTake`, moves nothing and runs no callback: it only copies, as in vanilla.
* **Dragging** works only if every touched slot is an input slot, and is **all or nothing**: if one slot's `canPut` or limit refuses, the whole drag is cancelled. A drag touching an icon or the player's inventory as well is cancelled too. A slot showing a placeholder cannot be dragged onto; click it instead. Each slot counts as one placement: `canPut` receives the slot's whole new stack, `onPut` the added amount, and `click.event()` is a synthetic cancelled event, not one from the server. Do not call `click.close()` or `click.open(...)` differently because of it, they work as usual.

## Refund on close

When the last viewer closes the GUI, every input slot with `refundOnClose(true)` gives its content back:

1. Your `onClose(player)` hook runs first.
2. Then remaining content is added to the player's inventory. What does not fit is dropped at the player's feet.

This also covers disconnects and a server shutdown (MendingCore closes all GUIs when it is disabled).

If you consume the content yourself, remove it so it is not refunded:

```java
@Override
protected void onClose(Player player) {
    ItemStack item = input(13).take();   // removes it from the slot
    if (item != null) process(player, item);
}
```

## Reading and reacting

Use `onChange` to refresh dependent icons, for example a result preview:

```java
input(INPUT).onChange(GuiClick::refresh);   // render() computes the output from content()
```

`render()` can call `input(INPUT).content()` safely. `refresh()` never touches input slot contents. A full example is in [Recipes](recipes.md).

## Output slots

An input slot lets players take things out **and** put things in. For a read-only result (crafting output, shop product) use a normal `GuiIcon` with an `onLeftClick` handler that checks the inputs, consumes them and gives the result with `InventoryUtils.giveOrDrop`. This is simpler and cannot be exploited by swapping items.

## Interaction with other features

| Situation | Behaviour |
| --- | --- |
| `gui.set(inputSlot, icon)` | Throws `IllegalStateException`. |
| `fill*` methods | Skip input slots. |
| `refresh()` | Leaves content and placeholder alone. |
| `gui.rows(smaller)` | Input slots beyond the new size are dropped, their content is refunded to a viewer. |
| `gui.title(...)` | Content is kept, no refund (the GUI only reopens). |
| Several viewers | One shared content. Refund happens when the last viewer closes. |

## Pitfalls

* **Don't** expect `canPut` to see single items: it receives the stack on the cursor (or the one being shift-clicked, or the hotbar stack), which can have an amount greater than the limit. The library limits the amount for you.
* **Don't** keep references to `content()` and expect them to change, it is a copy.
* **Do** use `maxStackSize(1)` for slots that hold a single object such as a tool.
* **Do** keep `refundOnClose(true)` unless you really handle the items yourself, otherwise players can lose items.
