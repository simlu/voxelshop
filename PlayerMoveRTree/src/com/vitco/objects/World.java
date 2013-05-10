package com.vitco.objects;

import com.vitco.util.RTree;

/**
 * A world that entities live in.
 */
public class World {
    // the RTree that manages all entities
    protected final RTree<Entity> entityList = new RTree<Entity>();

    // removes an entity from the world
    public void destroyEntity(Player player) {
        player.destroyEntity();
    }
}
