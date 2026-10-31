package dev.mending.core.api.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Fluent builder for {@link ItemStack}s. Names and lore are not italic unless you set italic explicitly.
 * The builder can be reused: every {@link #build()} returns a fresh stack.
 */
public final class ItemBuilder {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final ItemStack item;
    private final ItemMeta meta;

    private ItemBuilder(@NotNull ItemStack item) {
        if (item.isEmpty()) {
            throw new IllegalArgumentException("ItemBuilder needs a non-empty item (air has no meta)");
        }
        this.item = item;
        this.meta = item.getItemMeta();
    }

    public static @NotNull ItemBuilder of(@NotNull Material material) {
        return new ItemBuilder(new ItemStack(Objects.requireNonNull(material, "material")));
    }

    /**
     * Starts from a copy of the given item; the original is never modified.
     */
    public static @NotNull ItemBuilder of(@NotNull ItemStack item) {
        return new ItemBuilder(Objects.requireNonNull(item, "item").clone());
    }

    // ---------------------------------------------------------------- basics

    public @NotNull ItemBuilder amount(int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be at least 1, was " + amount);
        }
        this.item.setAmount(amount);
        return this;
    }

    public @NotNull ItemBuilder name(@NotNull Component name) {
        this.meta.displayName(noItalic(name));
        return this;
    }

    /**
     * @param name the name in MiniMessage format
     */
    public @NotNull ItemBuilder name(@NotNull String name) {
        return name(MINI_MESSAGE.deserialize(name));
    }

    // ---------------------------------------------------------------- lore

    public @NotNull ItemBuilder lore(@NotNull Component... lines) {
        return lore(Arrays.asList(lines));
    }

    public @NotNull ItemBuilder lore(@NotNull List<? extends Component> lines) {
        final List<Component> mapped = new ArrayList<>(lines.size());
        for (Component line : lines) {
            mapped.add(noItalic(line));
        }
        this.meta.lore(mapped);
        return this;
    }

    /**
     * @param lines the lines in MiniMessage format
     */
    public @NotNull ItemBuilder lore(@NotNull String... lines) {
        final List<Component> mapped = new ArrayList<>(lines.length);
        for (String line : lines) {
            mapped.add(MINI_MESSAGE.deserialize(line));
        }
        return lore(mapped);
    }

    public @NotNull ItemBuilder addLore(@NotNull Component... lines) {
        final List<Component> existing = this.meta.lore();
        final List<Component> merged = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
        merged.addAll(Arrays.asList(lines));
        return lore(merged);
    }

    /**
     * @param lines the lines in MiniMessage format
     */
    public @NotNull ItemBuilder addLore(@NotNull String... lines) {
        final Component[] components = new Component[lines.length];
        for (int i = 0; i < lines.length; i++) {
            components[i] = MINI_MESSAGE.deserialize(lines[i]);
        }
        return addLore(components);
    }

    public @NotNull ItemBuilder clearLore() {
        this.meta.lore(null);
        return this;
    }

    // ---------------------------------------------------------------- appearance and behaviour

    public @NotNull ItemBuilder enchant(@NotNull Enchantment enchantment, int level) {
        this.meta.addEnchant(enchantment, level, true);
        return this;
    }

    public @NotNull ItemBuilder removeEnchant(@NotNull Enchantment enchantment) {
        this.meta.removeEnchant(enchantment);
        return this;
    }

    /**
     * Forces the enchantment glint on, or restores the default for {@code false}.
     */
    public @NotNull ItemBuilder glow(boolean glow) {
        this.meta.setEnchantmentGlintOverride(glow ? Boolean.TRUE : null);
        return this;
    }

    public @NotNull ItemBuilder unbreakable(boolean unbreakable) {
        this.meta.setUnbreakable(unbreakable);
        return this;
    }

    public @NotNull ItemBuilder flags(@NotNull ItemFlag... flags) {
        this.meta.addItemFlags(flags);
        return this;
    }

    public @NotNull ItemBuilder removeFlags(@NotNull ItemFlag... flags) {
        this.meta.removeItemFlags(flags);
        return this;
    }

    /**
     * Hides all tooltip details such as attributes and enchantments.
     */
    public @NotNull ItemBuilder hideFlags() {
        this.meta.addItemFlags(ItemFlag.values());
        return this;
    }

    /**
     * Sets the item model (resource pack model, e.g. for Nexo items).
     */
    public @NotNull ItemBuilder itemModel(@Nullable NamespacedKey model) {
        this.meta.setItemModel(model);
        return this;
    }

    public <P, C> @NotNull ItemBuilder pdc(@NotNull NamespacedKey key, @NotNull PersistentDataType<P, C> type,
                                           @NotNull C value) {
        this.meta.getPersistentDataContainer().set(key, type, value);
        return this;
    }

    // ---------------------------------------------------------------- escape hatches

    /**
     * Direct access to the {@link ItemMeta} for anything this builder does not cover.
     */
    public @NotNull ItemBuilder meta(@NotNull Consumer<? super ItemMeta> action) {
        action.accept(this.meta);
        return this;
    }

    /**
     * Direct access to a specific meta type, e.g. {@code meta(SkullMeta.class, m -> ...)}.
     *
     * @throws IllegalArgumentException if the item has a different meta type
     */
    public <M extends ItemMeta> @NotNull ItemBuilder meta(@NotNull Class<M> type, @NotNull Consumer<? super M> action) {
        if (!type.isInstance(this.meta)) {
            throw new IllegalArgumentException(
                "Item of type " + this.item.getType() + " has no " + type.getSimpleName());
        }
        action.accept(type.cast(this.meta));
        return this;
    }

    // ---------------------------------------------------------------- build

    public @NotNull ItemStack build() {
        final ItemStack result = this.item.clone();
        result.setItemMeta(this.meta);
        return result;
    }

    private static Component noItalic(Component component) {
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }
}