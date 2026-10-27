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

import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.fisupala.xaerounfair.client.ResetSimulator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Let other listeners inspect synthetic instructions before suppressing their visible chat output.
@Mixin(value = ChatListener.class, priority = 1)
public class ChatListenerMixin {
    @Inject(method = "handleSystemMessage", at = @At("HEAD"), cancellable = true)
    private void xaerounfair$hideSimulated(Component message, boolean overlay, CallbackInfo ci) {
        if (ResetSimulator.consumeSimulatedMessage(message)) ci.cancel();
    }
}