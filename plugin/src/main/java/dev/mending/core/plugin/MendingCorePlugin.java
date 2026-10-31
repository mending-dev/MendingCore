package dev.mending.core.plugin;

import dev.mending.core.api.MendingCore;
import dev.mending.core.api.MendingCoreProvider;
import dev.mending.core.plugin.gui.GuiListener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class MendingCorePlugin extends JavaPlugin {

    private MendingCoreImpl core;

    @Override
    public void onEnable() {
        this.core = new MendingCoreImpl(this);

        MendingCoreProvider.register(this.core);
        getServer().getServicesManager().register(MendingCore.class, this.core, this, ServicePriority.Normal);

        getServer().getPluginManager().registerEvents(new GuiListener(), this);

        getSLF4JLogger().info("MendingCore {} enabled", this.core.version());
    }

    @Override
    public void onDisable() {
        // Close first: GUI close handlers (refunds) still need the listeners and the provider.
        if (this.core != null) {
            this.core.close();
            this.core = null;
        }
        getServer().getServicesManager().unregisterAll(this);
        MendingCoreProvider.unregister();
    }
}