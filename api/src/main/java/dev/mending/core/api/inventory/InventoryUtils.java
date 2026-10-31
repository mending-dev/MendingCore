package dev.mending.core.api.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InventoryUtils {

    private InventoryUtils() {
        throw new UnsupportedOperationException("This is a static utility class");
    }

    /**
     * @return true if the item is null, air or has an amount of 0
     */
    public static boolean isEmpty(@Nullable ItemStack item) {
        return item == null || item.isEmpty();
    }

    /**
     * Converts a contents array into a map of slot to item. Empty slots are left out.
     */
    public static @NotNull Map<Integer, ItemStack> toMap(@Nullable ItemStack[] contents) {
        final Map<Integer, ItemStack> map = new HashMap<>();
        if (contents == null) {
            return map;
        }
        for (int slot = 0; slot < contents.length; slot++) {
            if (!isEmpty(contents[slot])) {
                map.put(slot, contents[slot]);
            }
        }
        return map;
    }

    /**
     * Builds a contents array of the given size from a map of slot to item.
     *
     * @throws IllegalArgumentException if a slot does not fit into the array
     */
    public static @NotNull ItemStack[] toArray(@NotNull Map<Integer, ItemStack> map, int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative");
        }
        final ItemStack[] contents = new ItemStack[size];
        for (Map.Entry<Integer, ItemStack> entry : map.entrySet()) {
            final int slot = entry.getKey();
            if (slot < 0 || slot >= size) {
                throw new IllegalArgumentException("Slot " + slot + " does not fit into an array of size " + size);
            }
            contents[slot] = entry.getValue();
        }
        return contents;
    }

    /**
     * Puts the filler into every empty slot of the inventory.
     */
    public static void fillEmptySlots(@NotNull Inventory inventory, @NotNull ItemStack filler) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (isEmpty(inventory.getItem(slot))) {
                inventory.setItem(slot, filler);
            }
        }
    }

    /**
     * All non-empty items of the inventory (including armor and offhand for player inventories).
     */
    public static @NotNull List<ItemStack> items(@NotNull Inventory inventory) {
        final List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : inventory.getContents()) {
            if (!isEmpty(item)) {
                items.add(item);
            }
        }
        return items;
    }

    // COUNTING

    /**
     * Total amount of a material in the storage slots, regardless of item meta.
     */
    public static int count(@NotNull Inventory inventory, @NotNull Material material) {
        int count = 0;
        for (ItemStack item : inventory.getStorageContents()) {
            if (!isEmpty(item) && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Total amount of items similar to the given one (same type and meta) in the storage slots.
     */
    public static int count(@NotNull Inventory inventory, @NotNull ItemStack similarTo) {
        int count = 0;
        for (ItemStack item : inventory.getStorageContents()) {
            if (!isEmpty(item) && item.isSimilar(similarTo)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public static boolean contains(@NotNull Inventory inventory, @NotNull ItemStack similarTo) {
        for (ItemStack item : inventory.getStorageContents()) {
            if (!isEmpty(item) && item.isSimilar(similarTo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the storage slots contain at least {@code amount} items similar to the given one.
     */
    public static boolean has(@NotNull Inventory inventory, @NotNull ItemStack similarTo, int amount) {
        return count(inventory, similarTo) >= amount;
    }

    // SPACE, ADD, REMOVE

    /**
     * Whether the full amount of the given stack fits into the storage slots (free slots and partial stacks).
     */
    public static boolean hasSpaceFor(@NotNull Inventory inventory, @NotNull ItemStack item) {
        if (item.isEmpty()) {
            return true;
        }
        final int maxStackSize = item.getMaxStackSize();
        int missing = item.getAmount();

        for (ItemStack slotItem : inventory.getStorageContents()) {
            if (isEmpty(slotItem)) {
                missing -= maxStackSize;
            } else if (slotItem.isSimilar(item)) {
                missing -= Math.max(0, maxStackSize - slotItem.getAmount());
            }
            if (missing <= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes exactly {@code amount} items similar to the given one, or nothing at all.
     *
     * @return true if the items were removed, false if there were not enough
     */
    public static boolean remove(@NotNull Inventory inventory, @NotNull ItemStack similarTo, int amount) {
        if (amount <= 0) {
            return true;
        }
        if (count(inventory, similarTo) < amount) {
            return false;
        }

        final ItemStack[] contents = inventory.getStorageContents();
        int remaining = amount;
        for (int slot = 0; slot < contents.length && remaining > 0; slot++) {
            final ItemStack current = contents[slot];
            if (isEmpty(current) || !current.isSimilar(similarTo)) {
                continue;
            }
            final int take = Math.min(remaining, current.getAmount());
            remaining -= take;
            if (take == current.getAmount()) {
                inventory.setItem(slot, null);
            } else {
                final ItemStack reduced = current.clone();
                reduced.setAmount(current.getAmount() - take);
                inventory.setItem(slot, reduced);
            }
        }
        return true;
    }

    /**
     * Gives the items to the player and drops whatever does not fit at their feet.
     *
     * @return true if everything fit into the inventory, false if something was dropped
     */
    public static boolean giveOrDrop(@NotNull Player player, @NotNull ItemStack... items) {
        final ItemStack[] copies = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            copies[i] = items[i].clone();
        }
        final Map<Integer, ItemStack> leftovers = player.getInventory().addItem(copies);
        for (ItemStack drop : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
        return leftovers.isEmpty();
    }
}