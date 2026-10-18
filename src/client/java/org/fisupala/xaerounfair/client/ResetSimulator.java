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

public final class ResetSimulator {
    // Tickeissä liittymisestä; toinen lähetys varmistaa, jos Xaero alustuu myöhään
    private static final int[] DELAYS = {10, 60};

    private static int ticks = -1;
    private static int next = 0;
    private static boolean simulating = false;

    private ResetSimulator() {}

    public static boolean isSimulating() { return simulating; }

    public static void schedule() { ticks = 0; next = 0; }

    public static void cancel() { ticks = -1; }

    public static void tick(Minecraft mc) {
        if (ticks < 0) return;
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return;
        ticks++;
        if (ticks >= DELAYS[next]) {
            fire(mc);
            next++;
            if (next >= DELAYS.length) ticks = -1;
        }
    }

    private static void fire(Minecraft mc) {
        ClientPacketListener listener = mc.getConnection();
        if (listener == null) return;
        XaerounfairClient.LOG.info("Simulating resetxaero");
        simulating = true;
        try {
            listener.handleSystemChat(
                    new ClientboundSystemChatPacket(Component.literal(ChatFilter.RESET_MESSAGE), false));
        } finally {
            simulating = false;
        }
    }
}