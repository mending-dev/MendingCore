---
description: Adding MendingCore as a dependency to your own plugin.
---

# Using the API in your plugin

## 1. Gradle

Compile against the API, never against the plugin jar.

```kotlin
repositories {
    mavenLocal() // until the API is published to a repository
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    compileOnly("dev.mending.core:paper-api:2.0.0-SNAPSHOT")
}
```

`compileOnly` is correct: at runtime the classes come from the MendingCore plugin. Do **not** shade the API into your plugin.

Publish the API once from the MendingCore project:

```
./gradlew publishToMavenLocal
```

## 2. paper-plugin.yml

```yaml
name: MyPlugin
version: '1.0.0'
main: dev.example.MyPlugin
api-version: '26.3'
dependencies:
  server:
    MendingCore:
      load: BEFORE
      required: true
      join-classpath: true
```

| Key | Meaning |
| --- | --- |
| `load: BEFORE` | MendingCore is loaded before your plugin. |
| `required: true` | Your plugin does not start without MendingCore. |
| `join-classpath: true` | Your plugin can see the API classes. Without it you get `NoClassDefFoundError`. |

## 3. Use it

```java
public final class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        MendingCore core = MendingCore.get();
        getLogger().info("Running on MendingCore " + core.version());
    }
}
```

Most classes (`Gui`, `ItemBuilder`, `YamlConfig`, ...) do not need the `MendingCore` instance at all, you just use them.

## Optional dependency

If MendingCore is optional for your plugin, set `required: false` and keep every reference to API classes inside classes that are only loaded when the plugin is present. Referencing an API class from your main class loads it too early and throws `NoClassDefFoundError`.

```java
if (getServer().getPluginManager().isPluginEnabled("MendingCore")) {
    MendingFeatures.enable(this); // only this class touches the API
}
```

## Alternative: ServicesManager

`MendingCore` is also registered as a Bukkit service:

```java
MendingCore core = Bukkit.getServicesManager().load(MendingCore.class);
```

`MendingCore.get()` is faster (one volatile read) and throws a clear exception if the core is not enabled.
