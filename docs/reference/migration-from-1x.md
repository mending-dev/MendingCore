---
description: Moving plugins from MendingCore 1.x to 2.0, with class-by-class mapping.
---

# Migration from 1.x

MendingCore 2.0 is a rewrite. Packages, class names and some behaviour changed. This page lists everything you need to port a plugin.

## Checklist

1. Remove the 1.x jar from the server, install 2.0 ([Installation](../getting-started/installation.md)).
2. Change your dependency to `dev.mending:mending-core-api:2.0.0-SNAPSHOT` ([Using the API](../getting-started/using-the-api.md)).
3. Declare `MendingCore` as a dependency in `paper-plugin.yml` with `join-classpath: true`.
4. Replace imports (table below).
5. Port each class using the mappings on this page.
6. Delete any `GuiListener` registration, MendingCore registers its own.
7. Test menus with input slots and pagination, their behaviour changed.

## Packages

| 1.x | 2.0 |
| --- | --- |
| `dev.mending.core.paper.api.gui.*` | `dev.mending.core.api.gui.*` |
| `dev.mending.core.paper.api.gui.action.*` | `dev.mending.core.api.gui` (`InputSlot`) |
| `dev.mending.core.paper.api.gui.pagination.*` | `dev.mending.core.api.gui` (`Pagination`) |
| `dev.mending.core.paper.api.gui.villager.*` | `dev.mending.core.api.gui.villager` |
| `dev.mending.core.paper.api.item.*` | `dev.mending.core.api.item` |
| `dev.mending.core.paper.api.config.yaml.*` | `dev.mending.core.api.config` |
| `dev.mending.core.paper.api.inventory.*` | `dev.mending.core.api.inventory` |
| `dev.mending.core.paper.api.utils.*` | `dev.mending.core.api.util` |
| `dev.mending.core.paper.plugin.listener.gui.GuiListener` | internal, registered by MendingCore |
| `javax.annotation.Nonnull` and friends | `org.jetbrains.annotations.NotNull` / `Nullable` |

## Class mapping

| 1.x | 2.0 |
| --- | --- |
| `Gui` (abstract) | `Gui` (concrete, subclass or lambda) |
| `GuiIcon` | `GuiIcon` |
| `ActionSlot`, `ActionSlotManager` | `InputSlot` via `gui.input(slot)` |
| `PaginationManager` | `Pagination<T>` via `gui.pagination(slots, mapper)` |
| `VillagerGui` | `VillagerGui` + `Trade` |
| `ItemBuilder` | `ItemBuilder` |
| `SkullBuilder` | `Skulls` |
| `Configuration` | `YamlConfig` |
| `InventoryUtils` | `InventoryUtils` |
| `StringType` | `Parse` |
| `GuiListener` | internal |

## Gui

| 1.x | 2.0 |
| --- | --- |
| `abstract void update()` | `protected void render()` (not abstract) |
| `setTitle(Component)` | `title(Component)` |
| `setRows(int)` | `rows(int)` |
| `addItem(GuiIcon)` | `add(GuiIcon)` returns `boolean` (and now fills all slots, not just the first `rows` ones) |
| `setItem(int, GuiIcon)` | `set(int, GuiIcon)` |
| `fillSlotsBetween(icon, a, b)` | `fillRange(icon, a, b)` |
| `fill`, `fillSlots`, `fillRows`, `fillColumns` | same names, now skip input slots |
| `clear()` | `clear()` (now also clears the inventory) |
| `open(Player)` | `open(Player)` |
| `onOpen(InventoryOpenEvent)` | `onOpen(Player)` |
| `onClose(InventoryCloseEvent)` | `onClose(Player)` |
| `getActionSlotManager()` | `input(int slot)` |
| `hasActionSlots()` | `getInput(int)` returns `null` if none |
| `protected Player player` | removed, use `GuiClick.player()` or the hook parameters |
| `handleClick(event)` | internal |

New: `refresh()`, `close(Player)`, `closeAll()`, `viewers()`, `fillEmpty`, `fillBorder`, `set(row, column, icon)`, `lockPlayerInventory`, `create(...)` factories, MiniMessage title constructor.

