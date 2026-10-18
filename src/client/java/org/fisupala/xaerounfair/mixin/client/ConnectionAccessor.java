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

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Connection.class)
public interface ConnectionAccessor {
    @Accessor("channel")
    Channel xaerounfair$getChannel();
}