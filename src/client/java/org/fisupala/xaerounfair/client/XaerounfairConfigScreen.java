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
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class XaerounfairConfigScreen extends Screen {
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int HISTORY_BUTTON_SIZE = 20;
    private static final int HISTORY_BUTTON_GAP = 4;
    private static final int COLUMN_GAP = 8;
    private static final int ROW_GAP = 5;

    private final Screen parent;
    private final String serverAddress;
    private final XaerounfairConfig.Profile initialDefaults;
    private final XaerounfairConfig.Profile initialServerProfile;
    private XaerounfairConfig.Profile draftDefaults;
    private XaerounfairConfig.Profile draftServerProfile;
    private boolean editingServer;
    private Button modeButton;
    private int modeButtonX;
    private int modeButtonY;

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

        modeButtonX = left;
        modeButtonY = top;
        modeButton = addButton(left, top, settingText("Mode: " + modeLabel(profile.mode()), profile.mode() != originalProfile().mode()),
                "Left-click to view the last five targeted server messages. Right-click to switch modes; switching to Server enforced replays those messages.",
                button -> openMessageHistory());
        modeButton.setWidth(BUTTON_WIDTH - HISTORY_BUTTON_SIZE - HISTORY_BUTTON_GAP);
        this.addRenderableWidget(Button.builder(Component.literal("..."), button -> openMessageHistory())
                .bounds(left + BUTTON_WIDTH - HISTORY_BUTTON_SIZE, top, HISTORY_BUTTON_SIZE, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.literal("View recent server messages")))
                .build());
        Button profileButton = addButton(right, top, settingText("Manual profile: " + profileLabel(profile.manualProfile()),
                        profile.manualProfile() != originalProfile().manualProfile()),
                "Choose what to apply in Manual mode: Default filters everything; Minimap off applies nominimap; Nether Cave applies fairxaero plus xaerommnetherisfair; Basic FairPlay applies fairxaero.",
                button -> updateProfile(new XaerounfairConfig.Profile(profile.mode(), profile.manualProfile().next())));
        profileButton.active = profile.mode() == XaerounfairConfig.Mode.MANUAL;

        top += rowHeight + 4;
        Minecraft client = Minecraft.getInstance();
        Button applyButton = addButton(left, top, Component.literal("Apply"),
                "Save changed profiles, reset Xaero, then apply the selected profile's instruction(s) while connected.",
                button -> applyAndClose());
        applyButton.active = hasChanges() || client.player != null && client.level != null && client.getConnection() != null;
        addButton(right, top, Component.literal("Discard"),
                "Discard all unsaved changes and close this screen.", button -> discardAndClose());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 1 && modeButton != null
                && event.x() >= modeButtonX && event.x() < modeButtonX + modeButton.getWidth()
                && event.y() >= modeButtonY && event.y() < modeButtonY + modeButton.getHeight()) {
            cycleMode();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
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
                Component.literal("Save your changes before leaving, or discard them?"),
                Component.literal("Save"),
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

    private void openMessageHistory() {
        this.minecraft.setScreenAndShow(new ServerMessageHistoryScreen(this));
    }

    private void cycleMode() {
        XaerounfairConfig.Profile profile = selectedProfile();
        XaerounfairConfig.Mode newMode = profile.mode() == XaerounfairConfig.Mode.MANUAL
                ? XaerounfairConfig.Mode.SERVER_ENFORCED
                : XaerounfairConfig.Mode.MANUAL;
        updateProfile(new XaerounfairConfig.Profile(newMode, profile.manualProfile()));
        if (newMode == XaerounfairConfig.Mode.SERVER_ENFORCED) {
            replayRememberedMessages();
        }
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

    private void replayRememberedMessages() {
        List<String> messages = ChatFilterHandler.serverMessages().messagesOldestFirst().stream()
                .map(ServerMessageHistory.Message::raw)
                .toList();
        if (!ResetSimulator.replayMessagesNow(Minecraft.getInstance(), messages)) {
            XaerounfairClient.LOG.warn("Cannot replay remembered server messages without an active world and connection");
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
