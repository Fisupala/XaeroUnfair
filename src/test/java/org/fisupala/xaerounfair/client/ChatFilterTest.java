package org.fisupala.xaerounfair.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatFilterTest {
    @Test
    void matchesTargetedFormattingSequences() {
        assertTrue(ChatFilter.matches("§f§a§i§r§x§a§e§r§o"));
        assertTrue(ChatFilter.matches("prefix §x§a§e§r§o§m§m§n§e§t§h§e§r§i§s§f§a§i§r suffix"));
        assertTrue(ChatFilter.matches(ChatFilter.RESET_MESSAGE));
    }

    @Test
    void rejectsUnformattedTextAndUnrelatedFormatting() {
        assertFalse(ChatFilter.matches("fairxaero"));
        assertFalse(ChatFilter.matches("§f§a§i§r§x"));
        assertFalse(ChatFilter.matches("§f§h§e§l§l§o"));
    }

    @Test
    void safelyRejectsNull() {
        assertFalse(ChatFilter.matches((String) null));
    }
}
