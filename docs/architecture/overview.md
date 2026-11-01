---
description: Modules, packages and the rules that keep MendingCore maintainable.
---

# Overview

## Modules

```
MendingCore
├── api      → dev.mending.core:paper-api   (what you compile against)
└── plugin   → MendingCore-<version>.jar      (what runs on the server)
```

| Module | Contains | Depends on |
| --- | --- | --- |
| `api` | Interfaces, GUI framework, builders, utilities | Paper API (`compileOnlyApi`) |
| `plugin` | `MendingCorePlugin`, `MendingCoreImpl`, event listeners | `api`, Paper API |

## Packages

```
dev.mending.core.api
├── MendingCore, MendingCoreProvider
├── gui          Gui, GuiIcon, GuiClick, InputSlot, Pagination, Slots
│   └── villager VillagerGui, Trade
├── item         ItemBuilder, Skulls
├── scoreboard   PlayerBoards, Sidebar
├── display      BossBars, Titles
├── world        BlockPos, Position, BlockRegion
├── config       YamlConfig
├── inventory    InventoryUtils
└── util         Parse

dev.mending.core.plugin        (internal, may change at any time)
├── MendingCorePlugin, MendingCoreImpl
├── display      DisplayListener
└── gui          GuiListener
```

## Rules

1. `api` never depends on `plugin`. `plugin` depends on `api`.
2. `api` depends on Paper only. Third-party libraries do not leak into the API.
3. Everything in `dev.mending.core.plugin` is internal.
4. Listeners and server wiring live in `plugin`. The API describes behaviour and holds the logic, the plugin forwards events to it.
5. No static mutable state except `MendingCoreProvider`.
6. Methods marked `@ApiStatus.Internal` are public only because the plugin module needs them. Do not call them.

## Why the API is inside the plugin jar

Your plugin compiles against `paper-api`, but at runtime there is only one copy of those classes: the one inside `MendingCore-<version>.jar`. With `join-classpath: true` your plugin's class loader can see them. Because all plugins share that single copy, a `Gui` created by plugin A is recognised by the listener in MendingCore, which is what makes the GUI system work across plugins.

The API classes are **not relocated** inside the plugin jar. Relocating them would break every consumer.

## How events reach your GUI

```
Bukkit event ──► GuiListener (plugin module) ──► Gui.handleClick(...)  (api module)
```

`GuiListener` only checks whether the inventory holder is a `Gui` and forwards the event. All behaviour lives in the API, so it is documented and testable in one place. Details: [Events and safety](../gui/events-and-safety.md).

## Lifecycle

| Phase | What happens |
| --- | --- |
| Enable | `MendingCoreImpl` is created, registered in `MendingCoreProvider` and the `ServicesManager`, `GuiListener` is registered. |
| Disable | All open GUIs are closed (input slot contents are refunded), then the service and provider are unregistered. |
