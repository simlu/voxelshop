package com.vitco.core.world;

import com.threed.jpct.*;
import com.threed.jpct.util.Light;
import com.vitco.settings.DynamicSettings;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.misc.SaveResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.HashMap;

/**
 * Allows access to certain features of JPCT (e.g. textures)
 */
public final class WorldManager {
    private final static TextureManager textureManager = TextureManager.getInstance();

    // -------------------------------
    // Manage efficient textures (they are updated without huge amount of data allocation)

    // links textures to their effect
    private static final HashMap<Texture, Effect> effectList = new HashMap<Texture, Effect>();

    // an effect implementation to allow for efficient updating
    private final static class Effect implements ITextureEffect {
        private Texture texture = null;
        @Override
        public void init(Texture texture) {
            this.texture = texture;
        }

        BufferedImage img = null;
        private void setImage(BufferedImage img) {
            this.img = img;
            texture.applyEffect();
        }

        @Override
        public void apply(int[] dest, int[] source) {
            int[] pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
            System.arraycopy(pixels, 0, dest, 0, pixels.length);
            int diff = dest.length - pixels.length;
            System.arraycopy(pixels, pixels.length - diff, dest, pixels.length, diff);
            // we don't need the reference anymore
            // this is here in case the setImage call is async (!)
            this.img = null;
        }

        @Override
        public boolean containsAlpha() {
            // needs to be true (otherwise there is a color flip happening)
            return true;
        }
    }

    // load a texture to the world (from image)
    public static void loadEfficientTexture(String name, BufferedImage image, boolean useAlpha) {
        // check if we can replace
        Texture texture = !containsTexture(name) ? null : textureManager.getTexture(name);
        if (texture == null ||
                texture.getWidth() != image.getWidth() ||
                texture.getHeight() != image.getHeight()) {
            if (texture != null) {
                // remove old
                effectList.remove(texture);
            }
            // create texture with effect
            texture = new Texture(image, useAlpha);
            Effect effect = new Effect();
            texture.setEffect(effect);
            effectList.put(texture,effect);
            // load the texture
            loadTexture(name, texture);
        } else {
            Effect effect = effectList.get(texture);
            effect.setImage(image);
        }
    }

    public static boolean removeEfficientTexture(String name) {
        if (containsTexture(name)) {
            effectList.remove(textureManager.getTexture(name));
            // remove the texture
            textureManager.removeTexture(name);
            return true;
        }
        return false;
    }

    // ------------------------------
    // tiles are images that are placed on sides of voxels

    public static final HashMap<String, Image> tileList = new HashMap<String, Image>();

    // load a tile
    public static void loadTile(String name, ImageIcon image) {
        // this cast is necessary since pixel are stored differently in imageIcons
        tileList.put(name, image.getImage());
    }

    // get a tile
    public static Image getTile(String name) {
        return tileList.get(name);
    }

    // remove a tile
    public static boolean removeTile(String name) {
        return tileList.remove(name) != null;
    }

    // ------------------------------

    // load a texture to the world (from string)
    public static void loadTexture(String name, String url, boolean useAlpha) {
        Image image = new SaveResourceLoader(url).asImage();
        loadTexture(name, image, useAlpha);
    }

    // load a texture to the world (from image)
    public static void loadTexture(String name, Image image, boolean useAlpha) {
        Texture texture = new Texture(image, useAlpha);
        loadTexture(name, texture);
    }

    public static void loadTexture(String name, Texture texture) {
        if (textureManager.containsTexture(name)) {
            textureManager.replaceTexture(name, texture);
        } else {
            textureManager.addTexture(name, texture);
        }
    }

