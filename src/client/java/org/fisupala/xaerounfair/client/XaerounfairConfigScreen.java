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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class XaerounfairConfigScreen extends Screen {
    private static final int BUTTON_WIDTH = 310;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen parent;
    private boolean editingServer;

    public XaerounfairConfigScreen(Screen parent) {
        super(Component.literal("XaeroUnfair Settings"));
        this.parent = parent;
        this.editingServer = XaerounfairConfig.activeServerAddress() != null;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int left = centerX - BUTTON_WIDTH / 2;
        int top = Math.max(28, this.height / 2 - 100);
        String serverAddress = XaerounfairConfig.activeServerAddress();

        if (serverAddress != null) {
            String shortAddress = serverAddress.length() > 35 ? serverAddress.substring(0, 32) + "..." : serverAddress;
            String profileName = editingServer ? "This server: " + shortAddress : "Global defaults";
            addButton(left, top, profileName, button -> {
                editingServer = !editingServer;
                rebuild();
            });
        }

        XaerounfairConfig.Profile profile = selectedProfile();
        int row = serverAddress == null ? top : top + 25;
        addButton(left, row, "Mode: " + modeLabel(profile), button -> updateProfile(new XaerounfairConfig.Profile(
                profile.mode() == XaerounfairConfig.Mode.MANUAL
                        ? XaerounfairConfig.Mode.SERVER_ENFORCED
                        : XaerounfairConfig.Mode.MANUAL,
                profile.honorFairxaero(),
                profile.honorNether(),
                profile.honorNoMinimap()
        )));

        row += 27;
        addButton(left, row, restrictionLabel("No Cave Mode or entities (fairxaero)", profile.honorFairxaero()),
                button -> updateProfile(new XaerounfairConfig.Profile(profile.mode(), !profile.honorFairxaero(),
                        profile.honorNether(), profile.honorNoMinimap())));
        row += 25;
        addButton(left, row, restrictionLabel("Nether: no entities, Cave Mode allowed", profile.honorNether()),
                button -> updateProfile(new XaerounfairConfig.Profile(profile.mode(), profile.honorFairxaero(),
                        !profile.honorNether(), profile.honorNoMinimap())));
        row += 25;
        addButton(left, row, restrictionLabel("Minimap off", profile.honorNoMinimap()),
                button -> updateProfile(new XaerounfairConfig.Profile(profile.mode(), profile.honorFairxaero(),
                        profile.honorNether(), !profile.honorNoMinimap())));
        row += 34;
        Button resetButton = addButton(left, row, "Simulate reset sequence", button -> triggerReset());
        Minecraft client = Minecraft.getInstance();
        resetButton.active = client.player != null && client.level != null && client.getConnection() != null;
        row += 25;
        addButton(left, row, "Done", button -> onClose());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }

    private XaerounfairConfig.Profile selectedProfile() {
        String address = XaerounfairConfig.activeServerAddress();
        return editingServer && address != null
                ? XaerounfairConfig.serverProfile(address)
                : XaerounfairConfig.defaultsProfile();
    }

    private void updateProfile(XaerounfairConfig.Profile profile) {
        String address = XaerounfairConfig.activeServerAddress();
        if (editingServer && address != null) {
            XaerounfairConfig.setServerProfile(address, profile);
        } else {
            XaerounfairConfig.setDefaults(profile);
        }
        if (XaerounfairConfig.activeProfile().mode() == XaerounfairConfig.Mode.SERVER_ENFORCED) {
            ResetSimulator.cancel();
        }
        rebuild();
    }

    private void rebuild() {
        this.rebuildWidgets();
    }

    private Button addButton(int x, int y, String label, Button.OnPress action) {
        return this.addRenderableWidget(Button.builder(Component.literal(label), action)
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    private void triggerReset() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.getConnection() == null) {
            XaerounfairClient.LOG.warn("Cannot simulate Xaero reset without an active world and connection");
            return;
        }
        ResetSimulator.schedule();
    }

    private static String modeLabel(XaerounfairConfig.Profile profile) {
        return profile.mode() == XaerounfairConfig.Mode.SERVER_ENFORCED ? "Server enforced" : "Manual";
    }

    private static String restrictionLabel(String label, boolean honor) {
        return label + ": " + (honor ? "Honor" : "Ignore");
    }
}
