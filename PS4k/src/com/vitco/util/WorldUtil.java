package com.vitco.util;

import com.threed.jpct.*;
import com.threed.jpct.util.Light;
import com.vitco.res.VitcoSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Class that provides basic functions for world manipulation
 */
public class WorldUtil {
    //todo move this class somewhere else (not really a util anymore)

    private final static SimpleVector ZEROS = new SimpleVector(0,0,0);

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

    private final static HashMap<String, Object3D> boxTypes = new HashMap<String, Object3D>();

    private final static HashMap<String, String> rotationTranslation = new HashMap<String, String>();

    static {
        // pre-generate all the boxes
        float cdis = VitcoSettings.VOXEL_SIZE/2;
        SimpleVector upperLeftFront = new SimpleVector(-cdis,-cdis,-cdis);
        SimpleVector upperRightFront = new SimpleVector(cdis,-cdis,-cdis);
        SimpleVector lowerLeftFront = new SimpleVector(-cdis,cdis,-cdis);
        SimpleVector lowerRightFront = new SimpleVector(cdis,cdis,-cdis);

        SimpleVector upperLeftBack = new SimpleVector( -cdis, -cdis, cdis);
        SimpleVector upperRightBack = new SimpleVector(cdis, -cdis, cdis);
        SimpleVector lowerLeftBack = new SimpleVector( -cdis, cdis, cdis);
        SimpleVector lowerRightBack = new SimpleVector(cdis, cdis, cdis);
        // generate all possible objects
        for (int i = 0; i < 64; i++) {
            String bin = String.format("%06d", Integer.parseInt(Integer.toBinaryString(i)));
            int triCount = 0;
            for (int c = 0; c < 6; c++) {
                if (bin.charAt(c) == '0') {
                    triCount+=2;
                }
            }

            // set the amount of triangles we need
            Object3D box = new Object3D(triCount);

            // add the triangles
            if (bin.charAt(0) == '0') {
                // Right
                box.addTriangle(upperRightFront,0.25f+0.0625f*2,0.25f+0.0625f*2, lowerRightFront,0.25f+0.0625f*2,0.5f+0.0625f*2, upperRightBack,0.5f+0.0625f*2,0.25f+0.0625f*2);
                box.addTriangle(upperRightBack,0.5f+0.0625f*2,0.25f+0.0625f*2, lowerRightFront, 0.25f+0.0625f*2,0.5f+0.0625f*2, lowerRightBack,0.5f+0.0625f*2,0.5f+0.0625f*2);
            }
            if (bin.charAt(1) == '0') {
                // Left
                box.addTriangle(upperLeftFront,0+0.0625f,0.25f+0.0625f*2, upperLeftBack,0.25f+0.0625f,0.25f+0.0625f*2, lowerLeftFront,0+0.0625f,0.5f+0.0625f*2);
                box.addTriangle(upperLeftBack,0.25f+0.0625f,0.25f+0.0625f*2, lowerLeftBack,0.25f+0.0625f,0.5f+0.0625f*2, lowerLeftFront,0+0.0625f,0.5f+0.0625f*2);
            }
            if (bin.charAt(2) == '0') {
                // Lower
                box.addTriangle(lowerLeftBack,0.25f+0.0625f*2,0+0.0625f, lowerRightBack,0.5f+0.0625f*2,0+0.0625f, lowerLeftFront,0.25f+0.0625f*2,0.25f+0.0625f);
                box.addTriangle(lowerRightBack,0.5f+0.0625f*2,0+0.0625f, lowerRightFront,0.5f+0.0625f*2,0.25f+0.0625f, lowerLeftFront,0.25f+0.0625f*2,0.25f+0.0625f);
            }
            if (bin.charAt(3) == '0') {
                // Upper
                box.addTriangle(upperLeftBack,0+0.0625f,0+0.0625f, upperLeftFront,0+0.0625f,0.25f+0.0625f, upperRightBack,0.25f+0.0625f,0+0.0625f);
                box.addTriangle(upperRightBack,0.25f+0.0625f,0+0.0625f, upperLeftFront,0+0.0625f,0.25f+0.0625f, upperRightFront,0.25f+0.0625f,0.25f+0.0625f);
            }
            if (bin.charAt(4) == '0') {
                // Back
                box.addTriangle(upperLeftBack,0+0.0625f,0.5f+0.0625f*3, upperRightBack,0.25f+0.0625f,0.5f+0.0625f*3, lowerLeftBack,0+0.0625f,0.75f+0.0625f*3);
                box.addTriangle(upperRightBack,0.25f+0.0625f,0.5f+0.0625f*3, lowerRightBack,0.25f+0.0625f,0.75f+0.0625f*3, lowerLeftBack,0+0.0625f,0.75f+0.0625f*3);
            }
            if (bin.charAt(5) == '0') {
                // Front
                box.addTriangle(upperLeftFront,0.25f+0.0625f*2,0.5f+0.0625f*3, lowerLeftFront,0.25f+0.0625f*2,0.75f+0.0625f*3, upperRightFront,0.5f+0.0625f*2,0.5f+0.0625f*3);
                box.addTriangle(upperRightFront,0.5f+0.0625f*2,0.5f+0.0625f*3, lowerLeftFront,0.25f+0.0625f*2,0.75f+0.0625f*3, lowerRightFront,0.5f+0.0625f*2,0.75f+0.0625f*3);
            }

            boxTypes.put(bin, box);

            // generate the rotation translation hashmap
            // change box type according to rotation
            // (left, right, bottom, top, back, front)
            for (int rotation = 0; rotation < 4; rotation++) {
                char[] rot = bin.toCharArray();
                char tmp;
                switch (rotation) {
                    case 3:
                        // rotate
                        tmp = rot[0];
                        rot[0] = rot[4];
                        rot[4] = rot[1];
                        rot[1] = rot[5];
                        rot[5] = tmp;
                        break;
                    case 2:
                        // rotate
                        tmp = rot[0];
                        rot[0] = rot[1];
                        rot[1] = tmp;
                        tmp = rot[4];
                        rot[4] = rot[5];
                        rot[5] = tmp;
                        break;
                    case 1:
                        // rotate
                        tmp = rot[0];
                        rot[0] = rot[5];
                        rot[5] = rot[1];
                        rot[1] = rot[4];
                        rot[4] = tmp;
                        break;
                }
                rotationTranslation.put(bin + "_" + rotation, new String(rot) );
            }

        }

        // disable anti-aliasing for textures
        Config.texelFilter = false;
    }

