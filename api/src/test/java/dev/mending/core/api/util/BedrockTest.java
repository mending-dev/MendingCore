package dev.mending.core.api.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class BedrockTest {

    @Test
    void floodgateIdsAreBedrock() {
        assertTrue(Bedrock.isBedrock(UUID.fromString("00000000-0000-0000-0009-01f2ef6ee2db")));
    }

    @Test
    void javaAndOfflineIdsAreNot() {
        assertFalse(Bedrock.isBedrock(UUID.fromString("606e2ff0-ed77-4842-9d6c-e1d3321c7838")));
        assertFalse(Bedrock.isBedrock(UUID.nameUUIDFromBytes("OfflinePlayer:Rotsaft".getBytes(StandardCharsets.UTF_8))));
    }
}
