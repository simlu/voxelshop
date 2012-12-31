package com.pixelatedgames.fols.handlers;

import com.pixelatedgames.fols.protobufs.GenMsgs;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

public class login_handler extends ChannelInboundMessageHandlerAdapter<GenMsgs.gen_msg> {
    @Override
    public void messageReceived(ChannelHandlerContext channelHandlerContext, GenMsgs.gen_msg gen_msg) throws Exception {

    }
}
