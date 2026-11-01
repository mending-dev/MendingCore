package dev.mending.core.api.gui;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
 * Supported: left and right click with or without cursor item, shift click in both directions, number keys, dragging
 * and the creative middle click. Everything else stays cancelled. The content is refunded to the player when the GUI closes unless
 * {@link #refundOnClose(boolean)} is disabled or the content was removed in {@code onClose}.
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

    // CONFIGURATION

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

    // STATE

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

    // INTERNAL

    void render() {
        this.gui.rawInventory().setItem(this.slot, this.content != null ? this.content : this.placeholder);
    }

    void handle(@NotNull GuiClick click) {
        switch (click.type()) {
            case LEFT, RIGHT -> handleCursor(click);
            case SHIFT_LEFT, SHIFT_RIGHT -> handleShiftOut(click);
            case NUMBER_KEY -> handleNumberKey(click);
            case MIDDLE -> handleMiddle(click);
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
        final int moved = SlotMoves.insert(amountOf(this.content), stack.getAmount(), isSimilar(stack), limitFor(stack));
        if (moved <= 0) {
            return 0;
        }
        update(copyOf(stack, amountOf(this.content) + moved));
        this.onPut.accept(click, copyOf(stack, moved));
        this.onChange.accept(click);
        return moved;
    }

    /**
     * Whether a drag may leave this slot with {@code result}, the whole new stack the server would write.
     */
    boolean accepts(@NotNull GuiClick click, @NotNull ItemStack result) {
        // The server only drags onto a placeholder that matches the item, and would count it as content.
        if (this.content == null && this.placeholder != null) {
            return false;
        }
        return this.canPut.test(click, result) && result.getAmount() <= limitFor(result);
    }

    /**
     * Takes over what a drag wrote. The server sets the same stack in the inventory right after the event.
     */
    void applyDragged(@NotNull GuiClick click, @NotNull ItemStack result) {
        final int moved = result.getAmount() - (this.content == null ? 0 : this.content.getAmount());
        update(result.clone());
        this.onPut.accept(click, copyOf(result, moved));
        this.onChange.accept(click);
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

    // CLICK HANDLING

    private void handleCursor(GuiClick click) {
        final Player player = click.player();
        final ItemStack cursor = player.getItemOnCursor().clone();
        apply(click, cursor, SlotMoves.cursorClick(amountOf(this.content), amountOf(cursor), isSimilar(cursor),
            cursor.isEmpty() ? 0 : limitFor(cursor), click.type() == ClickType.RIGHT), player::setItemOnCursor);
    }

    /** A number key trades the slot with that hotbar slot; what the limit leaves stays in the hotbar. */
    private void handleNumberKey(GuiClick click) {
        if (!(click.event() instanceof InventoryClickEvent event)) {
            return;
        }
        final PlayerInventory inventory = click.player().getInventory();
        final int hotbar = event.getHotbarButton();
        final ItemStack item = inventory.getItem(hotbar);
        final ItemStack held = item == null ? ItemStack.empty() : item.clone();
        apply(click, held, SlotMoves.numberKey(amountOf(this.content), amountOf(held),
            held.isEmpty() ? 0 : limitFor(held)), stack -> inventory.setItem(hotbar, stack));
    }

    /**
     * Carries out a move between this slot and another stack, if {@code canTake} and {@code canPut} allow it.
     */
    private void apply(GuiClick click, ItemStack other, @Nullable SlotMoves.Outcome outcome,
                       Consumer<@Nullable ItemStack> setOther) {
        if (outcome == null) {
            return;
        }
        final ItemStack previous = this.content;
        final int before = amountOf(previous);
        final int put = outcome.swapped() ? outcome.content() : Math.max(0, outcome.content() - before);
        final int taken = outcome.swapped() ? before : Math.max(0, before - outcome.content());
        if (taken > 0 && !this.canTake.test(click, previous)) {
            return;
        }
        if (put > 0 && !this.canPut.test(click, other)) {
            return;
        }
        final ItemStack contentItem = outcome.swapped() || previous == null ? other : previous;
        final ItemStack otherItem = outcome.swapped() || other.isEmpty() ? previous : other;
        update(outcome.content() == 0 ? null : copyOf(contentItem, outcome.content()));
        setOther.accept(outcome.other() == 0 ? null : copyOf(otherItem, outcome.other()));
        if (taken > 0) {
            this.onTake.accept(click, copyOf(previous, taken));
        }
        if (put > 0) {
            this.onPut.accept(click, copyOf(other, put));
        }
        this.onChange.accept(click);
    }

    /** The middle click copies the content to the cursor, for players in creative mode only, as in vanilla. */
    private void handleMiddle(GuiClick click) {
        final Player player = click.player();
        if (this.content != null && player.getGameMode() == GameMode.CREATIVE && player.getItemOnCursor().isEmpty()
            && this.canTake.test(click, this.content)) {
            player.setItemOnCursor(copyOf(this.content, this.content.getMaxStackSize()));
        }
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

    // HELPERS

    private void update(@Nullable ItemStack newContent) {
        this.content = newContent;
        render();
    }

    private int limitFor(ItemStack item) {
        final int max = item.getMaxStackSize();
        return this.maxStackSize > 0 ? Math.min(this.maxStackSize, max) : max;
    }

    private boolean isSimilar(ItemStack item) {
        return this.content != null && this.content.isSimilar(item);
    }

    private static int amountOf(@Nullable ItemStack item) {
        return item == null || item.isEmpty() ? 0 : item.getAmount();
    }

    private static ItemStack copyOf(ItemStack item, int amount) {
        final ItemStack copy = item.clone();
        copy.setAmount(amount);
        return copy;
    }
}
