---
description: Skulls, player heads without blocking network calls.
---

# Skulls

`dev.mending.core.api.item.Skulls` creates player heads as `ItemBuilder`s, so you can keep chaining name, lore and flags.

```java
ItemStack head = Skulls.ofTexture(base64)
    .name("<gold>Trophy")
    .build();
```

## Methods

| Signature | Description |
| --- | --- |
| `ItemBuilder of(OfflinePlayer player)` | The head of a player. |
| `ItemBuilder of(UUID uuid)` | The head of the player with that UUID. |
| `ItemBuilder ofTexture(String base64)` | A head with a custom texture, given as the Base64 value of the `textures` profile property. |
| `ItemBuilder ofUrl(String textureUrl)` | A head from a full texture URL. |

## Custom textures

Most head databases give you a Base64 value:

```java
private static final String TROPHY = "eyJ0ZXh0dXJlcyI6...";

ItemBuilder trophy = Skulls.ofTexture(TROPHY);
```

From a texture URL:

```java
Skulls.ofUrl("http://textures.minecraft.net/texture/<hash>");
```

`ofUrl` wraps the URL into the textures JSON and Base64-encodes it. The URL is inserted as it is, so **never pass untrusted input** to it.

## Stacking

Heads with the same texture are identical: `ofTexture` derives a stable UUID from the Base64 value. They stack and compare equal with `isSimilar`.

## No blocking calls

Earlier versions called `PlayerProfile.complete()`, which performs a network request to Mojang's servers and blocks the main thread. `Skulls` never does that:

* `ofTexture` and `ofUrl` need no lookup at all.
* `of(OfflinePlayer)` and `of(UUID)` only set the owner, the skin is resolved lazily.

## Performance

Creating a profile for a texture is cheap but not free. Store heads that are used repeatedly (menu icons) in `static final` fields or [shared icons](../gui/performance.md).

## Use with GUIs

```java
GuiIcon.of(Skulls.of(target).name(Component.text(target.getName())))
```
