package com.vitco.objects;

import com.infomatiq.jsi.Rectangle;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An entity. This could be a mob, a player or anything that moves around.
 */
public abstract class Entity {
    // ========== static parameter ============
    // fine tune this for better performance
    // the higher, the less load delay but more load on the server
    // Note: you can set this value smaller 1 for debug purpose
    protected static final float bufferParameter = 2; // minimum value is 1
    // client screen size
    protected static final int screenWidth = 760;
    protected static final int screenHeight = 480;
    // client buffer size
    private static final int noRefreshWidth = Math.round((screenWidth * bufferParameter) / 2);
    private static final int noRefreshHeight = Math.round((screenHeight * bufferParameter) / 2);
    // ========================================

    // ====== entity state information ========
    // the world this entity lives in
    private final World world;
    // center of the current rectangle
    private int rectCenterX = 0;
    private int rectCenterY = 0;
    // holds the visible list of this entity
    private final HashSet<Entity> visibleList = new HashSet<Entity>();
    // holds the entities that became visible since the
    // last call to clearNewVisibleList()
    private final HashSet<Entity> newVisibleList = new HashSet<Entity>();
    // holds the entities that became invisible since the
    // last call to clearNewInvisibleList()
    private final HashSet<Entity> newInvisibleList = new HashSet<Entity>();

    // updates the buffer rect
    private void checkEntityInWorld(int x, int y) {
        if (Math.abs(x - rectCenterX) > noRefreshWidth ||
                Math.abs(y - rectCenterY) > noRefreshHeight) {
            world.entityList.delete(new Rectangle(rectCenterX, rectCenterY, rectCenterX, rectCenterY), this);
            refreshEntityInWorld(x, y);
        }
    }

    // helper to clean up this entity
    private void removeEntityFromWorld() {
        world.entityList.delete(new Rectangle(rectCenterX, rectCenterY, rectCenterX, rectCenterY), this);
        for (Entity toRemove : visibleList) {
            toRemove.removeFromVisibleList(this);
        }
        visibleList.clear();
        newVisibleList.clear();
        newInvisibleList.clear();
    }

    // helper - core update that refreshes this entity
    private void refreshEntityInWorld(int x, int y) {
        rectCenterX = x;
        rectCenterY = y;
        world.entityList.insert(new Rectangle(rectCenterX, rectCenterY, rectCenterX, rectCenterY), this);

        // -- update the visible list
        List<Entity> nearbyEntities =
                world.entityList.search(new Rectangle(x - noRefreshWidth * 2, y - noRefreshHeight * 2,
                        x + noRefreshWidth * 2, y + noRefreshHeight * 2));
        nearbyEntities.remove(this); // exclude the entity itself

        // find the entities that are no longer visible and remove them
        Set<Entity> removedEntities = new HashSet<Entity>(visibleList);
        removedEntities.removeAll(nearbyEntities);
        for (Entity toRemove : removedEntities) {
            toRemove.removeFromVisibleList(this);
            visibleList.remove(toRemove);
        }

        // find the entities that just became visible
        Set<Entity> newEntities = new HashSet<Entity>(nearbyEntities);
        newEntities.removeAll(visibleList);
        for (Entity toAdd : newEntities) {
            toAdd.addToVisibleList(this);
            visibleList.add(toAdd);
        }

        // update the (in)visible lists
        newVisibleList.removeAll(removedEntities);
        newVisibleList.addAll(newEntities);
        newInvisibleList.removeAll(newEntities);
        newInvisibleList.addAll(removedEntities);
    }

    // internal - add an entry to the visible list
    private void addToVisibleList(Entity toAdd) {
        visibleList.add(toAdd);
    }

    //  internal - removes an entry from the visible list
    private void removeFromVisibleList(Entity toRemove) {
        visibleList.remove(toRemove);
    }

    // called by the world when this entity is destroyed
    protected final void destroyEntity() {
        removeEntityFromWorld();
    }

    // getter method for tests
    protected final Point getRectCenter() {
        return new Point(rectCenterX, rectCenterY);
    }

    // ========================================

    // retrieve the visible list
    public final Entity[] getVisibleList() {
        Entity[] result = new Entity[visibleList.size()];
        visibleList.toArray(result);
        return result;
    }

    // retrieve the entities that became visible (through this entity changing position,
    // NOT through the other entity changing position) since the last
    // call to clearNewVisibleList()
    public final Entity[] getNewVisibleList() {
        Entity[] result = new Entity[newVisibleList.size()];
        newVisibleList.toArray(result);
        return result;
    }

    // reset the new visible neighbors
    public final void clearNewVisibleList() {
        newVisibleList.clear();
    }

    // retrieve the entities that became invisible (through this entity changing position,
    // NOT through the other entity changing position) since the last
    // call to clearNewInvisibleList()
    public final Entity[] getNewInvisibleList() {
        Entity[] result = new Entity[newInvisibleList.size()];
        newInvisibleList.toArray(result);
        return result;
    }

    // reset the new visible neighbors
    public final void clearNewInvisibleList() {
        newInvisibleList.clear();
    }

    // ================================================

    // current entity position
    protected int entityPositionX = 0;
    protected int entityPositionY = 0;

    // constructor
    public Entity(int x, int y, World world) {
        this.world = world;
        refreshEntityInWorld(x, y);
    }

    // update entity position
    public final void setPosition(int x, int y) {
        entityPositionX = x;
        entityPositionY = y;
        checkEntityInWorld(x, y);
    }
}
