package dev.mending.example.gui;

import dev.mending.core.paper.api.gui.Gui;
import dev.mending.core.paper.api.gui.GuiIcon;
import dev.mending.core.paper.api.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class EnchanterGui extends Gui {

    private final Gui previous;

    public EnchanterGui(Gui previous) {
        super(Component.text("Enchanter"), 6);
        this.previous = previous;

        getActionSlotManager().register(12, new ItemBuilder(Material.BARRIER))
            .onPrePickupClick((clickEvent, itemStack) -> itemStack.getType() == Material.BARRIER)
            .onPut((clickEvent, itemStack) -> {
                player.sendMessage(Component.text("onPut(): " +  itemStack.getType().toString()));
            })
            .onPickup((clickEvent, itemStack) -> {
                player.sendMessage(Component.text("onPickup(): " +  itemStack.getType().toString()));
            });

        getActionSlotManager().register(14, new ItemBuilder(Material.BARRIER))
                .onPrePickupClick((clickEvent, itemStack) -> itemStack.getType() == Material.BARRIER)
                .onPut((clickEvent, itemStack) -> {
                    player.sendMessage(Component.text("onPut(): " +  itemStack.getType().toString()));
                })
                .onPickup((clickEvent, itemStack) -> {
                    player.sendMessage(Component.text("onPickup(): " +  itemStack.getType().toString()));
                });
    }

    @Override
    public void update() {
        setItem(49, new GuiIcon(
                new ItemBuilder(Material.OAK_DOOR)
            ).onClick(clickEvent -> {
                this.previous.open(player);
            })
        );
    }
}
