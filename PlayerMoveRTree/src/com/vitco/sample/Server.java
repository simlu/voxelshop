package com.vitco.sample;

import com.vitco.objects.Entity;
import com.vitco.objects.Player;
import com.vitco.objects.World;

/**
 * Server Class (Sample)
 */
public class Server {

    private final World world = new World();

    public void onPlayerConnect(int x, int y) {
        // create new player instance and manage somehow
        Player player = new Player(x, y, world);
    }

    public void onPlayerDisconnect(Player player) {
        // notify everyone that this player left
        for (Entity entity : player.getVisibleList()) {
            // do the broadcasting, e.g.
            if (entity instanceof Player) {
                // entity.thisPlayerLeft(player)
            } else {
                // ???
            }
        }
        // remove this player from the world
        world.destroyEntity(player);
    }

    public void onPlayerMove(Player player, int x, int y) {
        // set the player position (this will trigger the visible list to update)
        player.setPosition(x, y);

        // get the visible list and broadcast the player position to all those entities
        // NOTE: this does not includes the player itself
        for (Entity entity : player.getVisibleList()) {
            // do the broadcasting, e.g.
            if (entity instanceof Player) {
                // entity.thereIsAPlayerNearby(player)
            } else {
                // ???
            }
        }

        // get the newly visible entities and broadcast to the player
        for (Entity entity : player.getNewVisibleList()) {
            if (entity instanceof Player) {
                // player.thereIsAPlayerNearby((Player)entity)
            } else {
                // player.thereIsAnEntityNearby(entity)
            }
        }
        player.clearNewVisibleList();

        // get the newly invisible entities and broadcast to the player
        for (Entity entity : player.getNewInvisibleList()) {
            if (entity instanceof Player) {
                // player.thisPlayerLeft((Player)entity)
            } else {
                // player.thisEntityLeft(entity)
            }
        }
        player.clearNewInvisibleList();
    }
}
