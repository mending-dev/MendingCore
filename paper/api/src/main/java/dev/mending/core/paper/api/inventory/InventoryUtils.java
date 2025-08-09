package dev.mending.core.paper.api.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class InventoryUtils {

    private InventoryUtils() {}

    /**
     * Converts an ItemStack array into a Map of slot -> ItemStack.
     * Filters out null and Material.AIR items.
     *
     * @param contents array of ItemStacks
     * @return map with occupied slots and their items
     */
    public static Map<Integer, ItemStack> toMap(ItemStack[] contents) {
        Map<Integer, ItemStack> map = new HashMap<>();
        if (contents == null) return map;

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() != Material.AIR) {
                map.put(i, item);
            }
        }
        return map;
    }

    /**
     * Builds an ItemStack array from a Map of slot -> ItemStack with the given size.
     *
     * @param map  map with slot indices and items
     * @param size size of the resulting array
     * @return ItemStack array with items at the correct slots
     */
    public static ItemStack[] toArray(Map<Integer, ItemStack> map) {
        ItemStack[] contents = new ItemStack[map.size()];
        for (Map.Entry<Integer, ItemStack> entry : map.entrySet()) {
            int slot = entry.getKey();
            if (slot >= 0 && slot < map.size()) {
                contents[slot] = entry.getValue();
            }
        }
        return contents;
    }

    /**
     * Fills all empty slots in the inventory with the specified filler item.
     *
     * @param inventory inventory to fill
     * @param filler   item to fill empty slots with
     */
    public static void fillEmptySlots(Inventory inventory, ItemStack filler) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                inventory.setItem(i, filler);
            }
        }
    }

    /**
     * Counts the total amount of a specific material in the inventory.
     *
     * @param inventory inventory to check
     * @param material  material to count
     * @return total count of the material in the inventory
     */
    public static int countItem(Inventory inventory, Material material) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Checks if the inventory has enough space to fit the given item stack.
     * Takes stacking rules and available slots into account.
     *
     * @param inventory inventory to check
     * @param item      item stack to fit
     * @return true if there is enough space, false otherwise
     */
    public static boolean hasSpaceFor(Inventory inventory, ItemStack item) {
        int amount = item.getAmount();
        Material material = item.getType();
        int maxStackSize = item.getMaxStackSize();

        for (ItemStack slotItem : inventory.getContents()) {
            if (slotItem == null || slotItem.getType() == Material.AIR) {
                amount -= maxStackSize;
            } else if (slotItem.isSimilar(item)) {
                amount -= maxStackSize - slotItem.getAmount();
            }
            if (amount <= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the inventory contains at least one item similar to the given item stack.
     *
     * @param inventory inventory to check
     * @param item      item stack to look for
     * @return true if at least one similar item exists, false otherwise
     */
    public static boolean containsItem(Inventory inventory, ItemStack item) {
        for (ItemStack slotItem : inventory.getContents()) {
            if (slotItem != null && slotItem.isSimilar(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of all items in the inventory excluding null and Material.AIR.
     *
     * @param inventory inventory to read from
     * @return list of all valid items
     */
    public static List<ItemStack> getAllItems(Inventory inventory) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Clones an ItemStack including its metadata.
     *
     * @param item ItemStack to clone
     * @return cloned ItemStack or null if input is null
     */
    public static ItemStack cloneItem(ItemStack item) {
        if (item == null) return null;
        return item.clone();
    }
}