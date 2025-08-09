package dev.mending.example.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import dev.mending.core.paper.api.config.json.Configuration;
import dev.mending.core.paper.api.inventory.InventoryUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Map;

@Getter @Setter
public class KitConfig extends Configuration {

    private ItemStack[] content;
    private Type type = new TypeToken<Map<Integer, ItemStack>>(){}.getType();

    public KitConfig(@NotNull JavaPlugin plugin, @NotNull String fileName) {
        super(plugin, fileName);
    }

    @Override
    public void onLoad(JsonObject json) {
        content = get("content", type);
    }

    @Override
    public void onPreSave(JsonObject json) {
        add("content", InventoryUtils.toMap(content), type);
    }
}
