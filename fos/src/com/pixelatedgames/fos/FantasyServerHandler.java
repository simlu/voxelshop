package com.pixelatedgames.fos;

import com.pixelatedgames.fos.handlers.LoginHandler;
import com.pixelatedgames.fos.protobufs.FantasyMessages;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: J
 * Date: 3/4/12
 * Time: 10:45 AM
 */
public class FantasyServerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(FantasyServerHandler.class);
    private final FantasyServer _fantasyServer;

    public FantasyServerHandler(FantasyServer fantasyServer) {
        _fantasyServer = fantasyServer;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        // get the client and make sure we're dealing with it
        FantasyClient fantasyClient = _fantasyServer.channelToFantasyClient.get(ctx.getChannel());
        if(fantasyClient != null) {
            // sync on the client to deal with one message at a time (not sure if this is necessary)
            synchronized (fantasyClient) {
                // get the message and make sure it is what it should be
                FantasyMessages.FantasyMessage fantasyMessage = (FantasyMessages.FantasyMessage)e.getMessage();
                if(fantasyMessage != null) {
                    int type = fantasyMessage.getType().getNumber();
                    _fantasyServer.runMessageHandler(type, fantasyClient, fantasyMessage);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.info("{} {}", ctx, e);
    }


    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        _fantasyServer.channelToFantasyClient.put(ctx.getChannel(), new FantasyClient(_fantasyServer, ctx.getChannel()));
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        _fantasyServer.channelToFantasyClient.remove(ctx.getChannel());
    }
}
