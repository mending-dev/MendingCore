package dev.mending.core.paper.api.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import javax.annotation.Nonnull;
import java.util.UUID;

public class SkullBuilder extends ItemBuilder {

    private final PlayerProfile playerProfile;

    public SkullBuilder(@Nonnull UUID uuid) {
        super(new ItemStack(Material.PLAYER_HEAD));
        this.playerProfile = Bukkit.createProfile(uuid);
    }

    public SkullBuilder(@Nonnull String name) {
        super(new ItemStack(Material.PLAYER_HEAD));
        this.playerProfile = Bukkit.createProfile(name);
    }

    public ItemStack build() {
        SkullMeta meta = (SkullMeta) this.itemMeta;
        playerProfile.complete();
        meta.setPlayerProfile(playerProfile);
        this.itemStack.setItemMeta(meta);
        return this.itemStack;
    }
}