```java
// 1.x
public final class MenuGui extends Gui {
    public MenuGui() { super(Component.text("Menu"), 3); }

    @Override
    public void update() {
        setItem(13, new GuiIcon(new ItemBuilder(Material.EMERALD).setName(Component.text("Buy")))
            .onClick(event -> buy((Player) event.getWhoClicked())));
    }
}

// 2.0
public final class MenuGui extends Gui {
    public MenuGui() { super(Component.text("Menu"), 3); }

    @Override
    protected void render() {
        set(13, GuiIcon.of(ItemBuilder.of(Material.EMERALD).name("<green>Buy"))
            .onLeftClick(click -> buy(click.player())));
    }
}
```

## GuiIcon

| 1.x | 2.0 |
| --- | --- |
| `new GuiIcon(ItemStack)` | `GuiIcon.of(ItemStack)` |
| `new GuiIcon(ItemBuilder)` | `GuiIcon.of(ItemBuilder)` |
| `onClick(Consumer<InventoryClickEvent>)` | `onClick(Consumer<GuiClick>)` |
| `setFixed(boolean)`, `isFixed()` | removed, icons are always fixed. Use `InputSlot` for movable items |
| `getItemStack()` | `itemStack()` (returns a copy) |
| `getAction()` | removed |

Event access: `click.event()`, player: `click.player()` (no cast needed).

## ActionSlot to InputSlot

| 1.x | 2.0 |
| --- | --- |
| `gui.getActionSlotManager().register(slot, item)` | `gui.input(slot).placeholder(item)` |
| `setRefundOnClose(boolean)` | `refundOnClose(boolean)` |
| `onPrePutClick((event, item) -> blocked)` | `canPut((click, item) -> allowed)` |
| `onPrePickupClick((event, item) -> blocked)` | `canTake((click, item) -> allowed)` |
| `onPut((event, item) -> ...)` | `onPut((click, item) -> ...)` |
| `onPickup((event, item) -> ...)` | `onTake((click, item) -> ...)` |

{% hint style="danger" %}
The pre-click predicates were **inverted**. In 1.x returning `true` meant "block". In 2.0 `canPut` and `canTake` return `true` to **allow**. Flip every condition when porting.
{% endhint %}

Changed behaviour: items are refunded on close (1.x never called the refund), occupied slots are merged or swapped instead of overwritten, max stack sizes are respected, and `filter(...)` is a shortcut for simple item checks.

## PaginationManager to Pagination

| 1.x | 2.0 |
| --- | --- |
| `new PaginationManager(gui)` + `registerPageSlots(...)` | `gui.pagination(slots, mapper)` |
| `addItem(GuiIcon...)` | `setItems(collection)` with a mapper |
| `getCurrentPage()` | `page()` |
| `setPage(int)` | `page(int)` (no refresh) or `goTo(int)` (refreshes) |
| `goNextPage()`, `goPreviousPage()` | `next()`, `previous()` (now refresh the GUI) |
| `goFirstPage()`, `goLastPage()` | `first()`, `last()` |
| `isFirstPage()`, `isLastPage()` | `!hasPrevious()`, `!hasNext()` |
| `getLastPage()` | `pageCount() - 1` |
| `update()` | automatic after `render()` |
| `getSlots()`, `registerPageSlotsBetween` | `Slots.range(...)`, `Slots.inner(...)` |

If you already have a list of ready icons, use an identity mapper:

```java
Pagination<GuiIcon> page = gui.pagination(slots, icon -> icon);
page.setItems(icons);
```

Page numbers stay 0-based.

## VillagerGui

| 1.x | 2.0 |
| --- | --- |
| `abstract void create()` | `protected void render()`, runs on every `open` |
| `addTrade(result, maxUses, ingredients...)` | `addTrade(Trade.of(result).ingredients(...).maxUses(n))` |
| `addTrade(result, ingredients...)` | `addTrade(result, ingredients...)` (unchanged) |

`create()` was called from the constructor, before subclass fields existed. `render()` runs on open instead.

## ItemBuilder

