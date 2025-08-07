package dev.mending.core.paper.plugin;

import dev.mending.core.paper.plugin.listener.GuiListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        registerEvents(getServer().getPluginManager());
    }

    private void registerEvents(final PluginManager pluginManager) {
        pluginManager.registerEvents(new GuiListener(), this);
    }

}
