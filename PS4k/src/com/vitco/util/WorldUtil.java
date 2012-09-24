package com.vitco.util;

import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.Light;
import com.vitco.res.VitcoSettings;

import java.awt.*;

/**
 * Class that provides basic functions for world manipulation
 */
public class WorldUtil {
    // add a light-source
    public static void addLight (World world, SimpleVector position, float strength) {
        Light light = new Light(world);
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

    private final static float cdis = VitcoSettings.VOXEL_SIZE/2;
    private final static SimpleVector c1 = new SimpleVector(cdis, cdis, cdis);
    private final static SimpleVector c2 = new SimpleVector(cdis, -cdis, cdis);
    private final static SimpleVector c3 = new SimpleVector(cdis, -cdis, -cdis);
    private final static SimpleVector c4 = new SimpleVector(cdis, cdis, -cdis);
    private final static SimpleVector c5 = new SimpleVector(-cdis, cdis, cdis);
    private final static SimpleVector c6 = new SimpleVector(-cdis, -cdis, cdis);
    private final static SimpleVector c7 = new SimpleVector(-cdis, -cdis, -cdis);
    private final static SimpleVector c8 = new SimpleVector(-cdis, cdis, -cdis);

    // add a box to the world
    public static int addBoxSides (World world, SimpleVector pos, float size, Color color, boolean[] sides, int triCount) {
        // set the amount of triangles we need
        Object3D box = new Object3D(triCount);

        // add the triangles
        if (!sides[0]) {
            box.addTriangle(c1, c2, c3);
            box.addTriangle(c1, c3, c4);
        }
        if (!sides[1]) {
            box.addTriangle(c7, c6, c5);
            box.addTriangle(c8, c7, c5);
        }
        if (!sides[2]) {
            box.addTriangle(c1, c8, c5);
            box.addTriangle(c1, c4, c8);
        }
        if (!sides[3]) {
            box.addTriangle(c2, c6, c7);
            box.addTriangle(c2, c7, c3);
        }
        if (!sides[4]) {
            box.addTriangle(c1, c6, c2);
            box.addTriangle(c1, c5, c6);
        }
        if (!sides[5]) {
            box.addTriangle(c3, c8, c4);
            box.addTriangle(c3, c7, c8);
        }

        // set other settings, build and add
        box.setAdditionalColor(color);
        box.setEnvmapped(Object3D.ENVMAP_ENABLED);
        box.setShadingMode(Object3D.SHADING_FAKED_FLAT);
        box.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
        box.setOrigin(pos);
        box.build();
        world.addObject(box);
        return box.getID();
    }

    // add a plane to the world
    // default rotation is parallel to xz-plane
    public static int addPlane (World world, SimpleVector pos, SimpleVector rotation, Float size, Color color, Integer alpha) {
        // add the world plane (ground)
        Object3D plane = Primitives.getPlane(1, size);
        plane.setCulling(false); //show from both sides
        plane.setTransparency(alpha);
        plane.setAdditionalColor(color);
        plane.setOrigin(pos);
        plane.rotateX(rotation.x + (float)Math.PI/2);
        plane.rotateY(rotation.y);
        plane.rotateZ(rotation.z);
        world.addObject(plane);
        return plane.getID();
    }
}
