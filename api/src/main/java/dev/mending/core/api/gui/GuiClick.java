package dev.mending.core.api.gui;

import dev.mending.core.api.MendingCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Context of a click inside a {@link Gui}. The wrapped event is already cancelled.
 */
public record GuiClick(@NotNull Gui gui, @NotNull Player player, @NotNull InventoryClickEvent event) {

    /**
     * @return the clicked slot (raw slot, equals the GUI slot for clicks inside the GUI)
     */
    public int slot() {
        return this.event.getRawSlot();
    }

    public @NotNull ClickType type() {
        return this.event.getClick();
    }

    public boolean isLeftClick() {
        return this.event.getClick().isLeftClick();
    }

    public boolean isRightClick() {
        return this.event.getClick().isRightClick();
    }

    public boolean isShiftClick() {
        return this.event.getClick().isShiftClick();
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