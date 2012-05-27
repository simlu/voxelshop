package com.pixelatedgames.fos.handlers;

import com.pixelatedgames.fos.FantasyClient;
import com.pixelatedgames.fos.protobufs.FantasyMessages;

/**
 * User: J
 * Date: 3/26/12
 * Time: 12:12 AM
 */
public interface IMessageHandler {
    // fantasyClient and fantasyMessage and guaranteed to be non-null
    public void messageReceived(FantasyClient fantasyClient, FantasyMessages.FantasyMessage fantasyMessage);
}
