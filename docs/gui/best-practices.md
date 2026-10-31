---
description: How to structure menus so they stay correct, readable and easy to change.
---

# Best practices

## Structure

### Let `render()` describe the menu, state live in fields

`render()` should be a pure description: read state, place icons. Handlers change state and call `refresh()`.

```java
public final class SettingsGui extends Gui {

    private boolean notifications;

    @Override
    protected void render() {                       // only reads state
        set(2, 5, toggle(notifications));
    }

    private GuiIcon toggle(boolean on) {
        return GuiIcon.of(ItemBuilder.of(on ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(on ? "<green>On" : "<red>Off"))
            .onLeftClick(click -> {
                notifications = !notifications;     // changes state
                click.refresh();                    // redraws
            });
    }
}
```

Do not mutate state inside `render()`. It runs on every refresh and the result must be the same for the same state.

### Subclass for anything with state, lambda for the rest

Use `Gui.create(title, rows, renderer)` for small static menus. As soon as you have fields, hooks or helper methods, write a subclass.

### Name your slots

Use constants and `Slots.slot(row, column)` instead of magic numbers.

### Keep logic out of handlers

Handlers should call a method of your own service (`shop.buy(player, item)`) and then refresh. This keeps menu code short and the logic testable without a server.

## Navigation

### Use click.open and click.close

Inside handlers always use `click.open(other)` and `click.close()`. They run on the next tick, which is safe. Never call `player.openInventory(...)` or `player.closeInventory()` directly inside a click handler.

### Keep a parent reference for back buttons

```java
public final class DetailGui extends Gui {

    private final Gui parent;

    public DetailGui(Gui parent) {
        super(Component.text("Details"), 3);
        this.parent = parent;
    }

    @Override
    protected void render() {
        set(3, 5, GuiIcon.of(Material.ARROW).onLeftClick(click -> click.open(parent)));
    }
}
```

Reusing the parent instance keeps its state (page, filter). Create a fresh instance instead if it should reset.

## One instance per player, or shared?

| | Per player | Shared |
| --- | --- | --- |
| Content | Can differ per player | Identical for everyone |
| State (page, filter) | Private | Shared, one player's click changes it for all |
| Cost | One inventory per player | One inventory in total |
| Good for | Profiles, shops with player data, paginated lists | Static selectors, rules, server info |

If the menu shows player-specific data or has page/filter state, create one instance per player. Shared GUIs with [input slots](input-slots.md) are almost always a mistake: all viewers share one slot.

## Click safety

* Use `onLeftClick` for actions with consequences, see [Icons and clicks](icons-and-clicks.md).
* Re-check conditions at click time (money, stock, permissions, free space).
* Change state before giving rewards, so a fast second click finds the updated state.
* Give items with `InventoryUtils.giveOrDrop`.

## Items and text

* Build icons from `ItemBuilder` so names and lore are not italic.
* Never insert player-controlled text into MiniMessage strings. Use `Component.text(...)`.
* Create the same filler icon once in a `static final` field.

## Cleanup

* The library holds no static references to your GUIs. A GUI is collected once nobody references it.
* If **you** keep GUIs in a map (for live updates), remove them in `onClose`. It also runs on disconnect.

```java
@Override protected void onOpen(Player player)  { OPEN.put(player.getUniqueId(), this); }
@Override protected void onClose(Player player) { OPEN.remove(player.getUniqueId(), this); }
```

* Cancel tasks started for a GUI (animations, timers) in `onClose`.

## Dynamic content

* Load data **before** creating the GUI, asynchronously, see [Pagination](pagination.md).
* For live updates call `refresh()` on open GUIs from a main-thread task. Do not refresh more often than needed, see [Performance](performance.md).

## Player inventory

Use `lockPlayerInventory(true)` for pure selection menus where rearranging inventory while the menu is open would only confuse players. Leave it off for menus where players are expected to interact with their inventory.

## Do and don't

| Do | Don't |
| --- | --- |
| Put every icon in `render()` | Set icons outside `render()` and then call `refresh()` |
| `click.open(...)`, `click.close()` | `player.openInventory(...)` inside a handler |
| `InputSlot` for movable items | Modify `getInventory()` directly |
| One GUI instance per player for player data | Share a stateful GUI between players |
| Cache constant icons | Rebuild identical icons on every refresh |
| Check conditions at click time | Trust what was true at render time |
| Async load, sync apply | Query a database in `render()` |
