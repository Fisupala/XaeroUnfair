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

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XaerounfairClient implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("xaerounfair");

    @Override
    public void onInitializeClient() {
        XaerounfairConfig.load();

        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            XaerounfairConfig.activateServer(client.getCurrentServer() == null ? null : client.getCurrentServer().ip);
            ResetSimulator.cancel();
            ChatFilterHandler.install(handler.getConnection());
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ChatFilterHandler.install(handler.getConnection());
            XaerounfairConfig.activateServer(client.getCurrentServer() == null ? null : client.getCurrentServer().ip);
            XaerounfairConfig.Profile profile = XaerounfairConfig.activeProfile();
            if (ResetSimulator.shouldRunOnJoin(profile)) {
                ResetSimulator.schedule(profile);
            } else {
                ResetSimulator.cancel();
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ResetSimulator.cancel();
            XaerounfairConfig.activateServer(null);
        });

        ClientTickEvents.END_CLIENT_TICK.register(ResetSimulator::tick);
    }
}