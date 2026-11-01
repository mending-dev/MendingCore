package dev.mending.core.api.world;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * A box of blocks, without a world. Both corners belong to it, so a region from a block to itself holds
 * that one block. The corners can be given in any order.
 */
public record BlockRegion(@NotNull BlockPos min, @NotNull BlockPos max) implements Iterable<BlockPos> {

    public BlockRegion {
        Objects.requireNonNull(min, "min");
        Objects.requireNonNull(max, "max");
        final BlockPos low = new BlockPos(Math.min(min.x(), max.x()), Math.min(min.y(), max.y()), Math.min(min.z(), max.z()));
        final BlockPos high = new BlockPos(Math.max(min.x(), max.x()), Math.max(min.y(), max.y()), Math.max(min.z(), max.z()));
        min = low;
        max = high;
    }

    public static @NotNull BlockRegion of(@NotNull Location first, @NotNull Location second) {
        return new BlockRegion(BlockPos.of(first), BlockPos.of(second));
    }

    /**
     * Reads the text of {@link #serialize()}.
     *
     * @return the region, or empty if the text is not one
     */
    public static @NotNull Optional<BlockRegion> parse(@NotNull String text) {
        final String[] corners = text.split(";", -1);
        if (corners.length != 2) {
            return Optional.empty();
        }
        final Optional<BlockPos> first = BlockPos.parse(corners[0]);
        final Optional<BlockPos> second = BlockPos.parse(corners[1]);
        return first.isPresent() && second.isPresent()
            ? Optional.of(new BlockRegion(first.get(), second.get()))
            : Optional.empty();
    }

    public int sizeX() {
        return this.max.x() - this.min.x() + 1;
    }

    public int sizeY() {
        return this.max.y() - this.min.y() + 1;
    }

    public int sizeZ() {
        return this.max.z() - this.min.z() + 1;
    }

    /**
     * @return the number of blocks
     */
    public long volume() {
        return (long) sizeX() * sizeY() * sizeZ();
    }

    public boolean contains(int x, int y, int z) {
        return x >= this.min.x() && x <= this.max.x()
            && y >= this.min.y() && y <= this.max.y()
            && z >= this.min.z() && z <= this.max.z();
    }

    public boolean contains(@NotNull BlockPos block) {
        return contains(block.x(), block.y(), block.z());
    }

    /**
     * Whether the block of a point is in this region; a player counts by the block their feet are in.
     */
    public boolean contains(@NotNull Position position) {
        return contains(position.block());
    }

    /**
     * Whether the block of a location is in this region. The world is not compared, a region has none.
     */
    public boolean contains(@NotNull Location location) {
        return contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public boolean contains(@NotNull BlockRegion other) {
        return contains(other.min) && contains(other.max);
    }

    public boolean intersects(@NotNull BlockRegion other) {
        return this.min.x() <= other.max.x() && this.max.x() >= other.min.x()
            && this.min.y() <= other.max.y() && this.max.y() >= other.min.y()
            && this.min.z() <= other.max.z() && this.max.z() >= other.min.z();
    }

    /**
     * @return the blocks both regions hold, or empty if they do not meet
     */
    public @NotNull Optional<BlockRegion> intersection(@NotNull BlockRegion other) {
        if (!intersects(other)) {
            return Optional.empty();
        }
        return Optional.of(new BlockRegion(
            new BlockPos(Math.max(this.min.x(), other.min.x()), Math.max(this.min.y(), other.min.y()), Math.max(this.min.z(), other.min.z())),
            new BlockPos(Math.min(this.max.x(), other.max.x()), Math.min(this.max.y(), other.max.y()), Math.min(this.max.z(), other.max.z()))));
    }

    /**
     * @param blocks how far every side moves out; must not make the region vanish
     * @throws IllegalArgumentException if a negative amount leaves nothing
     */
    public @NotNull BlockRegion expand(int blocks) {
        if (blocks < 0 && (sizeX() + 2 * blocks < 1 || sizeY() + 2 * blocks < 1 || sizeZ() + 2 * blocks < 1)) {
            throw new IllegalArgumentException("Shrinking by " + -blocks + " leaves nothing of " + this);
        }
        return new BlockRegion(this.min.offset(-blocks, -blocks, -blocks), this.max.offset(blocks, blocks, blocks));
    }

    /**
     * @return the exact middle of the box
     */
    public @NotNull Position center() {
        return new Position(this.min.x() + sizeX() / 2.0, this.min.y() + sizeY() / 2.0, this.min.z() + sizeZ() / 2.0);
    }

    /**
     * @return this region as text, {@code x,y,z;x,y,z}
     */
    public @NotNull String serialize() {
        return this.min.serialize() + ";" + this.max.serialize();
    }

    /**
     * Goes through every block, one at a time, so a large region costs no memory. Row by row along x, then z, then y.
     */
    @Override
    public @NotNull Iterator<BlockPos> iterator() {
        return new Iterator<>() {
            private int x = BlockRegion.this.min.x();
            private int y = BlockRegion.this.min.y();
            private int z = BlockRegion.this.min.z();
            private boolean done;

            @Override
            public boolean hasNext() {
                return !this.done;
            }

            @Override
            public BlockPos next() {
                if (this.done) {
                    throw new NoSuchElementException();
                }
                final BlockPos current = new BlockPos(this.x, this.y, this.z);
                advance();
                return current;
            }

            private void advance() {
                if (this.x < BlockRegion.this.max.x()) {
                    this.x++;
                    return;
                }
                this.x = BlockRegion.this.min.x();
                if (this.z < BlockRegion.this.max.z()) {
                    this.z++;
                    return;
                }
                this.z = BlockRegion.this.min.z();
                if (this.y < BlockRegion.this.max.y()) {
                    this.y++;
                    return;
                }
                this.done = true;
            }
        };
    }
}
