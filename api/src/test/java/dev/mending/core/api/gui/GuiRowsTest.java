package dev.mending.core.api.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class GuiRowsTest {

    @Test
    void rowsForTakesAsFewAsNeeded() {
        assertEquals(1, Gui.rowsFor(0));
        assertEquals(1, Gui.rowsFor(1));
        assertEquals(1, Gui.rowsFor(9));
        assertEquals(2, Gui.rowsFor(10));
        assertEquals(6, Gui.rowsFor(54));
        assertEquals(6, Gui.rowsFor(200));
    }
}
