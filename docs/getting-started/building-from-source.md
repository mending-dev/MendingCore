---
description: Building, running and publishing MendingCore.
---

# Building from source

## Prerequisites

* JDK 25 (Gradle can download it automatically through the toolchain resolver)
* Gradle 9.x. Generate the wrapper once:

```
gradle wrapper --gradle-version 9.6.0
```

## Commands

| Command | What it does |
| --- | --- |
| `./gradlew build` | Compiles, tests and builds all jars. |
| `./gradlew :plugin:runServer` | Starts a local Paper 26.3 test server with the plugin. |
| `./gradlew publishToMavenLocal` | Publishes `paper-api` to your local Maven repository. |
| `./gradlew clean build` | Clean build. |

## Output

| File | Content |
| --- | --- |
| `plugin/build/libs/MendingCore-<version>.jar` | The server plugin. Contains the API classes too. |
| `api/build/libs/paper-api-<version>.jar` | The API for other developers. |
| `api/build/libs/paper-api-<version>-sources.jar` | Sources for IDE navigation. |
| `api/build/libs/paper-api-<version>-javadoc.jar` | Javadoc. |

## Local test server

`:plugin:runServer` creates a `run/` folder inside the `plugin` module and downloads Paper. Start it once, accept the EULA in `run/eula.txt` and start it again.

{% hint style="info" %}
The `run/` folder is in `.gitignore`.
{% endhint %}

See [Build system](../architecture/build-system.md) for how the build is organised.
