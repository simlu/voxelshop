package com.pixelatedgames.es.components;

import com.pixelatedgames.fos.FantasyClient;

/**
 * User: J
 * Date: 3/26/12
 * Time: 2:06 PM
 */
public class ClientComponent implements IComponent{
    private final FantasyClient _fantasyClient;

    public ClientComponent(FantasyClient fantasyClient) {
        _fantasyClient = fantasyClient;
    }

    @Override
    public int getType() {
        return 0; // unique identifier
    }
}
