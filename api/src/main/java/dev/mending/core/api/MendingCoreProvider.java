package dev.mending.core.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Static access point for {@link MendingCore}. A single volatile read keeps lookups cheap.
 */
public final class MendingCoreProvider {

    private static volatile MendingCore instance;

    private MendingCoreProvider() {
        throw new UnsupportedOperationException("This is a static holder");
    }

    /**
     * @return the active instance
     * @throws IllegalStateException if MendingCore is not enabled yet
     */
    public static @NotNull MendingCore get() {
        final MendingCore core = instance;
        if (core == null) {
            throw new IllegalStateException(
                "MendingCore is not enabled. Declare it as a dependency in your paper-plugin.yml.");
        }
        return core;
    }

    @ApiStatus.Internal
    public static synchronized void register(@NotNull MendingCore core) {
        if (instance != null) {
            throw new IllegalStateException("MendingCore is already registered");
        }
        instance = core;
    }

    @ApiStatus.Internal
    public static synchronized void unregister() {
        instance = null;
    }
}