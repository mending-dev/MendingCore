package dev.mending.core.api.gui;

import dev.mending.core.api.MendingCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Context of a click inside a {@link Gui}.
 *
 * <p>A drag over {@linkplain InputSlot input slots} counts as one click per slot: left for spreading a stack, right for
 * placing one item each, and {@link #isDrag()} says so.</p>
 *
 * @param gui the GUI the click happened in
 * @param player the player who clicked
 * @param slot the clicked slot (raw slot, equals the GUI slot for clicks inside the GUI)
 * @param type the click type
 * @param event the raw event: an {@link InventoryClickEvent}, already cancelled, or for a drag the
 *     {@link InventoryDragEvent}, which is not. Do not change whether it is cancelled.
 */
public record GuiClick(@NotNull Gui gui, @NotNull Player player, int slot, @NotNull ClickType type,
                       @NotNull InventoryInteractEvent event) {

    /**
     * @return whether this is one slot of a drag rather than a click
     */
    public boolean isDrag() {
        return this.event instanceof InventoryDragEvent;
    }

    public boolean isLeftClick() {
        return this.type.isLeftClick();
    }

    public boolean isRightClick() {
        return this.type.isRightClick();
    }

    public boolean isShiftClick() {
        return this.type.isShiftClick();
    }

    /**
     * Re-renders the GUI this click happened in.
     */
    public void refresh() {
        this.gui.refresh();
    }

    /**
     * Closes the GUI of the player. Runs next tick, which is safe inside click handlers.
     */
    public void close() {
        final Player target = this.player;
        Bukkit.getScheduler().runTask(MendingCore.get().plugin(), () -> target.closeInventory());
    }

    /**
     * Opens another GUI for the player (e.g. a back button). Runs next tick, which is safe inside click handlers.
     */
    public void open(@NotNull Gui other) {
        final Player target = this.player;
        Bukkit.getScheduler().runTask(MendingCore.get().plugin(), () -> other.open(target));
    }
}