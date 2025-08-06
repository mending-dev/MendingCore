package dev.mending.core.paper.api.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Arrays;

public class ItemBuilder {

    protected ItemStack itemStack;
    protected ItemMeta itemMeta;

    public ItemBuilder(@Nonnull ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(@Nonnull Material material) {
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder setMeta(@Nonnull ItemMeta itemMeta) {
        this.itemMeta = itemMeta;
        return this;
    }

    public ItemBuilder setMaterial(@Nonnull Material material) {
        this.itemStack = this.itemStack.withType(material);
        return this;
    }

    public ItemBuilder setAmount(@Nonnegative int amount) {
        this.itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder setName(@Nonnull Component component) {
        if (component.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
            component = component.decoration(TextDecoration.ITALIC, false);
        }
        this.itemMeta.displayName(component);
        return this;
    }

    public ItemBuilder setLore(@Nonnull Component... components) {
        List<Component> lore = Arrays.stream(components)
            .map(c -> c.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET
                ? c.decoration(TextDecoration.ITALIC, false)
                : c)
            .toList();
        this.itemMeta.lore(lore);
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        this.itemMeta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder addEnchant(@Nonnull Enchantment enchantment, int level) {
        this.itemMeta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder removeEnchant(@Nonnull Enchantment enchantment) {
        this.itemMeta.removeEnchant(enchantment);
        return this;
    }

    public ItemBuilder addItemFlags(ItemFlag... flags) {
        this.itemMeta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder removeItemFlags(ItemFlag... flags) {
        this.itemMeta.removeItemFlags(flags);
        return this;
    }

    public ItemBuilder setCustomModelData(int data) {
        this.itemMeta.setCustomModelData(data);
        return this;
    }

    public ItemStack build() {
        this.itemStack.setItemMeta(this.itemMeta);
        return this.itemStack;
    }
}
