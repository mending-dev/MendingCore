package dev.mending.core.paper.api.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Gui implements InventoryHolder {

    private Inventory inventory;
    private Component title;
    private int rows;
    private final Map<Integer, GuiIcon> items = new HashMap<>();

    protected Player player;

    /**
     * Creates a new GUI with the given title and number of rows.
     *
     * @param title The title of the GUI, displayed to the player.
     * @param rows  The number of rows in the GUI (each row contains 9 slots).
     */
    public Gui(@Nonnull Component title, @Nonnegative int rows) {
        this.title = title;
        this.rows = rows;
        this.inventory = Bukkit.createInventory(this, this.rows * 9, title);
    }

    /**
     * Updates the title of the GUI.
     *
     * @param title The new title to set for the GUI.
     */
    public void setTitle(@Nonnull Component title) {
        this.title = title;
        updateGui();
    }

    /**
     * Updates the size (number of rows) of the GUI
     *
     * @param rows The new number of rows (each row has 9 slots).
     */
    public void setRows(@Nonnegative int rows) {
        this.rows = rows;
        updateGui();
    }

    /**
     * Updates the gui and recreates the inventory.
     */
    private void updateGui() {

        Inventory oldInventory = this.inventory;
        this.inventory = Bukkit.createInventory(this, this.rows * 9, this.title);
        update();

        for (HumanEntity viewer : List.copyOf(oldInventory.getViewers())) {
            if (viewer instanceof Player player) {
                player.openInventory(this.inventory);
            }
        }
    }

    /**
     * Adds a new item to the GUI in the first available empty slot.
     *
     * @param guiIcon The GuiIcon to be added to the GUI.
     */
    public void addItem(@Nonnull GuiIcon guiIcon) {
        for (int slot = 0; slot < this.rows; slot++) {
            if (!this.items.containsKey(slot)) {
                setItem(slot, guiIcon);
                break;
            }
        }
    }

    /**
     * Sets a specific item at the given slot in the GUI.
     *
     * @param slot    The slot number where the item should be placed.
     * @param guiIcon The GuiIcon to place in the specified slot.
     */
    public void setItem(@Nonnegative int slot, @Nonnull GuiIcon guiIcon) {
        this.items.put(slot, guiIcon);
        this.inventory.setItem(slot, guiIcon.getItemStack());
    }

    public void fill(@Nonnull GuiIcon guiIcon) {
        for (int slot = 0; slot < rows * 9; slot++) {
            setItem(slot, guiIcon);
        }
    }

    public void fillSlots(@Nonnull GuiIcon guiIcon, int... slots) {
        for (int slot : slots) {
            setItem(slot, guiIcon);
        }
    }

    public void fillSlotsBetween(@Nonnull GuiIcon guiIcon, @Nonnegative int start, @Nonnegative int end) {
        for (int slot = start; slot < (end + 1); slot++ ) {
            setItem(slot, guiIcon);
        }
    }

    public void fillRows(@Nonnull GuiIcon guiIcon, @Nonnegative int... rows) {
        for (int row : rows) {
            if (row < 1 || row > this.rows) continue;
            int start = (row - 1) * 9;
            int end = start + 9;
            for (int slot = start; slot < end; slot++) {
                setItem(slot, guiIcon);
            }
        }
    }

    public void fillColumns(@Nonnull GuiIcon guiIcon, @Nonnegative int... columns) {
        for (int column : columns) {
            if (column < 1 || column > 9) continue;
            int start = column - 1;
            for (int row = 0; row < this.rows; row++) {
                int slot = row * 9 + start;
                setItem(slot, guiIcon);
            }
        }
    }

    /**
     * Clear gui items
     */
    public void clear() {
        this.items.clear();
    }

    /**
     * Called to (re)build all icons and update the inventory content.
     * Automatically called when the GUI is opened.
     */
    public abstract void update();

    /**
     * Opens the GUI for the specified player.
     *
     * @param player The player for whom the GUI will be opened.
     */
    public void open(@Nonnull Player player) {
        this.player = player;
        player.openInventory(this.inventory);
    }

    /**
     * Called when the inventory is opened. Automatically triggers an update.
     *
     * @param event The event triggered when the inventory is opened.
     */
    public void onOpen(InventoryOpenEvent event) {
        update();
    }

    /**
     * Called when the inventory is closed.
     * Can be overridden to define custom close behavior.
     *
     * @param event The event triggered when the inventory is closed.
     */
    public void onClose(InventoryCloseEvent event) {}

    /**
     * Handles click events inside the GUI, managing item interaction and actions.
     *
     * @param event The event triggered when an item is clicked in the inventory.
     */
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();

        GuiIcon icon = this.items.get(slot);
        if (icon == null) {
            event.setCancelled(true);
            return;
        }

        if (icon.isFixed()) {
            event.setCancelled(true);
        }

        if (icon.getAction() != null) {
            icon.getAction().accept(event);
        }
    }

    /**
     * Returns the inventory of the GUI.
     *
     * @return The inventory object associated with the GUI.
     */
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}