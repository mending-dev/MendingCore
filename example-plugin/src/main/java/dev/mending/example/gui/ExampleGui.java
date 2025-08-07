package dev.mending.example.gui;

import dev.mending.core.paper.api.gui.Gui;
import dev.mending.core.paper.api.gui.GuiIcon;
import dev.mending.core.paper.api.item.ItemBuilder;
import dev.mending.example.ExamplePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class ExampleGui extends Gui {

    private final ExamplePlugin plugin;

    private int count = 0;
    private int rows = 1;
    private boolean toggle = false;

    private final EnchanterGui enchanterGui;
    private final AnotherGui anotherGui;

    public ExampleGui(ExamplePlugin plugin) {
        super(Component.text(0), 1);
        this.plugin = plugin;
        this.enchanterGui = new EnchanterGui(this);
        this.anotherGui = new AnotherGui(plugin, this);
    }

    public void update() {

        fill(new GuiIcon(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(Component.empty())));

        setItem(1, new GuiIcon(
            new ItemBuilder(Material.WHITE_DYE)
                .setName(Component.text("Increase Count"))
            ).onClick(e -> {
                count++;
                setTitle(Component.text(count));
            })
        );

        setItem(2, new GuiIcon(
            new ItemBuilder(Material.RED_DYE)
                .setName(Component.text("Decrease Count"))
            ).onClick(e -> {
                count--;
                setTitle(Component.text(count));
            })
        );

        setItem(4, new GuiIcon(
            new ItemBuilder(toggle ? Material.LIME_DYE : Material.GRAY_DYE)
                .setName(Component.text("Toggle: " + (toggle ? "On" : "Off")))
            ).onClick(e -> {
                toggle = !toggle;
                update();
            })
        );

        setItem(6, new GuiIcon(
            new ItemBuilder(Material.WHITE_DYE)
                .setName(Component.text("Increase rows"))
            ).onClick(e -> {
                if (rows < 6) {
                    rows++;
                    setRows(rows);
                }
            })
        );

        setItem(7, new GuiIcon(
            new ItemBuilder(Material.RED_DYE)
                .setName(Component.text("Decrease rows"))
            ).onClick(e -> {
                if (rows > 1) {
                    rows--;
                    setRows(rows);
                }
            })
        );

        if (rows > 2) {
            setItem(22, new GuiIcon(
                            new ItemBuilder(Material.ENCHANTING_TABLE)
                                    .setName(Component.text("Open Enchanter Gui"))
                    ).onClick(e -> {
                        enchanterGui.open(player);
                    })
            );
        }

        if (rows == 6) {
            setItem(49, new GuiIcon(
                new ItemBuilder(Material.CHEST)
                    .setName(Component.text("Open Another Gui"))
                ).onClick(e -> {
                    anotherGui.open(player);
                })
            );
        }
    }

}