package dev.mending.core.paper.plugin.listener.gui;

import dev.mending.core.paper.api.gui.Gui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Gui gui) {

            if (event.isShiftClick()) {

                event.setCancelled(true);

                if (gui.hasActionSlots()) {
                    gui.getActionSlotManager().handleShiftClick(event);
                    return;
                }
            }

            if (event.getRawSlot() > (event.getInventory().getSize() - 1)) {
                return;
            }

            gui.handleClick(event);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {

        if (!(event.getInventory().getHolder() instanceof Gui gui)) return;

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < gui.getInventory().getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof Gui gui) {
            gui.onOpen(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Gui gui) {
            gui.onClose(event);
        }
    }
}
