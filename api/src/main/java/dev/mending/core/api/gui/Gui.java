package dev.mending.core.api.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.DragType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A chest-style menu. Override {@link #render()} (or pass a lambda to {@link #create(Component, int, Consumer)})
 * to place icons; call {@link #refresh()} whenever the content should be rebuilt.
 *
 * Everything inside a GUI is locked by default. Players can only interact through {@link GuiIcon} handlers
 * and {@link InputSlot}s. Slots are 0-based, rows and columns are 1-based. All methods must be called on the
 * main thread.
 *
 * One instance can be shown to several players at once; they then share the same content. For per-player
 * content create one instance per player.
 */
public class Gui implements InventoryHolder {

    public static final int COLUMNS = 9;
    public static final int MAX_ROWS = 6;

    private final List<Pagination<?>> paginations = new ArrayList<>();
    private final Set<UUID> transitioning = new HashSet<>();

    private Component title;
    private int rows;
    private Inventory inventory;
    private GuiIcon[] icons;
    private InputSlot[] inputs;
    private Consumer<Gui> renderer;
    private boolean rendered;
    private boolean lockPlayerInventory;

    // Safe: Bukkit.createInventory only stores the holder and never calls methods on it.
    @SuppressWarnings("this-escape")
    public Gui(@NotNull Component title, int rows) {
        this.title = Objects.requireNonNull(title, "title");
        this.rows = checkRows(rows);
        this.icons = new GuiIcon[this.rows * COLUMNS];
        this.inputs = new InputSlot[this.rows * COLUMNS];
        this.inventory = Bukkit.createInventory(this, this.icons.length, title);
    }

    /**
     * @param title the title in MiniMessage format
     */
    public Gui(@NotNull String title, int rows) {
        this(MiniMessage.miniMessage().deserialize(title), rows);
    }

    public static @NotNull Gui create(@NotNull Component title, int rows) {
        return new Gui(title, rows);
    }

    public static @NotNull Gui create(@NotNull Component title, int rows, @NotNull Consumer<Gui> renderer) {
        return new Gui(title, rows).onRender(renderer);
    }

    // LIFECYCLE

    /**
     * Places the icons. Called on the first open and on every {@link #refresh()}.
     */
    protected void render() {
        if (this.renderer != null) {
            this.renderer.accept(this);
        }
    }

    /**
     * Called after the GUI was opened for a player.
     */
    protected void onOpen(@NotNull Player player) {
    }

    /**
     * Called when a player closed the GUI, before input slot contents are refunded. Remove an input
     * slot's content here (e.g. {@link InputSlot#clear()}) if you consumed it yourself.
     */
    protected void onClose(@NotNull Player player) {
    }

    /**
     * Sets the render function (alternative to overriding {@link #render()}).
     */
    public @NotNull Gui onRender(@NotNull Consumer<Gui> renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        return this;
    }

    // OPENING / REFRESHING

    public void open(@NotNull Player player) {
        ensureMainThread();
        ensureRendered();
        player.openInventory(this.inventory);
    }

    /**
     * Removes all icons (input slots keep their content) and runs {@link #render()} again.
     */
    public void refresh() {
        ensureMainThread();
        clearIcons();
        renderAll();
    }

    public void close(@NotNull Player player) {
        if (this.inventory.getViewers().contains(player)) {
            player.closeInventory();
        }
    }

    public void closeAll() {
        for (Player viewer : viewers()) {
            viewer.closeInventory();
        }
    }

    public @NotNull List<Player> viewers() {
        return viewersOf(this.inventory);
    }

    // TITLE & SIZE

    public @NotNull Component title() {
        return this.title;
    }

    /**
     * Changes the title. Viewers get the inventory reopened (the client cannot rename an open window).
     */
    public @NotNull Gui title(@NotNull Component title) {
        ensureMainThread();
        this.title = Objects.requireNonNull(title, "title");
        rebuildInventory();
        return this;
    }

    public int rows() {
        return this.rows;
    }

    public int size() {
        return this.icons.length;
    }

    /**
     * Changes the number of rows. Icons and input slots beyond the new size are dropped (input contents are
     * refunded to a viewer). Viewers get the inventory reopened.
     */
    public @NotNull Gui rows(int rows) {
        ensureMainThread();
        checkRows(rows);
        if (rows == this.rows) {
            return this;
        }
        final int newSize = rows * COLUMNS;

        final Player viewer = firstViewer();
        for (int i = newSize; i < this.inputs.length; i++) {
            if (this.inputs[i] != null && viewer != null) {
                this.inputs[i].refund(viewer);
            }
        }

        this.rows = rows;
        this.icons = Arrays.copyOf(this.icons, newSize);
        this.inputs = Arrays.copyOf(this.inputs, newSize);
        rebuildInventory();
        return this;
    }

    // ICONS

    public @NotNull Gui set(int slot, @Nullable GuiIcon icon) {
        checkSlot(slot);
        if (this.inputs[slot] != null) {
            throw new IllegalStateException("Slot " + slot + " is an input slot");
        }
        this.icons[slot] = icon;
        this.inventory.setItem(slot, icon == null ? null : icon.item());
        return this;
    }

    /**
     * Sets an icon by 1-based row and column.
     */
    public @NotNull Gui set(int row, int column, @Nullable GuiIcon icon) {
        return set(Slots.slot(row, column), icon);
    }

    public @NotNull Gui remove(int slot) {
        return set(slot, null);
    }

    public @Nullable GuiIcon icon(int slot) {
        checkSlot(slot);
        return this.icons[slot];
    }

    /**
     * Puts the icon into the first free slot.
     *
     * @return false if the GUI is full
     */
    public boolean add(@NotNull GuiIcon icon) {
        for (int slot = 0; slot < this.icons.length; slot++) {
            if (this.icons[slot] == null && this.inputs[slot] == null) {
                set(slot, icon);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes all icons. Input slots are not affected.
     */
    public @NotNull Gui clear() {
        clearIcons();
        return this;
    }

    /** Fills every slot (input slots are skipped), overwriting existing icons. */
    public @NotNull Gui fill(@NotNull GuiIcon icon) {
        for (int slot = 0; slot < this.icons.length; slot++) {
            paint(slot, icon);
        }
        return this;
    }

    /** Fills only slots that have neither an icon nor an input slot. */
    public @NotNull Gui fillEmpty(@NotNull GuiIcon icon) {
        for (int slot = 0; slot < this.icons.length; slot++) {
            if (this.icons[slot] == null) {
                paint(slot, icon);
            }
        }
        return this;
    }

    public @NotNull Gui fillSlots(@NotNull GuiIcon icon, int... slots) {
        for (int slot : slots) {
            paint(slot, icon);
        }
        return this;
    }

    /** Fills all slots from {@code from} to {@code to}, both inclusive. */
    public @NotNull Gui fillRange(@NotNull GuiIcon icon, int from, int to) {
        return fillSlots(icon, Slots.range(from, to));
    }

    /** Fills 1-based rows. */
    public @NotNull Gui fillRows(@NotNull GuiIcon icon, int... rows) {
        for (int row : rows) {
            if (row > this.rows) {
                throw new IllegalArgumentException("row " + row + " does not exist in a " + this.rows + "-row GUI");
            }
            fillSlots(icon, Slots.row(row));
        }
        return this;
    }

    /** Fills 1-based columns. */
    public @NotNull Gui fillColumns(@NotNull GuiIcon icon, int... columns) {
        for (int column : columns) {
            fillSlots(icon, Slots.column(column, this.rows));
        }
        return this;
    }

    public @NotNull Gui fillBorder(@NotNull GuiIcon icon) {
        return fillSlots(icon, Slots.border(this.rows));
    }

    // INPUT SLOTS / PAGINATION

    /**
     * Returns the input slot at the given position, creating it if necessary. An icon at that slot is removed.
     */
    public @NotNull InputSlot input(int slot) {
        checkSlot(slot);
        InputSlot input = this.inputs[slot];
        if (input == null) {
            this.icons[slot] = null;
            input = new InputSlot(this, slot);
            this.inputs[slot] = input;
            input.render();
        }
        return input;
    }

    public @Nullable InputSlot getInput(int slot) {
        checkSlot(slot);
        return this.inputs[slot];
    }

    /**
     * Creates a pagination that fills the given slots. It is rendered automatically after {@link #render()}.
     */
    public <T> @NotNull Pagination<T> pagination(int[] slots, @NotNull Function<? super T, GuiIcon> mapper) {
        final Pagination<T> pagination = new Pagination<>(this, slots, mapper);
        this.paginations.add(pagination);
        return pagination;
    }

    // SETTINGS

    /**
     * If true, players cannot click in their own inventory while this GUI is open. Default: false.
     * Shift clicks into input slots keep working.
     */
    public @NotNull Gui lockPlayerInventory(boolean lock) {
        this.lockPlayerInventory = lock;
        return this;
    }

    public boolean isPlayerInventoryLocked() {
        return this.lockPlayerInventory;
    }

    // INVENTORY HOLDER

    @Override
    public @NotNull Inventory getInventory() {
        ensureRendered();
        return this.inventory;
    }

    Inventory rawInventory() {
        return this.inventory;
    }

    // EVENT ENTRY POINTS (called by MendingCore)

    @ApiStatus.Internal
    public void handleClick(@NotNull InventoryClickEvent event) {
        if (event.getInventory().getSize() != this.icons.length) {
            event.setCancelled(true); // stale inventory (size changed)
            return;
        }
        final int raw = event.getRawSlot();
        if (raw < 0) {
            return; // clicked outside the window: vanilla behaviour (drops the cursor item)
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }

        if (raw >= this.icons.length) {
            // The player's own inventory is not the GUI's: what another plugin forbade stays forbidden.
            if (!event.isCancelled()) {
                handlePlayerInventoryClick(event, player);
            }
            return;
        }

        event.setCancelled(true);
        if (event.getClick() == ClickType.DOUBLE_CLICK) {
            return; // already preceded by two single clicks
        }

        final GuiClick click = new GuiClick(this, player, event.getRawSlot(), event.getClick(), event);
        final InputSlot input = this.inputs[raw];
        if (input != null) {
            input.handle(click);
            return;
        }
        final GuiIcon icon = this.icons[raw];
        if (icon != null) {
            icon.handle(click);
        }
    }

    @ApiStatus.Internal
    public void handleDrag(@NotNull InventoryDragEvent event) {
        final int size = event.getInventory().getSize();
        boolean touchesGui = false;
        boolean onlyInputs = true;
        for (int raw : event.getRawSlots()) {
            if (raw < size) {
                touchesGui = true;
                onlyInputs &= this.inputs[raw] != null;
            } else {
                onlyInputs = false;
            }
        }
        if (!touchesGui) {
            return; // only the player's own inventory
        }
        if (!onlyInputs || !(event.getWhoClicked() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }
        applyDrag(event, player);
    }

    @ApiStatus.Internal
    public void handleOpen(@NotNull Player player) {
        if (this.transitioning.contains(player.getUniqueId())) {
            return;
        }
        onOpen(player);
    }

    @ApiStatus.Internal
    public void handleClose(@NotNull Player player) {
        if (this.transitioning.contains(player.getUniqueId())) {
            return;
        }
        onClose(player);
        // The closing player is still listed as a viewer while the event runs.
        if (this.inventory.getViewers().size() <= 1) {
            for (InputSlot input : this.inputs) {
                if (input != null) {
                    input.refund(player);
                }
            }
        }
    }

    // INTERNALS

    /**
     * A drag over input slots only. All or nothing: the server writes the slots and the cursor from the event, so a drag
     * that one slot refuses is cancelled as a whole. Each slot counts as one click for the callbacks.
     */
    private void applyDrag(InventoryDragEvent event, Player player) {
        final ClickType type = event.getType() == DragType.SINGLE ? ClickType.RIGHT : ClickType.LEFT;
        final Map<Integer, GuiClick> clicks = new HashMap<>();
        event.setCancelled(false);
        for (Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet()) {
            final GuiClick click = new GuiClick(this, player, entry.getKey(), type, event);
            if (!this.inputs[entry.getKey()].accepts(click, entry.getValue())) {
                event.setCancelled(true);
                return;
            }
            clicks.put(entry.getKey(), click);
        }
        clicks.forEach((slot, click) -> this.inputs[slot].applyDragged(click, event.getNewItems().get(slot)));
    }

    private void handlePlayerInventoryClick(InventoryClickEvent event, Player player) {
        if (event.getClick().isShiftClick()) {
            event.setCancelled(true);
            shiftIntoInputs(event, player);
            return;
        }
        // Double click collects matching items from the GUI as well, so it must never pass.
        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR || this.lockPlayerInventory) {
            event.setCancelled(true);
        }
    }

    private void shiftIntoInputs(InventoryClickEvent event, Player player) {
        final ItemStack current = event.getCurrentItem();
        if (current == null || current.isEmpty()) {
            return;
        }
        final GuiClick click = new GuiClick(this, player, event.getRawSlot(), event.getClick(), event);
        final ItemStack remaining = current.clone();
        for (InputSlot input : this.inputs) {
            if (input == null) {
                continue;
            }
            final int moved = input.insert(click, remaining);
            if (moved <= 0) {
                continue;
            }
            remaining.setAmount(remaining.getAmount() - moved);
            if (remaining.getAmount() <= 0) {
                break;
            }
        }
        if (remaining.getAmount() != current.getAmount()) {
            event.setCurrentItem(remaining.getAmount() <= 0 ? null : remaining);
        }
    }

    private void ensureRendered() {
        if (!this.rendered) {
            renderAll();
        }
    }

    private void renderAll() {
        this.rendered = true;
        render();
        for (Pagination<?> pagination : this.paginations) {
            pagination.render();
        }
    }

    private void clearIcons() {
        for (int slot = 0; slot < this.icons.length; slot++) {
            if (this.icons[slot] != null) {
                this.icons[slot] = null;
                this.inventory.setItem(slot, null);
            }
        }
    }

    private void rebuildInventory() {
        final List<Player> viewers = viewersOf(this.inventory);
        this.inventory = Bukkit.createInventory(this, this.icons.length, this.title);

        for (int slot = 0; slot < this.icons.length; slot++) {
            if (this.inputs[slot] != null) {
                this.inputs[slot].render();
            } else if (this.icons[slot] != null) {
                this.inventory.setItem(slot, this.icons[slot].item());
            }
        }

        for (Player viewer : viewers) {
            // Reopening fires close/open events for the old inventory; they must not trigger hooks or refunds.
            this.transitioning.add(viewer.getUniqueId());
            try {
                viewer.openInventory(this.inventory);
            } finally {
                this.transitioning.remove(viewer.getUniqueId());
            }
        }
    }

    private void paint(int slot, GuiIcon icon) {
        checkSlot(slot);
        if (this.inputs[slot] == null) {
            set(slot, icon);
        }
    }

    private @Nullable Player firstViewer() {
        for (HumanEntity viewer : this.inventory.getViewers()) {
            if (viewer instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    private static List<Player> viewersOf(Inventory inventory) {
        final List<Player> players = new ArrayList<>();
        for (HumanEntity viewer : inventory.getViewers()) {
            if (viewer instanceof Player player) {
                players.add(player);
            }
        }
        return players;
    }

    private void checkSlot(int slot) {
        Objects.checkIndex(slot, this.icons.length);
    }

    private static int checkRows(int rows) {
        if (rows < 1 || rows > MAX_ROWS) {
            throw new IllegalArgumentException("rows must be between 1 and " + MAX_ROWS + ", was " + rows);
        }
        return rows;
    }

    private static void ensureMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("GUIs must only be used on the main thread");
        }
    }
}