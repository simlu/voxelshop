package com.pixelatedgames.fos;

import com.pixelatedgames.es.EntityManager;
import com.pixelatedgames.es.systems.FantasyClientSystem;
import com.pixelatedgames.fos.handlers.IMessageHandler;
import com.pixelatedgames.fos.handlers.LoginHandler;
import com.pixelatedgames.fos.protobufs.FantasyMessages;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * User: J
 * Date: 3/4/12
 * Time: 10:44 AM
 */
public class FantasyServer {
    // port to listen on
    private final int _port;

    // handles all entities
    private final EntityManager _entityManager = new EntityManager();

    // server update loop
    private static final int framerate = 60;
    private static ScheduledThreadPoolExecutor updateLoop = new ScheduledThreadPoolExecutor(1);

    // save our port
    public FantasyServer(int port) {
        _port = port;

        // initialize the message handlers
        initHandlers();

        // add the client specific processing system
        _entityManager.addEntitySystem(new FantasyClientSystem());
    }

    // run the actual server
    public void run() {
        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()
                )
        );

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new FantasyServerPipelineFactory(this));

        // Options for its children
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setOption("child.reuseAddress", true);

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(_port));

        // run update loop at twice the framerate
        updateLoop.scheduleAtFixedRate(new FantasyServerUpdateLoop(this, framerate), 0, 1000 / framerate / 2, TimeUnit.MILLISECONDS);
    }

    // so we create an update loop on another thread
    // then when it fires we call this function to update things...
    public void periodicProcess(long step) {
        // process all entity systems that are periodic?
        _entityManager.periodicProcess(step);
    }

    // get the server's entity manager
    public EntityManager getEntityManager() {
        return _entityManager;
    }

    // all currently connected clients
    // ONLY ALTERED BY FANTASYSERVERHANDLER
    public final ConcurrentHashMap<Channel, FantasyClient> channelToFantasyClient = new ConcurrentHashMap<Channel, FantasyClient>();

    // all message handlers
    private final ConcurrentHashMap<Integer, IMessageHandler> messageHandlers = new ConcurrentHashMap<Integer, IMessageHandler>();
    private void initHandlers() {
        messageHandlers.put(1, new LoginHandler());
    }
    // an easy way to call a message handler
    public void runMessageHandler(int type, FantasyClient fantasyClient, FantasyMessages.FantasyMessage fantasyMessage) {
        IMessageHandler messageHandler = messageHandlers.get(type);
        if(messageHandler != null) {
            messageHandler.messageReceived(fantasyClient, fantasyMessage);
        }
    }
}
