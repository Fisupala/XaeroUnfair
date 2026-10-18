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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.fisupala.xaerounfair.mixin.client.ConnectionAccessor;

public final class ChatFilterHandler extends ChannelInboundHandlerAdapter {
    public static final String NAME = "xaerounfair_first";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (shouldDrop(msg)) {
            XaerounfairClient.LOG.info("Dropped filtered chat packet");
            return; // ei välitetä eteenpäin: kukaan muu ei näe sitä
        }
        ctx.fireChannelRead(msg);
    }

    private static boolean shouldDrop(Object msg) {
        if (msg instanceof ClientboundSystemChatPacket p) return ChatFilter.matches(p.content());
        if (msg instanceof ClientboundDisguisedChatPacket p) return ChatFilter.matches(p.message());
        return false;
    }

    /** Idempotentti: turvallista kutsua monta kertaa (INIT, JOIN, Velocity-vaihdot). */
    public static void install(Connection connection) {
        Channel channel = ((ConnectionAccessor) connection).xaerounfair$getChannel();        if (channel == null) return;
        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.get(NAME) != null) return;
        if (pipeline.get("packet_handler") == null) {
            XaerounfairClient.LOG.warn("packet_handler not found in pipeline, filter not installed");
            return;
        }
        pipeline.addBefore("packet_handler", NAME, new ChatFilterHandler());
    }
}