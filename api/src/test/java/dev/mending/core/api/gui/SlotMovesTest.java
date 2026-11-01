package dev.mending.core.api.gui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.mending.core.api.gui.SlotMoves.Outcome;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * The rules of an input slot: what a click moves, and above all that no click loses or duplicates an item.
 */
final class SlotMovesTest {

    private static final int STACK = 64;

    @Test
    void leftClickTakesEverything() {
        assertEquals(new Outcome(0, 10, false), SlotMoves.cursorClick(10, 0, false, STACK, false));
    }

    @Test
    void rightClickTakesHalfRoundedUp() {
        assertEquals(new Outcome(5, 6, false), SlotMoves.cursorClick(11, 0, false, STACK, true));
        assertEquals(new Outcome(0, 1, false), SlotMoves.cursorClick(1, 0, false, STACK, true));
    }

    @Test
    void emptySlotAndEmptyCursorMoveNothing() {
        assertNull(SlotMoves.cursorClick(0, 0, false, STACK, false));
        assertNull(SlotMoves.numberKey(0, 0, STACK));
    }

    @Test
    void leftClickPutsUpToTheLimit() {
        assertEquals(new Outcome(10, 0, false), SlotMoves.cursorClick(0, 10, false, STACK, false));
        assertEquals(new Outcome(1, 9, false), SlotMoves.cursorClick(0, 10, false, 1, false));
    }

    @Test
    void rightClickPutsOne() {
        assertEquals(new Outcome(1, 9, false), SlotMoves.cursorClick(0, 10, false, STACK, true));
        assertEquals(new Outcome(4, 9, false), SlotMoves.cursorClick(3, 10, true, STACK, true));
    }

    @Test
    void mergingStopsAtTheLimit() {
        assertEquals(new Outcome(64, 6, false), SlotMoves.cursorClick(60, 10, true, STACK, false));
        assertNull(SlotMoves.cursorClick(64, 10, true, STACK, false));
        assertNull(SlotMoves.cursorClick(64, 10, true, STACK, true));
    }

    @Test
    void differentItemsSwapOnlyIfTheCursorFits() {
        assertEquals(new Outcome(3, 7, true), SlotMoves.cursorClick(7, 3, false, STACK, false));
        assertEquals(new Outcome(3, 7, true), SlotMoves.cursorClick(7, 3, false, STACK, true));
        assertNull(SlotMoves.cursorClick(7, 3, false, 1, false));
    }

    @Test
    void numberKeyTakesPutsAndSwaps() {
        assertEquals(new Outcome(0, 5, false), SlotMoves.numberKey(5, 0, STACK));
        assertEquals(new Outcome(5, 0, false), SlotMoves.numberKey(0, 5, STACK));
        assertEquals(new Outcome(4, 5, true), SlotMoves.numberKey(5, 4, STACK));
    }

    @Test
    void numberKeyLeavesWhatTheLimitRefuses() {
        assertEquals(new Outcome(1, 4, false), SlotMoves.numberKey(0, 5, 1));
        assertNull(SlotMoves.numberKey(1, 5, 1), "the rest would have nowhere to go");
    }

    @Test
    void insertFillsWhatFits() {
        assertEquals(5, SlotMoves.insert(0, 5, false, STACK));
        assertEquals(4, SlotMoves.insert(60, 5, true, STACK));
        assertEquals(0, SlotMoves.insert(64, 5, true, STACK));
        assertEquals(0, SlotMoves.insert(3, 5, false, STACK), "another item stays out");
        assertEquals(0, SlotMoves.insert(5, 5, true, 1), "a slot above a lowered limit takes nothing");
    }

    /**
     * Whatever is clicked in whatever order: every item stays, no item appears, and the slot never ends above its limit
     * through a move. Two item types, so a wrong swap shows too.
     */
    @Test
    void noSequenceOfClicksLosesOrDuplicatesItems() {
        final Random random = new Random(42);
        for (int run = 0; run < 2_000; run++) {
            final int limit = random.nextBoolean() ? STACK : 1 + random.nextInt(STACK);
            int contentType = 0;
            int content = random.nextInt(limit + 1);
            int otherType = 1 + random.nextInt(2);
            int other = random.nextInt(STACK + 1);
            final int[] totals = new int[3];
            totals[contentType] += content;
            totals[otherType] += other;

            for (int step = 0; step < 50; step++) {
                final boolean similar = contentType == otherType;
                final int choice = random.nextInt(4);
                final Outcome outcome = switch (choice) {
                    case 0 -> SlotMoves.cursorClick(content, other, similar, limit, false);
                    case 1 -> SlotMoves.cursorClick(content, other, similar, limit, true);
                    case 2 -> SlotMoves.numberKey(content, other, limit);
                    default -> {
                        final int moved = SlotMoves.insert(content, other, similar, limit);
                        yield moved == 0 ? null : new Outcome(content + moved, other - moved, false);
                    }
                };
                if (outcome == null) {
                    continue;
                }
                assertTrue(outcome.content() >= 0 && outcome.other() >= 0, "no negative amounts");
                if (outcome.content() > content) {
                    assertTrue(outcome.content() <= limit, "a move never goes above the limit");
                }
                if (outcome.swapped()) {
                    assertFalse(outcome.content() == 0 || outcome.other() == 0, "a swap needs two stacks");
                    final int type = contentType;
                    contentType = otherType;
                    otherType = type;
                } else if (content == 0 && outcome.content() > 0) {
                    contentType = otherType;
                } else if (other == 0 && outcome.other() > 0) {
                    otherType = contentType;
                }
                content = outcome.content();
                other = outcome.other();

                final int[] now = new int[3];
                now[contentType] += content;
                now[otherType] += other;
                assertArrayEquals(totals, now, "items per type stay the same");
            }
        }
    }
}
