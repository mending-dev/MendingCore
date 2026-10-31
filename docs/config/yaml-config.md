---
description: YamlConfig, loading, defaults and safe saving of YAML files.
---

# YamlConfig

`dev.mending.core.api.config.YamlConfig` manages one YAML file in your plugin's data folder.

```java
YamlConfig settings = YamlConfig.of(plugin, "settings").load();

String prefix = settings.config().getString("prefix");
```

## What it does

* If the file does not exist, it is copied from your plugin jar (same name). Without a bundled file an empty config is used.
* A bundled file of the same name is used as **defaults**: missing keys fall back to it when reading.
* Saving writes to a temporary file first and then replaces the original, so a crash cannot leave a half-written config.

## Creating

| Signature | Description |
| --- | --- |
| `YamlConfig(Plugin plugin, String fileName)` | Constructor, for subclasses. |
| `static YamlConfig of(Plugin plugin, String fileName)` | Factory. |

The file name is relative to the data folder. `.yml` is appended unless it already ends with `.yml` or `.yaml`. Names containing `..` are rejected with `IllegalArgumentException`. Subfolders work (`"data/players"`), but a bundled default must exist at the same path inside the jar.

## Loading

| Signature | Description |
| --- | --- |
| `YamlConfig updateMissingKeys(boolean)` | If enabled, keys that exist in the bundled file but not on disk are added and written on load. Set it **before** `load()`. |
| `YamlConfig load()` | Loads the file. Throws `IllegalStateException` (with the file name) if it cannot be read or the YAML is invalid. |
| `boolean reload()` | Reloads from disk. On failure the error is logged and the previous content is kept, it returns `false`. |

`load()` is for startup, where a broken config should stop the plugin. `reload()` is for a `/reload` command, where a typo must not wipe your running settings.

## Reading and writing

| Signature | Description |
| --- | --- |
| `FileConfiguration config()` | The loaded configuration. Throws `IllegalStateException` if `load()` was not called. |
| `File file()` | The file on disk. |

`config()` is the normal Bukkit `FileConfiguration`, all its methods work.

```java
YamlConfig data = YamlConfig.of(plugin, "data").load();
data.config().set("players." + uuid + ".kills", 12);
data.save();
```

## Saving

| Signature | Description |
| --- | --- |
| `void save()` | Saves synchronously. Throws `UncheckedIOException` on failure. Use this in `onDisable`. |
| `CompletableFuture<Void> saveAsync()` | Captures the content immediately, writes to disk asynchronously. Falls back to the calling thread if the plugin is already disabled. |

```java
data.saveAsync().exceptionally(error -> {
    plugin.getSLF4JLogger().error("Could not save data", error);
    return null;
});
```

{% hint style="warning" %}
`FileConfiguration` is not thread-safe. Modify it only on the main thread. `saveAsync()` is safe because it serialises the content to a string first, on the calling thread.
{% endhint %}

Writes are serialised with a lock and use a temporary file plus an atomic move (with a non-atomic fallback on file systems that do not support it).

## Defaults and updating

Put a complete `settings.yml` into your plugin's resources. Then:

* Reading a key that is missing on disk returns the default from the jar.
* With `updateMissingKeys(true)` new keys from an update are also written into the player's file.

```java
YamlConfig.of(plugin, "settings").updateMissingKeys(true).load();
```

Keys added this way have no comments. Saving rewrites the file with Bukkit's formatting, so hand-made formatting may change.

## Subclassing

Override the hooks to turn a file into typed values:

```java
public final class SettingsConfig extends YamlConfig {

    private String prefix = "";
    private int maxHomes = 3;

    public SettingsConfig(Plugin plugin) {
        super(plugin, "settings");
    }

    @Override
    protected void onLoad(FileConfiguration config) {
        this.prefix = config.getString("prefix", "");
        this.maxHomes = config.getInt("max-homes", 3);
    }

    @Override
    protected void onPreSave(FileConfiguration config) {
        config.set("max-homes", this.maxHomes);
    }

    public String prefix()   { return prefix; }
    public int maxHomes()    { return maxHomes; }
}
```

| Hook | Called |
| --- | --- |
| `onLoad(FileConfiguration)` | After a successful `load()` or `reload()`. |
| `onPreSave(FileConfiguration)` | Right before `save()` or `saveAsync()` writes. |

Reading values into fields once in `onLoad` is faster than calling `getString` on every access and gives you typed accessors.

## Thread safety

* `config()` is a volatile read, so the reference is always current.
* The content itself must only be changed on one thread.
* File writes are synchronised internally.

## Common mistakes

| Mistake | Effect | Fix |
| --- | --- | --- |
| Calling `config()` before `load()` | `IllegalStateException` | Call `load()` in `onEnable`. |
| Using `saveAsync()` in `onDisable` | Works (falls back to synchronous), but unclear | Use `save()`. |
| Editing the config from async tasks | Corrupt state | Edit on the main thread, then `saveAsync()`. |
| Expecting comments on keys added by `updateMissingKeys` | None are written | Add them to the file by hand. |
