   /*
    * XaeroUnfair
    * Copyright (C) 2026 fisupala
    *
    * This program is free software: you can redistribute it and/or modify
    * it under the terms of the GNU General Public License version 3 only,
    * as published by the Free Software Foundation.
    *
    * This program is distributed in the hope that it will be useful,
    * but WITHOUT ANY WARRANTY; without even the implied warranty of
    * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    * GNU General Public License for more details.
    */
package org.fisupala.xaerounfair.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public final class ResetSimulator {
    // Tickeissä liittymisestä; toinen lähetys varmistaa, jos Xaero alustuu myöhään
    private static final int[] DELAYS = {10, 60};
    private static final Set<Component> SIMULATED_MESSAGES =
            Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

    private static int ticks = -1;
    private static int next = 0;
    private static List<String> messagesAfterReset = List.of();
    private static String sequenceMode = "RESET_ONLY";

    private ResetSimulator() {}

    public static boolean isSimulatedMessage(Component message) {
        return message != null && SIMULATED_MESSAGES.contains(message);
    }

    public static boolean consumeSimulatedMessage(Component message) {
        return SIMULATED_MESSAGES.remove(message);
    }

    static void registerSimulatedMessage(Component message) {
        SIMULATED_MESSAGES.add(message);
    }

    public static boolean shouldRunOnJoin(XaerounfairConfig.Profile profile) {
        return profile.mode() == XaerounfairConfig.Mode.MANUAL;
    }

    public static void schedule(XaerounfairConfig.Profile profile) {
        ticks = 0;
        next = 0;
        messagesAfterReset = List.copyOf(profile.manualProfile().simulatedMessageKeys());
        sequenceMode = profile.manualProfile().name();
    }

    public static boolean simulateNow(Minecraft mc) {
        return startSequence(mc, "RESET_ONLY", List.of());
    }

    public static boolean replayMessagesNow(Minecraft mc, List<String> messages) {
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return false;
        cancel();
        for (String message : messages) {
            fire(mc, "SERVER_ENFORCED_REPLAY", message);
        }
        return true;
    }

    public static boolean applyProfileNow(Minecraft mc, XaerounfairConfig.Profile profile) {
        if (profile.mode() == XaerounfairConfig.Mode.SERVER_ENFORCED) {
            cancel();
            XaerounfairClient.LOG.info("Not simulating any Xaero messages for mode SERVER_ENFORCED");
            return true;
        }
        return startSequence(mc, profile.manualProfile().name(), profile.manualProfile().simulatedMessageKeys());
    }

    public static void cancel() {
        ticks = -1;
        messagesAfterReset = List.of();
        sequenceMode = "RESET_ONLY";
    }

    public static void tick(Minecraft mc) {
        if (ticks < 0) return;
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return;
        ticks++;
        if (ticks >= DELAYS[next]) {
            fire(mc, sequenceMode, "resetxaero");
            next++;
            if (next >= DELAYS.length) {
                List<String> messages = messagesAfterReset;
                messagesAfterReset = List.of();
                ticks = -1;
                messages.forEach(key -> fire(mc, sequenceMode, key));
                sequenceMode = "RESET_ONLY";
            }
        }
    }

    private static boolean startSequence(Minecraft mc, String mode, List<String> afterReset) {
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return false;
        cancel();
        sequenceMode = mode;
        fire(mc, sequenceMode, "resetxaero");
        ticks = 0;
        next = 1;
        messagesAfterReset = List.copyOf(afterReset);
        return true;
    }

    private static void fire(Minecraft mc, String mode, String messageKey) {
        ClientPacketListener listener = mc.getConnection();
        if (listener == null) return;
        String formattedMessage = ChatFilter.spaced(messageKey);
        XaerounfairClient.LOG.info("Simulating mode {} message/color code: {}", mode, formattedMessage);
        Component message = Component.literal(formattedMessage);
        ClientboundSystemChatPacket packet = new ClientboundSystemChatPacket(message, false);
        registerSimulatedMessage(message);
        try {
            packet.handle(listener);
        } finally {
            consumeSimulatedMessage(message);
        }
    }
}