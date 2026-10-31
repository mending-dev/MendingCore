package dev.mending.core.api.gui;

import dev.mending.core.api.item.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Splits a list of items over several pages of a {@link Gui}. Create one with
 * {@link Gui#pagination(int[], Function)}. Only the visible page is mapped to icons.
 *
 * Navigation methods ({@link #next()}, {@link #goTo(int)}, ...) refresh the GUI. Use {@link #page(int)}
 * and {@link #setItems(Collection)} inside {@code render()}, where no refresh is wanted.
 *
 * @param <T> the item type
 */
public final class Pagination<T> {

    private final Gui gui;
    private final int[] slots;
    private final Function<? super T, GuiIcon> mapper;

    private List<T> items = List.of();
    private int page;

    Pagination(@NotNull Gui gui, int[] slots, @NotNull Function<? super T, GuiIcon> mapper) {
        if (slots.length == 0) {
            throw new IllegalArgumentException("A pagination needs at least one slot");
        }
        this.gui = gui;
        this.slots = slots.clone();
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    // ITEMS

    /**
     * Replaces all items. Does not refresh the GUI.
     */
    public @NotNull Pagination<T> setItems(@NotNull Collection<? extends T> items) {
        this.items = List.copyOf(items);
        clampPage();
        return this;
    }

    public @NotNull List<T> items() {
        return this.items;
    }

    // STATE

    /**
     * @return the current page, 0-based
     */
    public int page() {
        return this.page;
    }

    /**
     * Sets the current page (clamped) without refreshing the GUI.
     */
    public @NotNull Pagination<T> page(int page) {
        this.page = clamp(page);
        return this;
    }

    public int pageSize() {
        return this.slots.length;
    }

    public int pageCount() {
        return Math.max(1, (this.items.size() + this.slots.length - 1) / this.slots.length);
    }

    public boolean hasNext() {
        return this.page < pageCount() - 1;
    }

    public boolean hasPrevious() {
        return this.page > 0;
    }

    // NAVIGATION (refreshes the GUI)

    public void next() {
        goTo(this.page + 1);
    }

    public void previous() {
        goTo(this.page - 1);
    }

    public void first() {
        goTo(0);
    }

    public void last() {
        goTo(pageCount() - 1);
    }

    public void goTo(int page) {
        final int target = clamp(page);
        if (target == this.page) {
            return;
        }
        this.page = target;
        this.gui.refresh();
    }

    // BUTTONS

    public @NotNull GuiIcon nextIcon(@NotNull ItemStack item) {
        return GuiIcon.of(item).onClick(click -> next());
    }

    public @NotNull GuiIcon nextIcon(@NotNull ItemBuilder item) {
        return GuiIcon.of(item).onClick(click -> next());
    }

    public @NotNull GuiIcon previousIcon(@NotNull ItemStack item) {
        return GuiIcon.of(item).onClick(click -> previous());
    }

    public @NotNull GuiIcon previousIcon(@NotNull ItemBuilder item) {
        return GuiIcon.of(item).onClick(click -> previous());
    }

    // INTERNAL

    void render() {
        clampPage();
        final int start = this.page * this.slots.length;
        for (int i = 0; i < this.slots.length; i++) {
            final int index = start + i;
            if (index >= this.items.size()) {
                break;
            }
            this.gui.set(this.slots[i], this.mapper.apply(this.items.get(index)));
        }
    }

    private void clampPage() {
        this.page = clamp(this.page);
    }

    private int clamp(int page) {
        return Math.max(0, Math.min(page, pageCount() - 1));
    }
}