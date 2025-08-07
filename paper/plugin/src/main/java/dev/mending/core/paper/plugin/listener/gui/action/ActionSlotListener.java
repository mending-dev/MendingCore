package dev.mending.core.paper.plugin.listener.gui.action;

import dev.mending.core.paper.api.gui.Gui;
import dev.mending.core.paper.api.gui.action.ActionSlotManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.WeakHashMap;

// TODO: Combine with GuiListener
public class ActionSlotListener implements Listener {

    private static final WeakHashMap<Gui, ActionSlotManager> MANAGERS = new WeakHashMap<>();

    public static void register(Gui gui, ActionSlotManager manager) {
        MANAGERS.put(gui, manager);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        for (Gui gui : MANAGERS.keySet()) {
            if (gui.getInventory().equals(event.getInventory())) {
                MANAGERS.get(gui).handleClose(event);
                break;
            }
        }
    }
}