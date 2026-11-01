package dev.mending.core.api.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

final class RegionsAndPositionsTest {

    private static final BlockRegion BOX = new BlockRegion(new BlockPos(2, 5, -1), new BlockPos(0, 3, 1));

    @Test
    void cornersAreSorted() {
        assertEquals(new BlockPos(0, 3, -1), BOX.min());
        assertEquals(new BlockPos(2, 5, 1), BOX.max());
    }

    @Test
    void bothCornersBelongToTheRegion() {
        assertTrue(BOX.contains(BOX.min()));
        assertTrue(BOX.contains(BOX.max()));
        assertFalse(BOX.contains(3, 5, 1));
        assertEquals(27, BOX.volume());
        final BlockRegion single = new BlockRegion(new BlockPos(1, 1, 1), new BlockPos(1, 1, 1));
        assertEquals(1, single.volume());
        assertEquals(new Position(1.5, 1.5, 1.5), single.center());
    }

    @Test
    void aPointCountsByItsBlockAlsoBelowZero() {
        final BlockRegion negative = new BlockRegion(new BlockPos(-1, 0, -1), new BlockPos(-1, 0, -1));
        assertTrue(negative.contains(new Position(-0.5, 0.5, -0.5)));
        assertEquals(new BlockPos(-1, 0, -1), new Position(-0.5, 0.5, -0.5).block());
        assertFalse(negative.contains(new Position(0.5, 0.5, -0.5)));
    }

    @Test
    void iterationVisitsEveryBlockOnce() {
        final List<BlockPos> blocks = new ArrayList<>();
        BOX.forEach(blocks::add);
        assertEquals(27, blocks.size());
        assertEquals(27, new HashSet<>(blocks).size());
        assertTrue(blocks.stream().allMatch(BOX::contains));
    }

    @Test
    void regionsIntersect() {
        final BlockRegion other = new BlockRegion(new BlockPos(2, 5, 1), new BlockPos(9, 9, 9));
        assertTrue(BOX.intersects(other));
        assertEquals(Optional.of(new BlockRegion(new BlockPos(2, 5, 1), new BlockPos(2, 5, 1))), BOX.intersection(other));
        final BlockRegion apart = new BlockRegion(new BlockPos(3, 3, 3), new BlockPos(4, 4, 4));
        assertFalse(BOX.intersects(apart));
        assertEquals(Optional.empty(), BOX.intersection(apart));
    }

    @Test
    void expandMovesEverySide() {
        assertEquals(125, BOX.expand(1).volume());
        assertEquals(1, BOX.expand(-1).volume());
        assertThrows(IllegalArgumentException.class, () -> BOX.expand(-2));
    }

    @Test
    void textRoundTrips() {
        assertEquals(Optional.of(BOX), BlockRegion.parse(BOX.serialize()));
        final BlockPos block = new BlockPos(-4, 70, 12);
        assertEquals(Optional.of(block), BlockPos.parse(block.serialize()));
        final Position position = new Position(1.5, 64.0, -3.25, 90.0f, -12.5f);
        assertEquals(Optional.of(position), Position.parse(position.serialize()));
        assertEquals(Optional.of(new Position(1.0, 2.0, 3.0)), Position.parse("1,2,3"));
    }

    @Test
    void brokenTextIsEmpty() {
        assertEquals(Optional.empty(), BlockPos.parse("1,2"));
        assertEquals(Optional.empty(), BlockPos.parse("1,2,x"));
        assertEquals(Optional.empty(), Position.parse("1,2,3,4"));
        assertEquals(Optional.empty(), BlockRegion.parse("1,2,3"));
    }

    @Test
    void standingIsTheMiddleOfTheFloor() {
        assertEquals(new Position(1.5, 2.0, -2.5), new BlockPos(1, 2, -3).standing());
    }
}
