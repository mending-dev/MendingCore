package dev.mending.core.paper.api.config.json.adapter;

import com.google.gson.*;
import dev.mending.core.paper.api.language.Lang;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        String materialName = obj.get("type").getAsString();
        Material material = Material.matchMaterial(materialName);

        if (material == null) {
            Bukkit.getLogger().warning("Unbekanntes Material in JSON: " + materialName);
            return null;
        }

        int amount = obj.has("amount") ? obj.get("amount").getAsInt() : 1;
        ItemStack itemStack = new ItemStack(material, amount);

        if (obj.has("meta")) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) return itemStack;

            JsonObject metaJson = obj.getAsJsonObject("meta");

            // DisplayName
            if (metaJson.has("displayName")) {
                String display = metaJson.get("displayName").getAsString();
                meta.displayName(Lang.deserialize(display));
            }

            // Lore
            if (metaJson.has("lore")) {
                JsonArray loreArray = metaJson.getAsJsonArray("lore");
                List<Component> lore = new ArrayList<>();
                for (JsonElement element : loreArray) {
                    lore.add(Lang.deserialize(element.getAsString()));
                }
                meta.lore(lore);
            }

            // ItemFlags
            if (metaJson.has("flags")) {
                JsonArray flagsArray = metaJson.getAsJsonArray("flags");
                for (JsonElement flagElem : flagsArray) {
                    try {
                        ItemFlag flag = ItemFlag.valueOf(flagElem.getAsString());
                        meta.addItemFlags(flag);
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            // Enchantments
            if (metaJson.has("enchantments")) {
                JsonObject enchants = metaJson.getAsJsonObject("enchantments");
                for (String key : enchants.keySet()) {
                    try {
                        Enchantment enchantment = Enchantment.getByKey(org.bukkit.NamespacedKey.fromString(key));
                        if (enchantment != null) {
                            int level = enchants.get(key).getAsInt();
                            meta.addEnchant(enchantment, level, true);
                        }
                    } catch (Exception ignored) {}
                }
            }

            // CustomModelData
            if (metaJson.has("customModelData")) {
                meta.setCustomModelData(metaJson.get("customModelData").getAsInt());
            }

            // SkullMeta
            if (meta instanceof SkullMeta && metaJson.has("owner")) {
                String ownerName = metaJson.get("owner").getAsString();
                OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerName);
                ((SkullMeta) meta).setOwner(owner.getName());
            }

            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    @Override
    public JsonElement serialize(ItemStack itemStack, Type type, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();

        if (itemStack == null) {
            return JsonNull.INSTANCE;
        }

        obj.addProperty("type", itemStack.getType().name());
        obj.addProperty("amount", itemStack.getAmount());

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            JsonObject metaJson = new JsonObject();

            if (meta.hasDisplayName() && meta.displayName() != null) {
                metaJson.addProperty("displayName", Lang.serialize(meta.displayName()));
            }

            if (meta.hasLore() && meta.lore() != null) {
                JsonArray loreArray = new JsonArray();
                for (Component line : meta.lore()) {
                    loreArray.add(Lang.serialize(line));
                }
                metaJson.add("lore", loreArray);
            }

            if (!meta.getItemFlags().isEmpty()) {
                JsonArray flagsArray = new JsonArray();
                for (ItemFlag flag : meta.getItemFlags()) {
                    flagsArray.add(flag.name());
                }
                metaJson.add("flags", flagsArray);
            }

            if (!meta.getEnchants().isEmpty()) {
                JsonObject enchantsJson = new JsonObject();
                meta.getEnchants().forEach((enchant, level) ->
                        enchantsJson.addProperty(enchant.getKey().toString(), level));
                metaJson.add("enchantments", enchantsJson);
            }

            if (meta.hasCustomModelData()) {
                metaJson.addProperty("customModelData", meta.getCustomModelData());
            }

            if (meta instanceof SkullMeta skullMeta && skullMeta.hasOwner()) {
                String owner = skullMeta.getOwner();
                if (owner != null) {
                    metaJson.addProperty("owner", owner);
                }
            }

            obj.add("meta", metaJson);
        }

        return obj;
    }
}