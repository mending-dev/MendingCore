---
description: Slot numbering, coordinates, the Slots helper and the fill methods.
---

# Slots and layouts

## Numbering

Slots are numbered from 0, row by row, left to right. For a 3-row GUI:

```
 0  1  2  3  4  5  6  7  8
 9 10 11 12 13 14 15 16 17
18 19 20 21 22 23 24 25 26
```

Rows and columns are **1-based**: row 2, column 5 is slot 13.

```java
gui.set(13, icon);      // slot
gui.set(2, 5, icon);    // row 2, column 5, same position
int slot = Slots.slot(2, 5);   // 13
```

## Slots

`dev.mending.core.api.gui.Slots` builds `int[]` arrays. Every call returns a new array.

| Signature | Description |
| --- | --- |
| `int slot(int row, int column)` | Slot index of a 1-based position. Throws `IllegalArgumentException` outside 1-6 / 1-9. |
| `int[] range(int from, int to)` | All slots from `from` to `to`, both inclusive, bounds in any order. |
| `int[] row(int row)` | All 9 slots of a 1-based row. |
| `int[] column(int column, int rows)` | All slots of a 1-based column in a GUI with `rows` rows. |
| `int[] border(int rows)` | The outer frame. |
| `int[] inner(int rows)` | Everything except the frame. Empty for fewer than 3 rows. |

For a 4-row GUI:

```
border            inner
# # # # # # # # #   . . . . . . . . .
# . . . . . . . #   . # # # # # # # .
# . . . . . . . #   . # # # # # # # .
# # # # # # # # #   . . . . . . . . .
```

## Filling

```java
GuiIcon filler = GuiIcon.of(ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()));

gui.fillBorder(filler);                 // frame
gui.fillRows(filler, 1, 6);             // rows 1 and 6
gui.fillColumns(filler, 1, 9);          // columns 1 and 9
gui.fillRange(filler, 0, 8);            // slots 0 to 8
gui.fillSlots(filler, 4, 13, 22);       // single slots
gui.fillEmpty(filler);                  // everything still free
gui.fill(filler);                       // everything, overwrites
```

| Method | Overwrites icons | Skips input slots |
| --- | --- | --- |
| `fill`, `fillSlots`, `fillRange`, `fillRows`, `fillColumns`, `fillBorder` | yes | yes |
| `fillEmpty` | no | yes |

### Typical order

Place the important icons first, then `fillEmpty` for the background. Or paint the background first and place icons on top, they overwrite it.

```java
@Override
protected void render() {
    fillBorder(FILLER);
    set(2, 5, mainButton());
    fillEmpty(FILLER);   // everything else
}
```

## Constants for slots

Give slots names instead of scattering numbers:

```java
private static final int INPUT = 11;
private static final int OUTPUT = 15;
private static final int CLOSE = Slots.slot(3, 5);
```

## Hot paths

`Slots.*` allocates an array on every call. If you use the same slots in every `render()`, keep them in a `static final int[]`:

```java
private static final int[] CONTENT = Slots.inner(5);
```
