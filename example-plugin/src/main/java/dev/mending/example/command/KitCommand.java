package dev.mending.example.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.mending.example.ExamplePlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class KitCommand {

    private final ExamplePlugin plugin;

    private final LiteralCommandNode<CommandSourceStack> command;

    public KitCommand(ExamplePlugin plugin) {

        this.plugin = plugin;

        this.command = Commands.literal("kit")
                .requires(source -> { return source.getSender() instanceof Player && source.getSender().hasPermission("command.kit"); })
                .then(Commands.literal("save").executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        plugin.getKitConfig().setContent(player.getInventory().getContents());
                        plugin.getKitConfig().save();
                        player.sendRichMessage("<green>Kit has been saved.");
                    }
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("load").executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        player.getInventory().setContents(plugin.getKitConfig().getContent());
                        player.sendRichMessage("<green>Kit has been loaded.");
                    }
                    return Command.SINGLE_SUCCESS;
                }))
                .build();
    }
}
