---
description: Naming, threading, nullability and error handling conventions of the API.
---

# Conventions

## Threading

Everything that touches the Bukkit world, inventories or GUIs must run on the **main thread**. Methods that open or change what players see check this and throw `IllegalStateException` when called from another thread:

* `Gui.open`, `Gui.refresh`, `Gui.title`, `Gui.rows`

Do slow work (database, files, HTTP) asynchronously and hand the result back to the main thread, see [Recipes](../gui/recipes.md).

`YamlConfig.saveAsync()` is the exception: only the disk write happens asynchronously.

## Indexing

| What | Base |
| --- | --- |
| Slots | 0-based, like Minecraft |
| Rows | 1-based |
| Columns | 1-based |
| Pages | 0-based (`Pagination.page()`) |

`Gui.set(2, 5, icon)` means row 2, column 5. See [Slots and layouts](../gui/slots-and-layouts.md).

## Text

* Methods taking a `Component` give you full control.
* Methods taking a `String` (e.g. `ItemBuilder.name(String)`, `Gui(String, int)`) parse **MiniMessage**.
* Names and lore built by `ItemBuilder` are **not italic** unless you set italic yourself.

{% hint style="warning" %}
Never concatenate untrusted text (player names, chat input) into a MiniMessage string. Use `Component.text(...)` for such parts, otherwise players can inject formatting tags.
{% endhint %}

## Nullability

The API uses `org.jetbrains.annotations.@NotNull` and `@Nullable`. Parameters and return values are non-null unless annotated otherwise. Parsing helpers return `Optional`, `OptionalInt`, `OptionalLong` or `OptionalDouble` instead of `null`.

## Defensive copies

Anything that holds an `ItemStack` copies it on the way in, and getters that return one return a copy (e.g. `InputSlot.content()`, `GuiIcon.itemStack()`). You can mutate what you get without affecting the library.

## Errors

| Situation | Reaction |
| --- | --- |
| Programming error (bad row, slot out of range, wrong thread) | Exception right away (`IllegalArgumentException`, `IllegalStateException`, `IndexOutOfBoundsException`) |
| Invalid user input (parsing) | Empty `Optional`, never an exception |
| Recoverable I/O (`YamlConfig.reload()`) | Logged, previous state kept |
| Unrecoverable I/O (`YamlConfig.load()`, `save()`) | Exception with the file name |

## Fluent API

Configuration methods return `this`. Methods that read or act return values or `void`.

## Internal API

Anything annotated `@ApiStatus.Internal` or inside `dev.mending.core.plugin` is not part of the contract.
