package dev.mending.core.paper.api.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.*;

public abstract class Configuration {

    protected final JavaPlugin plugin;
    private final String fileName;
    private File file;
    private JsonObject json;
    private final Gson gson;

    /**
     * Constructs a new Configuration handler for managing JSON configuration files.
     *
     * @param plugin   The plugin instance that this configuration is associated with.
     * @param fileName The name of the configuration file (without extension).
     */
    public Configuration(@Nonnull JavaPlugin plugin, @Nonnull String fileName) {
        this.plugin = plugin;
        this.fileName = fileName + ".json";
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Initializes the configuration by loading or creating the JSON file.
     */
    public void init() {
        this.file = new File(plugin.getDataFolder(), this.fileName);
        reload();
    }

    /**
     * Reloads the configuration file.
     * If it doesn't exist, it will be copied from resources.
     */
    public void reload() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(this.fileName, false);
        }

        try (Reader reader = new FileReader(file)) {
            this.json = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            this.json = new JsonObject();
        }

        onLoad(json);
    }

    /**
     * Saves the configuration to disk.
     */
    public void save() {
        onPreSave(json);
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            throw new RuntimeException("Could not save configuration: " + fileName, e);
        }
    }

    /**
     * Gets the underlying JsonObject to manipulate.
     */
    public JsonObject getJson() {
        return json;
    }

    /**
     * Called when the configuration is loaded.
     *
     * @param json The loaded JsonObject.
     */
    public abstract void onLoad(JsonObject json);

    /**
     * Called before saving the configuration.
     *
     * @param json The JsonObject about to be saved.
     */
    public abstract void onPreSave(JsonObject json);
}