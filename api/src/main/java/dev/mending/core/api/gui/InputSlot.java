package dev.mending.core.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A slot players can put items into and take items out of. Create one with {@link Gui#input(int)}.
 *
 * <p>Supported clicks: left and right click with or without cursor item, and shift click in both directions.
 * Everything else stays cancelled. The content is refunded to the player when the GUI closes unless
 * {@link #refundOnClose(boolean)} is disabled or the content was removed in {@code onClose}.</p>
 */
public final class InputSlot {

    private final Gui gui;
    private final int slot;

    private ItemStack placeholder;
    private ItemStack content;
    private int maxStackSize;
    private boolean refundOnClose = true;

    private BiPredicate<GuiClick, ItemStack> canPut = (click, item) -> true;
    private BiPredicate<GuiClick, ItemStack> canTake = (click, item) -> true;
    private BiConsumer<GuiClick, ItemStack> onPut = (click, item) -> { };
    private BiConsumer<GuiClick, ItemStack> onTake = (click, item) -> { };
    private Consumer<GuiClick> onChange = click -> { };

    InputSlot(@NotNull Gui gui, int slot) {
        this.gui = gui;
        this.slot = slot;
    }

    // ---------------------------------------------------------------- configuration

    /**
     * Item displayed while the slot is empty.
     */
    public @NotNull InputSlot placeholder(@Nullable ItemStack placeholder) {
        this.placeholder = placeholder == null ? null : placeholder.clone();
        render();
        return this;
    }

    /**
     * Maximum amount in this slot. 0 (default) uses the max stack size of the item.
     */
    public @NotNull InputSlot maxStackSize(int maxStackSize) {
        if (maxStackSize < 0) {
            throw new IllegalArgumentException("maxStackSize must not be negative");
        }
        this.maxStackSize = maxStackSize;
        return this;
    }

    public @NotNull InputSlot refundOnClose(boolean refundOnClose) {
        this.refundOnClose = refundOnClose;
        return this;
    }

    /**
     * Only items matching the filter can be put into this slot.
     */
    public @NotNull InputSlot filter(@NotNull Predicate<? super ItemStack> filter) {
        Objects.requireNonNull(filter, "filter");
        this.canPut = (click, item) -> filter.test(item);
        return this;
    }

    /**
     * Like {@link #filter(Predicate)}, but with access to the click (e.g. for permission checks).
     */
    public @NotNull InputSlot canPut(@NotNull BiPredicate<GuiClick, ItemStack> canPut) {
        this.canPut = Objects.requireNonNull(canPut, "canPut");
        return this;
    }

    /**
     * Decides whether the current content may be taken out.
     */
    public @NotNull InputSlot canTake(@NotNull BiPredicate<GuiClick, ItemStack> canTake) {
        this.canTake = Objects.requireNonNull(canTake, "canTake");
        return this;
    }

    /**
     * Called after items were put into the slot. The item is a copy of the moved amount.
     */
    public @NotNull InputSlot onPut(@NotNull BiConsumer<GuiClick, ItemStack> onPut) {
        this.onPut = Objects.requireNonNull(onPut, "onPut");
        return this;
    }

    /**
     * Called after items were taken out of the slot. The item is a copy of the moved amount.
     */
    public @NotNull InputSlot onTake(@NotNull BiConsumer<GuiClick, ItemStack> onTake) {
        this.onTake = Objects.requireNonNull(onTake, "onTake");
        return this;
    }

    /**
     * Called after every put or take, after the specific callbacks.
     */
    public @NotNull InputSlot onChange(@NotNull Consumer<GuiClick> onChange) {
        this.onChange = Objects.requireNonNull(onChange, "onChange");
        return this;
    }

    // ---------------------------------------------------------------- state

    public int slot() {
        return this.slot;
    }

    public boolean isEmpty() {
        return this.content == null;
    }

    /**
     * @return a copy of the current content, or null if the slot is empty
     */
    public @Nullable ItemStack content() {
        return this.content == null ? null : this.content.clone();
    }

    /**
     * Replaces the content. Null or an empty stack clears the slot.
     */
    public void set(@Nullable ItemStack item) {
        update(item == null || item.isEmpty() ? null : item.clone());
    }

    public void clear() {
        update(null);
    }

    /**
     * Removes and returns the content, or null if the slot is empty.
     */
    public @Nullable ItemStack take() {
        final ItemStack taken = this.content;
        update(null);
        return taken;
    }

    // ---------------------------------------------------------------- internal

    void render() {
        this.gui.rawInventory().setItem(this.slot, this.content != null ? this.content : this.placeholder);
    }

    void handle(@NotNull GuiClick click) {
        switch (click.type()) {
            case LEFT, RIGHT -> handleCursor(click);
            case SHIFT_LEFT, SHIFT_RIGHT -> handleShiftOut(click);
            default -> { /* unsupported click types stay cancelled */ }
        }
    }

    /**
     * Moves as much of {@code stack} into this slot as fits. Returns the moved amount.
     */
    int insert(@NotNull GuiClick click, @NotNull ItemStack stack) {
        if (!this.canPut.test(click, stack)) {
            return 0;
        }
        final int limit = limitFor(stack);
        final int moved;
        if (this.content == null) {
            moved = Math.min(stack.getAmount(), limit);
            if (moved <= 0) {
                return 0;
            }
            update(copyOf(stack, moved));
        } else if (this.content.isSimilar(stack)) {
            moved = Math.min(stack.getAmount(), limit - this.content.getAmount());
            if (moved <= 0) {
                return 0;
            }
            update(copyOf(this.content, this.content.getAmount() + moved));
        } else {
            return 0;
        }
        this.onPut.accept(click, copyOf(stack, moved));
        this.onChange.accept(click);
        return moved;
    }

    /**
     * Gives the content back to the player (drops what does not fit).
     */
    void refund(@NotNull Player player) {
        if (!this.refundOnClose || this.content == null) {
            return;
        }
        final ItemStack item = this.content;
        update(null);
        final Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
        leftovers.values().forEach(drop -> player.getWorld().dropItemNaturally(player.getLocation(), drop));
    }

    // ---------------------------------------------------------------- click handling

    private void handleCursor(GuiClick click) {
        final Player player = click.player();
        final boolean single = click.type() == ClickType.RIGHT;
        final ItemStack cursor = player.getItemOnCursor().clone();

        if (cursor.isEmpty()) {
            takeToCursor(click, player, single);
            return;
        }
        if (!this.canPut.test(click, cursor)) {
            return;
        }

        if (this.content == null) {
            final int amount = single ? 1 : Math.min(cursor.getAmount(), limitFor(cursor));
            if (amount <= 0) {
                return;
            }
            update(copyOf(cursor, amount));
            player.setItemOnCursor(remainder(cursor, amount));
            this.onPut.accept(click, copyOf(cursor, amount));
            this.onChange.accept(click);
            return;
        }

        if (this.content.isSimilar(cursor)) {
            final int space = limitFor(this.content) - this.content.getAmount();
            final int moved = Math.min(single ? 1 : cursor.getAmount(), space);
            if (moved <= 0) {
                return;
            }
            update(copyOf(this.content, this.content.getAmount() + moved));
            player.setItemOnCursor(remainder(cursor, moved));
            this.onPut.accept(click, copyOf(cursor, moved));
            this.onChange.accept(click);
            return;
        }

        // Different item: swap with the cursor.
        if (cursor.getAmount() > limitFor(cursor) || !this.canTake.test(click, this.content)) {
            return;
        }
        final ItemStack previous = this.content;
        update(cursor.clone());
        player.setItemOnCursor(previous);
        this.onTake.accept(click, previous.clone());
        this.onPut.accept(click, cursor.clone());
        this.onChange.accept(click);
    }

    private void takeToCursor(GuiClick click, Player player, boolean half) {
        if (this.content == null || !this.canTake.test(click, this.content)) {
            return;
        }
        final int total = this.content.getAmount();
        final int amount = half ? (total + 1) / 2 : total;
        final ItemStack taken = copyOf(this.content, amount);
        update(total - amount <= 0 ? null : copyOf(this.content, total - amount));
        player.setItemOnCursor(taken);
        this.onTake.accept(click, taken.clone());
        this.onChange.accept(click);
    }

    private void handleShiftOut(GuiClick click) {
        if (this.content == null || !this.canTake.test(click, this.content)) {
            return;
        }
        final ItemStack before = this.content.clone();
        final Map<Integer, ItemStack> leftovers = click.player().getInventory().addItem(before.clone());
        final ItemStack rest = leftovers.isEmpty() ? null : leftovers.get(0);
        final int moved = before.getAmount() - (rest == null ? 0 : rest.getAmount());
        if (moved <= 0) {
            return;
        }
        update(rest);
        this.onTake.accept(click, copyOf(before, moved));
        this.onChange.accept(click);
    }

    // ---------------------------------------------------------------- helpers

    private void update(@Nullable ItemStack newContent) {
        this.content = newContent;
        render();
    }

    private int limitFor(ItemStack item) {
        final int max = item.getMaxStackSize();
        return this.maxStackSize > 0 ? Math.min(this.maxStackSize, max) : max;
    }

    private static ItemStack copyOf(ItemStack item, int amount) {
        final ItemStack copy = item.clone();
        copy.setAmount(amount);
        return copy;
    }

    /** What stays on the cursor after {@code used} items were taken from it; null if nothing. */
    private static @Nullable ItemStack remainder(ItemStack cursor, int used) {
        final int left = cursor.getAmount() - used;
        return left <= 0 ? null : copyOf(cursor, left);
    }
}