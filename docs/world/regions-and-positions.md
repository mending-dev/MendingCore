---
description: BlockPos, Position and BlockRegion, coordinates without a world.
---

# Regions and positions

`dev.mending.core.api.world` holds coordinates without a world. A `Location` stops working once its world is unloaded. These records are safe to keep in fields and maps and to store in files.

```java
BlockRegion arena = new BlockRegion(new BlockPos(10, 60, 10), new BlockPos(-10, 80, -10));
if (arena.contains(player.getLocation())) { ... }
Location spawn = arena.center().toLocation(world);
```

## BlockPos

The coordinates of a block.

| Signature | Description |
| --- | --- |
| `new BlockPos(int x, int y, int z)` | A position. |
| `static of(Location)`, `static of(Block)` | From the server's types. |
| `offset(int, int, int)` | A moved copy. |
| `long distanceSquared(BlockPos)` | Without the root, so it is cheap and cannot round. |
| `Block block(World)` | The block there. Loads the chunk if needed. |
| `Location toLocation(World)` | The block's lowest corner. |
| `Position standing()` | The middle of its floor, where a player stands on it. |
| `String serialize()`, `static Optional<BlockPos> parse(String)` | As text, `x,y,z`. |

## Position

A point with a view direction.

| Signature | Description |
| --- | --- |
| `new Position(x, y, z)`, `new Position(x, y, z, yaw, pitch)` | A point. |
| `static of(Location)` | From a location. |
| `offset(double, double, double)` | A moved copy. |
| `withRotation(float yaw, float pitch)` | Another view direction. |
| `double distanceSquared(Position)` | Without the root. |
| `BlockPos block()` | The block it is in, also for negative coordinates. |
| `Location toLocation(World)` | A location in that world. |
| `String serialize()`, `static Optional<Position> parse(String)` | As text, `x,y,z,yaw,pitch`. The direction is optional when parsing. |

## BlockRegion

A box of blocks. **Both corners belong to it**, so a region from a block to itself holds that block. The corners can be given in any order, `min()` and `max()` are sorted.

| Signature | Description |
| --- | --- |
| `new BlockRegion(BlockPos, BlockPos)`, `static of(Location, Location)` | A region. |
| `int sizeX()`, `sizeY()`, `sizeZ()` | Size in blocks. |
| `long volume()` | Number of blocks. |
| `contains(int, int, int)`, `contains(BlockPos)` | Whether a block is inside. |
| `contains(Position)`, `contains(Location)` | Whether the block of a point is inside. A player counts by the block their feet are in. |
| `contains(BlockRegion)` | Whether a whole region is inside. |
| `intersects(BlockRegion)`, `Optional<BlockRegion> intersection(BlockRegion)` | Whether two regions meet, and what they share. |
| `expand(int)` | Every side moves out; a negative amount moves in. Leaving nothing throws `IllegalArgumentException`. |
| `Position center()` | The exact middle. |
| `String serialize()`, `static Optional<BlockRegion> parse(String)` | As text, `x,y,z;x,y,z`. |

A region can be used in a `for` loop. It goes through its blocks one at a time, so a large region costs no memory:

```java
for (BlockPos pos : arena) {
    pos.block(world).setType(Material.AIR);
}
```

{% hint style="warning" %}
A region has no world, so `contains(Location)` does not compare worlds. Check that yourself if players can be elsewhere. Changing many blocks in one tick can lag the server, spread large changes over ticks.
{% endhint %}
