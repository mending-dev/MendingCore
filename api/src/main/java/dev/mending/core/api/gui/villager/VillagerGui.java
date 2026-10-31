package dev.mending.core.api.gui.villager;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A villager trading window. Override {@link #render()} (or use {@link #onRender(Consumer)}) to add trades;
 * it runs on every {@link #open(Player)}, so trades can depend on the current state. Trades added outside
 * of {@code render()} are permanent.
 *
 * <p>Purchases are not intercepted by this class. Listen to Bukkit's trade events in your plugin if you
 * need to react to them.</p>
 */
public class VillagerGui {

    private final Component title;
    private final List<Trade> fixedTrades = new ArrayList<>();
    private final List<Trade> dynamicTrades = new ArrayList<>();
    private Consumer<VillagerGui> renderer;
    private boolean rendering;

    public VillagerGui(@NotNull Component title) {
        this.title = Objects.requireNonNull(title, "title");
    }

    public static @NotNull VillagerGui create(@NotNull Component title) {
        return new VillagerGui(title);
    }

    public static @NotNull VillagerGui create(@NotNull Component title, @NotNull Consumer<VillagerGui> renderer) {
        return new VillagerGui(title).onRender(renderer);
    }

    /**
     * Adds the trades. Called on every {@link #open(Player)}.
     */
    protected void render() {
        if (this.renderer != null) {
            this.renderer.accept(this);
        }
    }

    public @NotNull VillagerGui onRender(@NotNull Consumer<VillagerGui> renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        return this;
    }

    public @NotNull VillagerGui addTrade(@NotNull Trade trade) {
        Objects.requireNonNull(trade, "trade");
        (this.rendering ? this.dynamicTrades : this.fixedTrades).add(trade);
        return this;
    }

    /**
     * Shortcut for an unlimited trade without experience.
     */
    public @NotNull VillagerGui addTrade(@NotNull ItemStack result, @NotNull ItemStack... ingredients) {
        return addTrade(Trade.of(result).ingredients(ingredients));
    }

    public void open(@NotNull Player player) {
        this.dynamicTrades.clear();
        this.rendering = true;
        try {
            render();
        } finally {
            this.rendering = false;
        }

        final List<MerchantRecipe> recipes = new ArrayList<>(this.fixedTrades.size() + this.dynamicTrades.size());
        for (Trade trade : this.fixedTrades) {
            recipes.add(trade.toRecipe());
        }
        for (Trade trade : this.dynamicTrades) {
            recipes.add(trade.toRecipe());
        }

        final Merchant merchant = Bukkit.createMerchant(this.title);
        merchant.setRecipes(recipes);
        player.openMerchant(merchant, true);
    }
}