| 1.x | 2.0 |
| --- | --- |
| `new ItemBuilder(Material)` | `ItemBuilder.of(Material)` |
| `new ItemBuilder(ItemStack)` | `ItemBuilder.of(ItemStack)` (copies the item) |
| `setMeta(ItemMeta)` | `meta(Consumer<ItemMeta>)` |
| `setMaterial(Material)` | create a new builder |
| `setAmount(int)` | `amount(int)` |
| `setName(Component)` | `name(Component)` or `name(String)` (MiniMessage) |
| `setLore(Component...)` | `lore(Component...)`, `lore(String...)`, `addLore(...)` |
| `setUnbreakable(boolean)` | `unbreakable(boolean)` |
| `addEnchant(enchantment, level)` | `enchant(enchantment, level)` |
| `removeEnchant(enchantment)` | `removeEnchant(enchantment)` |
| `addItemFlags(...)`, `removeItemFlags(...)` | `flags(...)`, `removeFlags(...)` |
| `setCustomModelData(int)` | removed, use `itemModel(NamespacedKey)` or `meta(...)` |
| `setPersistentData(key, type, value)` | `pdc(key, type, value)` (type-safe) |
| `build()` | `build()` |

Behaviour: names and lore are no longer italic by default.

## SkullBuilder to Skulls

| 1.x | 2.0 |
| --- | --- |
| `new SkullBuilder(UUID)` | `Skulls.of(UUID)` |
| `new SkullBuilder(String name)` | `Skulls.of(Bukkit.getOfflinePlayer(name))` |
| (custom textures were manual) | `Skulls.ofTexture(base64)`, `Skulls.ofUrl(url)` |

`SkullBuilder.build()` blocked the main thread with a network request (`complete()`). `Skulls` never does.

## Configuration to YamlConfig

| 1.x | 2.0 |
| --- | --- |
| `abstract class Configuration` | `class YamlConfig` (concrete) |
| `new Configuration(plugin, "name")` | `YamlConfig.of(plugin, "name")` |
| `init()` | `load()` |
| `reload()` | `reload()` returns `boolean` |
| `save()` | `save()` or `saveAsync()` |
| `onLoad(FileConfiguration)`, `onPreSave(FileConfiguration)` | same |
| `plugin.saveResource` crash when no default exists | handled, an empty config is used |

## InventoryUtils

| 1.x | 2.0 |
| --- | --- |
| `toMap(contents)` | `toMap(contents)` |
| `toArray(map)` | `toArray(map, size)` (the old version lost items) |
| `fillEmptySlots` | `fillEmptySlots` |
| `countItem(inv, material)` | `count(inv, material)` (storage slots only) |
| `hasSpaceFor(inv, item)` | `hasSpaceFor(inv, item)` (storage slots only) |
| `containsItem(inv, item)` | `contains(inv, item)` |
| `getAllItems(inv)` | `items(inv)` |
| `cloneItem(item)` | removed, use `ItemStack#clone()` |

New: `isEmpty`, `count(inv, ItemStack)`, `has`, `remove`, `giveOrDrop`.

## StringType to Parse

| 1.x | 2.0 |
| --- | --- |
| `isBoolean(str)` | `Parse.isBoolean(str)` |
| `isInteger(str)` | `Parse.isInteger(str)` (ASCII digits only, overflow is invalid) |
| `parseBooleanSafe(str)` returns `Boolean` or `null` | `Parse.bool(str)` returns `Optional<Boolean>` |
| `parseIntegerSafe(str)` returns `Integer` or `null` | `Parse.integer(str)` returns `OptionalInt` |

New: `longValue`, `doubleValue`, `enumValue`.

```java
// 1.x
Integer amount = StringType.parseIntegerSafe(arg);
if (amount == null) return;

// 2.0
OptionalInt amount = Parse.integer(arg);
if (amount.isEmpty()) return;
```

## Behaviour changes at a glance

| Area | Change |
| --- | --- |
| GUI | All icons are fixed. Double-click collecting and shift-click moving are blocked. |
| GUI | Clicks outside the window are no longer cancelled. |
| GUI | Double clicks no longer trigger handlers three times. |
| GUI | `onOpen` no longer re-renders. Rendering happens on first open and on `refresh()`. |
| Input slots | Refund on close works, merge/swap instead of overwrite, predicates inverted. |
| Pagination | Slots after the last item are left empty, page changes refresh automatically. |
| Items | Names and lore not italic. |
| Config | Atomic saves, optional async save. |
