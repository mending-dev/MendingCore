package dev.mending.core.paper.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public class PaperPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getLogger().info("Plugin enabled.");
    }

    @Override
    public void onDisable() {
        getServer().getLogger().info("Plugin disabled.");
    }

}
