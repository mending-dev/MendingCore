package dev.mending.core.api.gui;

import dev.mending.core.api.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An item inside a {@link Gui} with optional click handlers. Icons can never be taken out of a GUI;
 * use an {@link InputSlot} for slots players may put items into.
 *
 * A handler registered for a specific {@link ClickType} wins over the general {@link #onClick(Consumer)}
 * handler. Double clicks never trigger handlers.
 */
public final class GuiIcon {

    private final ItemStack item;
    private Consumer<GuiClick> handler;
    private Map<ClickType, Consumer<GuiClick>> typedHandlers;

    private GuiIcon(@NotNull ItemStack item) {
        this.item = item;
    }

    public static @NotNull GuiIcon of(@NotNull ItemStack item) {
        return new GuiIcon(Objects.requireNonNull(item, "item").clone());
    }

    public static @NotNull GuiIcon of(@NotNull ItemBuilder builder) {
        return new GuiIcon(Objects.requireNonNull(builder, "builder").build());
    }

    public static @NotNull GuiIcon of(@NotNull Material material) {
        return new GuiIcon(new ItemStack(Objects.requireNonNull(material, "material")));
    }

    /**
     * An icon that closes the GUI when clicked. It stays a normal icon, so more handlers can be added.
     */
    public static @NotNull GuiIcon closing(@NotNull ItemStack item) {
        return of(item).onClick(GuiClick::close);
    }

    public static @NotNull GuiIcon closing(@NotNull ItemBuilder builder) {
        return of(builder).onClick(GuiClick::close);
    }

    /**
     * An icon that opens another GUI when clicked, e.g. a back button. Reusing an instance keeps its state.
     */
    public static @NotNull GuiIcon opening(@NotNull ItemStack item, @NotNull Gui target) {
        Objects.requireNonNull(target, "target");
        return of(item).onClick(click -> click.open(target));
    }

    public static @NotNull GuiIcon opening(@NotNull ItemBuilder builder, @NotNull Gui target) {
        Objects.requireNonNull(target, "target");
        return of(builder).onClick(click -> click.open(target));
    }

    /**
     * Same with a GUI that is created on every click.
     */
    public static @NotNull GuiIcon opening(@NotNull ItemStack item, @NotNull Supplier<? extends Gui> target) {
        Objects.requireNonNull(target, "target");
        return of(item).onClick(click -> click.open(target.get()));
    }

    public static @NotNull GuiIcon opening(@NotNull ItemBuilder builder, @NotNull Supplier<? extends Gui> target) {
        Objects.requireNonNull(target, "target");
        return of(builder).onClick(click -> click.open(target.get()));
    }

    /**
     * Handler for every click type that has no specific handler.
     */
    public @NotNull GuiIcon onClick(@NotNull Consumer<GuiClick> handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
        return this;
    }

    /**
     * Handler for one specific click type, e.g. {@link ClickType#SHIFT_LEFT}.
     */
    public @NotNull GuiIcon onClick(@NotNull ClickType type, @NotNull Consumer<GuiClick> handler) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(handler, "handler");
        if (this.typedHandlers == null) {
            this.typedHandlers = new EnumMap<>(ClickType.class);
        }
        this.typedHandlers.put(type, handler);
        return this;
    }

    public @NotNull GuiIcon onLeftClick(@NotNull Consumer<GuiClick> handler) {
        return onClick(ClickType.LEFT, handler);
    }

    public @NotNull GuiIcon onRightClick(@NotNull Consumer<GuiClick> handler) {
        return onClick(ClickType.RIGHT, handler);
    }

    /**
     * @return a copy of the displayed item
     */
    public @NotNull ItemStack itemStack() {
        return this.item.clone();
    }

    /** Internal fast path: the inventory copies the stack itself, so no clone is needed. */
    ItemStack item() {
        return this.item;
    }

    void handle(@NotNull GuiClick click) {
        final Consumer<GuiClick> specific = this.typedHandlers == null ? null : this.typedHandlers.get(click.type());
        final Consumer<GuiClick> target = specific != null ? specific : this.handler;
        if (target != null) {
            target.accept(click);
        }
    }
}