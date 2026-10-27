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

    @Test
    void manualModeFiltersOnlyRestrictionsThatAreNotHonored() {
        XaerounfairConfig.Profile profile = new XaerounfairConfig.Profile(
                XaerounfairConfig.Mode.MANUAL, true, false, true);
        XaerounfairConfig.Profile ignoreAll = XaerounfairConfig.Profile.defaults();

        assertFalse(ChatFilter.shouldFilter("§f§a§i§r§x§a§e§r§o", profile));
        assertTrue(ChatFilter.shouldFilter("§f§a§i§r§x§a§e§r§o", ignoreAll));
        assertTrue(ChatFilter.shouldFilter("§x§a§e§r§o§m§m§n§e§t§h§e§r§i§s§f§a§i§r", profile));
        assertTrue(ChatFilter.shouldFilter("§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r", profile));
        assertFalse(ChatFilter.shouldFilter("§n§o§m§i§n§i§m§a§p", profile));
        assertTrue(ChatFilter.shouldFilter("§n§o§m§i§n§i§m§a§p", ignoreAll));
        assertTrue(ChatFilter.shouldFilter(ChatFilter.RESET_MESSAGE, profile));
    }

    @Test
    void serverEnforcedModePassesEveryTargetedMessageThrough() {
        XaerounfairConfig.Profile profile = new XaerounfairConfig.Profile(
                XaerounfairConfig.Mode.SERVER_ENFORCED, false, false, false);

        assertFalse(ChatFilter.shouldFilter("§f§a§i§r§x§a§e§r§o", profile));
        assertFalse(ChatFilter.shouldFilter("§n§o§m§i§n§i§m§a§p", profile));
        assertFalse(ChatFilter.shouldFilter(ChatFilter.RESET_MESSAGE, profile));
    }
}
