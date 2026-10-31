---
description: Adding MendingCore as a dependency to your own plugin.
---

# Using the API in your plugin

## 1. Add the dependency

MendingCore is published as `dev.mending.core:paper-api`. Add the repository, then depend on the API
with `compileOnly` (Gradle) or `provided` (Maven). At runtime the classes come from the MendingCore
plugin, so never shade them into your own plugin.

| Type | Repository URL |
| --- | --- |
| Releases | `https://repo.mending.dev/releases` |
| Snapshots | `https://repo.mending.dev/snapshots` |

<details>
<summary>Gradle (Groovy)</summary>

```groovy
repositories {
    maven { url = 'https://repo.mending.dev/releases' }
    maven { url = 'https://repo.mending.dev/snapshots' }
    maven { url = 'https://repo.papermc.io/repository/maven-public/' }
}

dependencies {
    compileOnly 'io.papermc.paper:paper-api:26.3.build.+'
    compileOnly 'dev.mending.core:paper-api:2.0.0-SNAPSHOT'
}
```

</details>

<details>
<summary>Gradle (Kotlin)</summary>

```kotlin
repositories {
    maven("https://repo.mending.dev/releases")
    maven("https://repo.mending.dev/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    compileOnly("dev.mending.core:paper-api:2.0.0-SNAPSHOT")
}
```

</details>

<details>
<summary>Maven</summary>

```xml
<repositories>
    <repository>
        <id>mending-releases</id>
        <url>https://repo.mending.dev/releases</url>
    </repository>
    <repository>
        <id>mending-snapshots</id>
        <url>https://repo.mending.dev/snapshots</url>
    </repository>
    <repository>
        <id>papermc</id>
        <url>https://repo.papermc.io/repository/maven-public/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>26.3.build.+</version> <!-- pin a concrete build, Maven has no "+" -->
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>dev.mending.core</groupId>
        <artifactId>paper-api</artifactId>
        <version>2.0.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

</details>

For stable builds use a release version (e.g. `2.0.0`) — releases and snapshots live in different repositories.

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
