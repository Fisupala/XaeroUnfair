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
        ClientPlayConnectionEvents.INIT.register((handler, client) ->
                ChatFilterHandler.install(handler.getConnection()));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ChatFilterHandler.install(handler.getConnection());
            ResetSimulator.schedule(); // myös Velocity-palvelinvaihdossa
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ResetSimulator.cancel());

        ClientTickEvents.END_CLIENT_TICK.register(ResetSimulator::tick);
    }
}