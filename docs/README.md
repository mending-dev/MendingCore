---
description: The shared core library and API for all Mending plugins on Hokai.net.
---

# MendingCore

MendingCore is the shared foundation for all my plugins. It contains the utilities that would otherwise be copied from plugin to plugin: a GUI framework, an item builder, YAML config handling, inventory helpers and safe parsing.

{% hint style="info" %}
**Version 2.0.0-SNAPSHOT** is the successor of MendingCore 1.x. It is a rewrite, not a drop-in update. See [Migration from 1.x](reference/migration-from-1x.md).
{% endhint %}

## Two modules, one rule

| Module | Artifact | What it is |
| --- | --- | --- |
| `api` | `dev.mending.core:paper-api` | Contracts and utilities. This is all your plugins compile against. |
| `plugin` | `MendingCore-<version>.jar` | The server plugin: implementation, listeners, lifecycle. It also contains the API classes. |

Your plugins compile against the **API only** and declare the **plugin** as a runtime dependency. See [Using the API in your plugin](getting-started/using-the-api.md).

## What is inside

| Area | Classes | Page |
| --- | --- | --- |
| Entry point | `MendingCore`, `MendingCoreProvider` | [MendingCore](core/mending-core.md) |
| GUI | `Gui`, `GuiIcon`, `GuiClick`, `InputSlot`, `Pagination`, `Slots` | [GUI overview](gui/README.md) |
| Villager trading | `VillagerGui`, `Trade` | [Villager GUI](gui/villager-gui.md) |
| Items | `ItemBuilder`, `Skulls` | [ItemBuilder](items/item-builder.md), [Skulls](items/skulls.md) |
| Configuration | `YamlConfig` | [YamlConfig](config/yaml-config.md) |
| Inventories | `InventoryUtils` | [InventoryUtils](inventory/inventory-utils.md) |
| Parsing | `Parse` | [Parse](util/parse.md) |

## A first taste

```java
Gui.create(Component.text("Hello"), 3, gui -> {
    gui.fillBorder(GuiIcon.of(ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty())));
    gui.set(2, 5, GuiIcon.of(ItemBuilder.of(Material.EMERALD).name("<green>Click me"))
        .onLeftClick(click -> click.player().sendMessage(Component.text("Hi!"))));
}).open(player);
```

## Requirements

* Paper 26.3
* Java 25
* Gradle 9.x for building

## Where to start

* New to the library: [Installation](getting-started/installation.md), then [Using the API in your plugin](getting-started/using-the-api.md).
* Building menus: [GUI quickstart](gui/quickstart.md).
* Coming from MendingCore 1.x: [Migration from 1.x](reference/migration-from-1x.md).
