---
description: ItemBuilder, a fluent builder for ItemStacks.
---

# ItemBuilder

`dev.mending.core.api.item.ItemBuilder` builds `ItemStack`s fluently.

```java
ItemStack sword = ItemBuilder.of(Material.DIAMOND_SWORD)
    .name("<gradient:red:gold>Flame Blade")
    .lore("<gray>Forged in the nether", "", "<yellow>Right-click to ignite")
    .enchant(Enchantment.SHARPNESS, 5)
    .glow(true)
    .flags(ItemFlag.HIDE_ENCHANTS)
    .build();
```

## Key behaviours

* **Names and lore are not italic** unless you set italic yourself. Minecraft makes custom names italic by default, the builder removes that.
* **The source item is never modified.** `ItemBuilder.of(ItemStack)` works on a copy.
* **The builder is reusable.** Every `build()` returns a new stack.
* Air and empty items are rejected with `IllegalArgumentException`, because air has no meta.

## Creating

| Signature | Description |
| --- | --- |
| `static ItemBuilder of(Material material)` | New item of that material. |
| `static ItemBuilder of(ItemStack item)` | Starts from a copy of an existing item. |

## Basics

| Signature | Description |
| --- | --- |
| `amount(int amount)` | Stack size. Must be at least 1. |
| `name(Component name)` | Display name, not italic by default. |
| `name(String name)` | Display name in MiniMessage format. |

## Lore

| Signature | Description |
| --- | --- |
| `lore(Component... lines)` | Replaces the lore. |
| `lore(List<? extends Component> lines)` | Replaces the lore. |
| `lore(String... lines)` | Replaces the lore, each line in MiniMessage format. |
| `addLore(Component... lines)` | Appends lines. |
| `addLore(String... lines)` | Appends MiniMessage lines. |
| `clearLore()` | Removes the lore. |

An empty string line (`""`) is an empty line in the lore.

## Appearance and behaviour

| Signature | Description |
| --- | --- |
| `enchant(Enchantment, int level)` | Adds an enchantment, unsafe levels are allowed. |
| `removeEnchant(Enchantment)` | Removes an enchantment. |
| `glow(boolean)` | `true` forces the enchantment glint, `false` restores the default. |
| `unbreakable(boolean)` | Sets the item unbreakable. |
| `flags(ItemFlag...)` | Adds item flags (hide parts of the tooltip). |
| `removeFlags(ItemFlag...)` | Removes item flags. |
| `hideFlags()` | Adds all item flags. |
| `itemModel(NamespacedKey)` | Sets the item model, for example from a resource pack. `null` resets it. |
| `pdc(NamespacedKey key, PersistentDataType<P, C> type, C value)` | Stores persistent data, type-safe. |

```java
NamespacedKey key = new NamespacedKey(plugin, "id");
ItemStack item = ItemBuilder.of(Material.PAPER)
    .pdc(key, PersistentDataType.STRING, "voucher-42")
    .build();
```

## Escape hatches

For everything the builder does not cover:

| Signature | Description |
| --- | --- |
| `meta(Consumer<? super ItemMeta> action)` | Direct access to the `ItemMeta`. |
| `meta(Class<M> type, Consumer<? super M> action)` | Access to a specific meta type. Throws `IllegalArgumentException` if the item has another meta. |

```java
ItemBuilder.of(Material.LEATHER_CHESTPLATE)
    .meta(LeatherArmorMeta.class, meta -> meta.setColor(Color.RED))
    .build();
```

## Building

| Signature | Description |
| --- | --- |
| `ItemStack build()` | Returns a new `ItemStack` with all changes applied. |

## Use with GUIs

```java
GuiIcon.of(ItemBuilder.of(Material.EMERALD).name("<green>Buy"))
```

`GuiIcon.of(ItemBuilder)` calls `build()` once.

## MiniMessage and untrusted input

`name(String)` and the `String` lore methods parse MiniMessage. If part of the text comes from players (names, chat), never concatenate it into the string, they could inject tags. Build a `Component` instead:

```java
ItemBuilder.of(Material.PAPER)
    .name(Component.text(playerName, NamedTextColor.YELLOW))   // safe
```

## Performance

* MiniMessage parsing is the most expensive step. For text that never changes, build the `Component` once and reuse it.
* Build constant items and icons once, see [GUI performance](../gui/performance.md).
* `build()` copies the item and its meta, so do not call it in tight loops for identical items, cache the result.

## Notes

* `custom model data` has no dedicated method because Minecraft replaced it with the item model and data components. Use `itemModel(...)` or the `meta(...)` escape hatch.
* The builder is not thread-safe.
