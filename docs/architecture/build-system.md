---
description: How the Gradle build is organised and how to maintain it.
---

# Build system

## Files

| File | Purpose |
| --- | --- |
| `settings.gradle.kts` | Project name `MendingCore`, modules, central repositories, toolchain resolver. |
| `gradle.properties` | `group`, `version`, Gradle performance flags. |
| `gradle/libs.versions.toml` | Version catalog: Paper, run-paper. |
| `build-logic/` | Convention plugin `mending.java-conventions`. |
| `api/build.gradle.kts` | API module, publishing. |
| `plugin/build.gradle.kts` | Plugin module, resources, jar contents, `runServer`. |

## Convention plugin

`mending.java-conventions` is applied by every module and configures:

* Java 25 toolchain and `--release 25`
* UTF-8
* `-parameters` and lint flags (`-Xlint:all,-processing,-serial`)
* Javadoc without strict doclint
* JUnit 5

Change a setting once there and it applies to all modules.

## Repositories

Repositories are declared once in `settings.gradle.kts` with `FAIL_ON_PROJECT_REPOS`. Modules must not declare their own.

## Versions

```toml
[versions]
minecraft = "26.3"
paper = "26.3.build.+"
run-paper = "3.0.2"
```

* Bump `minecraft` and `paper` together. `minecraft` feeds `api-version` in `paper-plugin.yml` and `runServer`.
* `26.3.build.+` is a dynamic version. Pin it to a fixed build for reproducible releases.
* run-paper is pinned to **3.0.2**. Version 3.1.0 did not build in this project.

## Plugin jar contents

The plugin jar bundles the classes of the `api` module:

```kotlin
jar {
    dependsOn(configurations.runtimeClasspath)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.map { classpath ->
        classpath.map { if (it.isDirectory) it else zipTree(it) }
    })
}
```

`dependsOn(configurations.runtimeClasspath)` is required: the files come from `:api:jar`, and Gradle must know to build it first. Without it the build fails with *"Property has implicit dependency"*.

If you later add third-party libraries to the plugin module with `implementation`, they are bundled the same way, unrelocated. At that point consider a Paper `PluginLoader` with `MavenLibraryResolver` or the Shadow plugin with relocation, but never relocate the API itself.

## Publishing

The `api` module publishes `dev.mending:mending-core-api` with sources and Javadoc jars to a local `build/repo` folder (`repo/snapshots` for `-SNAPSHOT` versions, `repo/releases` otherwise). The GitHub workflow mirrors that folder into `mending-dev/maven-repo`. `./gradlew publishToMavenLocal` still publishes to `mavenLocal`.

## Configuration cache

`org.gradle.configuration-cache=true` is enabled. If a plugin or script is incompatible, remove that line from `gradle.properties` to find out.

## Compiler warnings

`Gui` suppresses the `this-escape` lint warning in its constructor with a comment explaining why: `Bukkit.createInventory` only stores the holder and never calls methods on it.
