package dev.mending.core.paper.api.gui.action;

import dev.mending.core.paper.api.gui.GuiIcon;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class ActionSlot {

    private final int slot;
    private final GuiIcon displayIcon;

    private boolean refundOnClose = true;

    private BiPredicate<InventoryClickEvent, ItemStack> prePutClickAction = (e, item) -> false;
    private BiPredicate<InventoryClickEvent, ItemStack> prePickupClickAction = (e, item) -> false;

    private BiConsumer<InventoryClickEvent, ItemStack> putAction = (e, item) -> {};
    private BiConsumer<InventoryClickEvent, ItemStack> pickupAction = (e, item) -> {};

    public ActionSlot(int slot, GuiIcon displayIcon) {
        this.slot = slot;
        this.displayIcon = displayIcon;
    }

    public int getSlot() {
        return slot;
    }

    public GuiIcon getDisplayIcon() {
        return displayIcon;
    }

    public boolean isRefundOnClose() {
        return refundOnClose;
    }

    public void setRefundOnClose(boolean refundOnClose) {
        this.refundOnClose = refundOnClose;
    }

    public ActionSlot onPrePutClick(BiPredicate<InventoryClickEvent, ItemStack> prePut) {
        this.prePutClickAction = Objects.requireNonNull(prePut);
        return this;
    }

    public ActionSlot onPut(BiConsumer<InventoryClickEvent, ItemStack> putAction) {
        this.putAction = Objects.requireNonNull(putAction);
        return this;
    }

    public ActionSlot onPrePickupClick(BiPredicate<InventoryClickEvent, ItemStack> prePickup) {
        this.prePickupClickAction = Objects.requireNonNull(prePickup);
        return this;
    }

    public ActionSlot onPickup(BiConsumer<InventoryClickEvent, ItemStack> pickupAction) {
        this.pickupAction = Objects.requireNonNull(pickupAction);
        return this;
    }

    public BiPredicate<InventoryClickEvent, ItemStack> getPrePutClickAction() {
        return prePutClickAction;
    }

    public BiPredicate<InventoryClickEvent, ItemStack> getPrePickupClickAction() {
        return prePickupClickAction;
    }

    public BiConsumer<InventoryClickEvent, ItemStack> getPutAction() {
        return putAction;
    }

    public BiConsumer<InventoryClickEvent, ItemStack> getPickupAction() {
        return pickupAction;
    }
}