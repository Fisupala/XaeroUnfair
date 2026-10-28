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

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ServerMessageHistoryScreen extends Screen {
    private static final int ROW_HEIGHT = 27;
    private static final int FIELD_HEIGHT = 20;

    private final Screen parent;

    public ServerMessageHistoryScreen(Screen parent) {
        super(Component.literal("Recent Server Messages"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        List<ServerMessageHistory.Message> messages = ChatFilterHandler.serverMessages().messagesOldestFirst();
        int availableWidth = Math.max(120, this.width - 32);
        int nameWidth = Math.min(150, Math.max(60, availableWidth / 3));
        int rawWidth = Math.max(40, availableWidth - nameWidth - 5);
        int totalWidth = rawWidth + nameWidth + 5;
        int left = (this.width - totalWidth) / 2;
        int rawX = left;
        int nameX = rawX + rawWidth + 5;
        int top = 48;

        for (int i = 0; i < messages.size(); i++) {
            ServerMessageHistory.Message message = messages.get(i);
            int y = top + i * ROW_HEIGHT;

            EditBox rawField = new EditBox(this.font, rawX, y, rawWidth, FIELD_HEIGHT, Component.literal("Message"));
            rawField.setMaxLength(2048);
            rawField.setValue(message.raw());
            rawField.setEditable(false);
            this.addRenderableWidget(rawField);

            EditBox plainField = new EditBox(this.font, nameX, y, nameWidth, FIELD_HEIGHT, Component.literal("Plain text"));
            plainField.setMaxLength(2048);
            plainField.setValue(message.plainText());
            plainField.setEditable(false);
            this.addRenderableWidget(plainField);
        }

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> close())
                .bounds(this.width / 2 - 50, Math.min(this.height - 30, top + messages.size() * ROW_HEIGHT + 8), 100, FIELD_HEIGHT)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        graphics.centeredText(this.font, Component.literal("Oldest first; Server enforced replays in this order"),
                this.width / 2, 34, 0xA0A0A0);
        if (ChatFilterHandler.serverMessages().messagesOldestFirst().isEmpty()) {
            graphics.centeredText(this.font, Component.literal("No targeted color-code messages received yet"),
                    this.width / 2, 65, 0xA0A0A0);
        }
    }

    @Override
    public void onClose() {
        close();
    }

    private void close() {
        this.minecraft.setScreenAndShow(parent);
    }
}
