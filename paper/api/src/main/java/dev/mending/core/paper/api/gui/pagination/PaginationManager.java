package dev.mending.core.paper.api.gui.pagination;

import dev.mending.core.paper.api.gui.Gui;
import dev.mending.core.paper.api.gui.GuiIcon;
import dev.mending.core.paper.api.item.ItemBuilder;
import org.bukkit.Material;

import java.util.*;

public class PaginationManager {

    private final Gui gui;
    private final List<Integer> slots = new ArrayList<>();
    private final List<GuiIcon> items = new ArrayList<>();
    private int page = 0;

    public PaginationManager(Gui gui) {
        this.gui = gui;
    }

    public List<Integer> getSlots() {
        return this.slots;
    }

    public void registerPageSlots(Integer... slots) {
        this.registerPageSlots(Arrays.asList(slots));
    }

    public void registerPageSlots(Collection<Integer> slots) {
        this.slots.addAll(slots);
    }

    public void registerPageSlotsBetween(int from, int to) {
        if (from > to) {
            registerPageSlotsBetween(to, from);
            return;
        }
        for (int i = from; i <= to; i++) {
            this.slots.add(i);
        }
    }

    public void unregisterAllPageSlots() {
        this.slots.clear();
    }

    public Gui getGui() {
        return this.gui;
    }

    public int getCurrentPage() {
        return this.page;
    }

    public PaginationManager setPage(int page) {
        this.page = page;
        return this;
    }

    public PaginationManager goNextPage() {
        if (this.page < getLastPage()) {
            this.page++;
        }
        return this;
    }

    public PaginationManager goPreviousPage() {
        if (this.page > 0) {
            this.page--;
        }
        return this;
    }

    public PaginationManager goFirstPage() {
        this.page = 0;
        return this;
    }

    public PaginationManager goLastPage() {
        this.page = getLastPage();
        return this;
    }

    public boolean isLastPage() {
        return this.page == getLastPage();
    }

    public boolean isFirstPage() {
        return this.page == 0;
    }

    public int getLastPage() {
        if (slots.isEmpty() || items.isEmpty()) return 0;
        int fullPages = items.size() / slots.size();
        return (items.size() % slots.size() == 0) ? fullPages - 1 : fullPages;
    }

    public void addItem(GuiIcon... icons) {
        this.items.addAll(Arrays.asList(icons));
    }

    public List<GuiIcon> getItems() {
        return this.items;
    }

    public void update() {
        if (this.page < 0) return;

        for (int i = 0; i < this.slots.size(); i++) {
            int itemIndex = i + (this.page * this.slots.size());
            int slot = this.slots.get(i);

            if (itemIndex < this.items.size()) {
                gui.setItem(slot, this.items.get(itemIndex));
            } else {
                gui.setItem(slot, new GuiIcon(new ItemBuilder(Material.AIR)));
            }
        }
    }
}