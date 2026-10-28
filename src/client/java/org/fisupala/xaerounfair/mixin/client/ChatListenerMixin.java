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
package org.fisupala.xaerounfair.mixin.client;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.fisupala.xaerounfair.client.ResetSimulator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Keep the message handler running so Xaero can inspect synthetic instructions; suppress only their output.
@Mixin(value = ChatListener.class, priority = 1)
public class ChatListenerMixin {
    @Redirect(
            method = "handleSystemMessage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addServerSystemMessage(Lnet/minecraft/network/chat/Component;)V"
            )
    )
    private void xaerounfair$hideSimulatedChat(ChatComponent chat, Component message) {
        if (!ResetSimulator.isSimulatedMessage(message)) {
            chat.addServerSystemMessage(message);
        }
    }

    @Redirect(
            method = "handleSystemMessage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/GameNarrator;saySystemChatQueued(Lnet/minecraft/network/chat/Component;)V"
            )
    )
    private void xaerounfair$hideSimulatedNarration(GameNarrator narrator, Component message) {
        if (!ResetSimulator.isSimulatedMessage(message)) {
            narrator.saySystemChatQueued(message);
        }
    }
}