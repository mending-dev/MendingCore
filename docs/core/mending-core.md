---
description: The MendingCore entry point and how to obtain it.
---

# MendingCore entry point

`dev.mending.core.api.MendingCore` is the entry point of the API. It tells you the core is running and gives access to its owner plugin.

## Methods

| Method | Returns | Description |
| --- | --- | --- |
| `static MendingCore get()` | `MendingCore` | The active instance. Throws `IllegalStateException` if MendingCore is not enabled yet. |
| `String version()` | `String` | Version of the running MendingCore plugin. |
| `Plugin plugin()` | `Plugin` | The MendingCore plugin instance, usable as owner for tasks and listeners. |
| `PlayerBoards boards()` | `PlayerBoards` | The players' scoreboards and sidebars, see [Scoreboards and sidebars](../scoreboard/sidebar.md). |
| `BossBars bossBars()` | `BossBars` | The players' boss bars, see [Boss bars and titles](../display/boss-bars-and-titles.md). |

## Getting the instance

```java
MendingCore core = MendingCore.get();
```

`MendingCore.get()` delegates to `MendingCoreProvider.get()`, a single volatile read. It is safe to call in hot paths.

Alternatively use the service registry:

```java
MendingCore core = Bukkit.getServicesManager().load(MendingCore.class);
```

## MendingCoreProvider

`MendingCoreProvider` is the static holder behind `MendingCore.get()`.

| Method | Description |
| --- | --- |
| `get()` | Returns the instance or throws `IllegalStateException`. |
| `register(core)` | Internal. Called by the plugin on enable. Throws if already registered. |
| `unregister()` | Internal. Called by the plugin on disable. |

{% hint style="info" %}
You only ever call `get()`. `register` and `unregister` are marked `@ApiStatus.Internal`.
{% endhint %}

## When is it available?

From the moment MendingCore is enabled until it is disabled. If your `paper-plugin.yml` declares the dependency with `load: BEFORE`, MendingCore is available in your `onLoad` and `onEnable`.

An `IllegalStateException` mentioning *"not enabled"* means the dependency is missing in your `paper-plugin.yml` or MendingCore failed to start, see [Troubleshooting](../reference/troubleshooting.md).

## Why does `plugin()` exist?

Some utilities need a plugin as owner, for example scheduling a task. `GuiClick.close()` and `GuiClick.open(Gui)` use it to run on the next tick. You can use it too:

```java
Bukkit.getScheduler().runTask(MendingCore.get().plugin(), () -> { /* ... */ });
```

For your own long-running tasks use **your** plugin as owner, so the tasks are cancelled when your plugin is disabled.
