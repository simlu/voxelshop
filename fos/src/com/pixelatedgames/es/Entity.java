package com.pixelatedgames.es;

import com.pixelatedgames.es.components.IComponent;
import com.pixelatedgames.es.systems.EntitySystem;

import java.util.concurrent.ConcurrentHashMap;

/**
 * User: J
 * Date: 3/24/12
 * Time: 12:43 AM
 */
public class Entity {
    private final EntityManager _entityManager;
    private final long _id;
    ConcurrentHashMap<Class<?>, IComponent> _components = new ConcurrentHashMap<Class<?>, IComponent>();

    public Entity(EntityManager entityManager, long id) {
        _entityManager = entityManager;
        _id = id;
    }

    public IComponent addComponent(IComponent component) {
        _components.put(component.getClass(), component);
        return component;
    }    
    
    public IComponent getComponent(Class<IComponent> componentClass) {
        return _components.get(componentClass);
    }

    public long getId() {
        return _id;
    }
}
