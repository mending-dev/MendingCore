package dev.mending.core.api.gui;

import java.util.Arrays;

/**
 * Helpers for building slot index arrays. Rows and columns are 1-based, slots are 0-based.
 */
public final class Slots {

    private Slots() {
        throw new UnsupportedOperationException("This is a static utility class");
    }

    /**
     * Converts a 1-based row and column into a slot index.
     */
    public static int slot(int row, int column) {
        if (row < 1 || row > Gui.MAX_ROWS) {
            throw new IllegalArgumentException("row must be between 1 and " + Gui.MAX_ROWS + ", was " + row);
        }
        if (column < 1 || column > Gui.COLUMNS) {
            throw new IllegalArgumentException("column must be between 1 and " + Gui.COLUMNS + ", was " + column);
        }
        return (row - 1) * Gui.COLUMNS + (column - 1);
    }

    /**
     * All slots from {@code from} to {@code to}, both inclusive. The bounds may be given in any order.
     */
    public static int[] range(int from, int to) {
        final int start = Math.min(from, to);
        final int end = Math.max(from, to);
        final int[] slots = new int[end - start + 1];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = start + i;
        }
        return slots;
    }

    /**
     * All slots of a 1-based row.
     */
    public static int[] row(int row) {
        final int start = slot(row, 1);
        return range(start, start + Gui.COLUMNS - 1);
    }

    /**
     * All slots of a 1-based column in a GUI with the given number of rows.
     */
    public static int[] column(int column, int rows) {
        checkRows(rows);
        final int offset = slot(1, column);
        final int[] slots = new int[rows];
        for (int row = 0; row < rows; row++) {
            slots[row] = row * Gui.COLUMNS + offset;
        }
        return slots;
    }

    /**
     * The outer frame of a GUI with the given number of rows.
     */
    public static int[] border(int rows) {
        return collect(rows, true);
    }

    /**
     * Everything except the outer frame. Empty for GUIs with fewer than 3 rows.
     */
    public static int[] inner(int rows) {
        return collect(rows, false);
    }

    private static int[] collect(int rows, boolean border) {
        checkRows(rows);
        final int[] buffer = new int[rows * Gui.COLUMNS];
        int count = 0;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < Gui.COLUMNS; column++) {
                final boolean isBorder = row == 0 || row == rows - 1 || column == 0 || column == Gui.COLUMNS - 1;
                if (isBorder == border) {
                    buffer[count++] = row * Gui.COLUMNS + column;
                }
            }
        }
        return Arrays.copyOf(buffer, count);
    }

    private static void checkRows(int rows) {
        if (rows < 1 || rows > Gui.MAX_ROWS) {
            throw new IllegalArgumentException("rows must be between 1 and " + Gui.MAX_ROWS + ", was " + rows);
        }
    }
}