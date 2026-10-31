package dev.mending.core.api.gui.villager;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A single trade of a {@link VillagerGui}: up to two ingredients for one result.
 */
public final class Trade {

    private final ItemStack result;
    private final List<ItemStack> ingredients = new ArrayList<>(2);
    private int maxUses = Integer.MAX_VALUE;
    private boolean experienceReward;

    private Trade(@NotNull ItemStack result) {
        this.result = result;
    }

    public static @NotNull Trade of(@NotNull ItemStack result) {
        Objects.requireNonNull(result, "result");
        if (result.isEmpty()) {
            throw new IllegalArgumentException("The result of a trade must not be empty");
        }
        return new Trade(result.clone());
    }

    /**
     * Adds an ingredient. A trade supports at most 2 ingredients.
     */
    public @NotNull Trade ingredient(@NotNull ItemStack ingredient) {
        Objects.requireNonNull(ingredient, "ingredient");
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException("An ingredient must not be empty");
        }
        if (this.ingredients.size() >= 2) {
            throw new IllegalStateException("A trade supports at most 2 ingredients");
        }
        this.ingredients.add(ingredient.clone());
        return this;
    }

    public @NotNull Trade ingredients(@NotNull ItemStack... ingredients) {
        for (ItemStack ingredient : ingredients) {
            ingredient(ingredient);
        }
        return this;
    }

    /**
     * How often the trade can be used per opening. Default: unlimited.
     */
    public @NotNull Trade maxUses(int maxUses) {
        if (maxUses < 1) {
            throw new IllegalArgumentException("maxUses must be at least 1, was " + maxUses);
        }
        this.maxUses = maxUses;
        return this;
    }

    /**
     * Whether the player receives experience orbs for the trade. Default: false.
     */
    public @NotNull Trade experienceReward(boolean experienceReward) {
        this.experienceReward = experienceReward;
        return this;
    }

    /** Creates a fresh recipe; recipes carry use counters and must not be shared between openings. */
    @NotNull MerchantRecipe toRecipe() {
        if (this.ingredients.isEmpty()) {
            throw new IllegalStateException("A trade needs at least one ingredient");
        }
        final MerchantRecipe recipe = new MerchantRecipe(this.result.clone(), 0, this.maxUses, this.experienceReward);
        for (ItemStack ingredient : this.ingredients) {
            recipe.addIngredient(ingredient.clone());
        }
        return recipe;
    }
}