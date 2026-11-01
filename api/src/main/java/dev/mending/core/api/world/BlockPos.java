package dev.mending.core.api.world;

import dev.mending.core.api.util.Parse;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * The coordinates of a block, without a world. Safe to keep and to store, unlike a {@link Location},
 * which stops working once its world is unloaded.
 */
public record BlockPos(int x, int y, int z) {

    public static @NotNull BlockPos of(@NotNull Location location) {
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static @NotNull BlockPos of(@NotNull Block block) {
        return new BlockPos(block.getX(), block.getY(), block.getZ());
    }

    /**
     * Reads the text of {@link #serialize()}.
     *
     * @return the position, or empty if the text is not one
     */
    public static @NotNull Optional<BlockPos> parse(@NotNull String text) {
        final String[] parts = text.split(",", -1);
        if (parts.length != 3) {
            return Optional.empty();
        }
        final OptionalInt x = Parse.integer(parts[0]);
        final OptionalInt y = Parse.integer(parts[1]);
        final OptionalInt z = Parse.integer(parts[2]);
        if (x.isEmpty() || y.isEmpty() || z.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new BlockPos(x.getAsInt(), y.getAsInt(), z.getAsInt()));
    }

    public @NotNull BlockPos offset(int dx, int dy, int dz) {
        return new BlockPos(this.x + dx, this.y + dy, this.z + dz);
    }

    public long distanceSquared(@NotNull BlockPos other) {
        final long dx = (long) this.x - other.x;
        final long dy = (long) this.y - other.y;
        final long dz = (long) this.z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * @return the block at this position; loads the chunk if needed
     */
    public @NotNull Block block(@NotNull World world) {
        return world.getBlockAt(this.x, this.y, this.z);
    }

    /**
     * @return the corner of the block with the lowest coordinates
     */
    public @NotNull Location toLocation(@NotNull World world) {
        return new Location(Objects.requireNonNull(world, "world"), this.x, this.y, this.z);
    }

    /**
     * @return the middle of the block's floor, where a player stands on it
     */
    public @NotNull Position standing() {
        return new Position(this.x + 0.5, this.y, this.z + 0.5);
    }

    /**
     * @return this position as text, {@code x,y,z}
     */
    public @NotNull String serialize() {
        return this.x + "," + this.y + "," + this.z;
    }
}
