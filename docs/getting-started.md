# Getting Started

This guide will help you quickly set up your development environment and start building your first plugin.

---

## ✅ Prerequisites

- Java **21**
- Maven or Gradle
- A **Paper** 1.21+ server (or compatible fork like Purpur)

> 🧠 Note: MendingCore only supports [Paper](https://papermc.io) and its forks — not Spigot or CraftBukkit.

---

## 📦 Installing MendingCore

Add the appropriate dependency to your project.

### Gradle (Groovy DSL)

```groovy
repositories {
    maven { url = uri("https://repo.mending.dev/releases") }
}

dependencies {
    implementation "dev.mending.core:paper-api:VERSION"
}
```

<details>
<summary>Gradle (Kotlin DSL)</summary>

```kotlin
repositories {
    maven("https://repo.yourdomain.com/releases")
}

dependencies {
    implementation("dev.mending.core:paper-api:VERSION")
}
```

</details>

### Maven

```xml
<repositories>
    <repository>
        <id>mendingcore</id>
        <url>https://repo.mending.dev/releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>dev.mending.core</groupId>
        <artifactId>paper-api</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```

> 🔁 Replace `VERSION` with the latest available version.

<details>
<summary>📸 Using Snapshot Versions?</summary>

If you're using a snapshot version like `1.0.0-SNAPSHOT`, be sure to replace the repository URL with the snapshot repository:

**Gradle:**
```groovy
repositories {
    maven { url = uri("https://repo.mending.dev/snapshots") }
}
```

**Maven:**
```xml
<repositories>
    <repository>
        <id>mendingcore-snapshots</id>
        <url>https://repo.mending.dev/snapshots</url>
    </repository>
</repositories>
```

</details>

---

## 🧪 Quick Start

1. Create a basic plugin class:

```java
public class MyPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // Coming Soon
    }
}
```

2. Register it in your `plugin.yml`:

```yaml
name: MyPlugin
version: 1.0
main: com.example.MyPlugin
api-version: 1.20
```

3. Build your plugin and place the `.jar` file in the `plugins/` folder of your Paper server.  
Also, make sure the MendingCore plugin itself is installed in the `plugins/` folder of your server.

---

## 🔍 Next Steps

Now that your plugin is set up, you can:

- [Learn about ItemBuilder](dev/items/itembuilder.md)
- [Explore the GUI System](dev/gui/gui.md)
- [Understand ActionSlots](dev/gui/action-slots.md)

Continue to the [Plugin Philosophy](philosophy.md) to learn more about the project's design principles.