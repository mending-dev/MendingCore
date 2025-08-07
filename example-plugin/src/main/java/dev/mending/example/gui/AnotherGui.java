package dev.mending.example.gui;

import dev.mending.core.paper.api.gui.Gui;
import dev.mending.core.paper.api.gui.GuiIcon;
import dev.mending.core.paper.api.gui.pagination.Pagination;
import dev.mending.core.paper.api.item.ItemBuilder;
import dev.mending.core.paper.api.item.SkullBuilder;
import dev.mending.example.ExamplePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class AnotherGui extends Gui {

    private final ExamplePlugin plugin;
    private final Gui previous;

    private final Pagination pagination = new Pagination(this);

    private GuiIcon previousIcon;
    private GuiIcon nextIcon;

    public AnotherGui(ExamplePlugin plugin, Gui previous) {

        super(Component.text("Another Gui"), 3);
        this.plugin = plugin;
        this.previous = previous;

        pagination.registerPageSlotsBetween(10, 16);

        for (int i = 0; i < 100; i++) {
            pagination.addItem(new GuiIcon(new ItemBuilder(Material.PAPER).setName(Component.text(i))));
        }

        updatePaginationItems();

        fillRows(new GuiIcon(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(Component.empty())), 1, 3);
    }

    @Override
    public void update() {

        pagination.goFirstPage();

        setItem(22, new GuiIcon(
            new ItemBuilder(Material.OAK_DOOR)
                .setName(Component.text("Back"))
            ).onClick(e -> {
                previous.open(player);
            })
        );

        pagination.update();
    }

    private void updatePaginationItems() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, asyncTask -> {

            GuiIcon previousIcon = new GuiIcon(
                    new SkullBuilder("MHF_ArrowLeft")
                            .setName(Component.text("Previous Page"))
                            .build()
            ).onClick(e -> {
                pagination.goPreviousPage();
                pagination.update();
            });

            GuiIcon nextIcon = new GuiIcon(
                    new SkullBuilder("MHF_ArrowRight")
                            .setName(Component.text("Next Page"))
                            .build()
            ).onClick(e -> {
                pagination.goNextPage();
                pagination.update();
            });

            Bukkit.getScheduler().runTask(plugin, syncTask -> {
                setItem(9, previousIcon);
                setItem(17, nextIcon);
            });

        });
    }
}
