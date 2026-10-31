---
description: InventoryUtils, helpers for counting, checking, removing and giving items.
---

# InventoryUtils

`dev.mending.core.api.inventory.InventoryUtils` is a static utility class.

## Storage slots

All counting, space and removal methods work on the **storage slots** only (`Inventory.getStorageContents()`). For a player that is the 36 inventory slots **without** armor and offhand. This avoids counting a worn helmet as stock or "removing" it by accident.

## Empty checks

| Signature | Description |
| --- | --- |
| `boolean isEmpty(ItemStack item)` | `true` for `null`, air and amount 0. |

## Converting

| Signature | Description |
| --- | --- |
| `Map<Integer, ItemStack> toMap(ItemStack[] contents)` | Slot to item, empty slots left out. `null` gives an empty map. |
| `ItemStack[] toArray(Map<Integer, ItemStack> map, int size)` | Array of the given size. A slot outside `0..size-1` throws `IllegalArgumentException`. |
| `List<ItemStack> items(Inventory inventory)` | All non-empty items, **including** armor and offhand for player inventories. |

```java
Map<Integer, ItemStack> saved = InventoryUtils.toMap(player.getInventory().getContents());
// ... later
player.getInventory().setContents(InventoryUtils.toArray(saved, 41));
```

{% hint style="info" %}
`toArray` takes the array size explicitly. Sizing it from the number of entries (as an earlier version did) loses every item whose slot is larger than the entry count.
{% endhint %}

## Filling

| Signature | Description |
| --- | --- |
| `void fillEmptySlots(Inventory inventory, ItemStack filler)` | Puts the filler into every empty slot. |

## Counting and checking

| Signature | Description |
| --- | --- |
| `int count(Inventory inventory, Material material)` | Total amount of a material, ignoring meta. |
| `int count(Inventory inventory, ItemStack similarTo)` | Total amount of items similar to the given one (same type and meta). |
| `boolean contains(Inventory inventory, ItemStack similarTo)` | Whether at least one similar item exists. |
| `boolean has(Inventory inventory, ItemStack similarTo, int amount)` | Whether at least `amount` similar items exist in total. |

Use `count(inventory, material)` for plain materials (currency like emerald) and the `ItemStack` variants for custom items with names, lore or data. "Similar" means `ItemStack.isSimilar`, the amount is ignored.

## Space

| Signature | Description |
| --- | --- |
| `boolean hasSpaceFor(Inventory inventory, ItemStack item)` | Whether the full amount fits, counting free slots and partial stacks. |

{% hint style="warning" %}
`hasSpaceFor` checks **one** stack. Checking several different rewards one after another can say "yes" for each while all together do not fit. For several items use `giveOrDrop`, which handles the overflow.
{% endhint %}

## Removing

| Signature | Description |
| --- | --- |
| `boolean remove(Inventory inventory, ItemStack similarTo, int amount)` | Removes exactly `amount` similar items. **All or nothing**: returns `false` and changes nothing if there are not enough. |

```java
if (InventoryUtils.remove(player.getInventory(), ticket, 1)) {
    reward(player);
}
```

An `amount` of 0 or less returns `true` and does nothing.

## Giving

| Signature | Description |
| --- | --- |
| `boolean giveOrDrop(Player player, ItemStack... items)` | Adds the items to the player's inventory and drops what does not fit at their feet. Returns `true` if everything fit. |

The passed items are copied, so you can reuse them.

```java
if (!InventoryUtils.giveOrDrop(player, reward)) {
    player.sendMessage(Component.text("Your inventory was full, items were dropped."));
}
```

## Performance

* All methods are linear in the inventory size (at most 54 for chests, 36 for player storage). They are cheap, but avoid calling `count` in a loop for many different items, count once per item type and reuse the result.
* `remove` counts first, then removes, so it iterates twice.
