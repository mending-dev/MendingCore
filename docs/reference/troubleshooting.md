---
description: General build, startup and dependency problems and how to fix them.
---

# Troubleshooting

For GUI-specific problems see [GUI troubleshooting](../gui/troubleshooting.md).

## Build

### Property has implicit dependency (`:plugin:jar` uses `:api:jar`)

The plugin jar bundles the API classes, so Gradle must build `:api:jar` first. In `plugin/build.gradle.kts` the `jar` task needs:

```kotlin
dependsOn(configurations.runtimeClasspath)
```

See [Build system](../architecture/build-system.md).

### run-paper does not resolve or fails

The project pins run-paper to **3.0.2** in `gradle/libs.versions.toml`. Newer versions did not build here.

### No suitable JDK / toolchain error

The build needs JDK 25. The toolchain resolver can download it automatically, so make sure the machine has internet access for the first build, or install JDK 25 manually.

### `Reference to runTask is ambiguous`

Pass a lambda with parentheses instead of a method reference:

```java
Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
```

### Warning: possible `this` escape

The `this-escape` lint warns when a constructor passes `this` to another method before the object is complete. It is suppressed in `Gui` because `Bukkit.createInventory` only stores the holder. In your own subclasses of MendingCore classes you will not see it unless you pass `this` somewhere in a constructor.

## Startup

### `IllegalStateException: MendingCore is not enabled`

* Your `paper-plugin.yml` does not declare MendingCore as a dependency, or the name is misspelled. It must be exactly `MendingCore`.
* MendingCore failed to start. Check the log for the first error.
* You call `MendingCore.get()` before the plugin loaded, for example from a static initialiser.

### `NoClassDefFoundError: dev/mending/core/api/...`

Your plugin cannot see the API classes. Check that the dependency block has `join-classpath: true` and that you did not shade the API into your jar.

### Plugin not found / two plugins with the same name

MendingCore 2.0 and 1.x share the plugin name. Remove the 1.x jar.

### Server refuses to load: api-version

`api-version` in `plugin/src/main/resources/paper-plugin.yml` comes from the `minecraft` entry in the version catalog. Update both together when moving to a new Minecraft version.

## Runtime

### `IllegalStateException: GUIs must only be used on the main thread`

An async task called `Gui.open`, `refresh`, `title` or `rows`. Hop back to the main thread:

```java
Bukkit.getScheduler().runTask(plugin, () -> gui.open(player));
```

### Config file not created

`YamlConfig` only copies a default if your jar contains a resource with the same path. Without one the file is created on the first `save()`.

### Config changes are lost on restart

You modified `config()` but never called `save()` or `saveAsync()`.

## Still stuck?

Collect the full stack trace and the MendingCore version (`/version MendingCore`) before asking.
