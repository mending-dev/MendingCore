---
description: Complete, copy-ready GUI examples.
---

# Recipes

All examples assume the usual imports: `net.kyori.adventure.text.Component`, `org.bukkit.Material`, `org.bukkit.entity.Player` the Bukkit types used (`ItemStack`, `Enchantment`, ...) and the MendingCore classes (`Gui`, `GuiIcon`, `GuiClick`, `InputSlot`, `Pagination`, `Slots`, `ItemBuilder`, `Skulls`, `InventoryUtils`).

## Shared icons

```java
public final class Icons {

    public static final GuiIcon FILLER =
        GuiIcon.of(ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()));

    private Icons() {}
}
```

## Confirm dialog

A reusable yes/no menu.

```java
public final class ConfirmGui extends Gui {

    private final Runnable onConfirm;
    private final Gui parent;   // may be null

    public ConfirmGui(Component question, Runnable onConfirm, Gui parent) {
        super(question, 3);
        this.onConfirm = onConfirm;
        this.parent = parent;
    }

    @Override
    protected void render() {
        fill(Icons.FILLER);
        set(2, 4, GuiIcon.of(ItemBuilder.of(Material.LIME_CONCRETE).name("<green>Confirm"))
            .onLeftClick(click -> {
                this.onConfirm.run();
                click.close();
            }));
        set(2, 6, GuiIcon.of(ItemBuilder.of(Material.RED_CONCRETE).name("<red>Cancel"))
            .onLeftClick(click -> {
                if (this.parent != null) click.open(this.parent);
                else click.close();
            }));
    }
}
```

Usage:

```java
new ConfirmGui(Component.text("Delete home?"), () -> homes.delete(player, "base"), this).open(player);
```

## Paginated player list

```java
public final class PlayerListGui extends Gui {

    private static final int[] CONTENT = Slots.inner(5);

    private final Pagination<Player> page = pagination(CONTENT, target ->
        GuiIcon.of(Skulls.of(target).name(Component.text(target.getName())))
            .onLeftClick(click -> click.player().sendMessage(Component.text("Selected " + target.getName()))));

    public PlayerListGui() {
        super(Component.text("Online players"), 5);
        this.page.setItems(Bukkit.getOnlinePlayers());
    }

    @Override
    protected void render() {
        fillBorder(Icons.FILLER);
        if (page.hasPrevious()) set(5, 3, page.previousIcon(ItemBuilder.of(Material.ARROW).name("<yellow>Previous")));
        if (page.hasNext())     set(5, 7, page.nextIcon(ItemBuilder.of(Material.ARROW).name("<yellow>Next")));
        set(5, 5, GuiIcon.of(ItemBuilder.of(Material.PAPER)
            .name("<gray>Page " + (page.page() + 1) + " / " + page.pageCount())));
    }
}
```

The player names are placed with `Component.text`, never concatenated into MiniMessage.

## Toggle with state

```java
public final class SettingsGui extends Gui {

    private boolean notifications = true;
    private boolean sounds = true;

    public SettingsGui() {
        super(Component.text("Settings"), 3);
    }

    @Override
    protected void render() {
        fill(Icons.FILLER);
        set(2, 4, toggle("Notifications", notifications, () -> notifications = !notifications));
        set(2, 6, toggle("Sounds", sounds, () -> sounds = !sounds));
    }

    private GuiIcon toggle(String label, boolean on, Runnable flip) {
        return GuiIcon.of(ItemBuilder.of(on ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(Component.text(label))
                .lore(on ? "<green>Enabled" : "<red>Disabled", "", "<gray>Click to toggle"))
            .onLeftClick(click -> {
                flip.run();
                click.refresh();
            });
    }
}
```

## Navigation with a back button

```java
public final class MainMenu extends Gui {

    public MainMenu() {
        super(Component.text("Menu"), 3);
    }

    @Override
    protected void render() {
        fill(Icons.FILLER);
        set(2, 5, GuiIcon.of(ItemBuilder.of(Material.COMPARATOR).name("<yellow>Settings"))
            .onLeftClick(click -> click.open(new SettingsGui())));
    }
}
```

For a back button pass the parent into the child, see [Best practices](best-practices.md).

## Upgrade station with input slot and output

The player puts a sword in, sees the result and takes it by clicking the output.

```java
public final class UpgradeGui extends Gui {

    private static final int INPUT = 11;
    private static final int OUTPUT = 15;

    public UpgradeGui() {
        super(Component.text("Upgrade"), 3);

        input(INPUT)
            .placeholder(ItemBuilder.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                .name("<gray>Put your sword here").build())
            .maxStackSize(1)
            .filter(item -> item.getType().name().endsWith("_SWORD"))
            .onChange(GuiClick::refresh);           // redraw the output when the input changes
    }

    @Override
    protected void render() {
        fillBorder(Icons.FILLER);

        ItemStack sword = input(INPUT).content();
        if (sword == null) {
            set(OUTPUT, GuiIcon.of(ItemBuilder.of(Material.BARRIER).name("<red>Nothing to upgrade")));
            return;
        }

        ItemStack result = ItemBuilder.of(sword).enchant(Enchantment.SHARPNESS, 5).build();
        set(OUTPUT, GuiIcon.of(result).onLeftClick(click -> {
            InputSlot slot = input(INPUT);
            if (slot.isEmpty()) return;             // re-check at click time
            slot.clear();                            // consume the input
            InventoryUtils.giveOrDrop(click.player(), result);
            click.refresh();
        }));
    }
}
```

Closing the GUI with a sword still in the input slot returns it to the player automatically.

## Shop with currency check

```java
GuiIcon.of(ItemBuilder.of(Material.DIAMOND).name("<aqua>Diamond").lore("<gray>Price: <gold>100"))
    .onLeftClick(click -> {
        Player player = click.player();
        if (!economy.has(player, 100)) {
            player.sendMessage(Component.text("Not enough money."));
            return;
        }
        economy.withdraw(player, 100);          // change state first
        InventoryUtils.giveOrDrop(player, new ItemStack(Material.DIAMOND));
        click.refresh();
    });
```

## Live updates for open GUIs

```java
public final class StatsGui extends Gui {

    private static final Map<UUID, StatsGui> OPEN = new HashMap<>();

    public StatsGui() {
        super(Component.text("Stats"), 3);
    }

    @Override protected void onOpen(Player player)  { OPEN.put(player.getUniqueId(), this); }
    @Override protected void onClose(Player player) { OPEN.remove(player.getUniqueId(), this); }

    @Override
    protected void render() {
        set(2, 5, GuiIcon.of(Material.PAPER));   // read current stats here
    }

    /** Call from the main thread whenever stats change. */
    public static void refreshAll() {
        for (StatsGui gui : List.copyOf(OPEN.values())) {
            gui.refresh();
        }
    }
}
```

`onClose` also runs on disconnect, so the map never leaks. The copy protects against changes to the map while refreshing.

## Async data, sync GUI

```java
public static void open(Plugin plugin, Player player) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        List<Entry> entries = repository.load(player.getUniqueId());   // blocking is fine here
        Bukkit.getScheduler().runTask(plugin, () -> new EntryGui(entries).open(player));
    });
}
```

{% hint style="info" %}
Pass a lambda with explicit parentheses (`() -> { ... }`) to the scheduler. A method reference such as `player::closeInventory` is ambiguous between `Runnable` and `Consumer<BukkitTask>` and does not compile.
{% endhint %}
