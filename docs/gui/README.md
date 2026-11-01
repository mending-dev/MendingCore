---
description: The GUI framework of MendingCore, concepts and map of the documentation.
---

# GUI overview

The GUI framework builds chest-style menus. It is designed around three ideas:

1. **Locked by default.** Nothing in a GUI can be moved, taken or duplicated unless you explicitly allow it through an [input slot](input-slots.md).
2. **One render function.** You describe what the menu looks like in `render()`. When state changes you call `refresh()`.
3. **Event handling in one place.** A single listener in the MendingCore plugin routes every inventory event to the right GUI. You never register inventory listeners yourself.

## The building blocks

| Class | Role |
| --- | --- |
| `Gui` | The menu. Holds icons, input slots, title and size. Subclass it or use it with a lambda. |
| `GuiIcon` | An item with click handlers. |
| `GuiClick` | Context passed to click handlers: player, GUI, slot, click type. |
| `InputSlot` | A slot players can put items into and take items out of. |
| `Pagination<T>` | Spreads a list over several pages. |
| `Slots` | Helpers to build slot arrays (rows, columns, border, inner area). |
| `ConfirmGui` | A menu with a confirm and a cancel button. |
| `TextInput` | Asks for a text in a Minecraft dialog window. |
| `VillagerGui`, `Trade` | A trading window, separate from `Gui`. |

## How it fits together

```
Gui ──┬── icons[]      GuiIcon (item + handlers)
      ├── inputs[]     InputSlot (content, filter, callbacks)
      └── paginations  Pagination<T> (items -> icons per page)

Player clicks ─► GuiListener ─► Gui.handleClick ─► GuiIcon / InputSlot handler(GuiClick)
```

## Map of this section

| Page | Read it for |
| --- | --- |
| [Quickstart](quickstart.md) | Your first menu in minutes. |
| [Lifecycle](lifecycle.md) | What happens when, and in which order. |
| [Gui reference](gui-reference.md) | Every method of `Gui`. |
| [Icons and clicks](icons-and-clicks.md) | `GuiIcon`, `GuiClick`, click types. |
| [Slots and layouts](slots-and-layouts.md) | Coordinates, `Slots`, fill methods. |
| [Input slots](input-slots.md) | Letting players put items in. |
| [Pagination](pagination.md) | Long lists. |
| [ConfirmGui](confirm-gui.md) | Yes/no menus. |
| [Text input](text-input.md) | Asking for a text. |
| [Villager GUI](villager-gui.md) | Trading windows. |
| [Events and safety](events-and-safety.md) | Exactly what is cancelled and why. |
| [Best practices](best-practices.md) | How to structure menus. |
| [Performance](performance.md) | Costs, pitfalls and tips. |
| [Recipes](recipes.md) | Complete examples. |
| [Troubleshooting](troubleshooting.md) | Symptoms and fixes. |

## Two ways to write a GUI

{% tabs %}
{% tab title="Lambda" %}
Best for small, static menus.

```java
Gui.create(Component.text("Menu"), 3, gui ->
    gui.set(2, 5, GuiIcon.of(Material.EMERALD).onLeftClick(c -> c.close()))
).open(player);
```
{% endtab %}

{% tab title="Subclass" %}
Best for menus with state, hooks or helper methods.

```java
public final class MenuGui extends Gui {

    public MenuGui() {
        super(Component.text("Menu"), 3);
    }

    @Override
    protected void render() {
        set(2, 5, GuiIcon.of(Material.EMERALD).onLeftClick(c -> c.close()));
    }
}
```
{% endtab %}
{% endtabs %}