    // load a texture to the world (from string)
    public static void loadTexture(String name, String url) {
        Image image = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource(url)
        )).getImage();
        loadTexture(name, image);
    }

    // load a texture to the world (from image)
    public static void loadTexture(String name, Image image) {
        // Create a texture from image
        BufferedImage text_top = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics g2 = text_top.getGraphics();
        g2.drawImage(image, 7, 7, 41, 41, 0, 0, 32, 32, null); // draw edges
        g2.drawImage(image, 8, 7, 40, 41, 0, 0, 32, 32, null); // draw edges
        g2.drawImage(image, 7, 8, 41, 40, 0, 0, 32, 32, null); // draw edges
        g2.drawImage(image, 8, 8, 40, 40, 0, 0, 32, 32, null);

        g2.drawImage(image, 47, 7, 81, 41, 64, 0, 32, 32, null); // draw edges
        g2.drawImage(image, 48, 7, 80, 41, 64, 0, 32, 32, null); // draw edges
        g2.drawImage(image, 47, 8, 81, 40, 64, 0, 32, 32, null); // draw edges
        g2.drawImage(image, 48, 8, 80, 40, 64, 0, 32, 32, null);

        g2.drawImage(image, 7, 47, 41, 81, 32, 32, 0, 64, null); // draw edges
        g2.drawImage(image, 8, 47, 40, 81, 32, 32, 0, 64, null); // draw edges
        g2.drawImage(image, 7, 48, 41, 80, 32, 32, 0, 64, null); // draw edges
        g2.drawImage(image, 8, 48, 40, 80, 32, 32, 0, 64, null);

        g2.drawImage(image, 47, 47, 81, 81, 32, 32, 64, 64, null); // draw edges
        g2.drawImage(image, 48, 47, 80, 81, 32, 32, 64, 64, null); // draw edges
        g2.drawImage(image, 47, 48, 81, 80, 32, 32, 64, 64, null); // draw edges
        g2.drawImage(image, 48, 48, 80, 80, 32, 32, 64, 64, null);

        g2.drawImage(image, 7, 87, 41, 121, 32, 64, 0, 96, null); // draw edges
        g2.drawImage(image, 8, 87, 40, 121, 32, 64, 0, 96, null); // draw edges
        g2.drawImage(image, 7, 88, 41, 120, 32, 64, 0, 96, null); // draw edges
        g2.drawImage(image, 8, 88, 40, 120, 32, 64, 0, 96, null);

        g2.drawImage(image, 47, 87, 81, 121, 32, 64, 64, 96, null); // draw edges
        g2.drawImage(image, 48, 87, 80, 121, 32, 64, 64, 96, null); // draw edges
        g2.drawImage(image, 47, 88, 81, 120, 32, 64, 64, 96, null); // draw edges
        g2.drawImage(image, 48, 88, 80, 120, 32, 64, 64, 96, null);

        Texture texture = new Texture(text_top);
        if (TextureManager.getInstance().containsTexture(name)) {
            TextureManager.getInstance().replaceTexture(name, texture);
        } else {
            TextureManager.getInstance().addTexture(name, texture);
        }
    }

    public static void loadTexture(String name, ImageIcon image) {
        // Create a texture from image
        BufferedImage text_top = new BufferedImage(image.getIconWidth(), image.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        text_top.getGraphics().drawImage(image.getImage(), 0, 0, null);
        loadTexture(name, text_top);
    }

    public static void removeTexture(String name) {
        TextureManager.getInstance().removeTexture(name);
    }

    // add a box to the world
    public static int addBoxSides (World world, SimpleVector pos, Color color,
                                   int textureId, String boxType, int rotation,
                                   boolean culling) {
        // translate the rotation
        boxType = rotationTranslation.get(boxType + "_" + rotation);

        Object3D box = boxTypes.get(boxType).cloneObject();
        // set other settings, build and add

        // select: color or texture
        if (textureId == -1 || !TextureManager.getInstance().containsTexture(String.valueOf(textureId))) { // for color overlay
            box.setAdditionalColor(color);
        } else { // for texture overlay
            box.setAdditionalColor(Color.WHITE);
            box.setTexture(String.valueOf(textureId));
        }

        // rotate the voxel
        if (rotation > 0) {
            box.rotateY((float)Math.PI * rotation / 2);
        }

        box.setOrigin(pos);
        box.setShadingMode(Object3D.SHADING_FAKED_FLAT);
        box.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
        box.setCulling(culling);

        if (boxType.equals("111111")) { // no need to show this object
            box.setVisibility(false);
        }
        box.build();
        // set true rotation center (this needs to be done after call build())
        // this is needed since build will compute the weighted center
        box.setRotationPivot(ZEROS);
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
