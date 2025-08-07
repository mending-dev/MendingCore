package dev.mending.example.gui;

import dev.mending.core.paper.api.gui.Gui;
import dev.mending.core.paper.api.gui.GuiIcon;
import dev.mending.core.paper.api.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class ExampleGui extends Gui {

    private int count = 0;
    private int rows = 1;
    private boolean toggle = false;

    public ExampleGui() {
        super(Component.text(0), 1);
    }

    public void update() {

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
                    setSize(rows);
                }
            })
        );

        setItem(7, new GuiIcon(
            new ItemBuilder(Material.RED_DYE)
                .setName(Component.text("Decrease rows"))
            ).onClick(e -> {
                if (rows > 1) {
                    rows--;
                    setSize(rows);
                }
            })
        );
    }

}