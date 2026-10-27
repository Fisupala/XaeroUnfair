package org.fisupala.xaerounfair.client;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void manualProfilesApplyTheirFullRestrictionSets() {
        String fairxaero = "§f§a§i§r§x§a§e§r§o";
        String noMinimap = "§n§o§m§i§n§i§m§a§p";
        String minimapNether = "§x§a§e§r§o§m§m§n§e§t§h§e§r§i§s§f§a§i§r";
        String worldMapNether = "§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r";
        XaerounfairConfig.Profile defaults = manual(XaerounfairConfig.ManualProfile.DEFAULT);
        XaerounfairConfig.Profile minimapOff = manual(XaerounfairConfig.ManualProfile.MINIMAP_OFF);
        XaerounfairConfig.Profile netherCave = manual(XaerounfairConfig.ManualProfile.NETHER_CAVE);
        XaerounfairConfig.Profile basicFairPlay = manual(XaerounfairConfig.ManualProfile.BASIC_FAIRPLAY);

        assertTrue(ChatFilter.shouldFilter(fairxaero, defaults));
        assertTrue(ChatFilter.shouldFilter(noMinimap, defaults));
        assertTrue(ChatFilter.shouldFilter(minimapNether, defaults));
        assertTrue(ChatFilter.shouldFilter(worldMapNether, defaults));
        assertTrue(ChatFilter.shouldFilter(ChatFilter.RESET_MESSAGE, defaults));

        assertTrue(ChatFilter.shouldFilter(fairxaero, minimapOff));
        assertFalse(ChatFilter.shouldFilter(noMinimap, minimapOff));
        assertTrue(ChatFilter.shouldFilter(minimapNether, minimapOff));
        assertTrue(ChatFilter.shouldFilter(fairxaero + noMinimap, minimapOff));

        assertTrue(ChatFilter.shouldFilter(fairxaero, netherCave));
        assertTrue(ChatFilter.shouldFilter(noMinimap, netherCave));
        assertFalse(ChatFilter.shouldFilter(minimapNether, netherCave));
        assertTrue(ChatFilter.shouldFilter(worldMapNether, netherCave));

        assertFalse(ChatFilter.shouldFilter(fairxaero, basicFairPlay));
        assertTrue(ChatFilter.shouldFilter(noMinimap, basicFairPlay));
        assertTrue(ChatFilter.shouldFilter(minimapNether, basicFairPlay));
        assertTrue(ChatFilter.shouldFilter(worldMapNether, basicFairPlay));
    }

    @Test
    void manualProfilesMapToOnlyTheirModeInstructions() {
        assertEquals(List.of(), XaerounfairConfig.ManualProfile.DEFAULT.simulatedMessageKeys());
        assertEquals(List.of("nominimap"), XaerounfairConfig.ManualProfile.MINIMAP_OFF.simulatedMessageKeys());
        assertEquals(List.of("xaerommnetherisfair"),
                XaerounfairConfig.ManualProfile.NETHER_CAVE.simulatedMessageKeys());
        assertEquals(List.of("fairxaero"), XaerounfairConfig.ManualProfile.BASIC_FAIRPLAY.simulatedMessageKeys());
    }

    @Test
    void simulatedInstructionFormattingIncludesXaeroColorCodes() {
        assertEquals("§n§o§m§i§n§i§m§a§p",
                ChatFilter.spaced(XaerounfairConfig.ManualProfile.MINIMAP_OFF.simulatedMessageKeys().getFirst()));
        assertEquals("§x§a§e§r§o§m§m§n§e§t§h§e§r§i§s§f§a§i§r",
                ChatFilter.spaced(XaerounfairConfig.ManualProfile.NETHER_CAVE.simulatedMessageKeys().getFirst()));
        assertEquals("§f§a§i§r§x§a§e§r§o",
                ChatFilter.spaced(XaerounfairConfig.ManualProfile.BASIC_FAIRPLAY.simulatedMessageKeys().getFirst()));
    }

    @Test
    void onlyDefaultManualProfileRunsResetOnJoin() {
        assertTrue(ResetSimulator.shouldRunOnJoin(manual(XaerounfairConfig.ManualProfile.DEFAULT)));
        assertFalse(ResetSimulator.shouldRunOnJoin(manual(XaerounfairConfig.ManualProfile.MINIMAP_OFF)));
        assertFalse(ResetSimulator.shouldRunOnJoin(manual(XaerounfairConfig.ManualProfile.NETHER_CAVE)));
        assertFalse(ResetSimulator.shouldRunOnJoin(manual(XaerounfairConfig.ManualProfile.BASIC_FAIRPLAY)));
        assertFalse(ResetSimulator.shouldRunOnJoin(new XaerounfairConfig.Profile(
                XaerounfairConfig.Mode.SERVER_ENFORCED, XaerounfairConfig.ManualProfile.DEFAULT)));
    }

    @Test
    void serverEnforcedModePassesEveryTargetedMessageThrough() {
        XaerounfairConfig.Profile profile = new XaerounfairConfig.Profile(
                XaerounfairConfig.Mode.SERVER_ENFORCED, XaerounfairConfig.ManualProfile.DEFAULT);

        assertFalse(ChatFilter.shouldFilter("§f§a§i§r§x§a§e§r§o", profile));
        assertFalse(ChatFilter.shouldFilter("§n§o§m§i§n§i§m§a§p", profile));
        assertFalse(ChatFilter.shouldFilter(ChatFilter.RESET_MESSAGE, profile));
    }

    private static XaerounfairConfig.Profile manual(XaerounfairConfig.ManualProfile profile) {
        return new XaerounfairConfig.Profile(XaerounfairConfig.Mode.MANUAL, profile);
    }
}
