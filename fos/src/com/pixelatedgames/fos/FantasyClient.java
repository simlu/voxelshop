package com.pixelatedgames.fos;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: J
 * Date: 3/25/12
 * Time: 11:58 PM
 */
public class FantasyClient {
    private static final Logger logger = LoggerFactory.getLogger(FantasyClient.class);
    private final FantasyServer _fantasyServer;
    private final Channel _channel;

    public FantasyClient(FantasyServer fantasyServer, Channel channel) {
        _fantasyServer = fantasyServer;
        _channel = channel;

        logger.info("CONNECTED {}", channel);

        // listen for channel closing
        _channel.getCloseFuture().addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) {
                // clean up and stop processing
                // NEEDED HERE

                // kill ourself

                logger.info("DISCONNECTED {}", future.getChannel());
            }
        });
    }

    public void kick() {
        _channel.close();
    }

    public FantasyServer getFantasyServer() {
        return _fantasyServer;
    }

    public Channel getChannel() {
        return _channel;
    }
}
