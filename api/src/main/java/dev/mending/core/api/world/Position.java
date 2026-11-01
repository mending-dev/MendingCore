package dev.mending.core.api.world;

import dev.mending.core.api.util.Parse;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * A point with a view direction, without a world. Safe to keep and to store, unlike a {@link Location},
 * which stops working once its world is unloaded.
 */
public record Position(double x, double y, double z, float yaw, float pitch) {

    public Position(double x, double y, double z) {
        this(x, y, z, 0.0f, 0.0f);
    }

    public static @NotNull Position of(@NotNull Location location) {
        return new Position(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    /**
     * Reads the text of {@link #serialize()}, with or without the view direction.
     *
     * @return the position, or empty if the text is not one
     */
    public static @NotNull Optional<Position> parse(@NotNull String text) {
        final String[] parts = text.split(",", -1);
        if (parts.length != 3 && parts.length != 5) {
            return Optional.empty();
        }
        final double[] values = new double[parts.length];
        for (int index = 0; index < parts.length; index++) {
            final OptionalDouble value = Parse.doubleValue(parts[index]);
            if (value.isEmpty()) {
                return Optional.empty();
            }
            values[index] = value.getAsDouble();
        }
        return Optional.of(parts.length == 3
            ? new Position(values[0], values[1], values[2])
            : new Position(values[0], values[1], values[2], (float) values[3], (float) values[4]));
    }

    public @NotNull Position offset(double dx, double dy, double dz) {
        return new Position(this.x + dx, this.y + dy, this.z + dz, this.yaw, this.pitch);
    }

    public @NotNull Position withRotation(float yaw, float pitch) {
        return new Position(this.x, this.y, this.z, yaw, pitch);
    }

    public double distanceSquared(@NotNull Position other) {
        final double dx = this.x - other.x;
        final double dy = this.y - other.y;
        final double dz = this.z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * @return the block this point is in
     */
    public @NotNull BlockPos block() {
        return new BlockPos((int) Math.floor(this.x), (int) Math.floor(this.y), (int) Math.floor(this.z));
    }

    public @NotNull Location toLocation(@NotNull World world) {
        return new Location(Objects.requireNonNull(world, "world"), this.x, this.y, this.z, this.yaw, this.pitch);
    }

    /**
     * @return this position as text, {@code x,y,z,yaw,pitch}
     */
    public @NotNull String serialize() {
        return this.x + "," + this.y + "," + this.z + "," + this.yaw + "," + this.pitch;
    }
}
