package dev.mending.example.config;

import com.google.gson.JsonObject;
import dev.mending.core.paper.api.config.json.Configuration;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public class LocationConfig extends Configuration {

    private Location spawnLocation;

    public LocationConfig(@NotNull JavaPlugin plugin, @NotNull String fileName) {
        super(plugin, fileName);
    }

    @Override
    public void onLoad(JsonObject json) {
        spawnLocation = get("spawn", Location.class);
    }

    @Override
    public void onPreSave(JsonObject json) {
        add("spawn", spawnLocation, Location.class);
    }
}
