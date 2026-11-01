---
description: Bedrock, telling Bedrock players from Java players by their UUID.
---

# Bedrock

`dev.mending.core.api.util.Bedrock` tells whether a player plays on Bedrock. It needs neither Floodgate nor Geyser as a dependency.

```java
if (Bedrock.isBedrock(player)) {
    player.sendMessage(Component.text("Hello Bedrock!"));
}
```

## Methods

| Signature | Description |
| --- | --- |
| `boolean isBedrock(UUID uuid)` | Whether this is a UUID Floodgate gives a Bedrock player. |
| `boolean isBedrock(OfflinePlayer player)` | The same for a player, online or offline. |

## How it works

Floodgate builds the UUID of a Bedrock player from a zero upper half and the Xbox id, for example `00000000-0000-0000-0009-01f2ef6ee2db`. Java accounts and offline-mode players never have such a UUID.

## Notes

* A Bedrock player who linked a Java account plays under that account, so the result is `false`.
* Without Floodgate, Geyser players log in with a Java account, so the result is `false` as well.