    public static void loadTexture(String name, ImageIcon image, boolean useAlpha) {
        // Create a texture from image
        // this cast is necessary since pixel are stored differently in imageIcons ????
        BufferedImage convertedImage = new BufferedImage(image.getIconWidth(), image.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        convertedImage.getGraphics().drawImage(image.getImage(), 0, 0, null);
        loadTexture(name, convertedImage, useAlpha);
    }

    public static boolean containsTexture(String name) {
        return textureManager.containsTexture(name);
    }

    public static boolean removeTexture(String name) {
        if (containsTexture(name)) {
            // remove the texture
            textureManager.removeTexture(name);
            return true;
        }
        return false;
    }

    public static int getTextureId(String name) {
        return textureManager.getTextureID(name);
    }

    // ---------------------------

    // static constructor
    static {
        // load texture for bounding box
        WorldManager.loadTexture(
                "__grid__",
                new SaveResourceLoader("resource/tex/bounding_box_512.png").asImage(),
                false
        );
    }

    // add a light-source
    public static Light addLight(World world, SimpleVector position, float strength) {
        Light light = new Light(world);
        light.setDiscardDistance(-1);
        light.setIntensity(strength, strength, strength);
        light.setPosition(position);
        return light;
    }

    // add a box to the world
    public static int addBox(World world, SimpleVector pos, float size, Color color) {
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

    // get the grid plane (the result depends on the current settings!)
    public static Object3D getGridPlane() {

        // create object (container)
        Object3D box=new Object3D(12);

        // add triangles
        SimpleVector upperLeftFront=new SimpleVector(-1,-1,-1);
        SimpleVector upperRightFront=new SimpleVector(1,-1,-1);
        SimpleVector lowerLeftFront=new SimpleVector(-1,1,-1);
        SimpleVector lowerRightFront=new SimpleVector(1,1,-1);

        SimpleVector upperLeftBack = new SimpleVector( -1, -1, 1);
        SimpleVector upperRightBack = new SimpleVector(1, -1, 1);
        SimpleVector lowerLeftBack = new SimpleVector( -1, 1, 1);
        SimpleVector lowerRightBack = new SimpleVector(1, 1, 1);

        // scale
        for (SimpleVector vec : new SimpleVector[] {
                upperLeftFront, upperRightFront, lowerLeftFront, lowerRightFront,
                upperLeftBack, upperRightBack, lowerLeftBack, lowerRightBack
        }) {
            vec.x *= DynamicSettings.VOXEL_PLANE_WORLD_SIZE_X /2;
            vec.y *= DynamicSettings.VOXEL_PLANE_WORLD_SIZE_Y /2;
            vec.z *= DynamicSettings.VOXEL_PLANE_WORLD_SIZE_Z /2;
        }

        float uvX = DynamicSettings.VOXEL_PLANE_SIZE_X/16f;
        float uvY = DynamicSettings.VOXEL_PLANE_SIZE_Y/16f;
        float uvZ = DynamicSettings.VOXEL_PLANE_SIZE_Z/16f;

        // Front
        box.addTriangle(upperLeftFront, uvX, uvY, lowerLeftFront, uvX, 0, upperRightFront, 0, uvY); // xy
        box.addTriangle(upperRightFront, 0, uvY, lowerLeftFront, uvX, 0, lowerRightFront, 0, 0);
        // Back
        box.addTriangle(upperLeftBack, uvX, uvY, upperRightBack, 0, uvY, lowerLeftBack, uvX, 0);
        box.addTriangle(upperRightBack, 0, uvY, lowerRightBack, 0, 0, lowerLeftBack, uvX, 0);
        // Upper
        box.addTriangle(upperLeftBack, uvX, uvZ, upperLeftFront, uvX, 0, upperRightBack, 0, uvZ); // xz
        box.addTriangle(upperRightBack, 0, uvZ, upperLeftFront, uvX, 0, upperRightFront, 0, 0);
        // Lower
        box.addTriangle(lowerLeftBack, uvX, uvZ, lowerRightBack, 0, uvZ, lowerLeftFront, uvX, 0);
        box.addTriangle(lowerRightBack, 0, uvZ, lowerRightFront, 0, 0, lowerLeftFront, uvX, 0);
        // Left
        box.addTriangle(upperLeftBack, uvY, uvZ, lowerLeftBack, 0, uvZ, upperLeftFront, uvY, 0); // yz
        box.addTriangle(lowerLeftBack, 0, uvZ, lowerLeftFront, 0, 0, upperLeftFront, uvY, 0);
        // Right
        box.addTriangle(upperRightBack, uvY, uvZ, upperRightFront, uvY, 0, lowerRightBack, 0, uvZ);
        box.addTriangle(lowerRightBack, 0, uvZ, upperRightFront, uvY, 0, lowerRightFront, 0, 0);

        // set texture
        box.setAdditionalColor(Color.WHITE);
        box.setTransparency(0);
        box.setTexture("__grid__");

        // scale and place correctly
        box.setOrigin(new SimpleVector(
                DynamicSettings.VOXEL_PLANE_SIZE_X%2 == 0 ? -0.5f * VitcoSettings.VOXEL_SIZE : 0,
                -DynamicSettings.VOXEL_PLANE_WORLD_SIZE_Y /2 + VitcoSettings.VOXEL_GROUND_DISTANCE,
                DynamicSettings.VOXEL_PLANE_SIZE_Z%2 == 0 ? -0.5f * VitcoSettings.VOXEL_SIZE : 0
        ));

        // make the "inside" visible
        box.invertCulling(true);

        box.build();
        return box;
    }

}
