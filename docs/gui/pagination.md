---
description: Pagination in depth, spreading long lists over several pages.
---

# Pagination

`Pagination<T>` shows a list of items on several pages. It only maps the items of the **visible page** to icons, so large lists are cheap.

## Creating

```java
Pagination<Warp> page = gui.pagination(Slots.inner(5), warp ->
    GuiIcon.of(ItemBuilder.of(warp.icon()).name(Component.text(warp.name())))
        .onLeftClick(click -> teleport(click.player(), warp)));
```

`Gui.pagination(int[] slots, Function<? super T, GuiIcon> mapper)`

* `slots` are the slots used for items, in the order they are filled. The array is copied.
* `mapper` turns one item into an icon. It runs for every visible item on every render.
* An empty slot array throws `IllegalArgumentException`.

The pagination is registered in the GUI and renders **automatically** after your `render()` on the first open and on every `refresh()`. You never call its render yourself.

## Providing items

```java
page.setItems(warps);   // any Collection<? extends T>
```

* The collection is copied (`List.copyOf`), so later changes to your list do not affect the pagination. `null` elements are not allowed.
* The current page is clamped if the list got shorter.
* `setItems` does **not** refresh the GUI, so it is safe to call inside `render()` or the constructor.

## State

| Method | Description |
| --- | --- |
| `List<T> items()` | The current (immutable) item list. |
| `int page()` | Current page, **0-based**. |
| `Pagination<T> page(int)` | Sets the page (clamped) **without** refreshing. Use inside `render()`. |
| `int pageSize()` | Number of slots per page. |
| `int pageCount()` | Number of pages, at least 1 (an empty list has one empty page). |
| `boolean hasNext()`, `boolean hasPrevious()` | Whether there is a next or previous page. |

## Navigation

These methods change the page and **refresh the whole GUI**:

| Method | Description |
| --- | --- |
| `next()` | Next page, does nothing on the last page. |
| `previous()` | Previous page, does nothing on the first page. |
| `first()`, `last()` | Jump to the first or last page. |
| `goTo(int page)` | Jump to a page (clamped). Does nothing if it is already the current page. |

{% hint style="danger" %}
Do not call `next()`, `previous()` or `goTo()` from inside `render()`. They refresh the GUI, which calls `render()` again. Use `page(int)` there.
{% endhint %}

## Buttons

```java
page.nextIcon(ItemStack)      page.nextIcon(ItemBuilder)
page.previousIcon(ItemStack)  page.previousIcon(ItemBuilder)
```

They return a `GuiIcon` whose click handler calls `next()` or `previous()`. Show them only when they make sense:

```java
@Override
protected void render() {
    fillBorder(FILLER);
    if (page.hasPrevious()) set(5, 3, page.previousIcon(ItemBuilder.of(Material.ARROW).name("<yellow>Back")));
    if (page.hasNext())     set(5, 7, page.nextIcon(ItemBuilder.of(Material.ARROW).name("<yellow>Next")));
    set(5, 5, GuiIcon.of(ItemBuilder.of(Material.PAPER)
        .name("<gray>Page " + (page.page() + 1) + "/" + page.pageCount())));
}
```

Because `refresh()` clears all icons first, a button that is not placed in `render()` disappears automatically.

## Filtering and sorting

Keep the filter state in a field, compute the list in `render()` and call `refresh()` when the filter changes:

```java
private boolean onlyFavorites;

@Override
protected void render() {
    page.setItems(onlyFavorites ? all.stream().filter(Warp::favorite).toList() : all);
    // buttons ...
}
```

When the filter changes, reset the page so players do not land on a page that no longer exists (it would be clamped anyway):

```java
onlyFavorites = !onlyFavorites;
page.page(0);
click.refresh();
```

## Several paginations in one GUI

You can create more than one, for example a list and a category bar. Each has its own slots, page and items, and all render after `render()` in creation order. Their slots must not overlap.

## Loading data asynchronously

Never load from a database inside the mapper or `render()`. Load first, then build the GUI:

```java
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    List<Entry> entries = repository.load(player.getUniqueId());      // blocking is fine here
    Bukkit.getScheduler().runTask(plugin, () -> new EntryGui(entries).open(player));
});
```

## Rules and limits

* Pagination slots must not be [input slots](input-slots.md): rendering throws `IllegalStateException`.
* The mapper should be cheap and free of side effects. Cache heavy icons, see [Performance](performance.md).
* The pagination keeps a reference to the GUI. When you drop the GUI, the pagination is collected with it.
* `Pagination` has no public constructor, always create it with `Gui.pagination`.
