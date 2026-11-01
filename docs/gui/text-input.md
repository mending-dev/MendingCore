---
description: TextInput, asking a player for a text in a Minecraft dialog window.
---

# Text input

`TextInput` asks a player for a text in one of Minecraft's dialog windows, with a real text field. No anvil trick, no chat listener.

```java
TextInput.create(Component.text("Rename your home"))
    .label(Component.text("Name"))
    .initial(home.name())
    .maxLength(16)
    .validate(text -> text.isBlank() ? Component.text("Please enter a name.") : null)
    .onSubmit((player, text) -> homes.rename(player, home, text))
    .returnTo(this)
    .open(player);
```

Inside a click handler, open it on the next tick:

```java
GuiIcon.of(Material.NAME_TAG).onLeftClick(click -> click.open(renameInput));
```

## Options

Only the title is required. Nothing else has fixed text: the buttons say OK and Cancel in the **player's own language**, from the client, until you give them labels.

| Signature | Description |
| --- | --- |
| `static TextInput create(Component title)` | A new input. |
| `label(Component)` | The name above the field. |
| `labelVisible(boolean)` | Whether it is shown. Default: `true`. |
| `message(Component)` | A paragraph above the field. Call it again for more. |
| `initial(String)` | What the field holds at first. |
| `maxLength(int)` | The most characters. Default: 256. |
| `width(int)` | Width in pixels, 1 to 1024. Default: 200. |
| `multiline(int maxLines, int height)` | Several lines, height 1 to 512 pixels. |
| `confirmButton(Component[, Component tooltip])` | Label and tooltip of the confirm button. |
| `cancelButton(Component[, Component tooltip])` | Label and tooltip of the cancel button. |
| `closeWithEscape(boolean)` | Whether Escape closes the window. Default: `true`. |
| `timeout(Duration)` | How long the buttons work after opening. Default: 10 minutes. |

## Reacting

| Signature | Description |
| --- | --- |
| `validate(Function<String, Component>)` | Returns a message to refuse the text, `null` to accept. A refusal shows the window again with what the player typed and the message. |
| `onSubmit(BiConsumer<Player, String>)` | Gets an accepted text. Runs on the main thread. |
| `onCancel(Consumer<Player>)` | The cancel button was used. Runs on the main thread. |
| `returnTo(Gui)` | Refreshes this menu and opens it again after submit and cancel. |

{% hint style="warning" %}
The text comes from a player. Never put it into a MiniMessage string, use `Component.text(text)`.
{% endhint %}

## Full control

For everything else there is direct access to Paper's builders. They run last, so they win.

```java
TextInput.create(title)
    .base(window -> window.pause(false).externalTitle(Component.text("Rename")))
    .input(field -> field.labelVisible(false));
```

## Behaviour

* Opening closes an open inventory first, so a menu's [input slots](input-slots.md) refund their content and `onClose` runs. `returnTo` opens the menu again.
* One instance can be opened for many players, and again and again. Each opening builds its own window.
* Each button works once per opening. After `timeout` or a server restart it does nothing.
* A player who leaves or closes the window without a button triggers no callback, unless Minecraft treats Escape as cancel for this window type. Do not rely on a callback to release something.
* `open` runs on the main thread only and throws `IllegalStateException` otherwise.
