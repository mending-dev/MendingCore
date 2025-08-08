package dev.mending.example;

import dev.mending.core.paper.api.item.ItemBuilder;
import dev.mending.example.config.ExampleConfiguration;
import dev.mending.example.gui.ExampleGui;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class ExamplePlugin extends JavaPlugin implements Listener {

    private ExampleConfiguration exampleConfiguration = new ExampleConfiguration(this, "example");

    @Override
    public void onEnable() {
        exampleConfiguration.init();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        final Player player = e.getPlayer();
        final Inventory inventory = player.getInventory();

        player.sendMessage(exampleConfiguration.getGreeting());
        inventory.setItem(0, new ItemBuilder(Material.DIAMOND).setName(Component.text("Your Item")).build());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        if (!e.getAction().isRightClick() || e.getItem() == null || e.getItem().getType().equals(Material.AIR)) {
            return;
        }

        final Player player = e.getPlayer();

        if (e.getItem().getType().equals(Material.DIAMOND)) {
            new ExampleGui(this).open(player);
        }
    }

}
