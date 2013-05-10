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
        synchronized (world) {
            Player player = new Player(x, y, world);
        }
    }

    public void onPlayerDisconnect(Player player) {
        // needs to be "batch" synchronized since another might "find" this entity
        // while we're notifying the player in the visibility list
        Entity[] visList;
        synchronized (world) {
            visList = player.getVisibleList();
            // remove this player from the world
            world.destroyEntity(player);
        }
        // notify everyone that this player left
        for (Entity entity : visList) {
            // do the broadcasting, e.g.
            if (entity instanceof Player) {
                // entity.thisPlayerLeft(player)
            } else {
                // ???
            }
        }

    }

    public void onPlayerMove(Player player, int x, int y) {
        // set the player position (this will trigger the visible list to update)
        Entity[] visList;
        Entity[] newVisList;
        Entity[] newInvisList;
        // this needs to be "batch" synchronized since the visibility list is
        // updated by other entities moving and that would make the other two
        // lists outdated as well
        synchronized (world) {
            player.setPosition(x, y);
            visList = player.getVisibleList();
            newVisList = player.getNewVisibleList();
            player.clearNewVisibleList();
            newInvisList = player.getNewInvisibleList();
            player.clearNewInvisibleList();
        }

        // get the visible list and broadcast the player position to all those entities
        // NOTE: this does not includes the player itself
        for (Entity entity : visList) {
            // do the broadcasting, e.g.
            if (entity instanceof Player) {
                // entity.thereIsAPlayerNearby(player)
            } else {
                // ???
            }
        }

        // get the newly visible entities and broadcast to the player
        for (Entity entity : newVisList) {
            if (entity instanceof Player) {
                // player.thereIsAPlayerNearby((Player)entity)
            } else {
                // player.thereIsAnEntityNearby(entity)
            }
        }


        // get the newly invisible entities and broadcast to the player
        for (Entity entity : newInvisList) {
            if (entity instanceof Player) {
                // player.thisPlayerLeft((Player)entity)
            } else {
                // player.thisEntityLeft(entity)
            }
        }

    }
}
