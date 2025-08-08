package dev.mending.example.config;

import com.google.gson.JsonObject;
import dev.mending.core.paper.api.config.json.Configuration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ExampleConfiguration extends Configuration {

    private String greeting;

    public ExampleConfiguration(@NotNull JavaPlugin plugin, @NotNull String fileName) {
        super(plugin, fileName);
    }

    @Override
    public void onLoad(JsonObject json) {
        this.greeting = json.has("greeting") ? json.get("greeting").getAsString() : "Hello, World!";
    }

    @Override
    public void onPreSave(JsonObject json) {
        json.addProperty("greeting", greeting);
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String newGreeting) {
        this.greeting = newGreeting;
    }
}
