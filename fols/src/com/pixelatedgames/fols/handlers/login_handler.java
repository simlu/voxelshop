package com.pixelatedgames.fols.handlers;

import com.pixelatedgames.fos.protobufs.FoMsgs;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class login_handler extends ChannelInboundMessageHandlerAdapter<FoMsgs.fo_msg> {
    @Override
    public void messageReceived(ChannelHandlerContext channelHandlerContext,FoMsgs.fo_msg fo_msg) throws Exception {

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent)evt;

            // if we haven't received any data in awhile disconnect
            if(e.state() == IdleState.READER_IDLE) {
                ctx.channel().close();
            }
        }
    }
}
