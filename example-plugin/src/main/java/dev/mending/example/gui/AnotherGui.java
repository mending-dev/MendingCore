package dev.mending.example.gui;

import dev.mending.core.paper.api.gui.Gui;
import dev.mending.core.paper.api.gui.GuiIcon;
import dev.mending.core.paper.api.gui.pagination.PaginationManager;
import dev.mending.core.paper.api.item.ItemBuilder;
import dev.mending.core.paper.api.item.SkullBuilder;
import dev.mending.example.ExamplePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class AnotherGui extends Gui {

    private final ExamplePlugin plugin;
    private final Gui previous;

    private final PaginationManager paginationManager = new PaginationManager(this);

    private GuiIcon previousIcon;
    private GuiIcon nextIcon;

    public AnotherGui(ExamplePlugin plugin, Gui previous) {

        super(Component.text("Another Gui"), 3);
        this.plugin = plugin;
        this.previous = previous;

        paginationManager.registerPageSlotsBetween(10, 16);

        for (int i = 0; i < 100; i++) {
            paginationManager.addItem(new GuiIcon(new ItemBuilder(Material.PAPER).setName(Component.text(i))));
        }

        updatePaginationItems();

        fillRows(new GuiIcon(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(Component.empty())), 1, 3);
    }

    @Override
    public void update() {

        paginationManager.goFirstPage();

        setItem(22, new GuiIcon(
            new ItemBuilder(Material.OAK_DOOR)
                .setName(Component.text("Back"))
            ).onClick(e -> {
                previous.open(player);
            })
        );

        paginationManager.update();
    }

    private void updatePaginationItems() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, asyncTask -> {

            GuiIcon previousIcon = new GuiIcon(
                    new SkullBuilder("MHF_ArrowLeft")
                            .setName(Component.text("Previous Page"))
                            .build()
            ).onClick(e -> {
                paginationManager.goPreviousPage();
                paginationManager.update();
            });

            GuiIcon nextIcon = new GuiIcon(
                    new SkullBuilder("MHF_ArrowRight")
                            .setName(Component.text("Next Page"))
                            .build()
            ).onClick(e -> {
                paginationManager.goNextPage();
                paginationManager.update();
            });

            Bukkit.getScheduler().runTask(plugin, syncTask -> {
                setItem(9, previousIcon);
                setItem(17, nextIcon);
            });

        });
    }
}
