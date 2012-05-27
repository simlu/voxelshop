package com.pixelatedgames.es.systems;

import com.pixelatedgames.es.Entity;

import java.util.concurrent.ConcurrentHashMap;

/**
 * User: J
 * Date: 3/26/12
 * Time: 2:04 PM
 */
public class EntitySystem {
    private final ConcurrentHashMap<Long, Entity> _activeEntities = new ConcurrentHashMap<Long, Entity>();

    public void periodicProcess(long step){}
}
