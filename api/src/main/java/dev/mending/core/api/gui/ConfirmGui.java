package dev.mending.core.api.gui;

import dev.mending.core.api.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A menu with a confirm and a cancel button. Title and items are yours, nothing is built in.
 *
 * Both buttons react to a left click and close the menu first, so a second fast click cannot run the action
 * twice. Add more icons with {@link #onRender(Consumer)} or in a subclass; they are placed after the buttons.
 */
public class ConfirmGui extends Gui {

    private static final int LEFT_COLUMN = 4;
    private static final int RIGHT_COLUMN = 6;

    private final ItemStack confirmItem;
    private final ItemStack cancelItem;
    private final Set<UUID> decided = new HashSet<>();

    private Consumer<GuiClick> onConfirm = click -> { };
    private Consumer<GuiClick> onCancel = click -> { };
    private GuiIcon filler;
    private int confirmSlot = -1;
    private int cancelSlot = -1;
    private boolean autoClose = true;

    @SuppressWarnings("this-escape")
    public ConfirmGui(@NotNull Component title, int rows, @NotNull ItemStack confirm, @NotNull ItemStack cancel) {
        super(title, rows);
        this.confirmItem = Objects.requireNonNull(confirm, "confirm").clone();
        this.cancelItem = Objects.requireNonNull(cancel, "cancel").clone();
        lockPlayerInventory(true);
    }

    /**
     * A dialog with 3 rows.
     */
    public static @NotNull ConfirmGui create(@NotNull Component title, @NotNull ItemBuilder confirm, @NotNull ItemBuilder cancel) {
        return new ConfirmGui(title, 3, confirm.build(), cancel.build());
    }

    public @NotNull ConfirmGui onConfirm(@NotNull Consumer<GuiClick> onConfirm) {
        this.onConfirm = Objects.requireNonNull(onConfirm, "onConfirm");
        return this;
    }

    public @NotNull ConfirmGui onCancel(@NotNull Consumer<GuiClick> onCancel) {
        this.onCancel = Objects.requireNonNull(onCancel, "onCancel");
        return this;
    }

    /**
     * Fills every other slot with this icon.
     */
    public @NotNull ConfirmGui filler(@NotNull GuiIcon filler) {
        this.filler = Objects.requireNonNull(filler, "filler");
        return this;
    }

    /**
     * Moves the confirm button. By default it is in the middle row, column 4.
     */
    public @NotNull ConfirmGui confirmSlot(int slot) {
        this.confirmSlot = slot;
        return this;
    }

    /**
     * Moves the cancel button. By default it is in the middle row, column 6.
     */
    public @NotNull ConfirmGui cancelSlot(int slot) {
        this.cancelSlot = slot;
        return this;
    }

    /**
     * Whether a button closes the menu before its action runs. Default: true. Without it the menu stays open,
     * the action decides what happens, and fast clicks can run it several times.
     */
    public @NotNull ConfirmGui autoClose(boolean autoClose) {
        this.autoClose = autoClose;
        return this;
    }

    @Override
    protected void render() {
        if (this.filler != null) {
            fill(this.filler);
        }
        final int middle = (rows() + 1) / 2;
        set(this.confirmSlot >= 0 ? this.confirmSlot : Slots.slot(middle, LEFT_COLUMN),
            GuiIcon.of(this.confirmItem).onLeftClick(click -> decide(click, this.onConfirm)));
        set(this.cancelSlot >= 0 ? this.cancelSlot : Slots.slot(middle, RIGHT_COLUMN),
            GuiIcon.of(this.cancelItem).onLeftClick(click -> decide(click, this.onCancel)));
        super.render();
    }

    /**
     * Subclasses overriding this must call {@code super.onOpen(player)}.
     */
    @Override
    protected void onOpen(@NotNull Player player) {
        this.decided.remove(player.getUniqueId());
    }

    /**
     * Subclasses overriding this must call {@code super.onClose(player)}.
     */
    @Override
    protected void onClose(@NotNull Player player) {
        this.decided.remove(player.getUniqueId());
    }

    private void decide(GuiClick click, Consumer<GuiClick> action) {
        if (this.autoClose) {
            if (!this.decided.add(click.player().getUniqueId())) {
                return;
            }
            click.close();
        }
        action.accept(click);
    }
}
