---
description: VillagerGui and Trade, trading windows.
---

# Villager GUI

`VillagerGui` opens a villager trading window with trades you define. It is separate from `Gui`: it does not use inventory slots, icons or input slots.

Package: `dev.mending.core.api.gui.villager`.

## Quickstart

```java
VillagerGui.create(Component.text("Blacksmith"), gui -> {
    gui.addTrade(Trade.of(new ItemStack(Material.DIAMOND_SWORD))
        .ingredient(new ItemStack(Material.EMERALD, 12))
        .maxUses(3));

    gui.addTrade(new ItemStack(Material.BREAD, 4), new ItemStack(Material.WHEAT, 6));
}).open(player);
```

## VillagerGui

| Signature | Description |
| --- | --- |
| `VillagerGui(Component title)` | Constructor for subclasses. |
| `static VillagerGui create(Component title)` | Factory. |
| `static VillagerGui create(Component title, Consumer<VillagerGui> renderer)` | Factory with a render function. |
| `protected void render()` | Add trades here. Runs on **every** `open(Player)`. |
| `VillagerGui onRender(Consumer<VillagerGui>)` | Sets the render function. |
| `VillagerGui addTrade(Trade trade)` | Adds a trade. |
| `VillagerGui addTrade(ItemStack result, ItemStack... ingredients)` | Shortcut for an unlimited trade without experience. |
| `void open(Player player)` | Builds the recipes and opens the window. |

### Fixed and dynamic trades

* Trades added **inside** `render()` are rebuilt on every `open`. Use this for trades that depend on state (stock, player level, time).
* Trades added **outside** `render()` (e.g. in a constructor) are permanent and kept for every open.

`render()` is not called from the constructor. This avoids the classic bug where a subclass method runs before the subclass fields are initialised.

## Trade

| Signature | Description |
| --- | --- |
| `static Trade of(ItemStack result)` | Starts a trade. Empty results throw `IllegalArgumentException`. |
| `Trade ingredient(ItemStack)` | Adds an ingredient. A trade has at most **2**, a third throws `IllegalStateException`. |
| `Trade ingredients(ItemStack...)` | Adds several ingredients. |
| `Trade maxUses(int)` | How often the trade can be used per opening. Default unlimited. Must be at least 1. |
| `Trade experienceReward(boolean)` | Whether players get experience. Default `false`. |

A trade needs at least one ingredient, otherwise `open` throws `IllegalStateException`.

Every `open` creates fresh `MerchantRecipe`s from the trades, so use counters are per opening and never shared between players.

## Reacting to purchases

`VillagerGui` does not intercept purchases. Use Paper's trade events (for example `PlayerPurchaseEvent`) or `InventoryClickEvent` on the merchant inventory in your plugin if you need to react, charge, log or limit trades.

## Subclass example

```java
public final class ShopGui extends VillagerGui {

    private final Stock stock;

    public ShopGui(Stock stock) {
        super(Component.text("Shop"));
        this.stock = stock;
    }

    @Override
    protected void render() {
        if (stock.available(Material.DIAMOND) > 0) {
            addTrade(Trade.of(new ItemStack(Material.DIAMOND)).ingredient(new ItemStack(Material.EMERALD, 8)));
        }
    }
}
```
