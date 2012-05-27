package com.pixelatedgames.es;

import com.pixelatedgames.es.components.IComponent;
import com.pixelatedgames.es.systems.EntitySystem;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * User: J
 * Date: 3/26/12
 * Time: 1:40 PM
 */
public class EntityManager {
    private long _uniqueEntityId;
    private final ConcurrentHashMap<Long, Entity> _activeEntities = new ConcurrentHashMap<Long, Entity>();
    private final ConcurrentLinkedQueue<EntitySystem> _activeSystems = new ConcurrentLinkedQueue<EntitySystem>();
    private final ConcurrentHashMap<Class<?>, EntitySystem> _activeSystemsByClass = new ConcurrentHashMap<Class<?>, EntitySystem>();
    
    public Entity createEntity() {
        // could be creating several at the same time
        // make sure id's are incremental
        synchronized (this) {
            // create the entity with a new id
            // give it this for component add/remove/etc
            Entity entity = new Entity(this, _uniqueEntityId);
            // store it as active
            _activeEntities.put(entity.getId(), entity);
            return entity;
        }
    }

    public EntitySystem addEntitySystem(EntitySystem entitySystem) {
        _activeSystems.add(entitySystem);
        _activeSystemsByClass.put(entitySystem.getClass(), entitySystem);
        return entitySystem;
        /*
        system.setWorld(world);

        //systems.put(system.getClass(), system);

        if(!bagged.contains(system))
            bagged.add(system);

        system.setSystemBit(SystemBitManager.getBitFor(system.getClass()));

        //return system;
        */
    }

    public void periodicProcess(long step) {
        // iterate the systems and process
        Iterator it = _activeSystems.iterator();
        while (it.hasNext()) {
            EntitySystem entitySystem = (EntitySystem)it.next();

        }
    }

    private long nextEntityId() {
        _uniqueEntityId++;
        if(_uniqueEntityId < 0) {
            _uniqueEntityId = 0;
        }
        return _uniqueEntityId;
    }
}
