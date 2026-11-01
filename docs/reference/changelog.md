---
description: Release notes of MendingCore.
---

# Changelog

## 2.0.0-SNAPSHOT

Complete rewrite of MendingCore as a two-module library: `api` for developers, `plugin` for the server. Successor of MendingCore 1.x, see [Migration from 1.x](migration-from-1x.md).

### Architecture

* Split into `api` (`dev.mending.core:paper-api`) and `plugin` (`MendingCore`).
* Entry point `MendingCore` with `MendingCoreProvider` and `ServicesManager` registration.
* Build logic in a convention plugin, version catalog, Java 25, Paper 26.3.
* All open GUIs are closed on shutdown.

### GUI

* New `Gui` with `render()` / `refresh()` lifecycle, lambda or subclass.
* New `GuiClick` context object, `click.close()` and `click.open(gui)` run on the next tick.
* `InputSlot` replaces `ActionSlot`: filter, stack limit, callbacks, merge and swap, refund on close.
* `Pagination<T>` replaces `PaginationManager`: lazy mapping, automatic refresh.
* `Slots` helper and `fill*` methods (`fillBorder`, `fillEmpty`, `fillRange`).
* Fixed: items placed in action slots were lost on close.
* Input slots also take number keys, dragging and the creative middle click.
* Clicks another plugin cancelled before still reach a GUI's own slots, so its menus keep working.
* Fixed: occupied action slots overwrote items.
* Fixed: double-click collecting pulled icons out of the GUI.
* Fixed: handlers ran three times on double click.
* Fixed: `addItem` only searched the first `rows` slots.
* Fixed: `clear()` did not clear the inventory.
* Fixed: clicking outside the window was cancelled.
* Fixed: reopening for title or size changes triggered close hooks.
* Performance: array-based slot storage, `getHolder(false)` in the listener.
* `VillagerGui` renders on open and gets a `Trade` builder.

### Items

* `ItemBuilder`: copies input, non-italic names and lore, MiniMessage overloads, typed PDC, escape hatches.
* `Skulls` replaces `SkullBuilder` and never blocks the main thread.

### Configuration

* `YamlConfig` replaces `Configuration`: defaults, optional key updates, atomic and async saving, no crash without bundled resource.

### Utilities

* `InventoryUtils`: fixed `toArray`, storage-slot semantics, new `has`, `remove`, `giveOrDrop`.
* `Parse` replaces `StringType`: `Optional` results, ASCII-only digits, no locale pitfalls.
