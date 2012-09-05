package com.vitco.util;

import com.threed.jpct.*;
import com.threed.jpct.util.Light;

import java.awt.*;

/**
 * Class that provides basic functions for world manipulation
 */
public class WorldUtil {
    // add a light-source
    public static void addLight (World world, SimpleVector position, float strength) {
        Light light = new Light(world);
        light.setAttenuation(1000); // high ~ light shines far
        light.setDiscardDistance(-1);
        light.setIntensity(strength, strength, strength);
        light.setPosition(position);
    }

    // add a box to the world
    public static int addBox (World world, SimpleVector pos, float size, Color color) {
        Object3D box = Primitives.getCube(size);
        box.setAdditionalColor(color);
        box.setEnvmapped(Object3D.ENVMAP_ENABLED);
        box.setShadingMode(Object3D.SHADING_FAKED_FLAT);
        box.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
        box.setOrigin(pos);
        box.rotateY((float) Math.PI / 4); // align correctly
        box.build();
        world.addObject(box);
        return box.getID();
    }
}
