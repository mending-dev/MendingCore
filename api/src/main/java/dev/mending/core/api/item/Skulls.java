package dev.mending.core.api.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 * Factory for player head {@link ItemBuilder}s. Never blocks on network requests.
 *
 * Heads created from the same texture are identical and therefore stack.
 */
public final class Skulls {

    private Skulls() {
        throw new UnsupportedOperationException("This is a static utility class");
    }

    /**
     * Head of a player. The skin is resolved by the client/server lazily.
     */
    public static @NotNull ItemBuilder of(@NotNull OfflinePlayer player) {
        Objects.requireNonNull(player, "player");
        return ItemBuilder.of(Material.PLAYER_HEAD).meta(SkullMeta.class, meta -> meta.setOwningPlayer(player));
    }

    public static @NotNull ItemBuilder of(@NotNull UUID uuid) {
        return of(Bukkit.getOfflinePlayer(Objects.requireNonNull(uuid, "uuid")));
    }

    /**
     * Head with a custom texture.
     *
     * @param base64 the Base64 value of the "textures" profile property
     */
    public static @NotNull ItemBuilder ofTexture(@NotNull String base64) {
        Objects.requireNonNull(base64, "base64");
        // A stable UUID per texture keeps identical heads stackable.
        final UUID id = UUID.nameUUIDFromBytes(base64.getBytes(StandardCharsets.UTF_8));
        final PlayerProfile profile = Bukkit.createProfile(id, null);
        profile.setProperty(new ProfileProperty("textures", base64));
        return ItemBuilder.of(Material.PLAYER_HEAD).meta(SkullMeta.class, meta -> meta.setPlayerProfile(profile));
    }

    /**
     * Head with a custom texture.
     *
     * @param textureUrl the full texture URL, e.g. {@code http://textures.minecraft.net/texture/<hash>}
     */
    public static @NotNull ItemBuilder ofUrl(@NotNull String textureUrl) {
        Objects.requireNonNull(textureUrl, "textureUrl");
        final String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + textureUrl + "\"}}}";
        return ofTexture(Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8)));
    }
}