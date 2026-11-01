package dev.mending.core.api.scoreboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;
import net.kyori.adventure.text.Component;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * What a sidebar writes on a board. The board is a stand-in that already has the objective, because registering a
 * new one needs a server.
 */
final class SidebarTest {

    private static final Component TITLE = Component.text("title");

    private FakeBoard board;
    private Sidebar sidebar;

    @BeforeEach
    void setUp() {
        this.board = new FakeBoard();
        this.sidebar = new Sidebar(this.board.proxy());
    }

    private static List<Component> lines(int count) {
        return IntStream.range(0, count).mapToObj(index -> (Component) Component.text("line " + index)).toList();
    }

    private static String marker(int index) {
        return "§" + Integer.toHexString(index);
    }

    @Test
    void firstUpdateWritesEveryLineTopDown() {
        this.sidebar.write(TITLE, lines(3));

        assertEquals(Map.of(marker(0), 3, marker(1), 2, marker(2), 1), this.board.scores);
        assertEquals(Component.text("line 1"), this.board.teams.get("mc_line_1"));
        assertEquals(List.of(marker(1)), this.board.entries.get("mc_line_1"));
    }

    @Test
    void shorterSidebarRemovesItsBottomLines() {
        this.sidebar.write(TITLE, lines(5));
        this.sidebar.write(TITLE, lines(2));

        assertEquals(Map.of(marker(0), 2, marker(1), 1), this.board.scores);
        assertEquals(List.of("mc_line_0", "mc_line_1"), List.copyOf(this.board.teams.keySet()));
    }

    @Test
    void longerSidebarMovesEveryLine() {
        this.sidebar.write(TITLE, lines(2));
        this.sidebar.write(TITLE, lines(4));

        assertEquals(Map.of(marker(0), 4, marker(1), 3, marker(2), 2, marker(3), 1), this.board.scores);
        assertEquals(4, this.board.teams.size());
    }

    @Test
    void sameContentWritesNothing() {
        this.sidebar.write(TITLE, lines(3));
        this.board.writes = 0;

        this.sidebar.write(TITLE, lines(3));

        assertEquals(0, this.board.writes);
    }

    @Test
    void oneChangedLineWritesOnlyThatLine() {
        this.sidebar.write(TITLE, lines(3));
        this.board.writes = 0;

        this.sidebar.write(TITLE, List.of(Component.text("line 0"), Component.text("changed"), Component.text("line 2")));

        assertEquals(2, this.board.writes, "its text and its order value");
        assertEquals(Component.text("changed"), this.board.teams.get("mc_line_1"));
    }

    @Test
    void anotherTitleWritesOnlyTheTitle() {
        this.sidebar.write(TITLE, lines(3));
        this.board.writes = 0;

        this.sidebar.write(Component.text("other"), lines(3));

        assertEquals(1, this.board.writes);
        assertEquals(Component.text("other"), this.board.title);
    }

    @Test
    void linesBeyondTheLimitAreCut() {
        this.sidebar.write(TITLE, lines(Sidebar.MAX_LINES + 3));

        assertEquals(Sidebar.MAX_LINES, this.board.scores.size());
        assertEquals(Sidebar.MAX_LINES, this.sidebar.lines().size());
    }

    @Test
    void identicalLinesStaySeparate() {
        this.sidebar.write(TITLE, List.of(Component.empty(), Component.empty()));

        assertEquals(2, this.board.scores.size());
        assertEquals(2, this.board.teams.size());
    }

    @Test
    void removeTakesEverythingAndTheNextWriteBringsItBack() {
        this.sidebar.write(TITLE, lines(3));

        this.sidebar.remove();

        assertTrue(this.board.scores.isEmpty());
        assertTrue(this.board.teams.isEmpty());
        assertTrue(this.board.unregistered);
        assertTrue(this.sidebar.lines().isEmpty());

        this.sidebar.write(TITLE, lines(2));

        assertEquals(Map.of(marker(0), 2, marker(1), 1), this.board.scores);
    }

    @Test
    void anObjectiveOutOfTheSlotGoesBack() {
        this.board.slot = null;

        this.sidebar.write(TITLE, lines(1));

        assertEquals(DisplaySlot.SIDEBAR, this.board.slot);
    }

    private static final class FakeBoard {

        private final Map<String, Integer> scores = new TreeMap<>();
        private final Map<String, Component> teams = new TreeMap<>();
        private final Map<String, List<String>> entries = new TreeMap<>();
        private DisplaySlot slot = DisplaySlot.SIDEBAR;
        private Component title;
        private boolean unregistered;
        private int writes;

        private final Objective objective = stub(Objective.class, (method, arguments) -> switch (method) {
            case "getDisplaySlot" -> this.slot;
            case "setDisplaySlot" -> {
                this.slot = (DisplaySlot) arguments[0];
                yield null;
            }
            case "displayName" -> {
                this.title = (Component) arguments[0];
                this.writes++;
                yield null;
            }
            case "unregister" -> {
                this.unregistered = true;
                yield null;
            }
            case "getScore" -> score((String) arguments[0]);
            default -> null;
        });

        private final Scoreboard board = stub(Scoreboard.class, (method, arguments) -> switch (method) {
            case "getObjective" -> this.objective;
            case "getTeam" -> this.teams.containsKey((String) arguments[0]) ? team((String) arguments[0]) : null;
            case "registerNewTeam" -> {
                this.teams.put((String) arguments[0], Component.empty());
                yield team((String) arguments[0]);
            }
            case "resetScores" -> {
                this.scores.remove((String) arguments[0]);
                yield null;
            }
            default -> null;
        });

        Scoreboard proxy() {
            return this.board;
        }

        private Score score(String entry) {
            return stub(Score.class, (method, arguments) -> {
                if ("setScore".equals(method)) {
                    this.scores.put(entry, (Integer) arguments[0]);
                    this.writes++;
                }
                return null;
            });
        }

        private Team team(String name) {
            return stub(Team.class, (method, arguments) -> {
                switch (method) {
                    case "prefix" -> {
                        this.teams.put(name, (Component) arguments[0]);
                        this.writes++;
                    }
                    case "addEntry" -> this.entries.put(name, List.of((String) arguments[0]));
                    case "unregister" -> this.teams.remove(name);
                    default -> {
                    }
                }
                return null;
            });
        }

        private static <T> T stub(Class<T> type, Handler handler) {
            return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
                (proxy, method, arguments) -> handler.handle(method.getName(), arguments)));
        }

        @FunctionalInterface
        private interface Handler {
            Object handle(String method, Object[] arguments);
        }
    }
}
