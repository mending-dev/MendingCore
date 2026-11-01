package dev.mending.core.api.gui;

import org.jetbrains.annotations.Nullable;

/**
 * What a click does to an input slot and the stack it deals with (the cursor or a hotbar slot), in amounts only. No
 * server types, so the rules that must never lose or duplicate items can be tested without a server.
 */
final class SlotMoves {

    private SlotMoves() {
    }

    /**
     * The amounts after a move.
     *
     * @param content the amount in the slot
     * @param other the amount of the other stack
     * @param swapped whether the two stacks changed places, so each now holds the other's item
     */
    record Outcome(int content, int other, boolean swapped) {
    }

    /**
     * A left or right click on the slot.
     *
     * @param content the amount in the slot
     * @param cursor the amount on the cursor
     * @param similar whether both are the same item
     * @param limit the most the slot holds of the cursor's item
     * @param single whether it is a right click: one item in, half of the content out
     * @return the amounts, or {@code null} if nothing moves
     */
    static @Nullable Outcome cursorClick(int content, int cursor, boolean similar, int limit, boolean single) {
        if (cursor == 0) {
            if (content == 0) {
                return null;
            }
            final int taken = single ? (content + 1) / 2 : content;
            return new Outcome(content - taken, taken, false);
        }
        if (content == 0 || similar) {
            final int moved = Math.min(single ? 1 : cursor, limit - content);
            return moved <= 0 ? null : new Outcome(content + moved, cursor - moved, false);
        }
        return swap(content, cursor, limit);
    }

    /**
     * A number key on the slot: it trades with that hotbar slot.
     *
     * @param content the amount in the slot
     * @param held the amount in the hotbar slot
     * @param limit the most the slot holds of the hotbar's item
     * @return the amounts, or {@code null} if nothing moves
     */
    static @Nullable Outcome numberKey(int content, int held, int limit) {
        if (held == 0) {
            return content == 0 ? null : new Outcome(0, content, false);
        }
        if (content == 0) {
            final int moved = Math.min(held, limit);
            return moved <= 0 ? null : new Outcome(moved, held - moved, false);
        }
        return swap(content, held, limit);
    }

    /**
     * How much of a stack a shift click moves into the slot.
     *
     * @param content the amount in the slot
     * @param stack the amount of the stack
     * @param similar whether both are the same item
     * @param limit the most the slot holds of the stack's item
     * @return the amount moved, 0 if none
     */
    static int insert(int content, int stack, boolean similar, int limit) {
        return content == 0 || similar ? Math.max(0, Math.min(stack, limit - content)) : 0;
    }

    private static @Nullable Outcome swap(int content, int other, int limit) {
        return other > limit ? null : new Outcome(other, content, true);
    }
}
