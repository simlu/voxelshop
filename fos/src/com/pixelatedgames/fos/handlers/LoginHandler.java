package com.pixelatedgames.fos.handlers;

import com.google.protobuf.Descriptors;
import com.pixelatedgames.es.Entity;
import com.pixelatedgames.es.EntityManager;
import com.pixelatedgames.es.components.ClientComponent;
import com.pixelatedgames.es.components.PositionComponent;
import com.pixelatedgames.fos.FantasyClient;
import com.pixelatedgames.fos.FantasyServer;
import com.pixelatedgames.fos.protobufs.FantasyMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: J
 * Date: 3/26/12
 * Time: 12:13 AM
 */
public class LoginHandler implements IMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    @Override
    public void messageReceived(FantasyClient fantasyClient, FantasyMessages.FantasyMessage fantasyMessage) {
        if(fantasyMessage.hasLogin()) {
            FantasyMessages.Login login = fantasyMessage.getLogin();

            // validate login somehow

            // if we're good to go
            if(true) {
                // create en entity to hold the client
                EntityManager entityManager = fantasyClient.getFantasyServer().getEntityManager();
                Entity entity = entityManager.createEntity();
                // add the connection info
                entity.addComponent(new ClientComponent(fantasyClient));

                // load the player data here to populate components
                // should this go here? maybe, maybe not
                // where do we tell this client about all entities near it?

                // position it
                PositionComponent positionComponent = new PositionComponent(0,0);
                FantasyMessages.Position position =
                        FantasyMessages.Position.newBuilder()
                        .setX(22)
                        .setY(43)
                        .build();                
                FantasyMessages.FantasyMessage.Builder messageBuilder =
                        FantasyMessages.FantasyMessage.newBuilder()
                        .setType(position.getType());

                // figure out type...
                // how can we map this...
                messageBuilder.setPosition(position);

                //entity.addComponent();

                // lets test sending messages back
                fantasyClient.getChannel().write(messageBuilder.build());


                logger.info("{} logged in.", login.getUsername());
            } else {
                // kick if bad
                //fantasyClient.kick();
            }
        }
    }
}
