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

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class XaerounfairConfigScreen extends Screen {
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int COLUMN_GAP = 8;
    private static final int ROW_GAP = 5;

    private final Screen parent;
    private final String serverAddress;
    private final XaerounfairConfig.Profile initialDefaults;
    private final XaerounfairConfig.Profile initialServerProfile;
    private XaerounfairConfig.Profile draftDefaults;
    private XaerounfairConfig.Profile draftServerProfile;
    private boolean editingServer;

    public XaerounfairConfigScreen(Screen parent) {
        super(Component.literal("XaeroUnfair Settings"));
        this.parent = parent;
        this.serverAddress = XaerounfairConfig.activeServerAddress();
        this.editingServer = serverAddress != null;
        this.initialDefaults = XaerounfairConfig.defaultsProfile();
        this.draftDefaults = initialDefaults;
        this.initialServerProfile = serverAddress == null ? null : XaerounfairConfig.serverProfile(serverAddress);
        this.draftServerProfile = initialServerProfile;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int totalWidth = BUTTON_WIDTH * 2 + COLUMN_GAP;
        int left = centerX - totalWidth / 2;
        int right = left + BUTTON_WIDTH + COLUMN_GAP;
        int top = Math.max(24, this.height / 2 - 100);
        int rowHeight = BUTTON_HEIGHT + ROW_GAP;
        XaerounfairConfig.Profile profile = selectedProfile();

        if (serverAddress != null) {
            String target = editingServer ? "Profile: server" : "Profile: global";
            String targetTooltip = editingServer
                    ? "Edit the profile for " + serverAddress + "."
                    : "Edit the fallback profile used by servers without their own settings.";
            addButton(left, top, Component.literal(target).withStyle(hasChanges() ? ChatFormatting.YELLOW : ChatFormatting.WHITE),
                    targetTooltip,
                    button -> {
                        editingServer = !editingServer;
                        rebuildWidgets();
                    });
            top += rowHeight;
        }

        addButton(left, top, settingText("Mode: " + modeLabel(profile.mode()), profile.mode() != originalProfile().mode()),
                "Server enforced passes all FairPlay messages through. Manual applies the selected profile.",
                button -> updateProfile(new XaerounfairConfig.Profile(
                        profile.mode() == XaerounfairConfig.Mode.MANUAL
                                ? XaerounfairConfig.Mode.SERVER_ENFORCED
                                : XaerounfairConfig.Mode.MANUAL,
                        profile.manualProfile()
                )));
        Button profileButton = addButton(right, top, settingText("Manual profile: " + profileLabel(profile.manualProfile()),
                        profile.manualProfile() != originalProfile().manualProfile()),
                "Choose what to apply in Manual mode: Default filters everything; Minimap off applies nominimap; Nether Cave applies only xaerommnetherisfair; Basic FairPlay applies fairxaero.",
                button -> updateProfile(new XaerounfairConfig.Profile(profile.mode(), profile.manualProfile().next())));
        profileButton.active = profile.mode() == XaerounfairConfig.Mode.MANUAL;

        top += rowHeight + 4;
        Button resetButton = addButton(left, top, Component.literal("Simulate reset sequence"),
                "Run Xaero's reset sequence now. This is available only in Manual mode.",
                button -> triggerReset());
        resetButton.setWidth(totalWidth);
        resetButton.setX(left);
        Minecraft client = Minecraft.getInstance();
        resetButton.active = profile.mode() == XaerounfairConfig.Mode.MANUAL
                && client.player != null && client.level != null && client.getConnection() != null;

        top += rowHeight + 4;
        Button applyButton = addButton(left, top, Component.literal("Apply"),
                "Save changed profiles, reset Xaero, then apply only the selected mode's instruction while connected.",
                button -> applyAndClose());
        applyButton.active = hasChanges() || client.player != null && client.level != null && client.getConnection() != null;
        addButton(right, top, Component.literal("Discard"),
                "Discard all unsaved changes and close this screen.", button -> discardAndClose());
    }

    @Override
    public void onClose() {
        if (!hasChanges()) {
            closeScreen();
            return;
        }

        BooleanConsumer choice = save -> {
            if (save) {
                persistAndApply();
            } else {
                discardChanges();
            }
            closeScreen();
        };
        this.minecraft.setScreenAndShow(new ConfirmScreen(
                choice,
                Component.literal("Unsaved XaeroUnfair settings"),
                Component.literal("Apply your changes before leaving?"),
                Component.literal("Apply"),
                Component.literal("Discard")
        ));
    }

    private XaerounfairConfig.Profile selectedProfile() {
        return editingServer && serverAddress != null ? draftServerProfile : draftDefaults;
    }

    private XaerounfairConfig.Profile originalProfile() {
        return editingServer && serverAddress != null ? initialServerProfile : initialDefaults;
    }

    private boolean hasChanges() {
        return !draftDefaults.equals(initialDefaults)
                || (serverAddress != null && !draftServerProfile.equals(initialServerProfile));
    }

    private void updateProfile(XaerounfairConfig.Profile profile) {
        if (editingServer && serverAddress != null) {
            draftServerProfile = profile;
        } else {
            draftDefaults = profile;
        }
        rebuildWidgets();
    }

    private void applyAndClose() {
        persistAndApply();
        closeScreen();
    }

    private void discardAndClose() {
        discardChanges();
        closeScreen();
    }

    private void persistChanges() {
        if (!draftDefaults.equals(initialDefaults)) {
            XaerounfairConfig.setDefaults(draftDefaults);
        }
        if (serverAddress != null && !draftServerProfile.equals(initialServerProfile)) {
            XaerounfairConfig.setServerProfile(serverAddress, draftServerProfile);
        }
    }

    private void persistAndApply() {
        persistChanges();
        Minecraft client = Minecraft.getInstance();
        if (!ResetSimulator.applyProfileNow(client, XaerounfairConfig.activeProfile())
                && client.player != null && client.level != null && client.getConnection() != null) {
            XaerounfairClient.LOG.warn("Cannot apply XaeroUnfair mode without an active world and connection");
        }
    }

    private void discardChanges() {
        draftDefaults = initialDefaults;
        draftServerProfile = initialServerProfile;
    }

    private void closeScreen() {
        this.minecraft.setScreenAndShow(parent);
    }

    private Button addButton(int x, int y, Component label, String tooltip, Button.OnPress action) {
        return this.addRenderableWidget(Button.builder(label, action)
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.literal(tooltip)))
                .build());
    }

    private void triggerReset() {
        Minecraft client = Minecraft.getInstance();
        if (!ResetSimulator.simulateNow(client)) {
            XaerounfairClient.LOG.warn("Cannot simulate Xaero reset without an active world and connection");
        }
    }

    private Component settingText(String text, boolean changed) {
        return Component.literal(text).withStyle(changed ? ChatFormatting.YELLOW : ChatFormatting.WHITE);
    }

    private static String modeLabel(XaerounfairConfig.Mode mode) {
        return mode == XaerounfairConfig.Mode.SERVER_ENFORCED ? "Server enforced" : "Manual";
    }

    private static String profileLabel(XaerounfairConfig.ManualProfile profile) {
        return switch (profile) {
            case DEFAULT -> "Default";
            case MINIMAP_OFF -> "Minimap off";
            case NETHER_CAVE -> "Nether Cave";
            case BASIC_FAIRPLAY -> "Basic FairPlay";
        };
    }

}
