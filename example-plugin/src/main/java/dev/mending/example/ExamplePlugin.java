package dev.mending.example;

import dev.mending.core.paper.api.item.ItemBuilder;
import dev.mending.core.paper.api.item.SkullBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class ExamplePlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        final Player player = e.getPlayer();
        final Inventory inventory = player.getInventory();

        inventory.setItem(0, new ItemBuilder(Material.DIAMOND).setName(Component.text("Test 1")).build());
        inventory.setItem(1, new SkullBuilder(player.getUniqueId()).setName(Component.text("Test 2")).build());
    }

}
