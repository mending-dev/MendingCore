---
description: Build and open your first GUI, then add state, an input slot and a page list.
---

# Quickstart

## 1. The smallest GUI

```java
Gui gui = Gui.create(Component.text("Hello"), 3, g ->
    g.set(2, 5, GuiIcon.of(Material.EMERALD)));

gui.open(player);
```

* `Gui.create(title, rows, renderer)` makes a menu with 3 rows.
* The renderer runs the first time the GUI is opened.
* `set(2, 5, icon)` places an icon in row 2, column 5 (1-based).

## 2. Make it clickable

```java
GuiIcon.of(ItemBuilder.of(Material.EMERALD).name("<green>Claim reward"))
    .onLeftClick(click -> {
        click.player().sendMessage(Component.text("Claimed!"));
        click.close();
    });
```

`click` is a [GuiClick](icons-and-clicks.md). `click.close()` closes the menu on the next tick, which is safe inside a handler.

## 3. Add state with a subclass

```java
public final class CounterGui extends Gui {

    private int count;

    public CounterGui() {
        super(Component.text("Counter"), 3);
    }

    @Override
    protected void render() {
        set(2, 5, GuiIcon.of(ItemBuilder.of(Material.PAPER).name("<yellow>Count: " + count))
            .onLeftClick(click -> {
                count++;
                click.refresh();   // runs render() again
            }));
    }
}
```

The pattern: **handlers change state, `refresh()` redraws, `render()` only reads state.**

## 4. Let players put items in

```java
InputSlot slot = gui.input(13)
    .placeholder(ItemBuilder.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE).name("<gray>Insert item").build())
    .filter(item -> item.getType() == Material.DIAMOND);
```

Players can now put diamonds into slot 13. Whatever is left in the slot when the GUI closes is returned to the player. See [Input slots](input-slots.md).

## 5. Show a long list

```java
Pagination<Player> page = gui.pagination(Slots.inner(5), target ->
    GuiIcon.of(Skulls.of(target).name(Component.text(target.getName()))));

page.setItems(Bukkit.getOnlinePlayers());
```

Place `page.nextIcon(...)` and `page.previousIcon(...)` as buttons. See [Pagination](pagination.md).

## Next

* How everything is ordered: [Lifecycle](lifecycle.md)
* Complete examples: [Recipes](recipes.md)
