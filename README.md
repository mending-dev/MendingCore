# MendingCore


A foundational library designed to provide essential utilities and shared functionality for all of my other Minecraft plugins. It acts as a core dependency that must be installed on your server to enable seamless integration and shared features across multiple plugins.

While this Plugin is designed primarily to support my own plugin ecosystem, it is fully open and available for anyone to use in their own plugins. Feel free to integrate and build upon it to simplify your development and share utilities across your projects.

---

## 🚀 Features

### Available Now
- **ItemBuilder / SkullBuilder**  
  Simplify item creation with an intuitive API for building custom items and player skulls.

### Coming Soon
- **JSON Configurations**  
  Flexible and powerful configuration handling using JSON format.
- **Language Utilities**  
  Tools to easily manage multilingual support and localization.
- **Custom GUIs & Villager Trades**  
  Create interactive, custom graphical user interfaces and unique villager trades.
- **Additional Utilities**  
  More helpful tools and APIs to enhance plugin development and functionality.

---

## 📦 How to Use MendingCore API

### 1. Installation

The **MendingCore plugin must be installed on the server** before any dependent plugin can use its API. It acts as a shared core and provides necessary classes and services.

### 2. Add MendingCore as a Dependency

Add the Maven repository to your build tool configuration:

#### Gradle (Groovy DSL)

```groovy
repositories {
    mavenCentral()
    maven {
        url = "https://repo.mending.dev/releases" // or snapshots if needed
    }
}

dependencies {
    implementation "dev.mending.core:paper-api:1.0.0"
}
```

#### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.mending.dev/releases") // or snapshots if needed
}

dependencies {
    implementation("dev.mending.core:paper-api:1.0.0")
}
```

#### Maven

```xml
<repositories>
    <repository>
        <id>mendingcore-repo</id>
        <url>https://repo.mending.dev/releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>dev.mending.core</groupId>
        <artifactId>paper-api</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### 3. Declare MendingCore as a Plugin Dependency

In your plugin's `plugin.yml` or `paper-plugin.yml`, declare a dependency to ensure MendingCore loads first:

```yaml
depend: [MendingCore]
```

---

## 📚 Documentation & Support

Detailed documentation, examples, and tutorials will be added soon alongside upcoming features.  
For questions or feedback, please open an issue on the GitHub repository.

---

## ⚖️ License

MendingCore is licensed under the MIT License. See [LICENSE](LICENSE) for details.