package dev.mending.example.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.mending.example.ExamplePlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class SetSpawnCommand {

    private final ExamplePlugin plugin;

    private final LiteralCommandNode<CommandSourceStack> command;

    public SetSpawnCommand(ExamplePlugin plugin) {

        this.plugin = plugin;

        this.command = Commands.literal("setspawn")
                .requires(source -> { return source.getSender() instanceof Player && source.getSender().hasPermission("command.setspawn"); })
                .executes(ctx -> {
                    final Player player = (Player) ctx.getSource().getSender();
                    plugin.getLocationConfig().setSpawnLocation(player.getLocation());
                    plugin.getLocationConfig().save();
                    player.sendRichMessage("<green>Spawnpoint has been set!");
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
