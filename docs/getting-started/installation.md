---
description: Installing the MendingCore plugin on a server.
---

# Installation

## Server

1. Download or build `MendingCore-<version>.jar` (see [Building from source](building-from-source.md)).
2. Put it into the server's `plugins/` folder.
3. Start the server. The log shows `MendingCore <version> enabled`.

{% hint style="warning" %}
MendingCore 2.0 uses the same plugin name as MendingCore 1.x. **Remove the old 1.x jar** before installing 2.0, two plugins with the same name cannot run together. Plugins written against 1.x must be migrated first, see [Migration from 1.x](../reference/migration-from-1x.md).
{% endhint %}

## Requirements

| Component | Version |
| --- | --- |
| Paper | 26.3 |
| Java | 25 |

## Load order

Plugins that depend on MendingCore declare it in their `paper-plugin.yml`, so the server loads and enables MendingCore first. See [Using the API in your plugin](using-the-api.md).

## Shutdown behaviour

When MendingCore is disabled it closes every open [Gui](../gui/README.md) first. This makes sure items in [input slots](../gui/input-slots.md) are returned to the players.
