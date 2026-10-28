package org.fisupala.xaerounfair.client;

import net.minecraft.network.chat.Component;
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
    void manualProfilesAlwaysFilterEveryTargetedMessage() {
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
        assertTrue(ChatFilter.shouldFilter(noMinimap, minimapOff));
        assertTrue(ChatFilter.shouldFilter(minimapNether, minimapOff));
        assertTrue(ChatFilter.shouldFilter(worldMapNether, minimapOff));

        assertTrue(ChatFilter.shouldFilter(fairxaero, netherCave));
        assertTrue(ChatFilter.shouldFilter(noMinimap, netherCave));
        assertTrue(ChatFilter.shouldFilter(minimapNether, netherCave));
        assertTrue(ChatFilter.shouldFilter(worldMapNether, netherCave));

        assertTrue(ChatFilter.shouldFilter(fairxaero, basicFairPlay));
        assertTrue(ChatFilter.shouldFilter(noMinimap, basicFairPlay));
        assertTrue(ChatFilter.shouldFilter(minimapNether, basicFairPlay));
        assertTrue(ChatFilter.shouldFilter(worldMapNether, basicFairPlay));
    }

    @Test
    void syntheticMessagesAreNotFiltered() {
        Component synthetic = Component.literal(ChatFilter.RESET_MESSAGE);
        ResetSimulator.registerSimulatedMessage(synthetic);

        try {
            assertFalse(ChatFilter.shouldFilter(synthetic, manual(XaerounfairConfig.ManualProfile.DEFAULT)));
            assertFalse(ChatFilter.shouldFilter(synthetic, manual(XaerounfairConfig.ManualProfile.MINIMAP_OFF)));
        } finally {
            ResetSimulator.consumeSimulatedMessage(synthetic);
        }
    }

    @Test
    void serverMessageHistoryKeepsLastFiveTargetedMessagesInArrivalOrder() {
        ServerMessageHistory history = new ServerMessageHistory();
        for (int i = 1; i <= 6; i++) {
            history.remember("message" + i + " " + ChatFilter.spaced("fairxaero"));
        }

        assertEquals(5, history.messagesOldestFirst().size());
        assertEquals("message2 fairxaero", history.messagesOldestFirst().getFirst().plainText());
        assertEquals("message6 fairxaero", history.messagesOldestFirst().getLast().plainText());
        assertEquals("message2 " + ChatFilter.spaced("fairxaero"),
                history.messagesOldestFirst().getFirst().raw());
    }

    @Test
    void serverMessageHistoryIgnoresUnrelatedFormatting() {
        ServerMessageHistory history = new ServerMessageHistory();
        history.remember("§aordinary formatted message");

        assertTrue(history.messagesOldestFirst().isEmpty());
    }

    @Test
    void manualProfilesMapToTheirModeInstructions() {
        assertEquals(List.of(), XaerounfairConfig.ManualProfile.DEFAULT.simulatedMessageKeys());
        assertEquals(List.of("nominimap"), XaerounfairConfig.ManualProfile.MINIMAP_OFF.simulatedMessageKeys());
        assertEquals(List.of("fairxaero", "xaerommnetherisfair"),
                XaerounfairConfig.ManualProfile.NETHER_CAVE.simulatedMessageKeys());
        assertEquals(List.of("fairxaero"), XaerounfairConfig.ManualProfile.BASIC_FAIRPLAY.simulatedMessageKeys());
    }

    @Test
    void simulatedInstructionFormattingIncludesXaeroColorCodes() {
        assertEquals("§n§o§m§i§n§i§m§a§p",
                ChatFilter.spaced(XaerounfairConfig.ManualProfile.MINIMAP_OFF.simulatedMessageKeys().getFirst()));
        assertEquals("§f§a§i§r§x§a§e§r§o",
                ChatFilter.spaced(XaerounfairConfig.ManualProfile.NETHER_CAVE.simulatedMessageKeys().getFirst()));
        assertEquals("§x§a§e§r§o§m§m§n§e§t§h§e§r§i§s§f§a§i§r",
                ChatFilter.spaced(XaerounfairConfig.ManualProfile.NETHER_CAVE.simulatedMessageKeys().get(1)));
        assertEquals("§f§a§i§r§x§a§e§r§o",
                ChatFilter.spaced(XaerounfairConfig.ManualProfile.BASIC_FAIRPLAY.simulatedMessageKeys().getFirst()));
    }

    @Test
    void everyManualProfileRunsItsSimulationOnJoin() {
        assertTrue(ResetSimulator.shouldRunOnJoin(manual(XaerounfairConfig.ManualProfile.DEFAULT)));
        assertTrue(ResetSimulator.shouldRunOnJoin(manual(XaerounfairConfig.ManualProfile.MINIMAP_OFF)));
        assertTrue(ResetSimulator.shouldRunOnJoin(manual(XaerounfairConfig.ManualProfile.NETHER_CAVE)));
        assertTrue(ResetSimulator.shouldRunOnJoin(manual(XaerounfairConfig.ManualProfile.BASIC_FAIRPLAY)));
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
