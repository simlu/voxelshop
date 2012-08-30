package com.pixelatedgames.fos.handlers;

import com.pixelatedgames.fos.protobufs.FantasyMessages;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

public class fos_server_handler extends ChannelInboundMessageHandlerAdapter<FantasyMessages.fantasy_message> {
    @Override
    public void messageReceived(ChannelHandlerContext channelHandlerContext, FantasyMessages.fantasy_message fantasyMessage) throws Exception {

    }
}
