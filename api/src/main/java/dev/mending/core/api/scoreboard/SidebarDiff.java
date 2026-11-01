package dev.mending.core.api.scoreboard;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * What a sidebar has to write to go from one set of lines to the next. No server types, so it can be tested
 * without a server.
 *
 * @param rewritten the indexes whose text changed
 * @param rescored the indexes whose order value must be written
 * @param removed the indexes that no longer exist
 */
record SidebarDiff(@NotNull List<Integer> rewritten, @NotNull List<Integer> rescored, @NotNull List<Integer> removed) {

    static <T> @NotNull SidebarDiff between(@NotNull List<T> before, @NotNull List<T> after) {
        // The order value is "line count minus index", so another line count moves every line, changed or not.
        final boolean countChanged = before.size() != after.size();
        final List<Integer> rewritten = new ArrayList<>();
        final List<Integer> rescored = new ArrayList<>();
        for (int index = 0; index < after.size(); index++) {
            final boolean changed = index >= before.size() || !before.get(index).equals(after.get(index));
            if (changed) {
                rewritten.add(index);
            }
            if (changed || countChanged) {
                rescored.add(index);
            }
        }
        final List<Integer> removed = new ArrayList<>();
        for (int index = after.size(); index < before.size(); index++) {
            removed.add(index);
        }
        return new SidebarDiff(rewritten, rescored, removed);
    }
}
