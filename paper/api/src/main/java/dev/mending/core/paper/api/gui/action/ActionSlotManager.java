package dev.mending.core.paper.api.gui.action;

import dev.mending.core.paper.api.gui.Gui;
import dev.mending.core.paper.api.gui.GuiIcon;
import dev.mending.core.paper.api.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ActionSlotManager {

    private final Gui gui;
    private final Map<Integer, ActionSlot> slots = new HashMap<>();

    public ActionSlotManager(Gui gui) {
        this.gui = gui;
    }

    public ActionSlot register(int slot, ItemStack itemStack) {
        GuiIcon icon = new GuiIcon(itemStack).onClick(event -> {
            ActionSlot actionSlot = slots.get(slot);
            if (actionSlot == null) return;

            ItemStack cursor = event.getCursor();
            if (!isNullOrAir(cursor)) {
                if (actionSlot.getPrePutClickAction().test(event, cursor)) return;

                ItemStack toPlace = cursor.clone();
                if (event.isRightClick() && cursor.getAmount() > 1) {
                    cursor.setAmount(cursor.getAmount() - 1);
                    toPlace.setAmount(1);
                    event.setCursor(cursor);
                } else {
                    event.setCursor(null);
                }

                setItem(slot, toPlace);
                actionSlot.getPutAction().accept(event, toPlace);

            } else {
                ItemStack current = gui.getInventory().getItem(slot);
                if (isNullOrAir(current)) return;
                if (actionSlot.getPrePickupClickAction().test(event, current)) return;

                event.setCursor(current);
                gui.setItem(slot, actionSlot.getDisplayIcon());
                actionSlot.getPickupAction().accept(event, current);
            }
        });

        gui.setItem(slot, icon);
        ActionSlot actionSlot = new ActionSlot(slot, icon);
        slots.put(slot, actionSlot);
        return actionSlot;
    }

    public ActionSlot register(int slot, ItemBuilder itemBuilder) {
        return this.register(slot, itemBuilder.build());
    }

    public void handleClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inv = event.getInventory();

        for (Map.Entry<Integer, ActionSlot> entry : slots.entrySet()) {
            ActionSlot slot = entry.getValue();
            if (!slot.isRefundOnClose()) continue;

            ItemStack item = inv.getItem(entry.getKey());
            if (item != null && !item.isSimilar(slot.getDisplayIcon().getItemStack())) {
                HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
                leftovers.values().forEach(drop -> player.getWorld().dropItemNaturally(player.getLocation(), drop));
            }
        }
    }

    public void handleShiftClick(InventoryClickEvent event) {
        final ItemStack itemStack = event.getCurrentItem();
        if (isNullOrAir(itemStack)) return;

        final int rawSlot = event.getRawSlot();
        final Player player = (Player) event.getWhoClicked();
        final Inventory inventory = gui.getInventory();

        if (rawSlot >= inventory.getSize()) {
            for (Map.Entry<Integer, ActionSlot> entry : slots.entrySet()) {
                int slot = entry.getKey();
                ActionSlot actionSlot = entry.getValue();

                ItemStack current = inventory.getItem(slot);

                if (isNullOrAir(current) || current.isSimilar(actionSlot.getDisplayIcon().getItemStack())) {
                    if (actionSlot.getPrePutClickAction().test(event, itemStack)) {
                        return;
                    }

                    setItem(slot, itemStack);
                    actionSlot.getPutAction().accept(event, itemStack);
                    event.setCurrentItem(null);
                    return;
                }
            }

        } else {
            ActionSlot actionSlot = slots.get(rawSlot);
            if (actionSlot == null) return;

            ItemStack itemInGui = inventory.getItem(rawSlot);
            if (isNullOrAir(itemInGui)) return;

            if (actionSlot.getPrePickupClickAction().test(event, itemInGui)) {
                return;
            }

            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(itemInGui);
            if (!leftovers.isEmpty()) {
                return;
            }

            setItem(rawSlot, actionSlot.getDisplayIcon().getItemStack());
            actionSlot.getPickupAction().accept(event, itemInGui);
        }
    }

    private void setItem(int slot, ItemStack item) {
        gui.getInventory().setItem(slot, item);
    }

    public Collection<ActionSlot> getSlots() {
        return slots.values();
    }

    private boolean isNullOrAir(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }
}