package com.pixelatedgames.fos;

import com.pixelatedgames.fos.handlers.fos_server_initializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class fos_server {
    private ServerBootstrap b = new ServerBootstrap();

    public void run() throws Exception {
        try {
            b.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                    .channel(new NioServerSocketChannel())
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .localAddress(8080)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                new LoggingHandler(LogLevel.INFO),
                                new fos_server_initializer()
                            );
                        }
                    });

            // Start the server.
            ChannelFuture f = b.bind().sync();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            b.shutdown();
        }
    }
}
