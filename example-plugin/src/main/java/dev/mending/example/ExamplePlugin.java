package dev.mending.example;

import dev.mending.core.paper.api.item.ItemBuilder;
import dev.mending.core.paper.api.language.Lang;
import dev.mending.core.paper.api.language.json.Language;
import dev.mending.example.command.KitCommand;
import dev.mending.example.command.SetSpawnCommand;
import dev.mending.example.config.KitConfig;
import dev.mending.example.config.LocationConfig;
import dev.mending.example.gui.ExampleGui;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class ExamplePlugin extends JavaPlugin implements Listener {

    private final Language language = new Language(this);
    private final LocationConfig locationConfig = new LocationConfig(this, "locations");
    private final KitConfig kitConfig = new KitConfig(this, "kits");

    @Override
    public void onEnable() {

        language.init();
        locationConfig.init();
        kitConfig.init();

        getServer().getPluginManager().registerEvents(this, this);
        registerCommands();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        final Player player = e.getPlayer();
        final Inventory inventory = player.getInventory();

        e.joinMessage(language.get("welcome").replaceText(Lang.replace("%player%", player.getName())));
        player.teleport(locationConfig.getSpawnLocation());
        inventory.setItem(0, new ItemBuilder(Material.DIAMOND).setName(Lang.deserialize("<green>Your Item")).setPersistentData(new NamespacedKey(this, "test"), PersistentDataType.INTEGER, 100).build());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        if (!e.getAction().isRightClick() || e.getItem() == null || e.getItem().getType().equals(Material.AIR)) {
            return;
        }

        final Player player = e.getPlayer();

        if (e.getItem().getType().equals(Material.DIAMOND)) {
            new ExampleGui(this).open(player);
            player.sendMessage("" + e.getItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(this, "test"), PersistentDataType.INTEGER));
        }
    }

    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(new SetSpawnCommand(this).getCommand());
            commands.registrar().register(new KitCommand(this).getCommand());
        });
    }

}
