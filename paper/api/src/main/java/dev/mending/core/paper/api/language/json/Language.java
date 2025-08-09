package dev.mending.core.paper.api.language.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.mending.core.paper.api.config.json.Configuration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Language extends Configuration {

    private final Map<String, String> variables = new HashMap<>();
    private final Map<String, Component> messages = new HashMap<>();
    private String prefix;

    /**
     * Constructs a new Language handler for managing JSON-based localization.
     *
     * @param plugin The plugin instance.
     */
    public Language(@Nonnull JavaPlugin plugin) {
        super(plugin, "language");
    }

    @Override
    protected void onLoad(JsonObject json) {
        this.messages.clear();
        this.variables.clear();

        MiniMessage miniMessage = MiniMessage.miniMessage();

        // Load prefix
        JsonElement prefixElement = json.get("prefix");
        if (prefixElement != null && prefixElement.isJsonPrimitive()) {
            this.prefix = prefixElement.getAsString();
        }

        // Load variables
        JsonObject vars = json.has("var") && json.get("var").isJsonObject()
                ? json.getAsJsonObject("var") : new JsonObject();

        for (Map.Entry<String, JsonElement> entry : vars.entrySet()) {
            if (entry.getValue().isJsonPrimitive()) {
                this.variables.put(entry.getKey(), entry.getValue().getAsString());
            }
        }

        // Load messages
        JsonObject messagesObj = json.has("messages") && json.get("messages").isJsonObject()
                ? json.getAsJsonObject("messages") : new JsonObject();

        for (Map.Entry<String, JsonElement> entry : messagesObj.entrySet()) {
            if (!entry.getValue().isJsonPrimitive()) continue;

            String raw = entry.getValue().getAsString();

            // Replace <prefix>
            if (prefix != null && raw.contains("<prefix>")) {
                raw = raw.replace("<prefix>", prefix);
            }

            // Replace <var:...>
            AtomicReference<String> result = new AtomicReference<>(raw);
            this.variables.forEach((key, value) -> {
                String placeholder = "<var:" + key + ">";
                if (result.get().contains(placeholder)) {
                    result.set(result.get().replace(placeholder, value));
                }
            });

            // Deserialize and store
            Component deserialized = miniMessage.deserialize(result.get());
            this.messages.put(entry.getKey(), deserialized);
        }

        // Clear variables if not needed after replacement
        this.variables.clear();
    }

    /**
     * Retrieves a localized message by key.
     *
     * @param key The key of the message.
     * @return The deserialized Component or the key as fallback.
     */
    public Component get(@Nonnull String key) {
        return this.messages.getOrDefault(key, Component.text(key));
    }
}