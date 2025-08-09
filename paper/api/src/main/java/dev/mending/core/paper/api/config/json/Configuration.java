package dev.mending.core.paper.api.config.json;

import com.google.gson.*;
import dev.mending.core.paper.api.config.json.adapter.ItemStackAdapter;
import dev.mending.core.paper.api.config.json.adapter.LocationAdapter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.reflect.Type;

public abstract class Configuration {

    protected final JavaPlugin plugin;
    protected final String fileName;
    protected final Gson gson;
    protected File file;

    private JsonObject json;

    /**
     * Constructs a new Configuration handler for managing JSON configuration files.
     *
     * @param plugin   The plugin instance that this configuration is associated with.
     * @param fileName The name of the configuration file (without extension).
     */
    public Configuration(@Nonnull JavaPlugin plugin, @Nonnull String fileName) {
        this.plugin = plugin;
        this.fileName = fileName + ".json";
        this.gson = createGson();
    }

    /**
     * Initializes the gson object.
     */
    protected Gson createGson() {
        return new GsonBuilder()
            .registerTypeAdapter(Location.class, new LocationAdapter())
            .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
            .excludeFieldsWithoutExposeAnnotation()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
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
     * Get value from jsonObject if exists
     */
    protected <T> T get(String memberName, Type typeOfT) throws JsonSyntaxException {
        return json.has(memberName) ? gson.fromJson(json.get(memberName), typeOfT) : null;
    }

    /**
     * Add value tp jsonObject
     */
    protected void add(String property, Object src, Type typeOfSrc) {
        json.add(property, gson.toJsonTree(src, typeOfSrc));
    }

    /**
     * Called when the configuration is loaded.
     *
     * @param json The loaded JsonObject.
     */
    protected void onLoad(JsonObject json) {};

    /**
     * Called before saving the configuration.
     *
     * @param json The JsonObject about to be saved.
     */
    protected void onPreSave(JsonObject json) {};
}