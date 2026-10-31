package dev.mending.core.api.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A YAML file in the plugin's data folder.
 *
 * <ul>
 *   <li>If the file does not exist, it is copied from the plugin jar (same name). Without a bundled file an
 *       empty config is used.</li>
 *   <li>A bundled file of the same name is used as defaults: missing keys fall back to it when reading.
 *       With {@link #updateMissingKeys(boolean)} they are also written into the file.</li>
 *   <li>Saving writes to a temporary file first and then replaces the original, so a crash cannot leave a
 *       half-written config behind.</li>
 * </ul>
 *
 * <p>Use it directly ({@code YamlConfig.of(plugin, "settings").load()}) or subclass it and override
 * {@link #onLoad(FileConfiguration)} / {@link #onPreSave(FileConfiguration)}.</p>
 */
public class YamlConfig {

    private final Plugin plugin;
    private final String fileName;
    private final File file;
    private final Object writeLock = new Object();

    private boolean updateMissingKeys;
    private volatile FileConfiguration config;

    /**
     * @param fileName the file name relative to the data folder; ".yml" is appended if no YAML extension is given
     */
    public YamlConfig(@NotNull Plugin plugin, @NotNull String fileName) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(fileName, "fileName");
        if (fileName.contains("..")) {
            throw new IllegalArgumentException("fileName must not contain '..'");
        }
        this.fileName = fileName.endsWith(".yml") || fileName.endsWith(".yaml") ? fileName : fileName + ".yml";
        this.file = new File(plugin.getDataFolder(), this.fileName);
    }

    public static @NotNull YamlConfig of(@NotNull Plugin plugin, @NotNull String fileName) {
        return new YamlConfig(plugin, fileName);
    }

    /**
     * If enabled, keys that exist in the bundled file but not in the file on disk are added on load.
     * Added keys are appended without comments. Must be set before {@link #load()}.
     */
    public @NotNull YamlConfig updateMissingKeys(boolean updateMissingKeys) {
        this.updateMissingKeys = updateMissingKeys;
        return this;
    }

    // ---------------------------------------------------------------- loading

    /**
     * Loads the file.
     *
     * @throws IllegalStateException if the file cannot be read or contains invalid YAML
     */
    public @NotNull YamlConfig load() {
        try {
            this.config = read();
        } catch (IOException | InvalidConfigurationException e) {
            throw new IllegalStateException("Could not load " + this.fileName, e);
        }
        onLoad(this.config);
        return this;
    }

    /**
     * Reloads the file from disk. If it cannot be read, the error is logged and the previous content is kept.
     *
     * @return whether the reload succeeded
     */
    public boolean reload() {
        try {
            this.config = read();
        } catch (IOException | InvalidConfigurationException e) {
            this.plugin.getSLF4JLogger().error("Could not reload {}; keeping the previous content", this.fileName, e);
            return false;
        }
        onLoad(this.config);
        return true;
    }

    // ---------------------------------------------------------------- access

    /**
     * @throws IllegalStateException if {@link #load()} was not called yet
     */
    public @NotNull FileConfiguration config() {
        final FileConfiguration current = this.config;
        if (current == null) {
            throw new IllegalStateException(this.fileName + " is not loaded; call load() first");
        }
        return current;
    }

    public @NotNull File file() {
        return this.file;
    }

    // ---------------------------------------------------------------- saving

    /**
     * Saves the file synchronously. Use this in {@code onDisable}.
     */
    public void save() {
        final FileConfiguration current = config();
        onPreSave(current);
        try {
            write(current.saveToString());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save " + this.fileName, e);
        }
    }

    /**
     * Saves the file without blocking the main thread. The content is captured immediately; only the disk
     * write happens asynchronously. Falls back to the calling thread if the plugin is already disabled.
     */
    public @NotNull CompletableFuture<Void> saveAsync() {
        final FileConfiguration current = config();
        onPreSave(current);
        final String data = current.saveToString();

        return CompletableFuture.runAsync(() -> {
            try {
                write(data);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not save " + this.fileName, e);
            }
        }, task -> {
            if (this.plugin.isEnabled()) {
                Bukkit.getScheduler().runTaskAsynchronously(this.plugin, task);
            } else {
                task.run();
            }
        });
    }

    // ---------------------------------------------------------------- hooks

    /**
     * Called after the file was loaded or reloaded successfully.
     */
    protected void onLoad(@NotNull FileConfiguration config) {
    }

    /**
     * Called right before the file is saved, e.g. to write values back into the config.
     */
    protected void onPreSave(@NotNull FileConfiguration config) {
    }

    // ---------------------------------------------------------------- internals

    private @NotNull FileConfiguration read() throws IOException, InvalidConfigurationException {
        final YamlConfiguration defaults = readDefaults();

        if (!this.file.exists()) {
            if (defaults != null) {
                this.plugin.saveResource(this.fileName, false);
            } else {
                final File parent = this.file.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
            }
        }

        final YamlConfiguration loaded = new YamlConfiguration();
        if (this.file.exists()) {
            loaded.load(this.file);
        }

        if (defaults != null) {
            loaded.setDefaults(defaults);
            if (this.updateMissingKeys && addMissingKeys(loaded, defaults)) {
                write(loaded.saveToString());
            }
        }
        return loaded;
    }

    private @Nullable YamlConfiguration readDefaults() throws IOException, InvalidConfigurationException {
        try (InputStream in = this.plugin.getResource(this.fileName)) {
            if (in == null) {
                return null;
            }
            final YamlConfiguration defaults = new YamlConfiguration();
            defaults.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return defaults;
        }
    }

    private static boolean addMissingKeys(FileConfiguration target, ConfigurationSection defaults) {
        boolean changed = false;
        for (String key : defaults.getKeys(true)) {
            if (defaults.isConfigurationSection(key) || target.contains(key, true)) {
                continue;
            }
            target.set(key, defaults.get(key));
            changed = true;
        }
        return changed;
    }

    private void write(String data) throws IOException {
        synchronized (this.writeLock) {
            final Path target = this.file.toPath();
            final Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            final Path temp = target.resolveSibling(target.getFileName() + ".tmp");
            Files.writeString(temp, data, StandardCharsets.UTF_8);
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}