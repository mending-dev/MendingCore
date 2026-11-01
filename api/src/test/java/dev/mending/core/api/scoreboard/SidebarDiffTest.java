package dev.mending.core.api.scoreboard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

final class SidebarDiffTest {

    @Test
    void sameLinesWriteNothing() {
        assertEquals(new SidebarDiff(List.of(), List.of(), List.of()), SidebarDiff.between(List.of("a", "b"), List.of("a", "b")));
    }

    @Test
    void firstRenderWritesEverything() {
        assertEquals(new SidebarDiff(List.of(0, 1), List.of(0, 1), List.of()), SidebarDiff.between(List.of(), List.of("a", "b")));
    }

    @Test
    void aChangedLineWritesOnlyThatLine() {
        assertEquals(new SidebarDiff(List.of(1), List.of(1), List.of()), SidebarDiff.between(List.of("a", "b", "c"), List.of("a", "x", "c")));
    }

    @Test
    void anotherCountMovesEveryLine() {
        assertEquals(new SidebarDiff(List.of(1), List.of(0, 1), List.of()), SidebarDiff.between(List.of("a"), List.of("a", "b")));
    }

    @Test
    void aShorterSidebarRemovesItsEnd() {
        assertEquals(new SidebarDiff(List.of(), List.of(0), List.of(1, 2)), SidebarDiff.between(List.of("a", "b", "c"), List.of("a")));
        assertEquals(new SidebarDiff(List.of(), List.of(), List.of(0, 1)), SidebarDiff.between(List.of("a", "b"), List.of()));
    }
}
