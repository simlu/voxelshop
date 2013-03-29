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
    public static Light addLight (World world, SimpleVector position, float strength) {
        Light light = new Light(world);
        light.setDiscardDistance(-1);
        light.setIntensity(strength, strength, strength);
        light.setPosition(position);
        return light;
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

    // holds the uv rotation data
    private final static HashMap<String, float[]> uvRotation = new HashMap<String, float[]>();

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
                box.addTriangle(upperRightFront,0.001f,0.001f, lowerRightFront,0.001f,0.999f, upperRightBack,0.999f,0);
                box.addTriangle(upperRightBack,0.999f,0.001f, lowerRightFront, 0.001f,0.999f, lowerRightBack,0.999f,0.999f);
            }
            if (bin.charAt(1) == '0') {
                // Left
                box.addTriangle(upperLeftFront,0.999f,0.001f, upperLeftBack,0.001f,0.001f, lowerLeftFront,0.999f,0.999f);
                box.addTriangle(upperLeftBack,0.001f,0.001f, lowerLeftBack,0.001f,0.999f, lowerLeftFront,0.999f,0.999f);
            }
            if (bin.charAt(2) == '0') {
                // Lower
                box.addTriangle(lowerLeftBack,0.999f,0.001f, lowerRightBack,0.001f,0.001f, lowerLeftFront,0.999f,0.999f);
                box.addTriangle(lowerRightBack,0.001f,0.001f, lowerRightFront,0.001f,0.999f, lowerLeftFront,0.999f,0.999f);
            }
            if (bin.charAt(3) == '0') {
                // Upper
                box.addTriangle(upperLeftBack,0.001f,0.001f, upperLeftFront,0.001f,0.999f, upperRightBack,0.999f,0);
                box.addTriangle(upperRightBack,0.999f,0.001f, upperLeftFront,0.001f,0.999f, upperRightFront,0.999f,0.999f);
            }
            if (bin.charAt(4) == '0') {
                // Back
                box.addTriangle(upperLeftBack,0.999f,0.001f, upperRightBack,0.001f,0.001f, lowerLeftBack,0.999f,0.999f);
                box.addTriangle(upperRightBack,0.001f,0.001f, lowerRightBack,0.001f,0.999f, lowerLeftBack,0.999f,0.999f);
            }
            if (bin.charAt(5) == '0') {
                // Front
                box.addTriangle(upperLeftFront,0.001f,0.001f, lowerLeftFront,0.001f,0.999f, upperRightFront,0.999f,0);
                box.addTriangle(upperRightFront,0.999f,0.001f, lowerLeftFront,0.001f,0.999f, lowerRightFront,0.999f,0.999f);
            }

            boxTypes.put(bin, box);

        }

        // pre-compute rotation + flipping (in that order!)
        int[][][] uvData = new int[][][]{
                new int[][]{
                        new int[]{0, 0, 0, 1, 1, 0},
                        new int[]{1, 0, 0, 1, 1, 1}
                },
                new int[][]{
                        new int[]{1, 0, 0, 0, 1, 1},
                        new int[]{0, 0, 0, 1, 1, 1}
                },
                new int[][]{
                        new int[]{1, 0, 0, 0, 1, 1},
                        new int[]{0, 0, 0, 1, 1, 1}
                },
                new int[][]{
                        new int[]{0, 0, 0, 1, 1, 0},
                        new int[]{1, 0, 0, 1, 1, 1}
                },
                new int[][]{
                        new int[]{1, 0, 0, 0, 1, 1},
                        new int[]{0, 0, 0, 1, 1, 1}
                },
                new int[][]{
                        new int[]{0, 0, 0, 1, 1, 0},
                        new int[]{1, 0, 0, 1, 1, 1}
                }
        };

        for (int index = 0; index < uvData.length; index++) {
            int[][] uvs = uvData[index];
            for (int flip = 0; flip <= 1; flip++) {
                for (int rotate = 0; rotate <= 3; rotate++) {
                    // holds the result data
                    float[] result = new float[12];

                    int[] t1 = uvs[0];
                    int[] t2 = uvs[1];

                    for (int i = 0; i < 3; i ++) {
                        int[] uv1 = new int[] {t1[i*2], t1[i*2+1]};
                        int[] uv2 = new int[] {t2[i*2], t2[i*2+1]};
                        for (int r = 0; r < rotate; r++) {
                            uv1 = rotate(uv1);
                            uv2 = rotate(uv2);
                        }
                        if (flip == 1) {
                            uv1 = flip(uv1);
                            uv2 = flip(uv2);
                        }
                        result[i*2] = uv1[0];
                        result[i*2+1] = uv1[1];
                        result[i*2+6] = uv2[0];
                        result[i*2+6+1] = uv2[1];
                    }

                    // interpolation of result
                    for (int i = 0; i < result.length; i++) {
                        result[i] = result[i] == 1 ? result[i] - 0.001f : result[i] + 0.001f;
                    }

                    uvRotation.put(index + "_" + rotate + "_" + flip, result);
                }
            }
        }

        // disable anti-aliasing for textures
        Config.texelFilter = false;

        textureManager = TextureManager.getInstance();
    }

    // internal - helper
    private static int[] rotate(int[] uv) {
        switch (uv[0]*2 + uv[1]) {
            case 0: return new int[] {1,0};
            case 1: return new int[] {0,0};
            case 2: return new int[] {1,1};
            case 3: return new int[] {0,1};
        }
        return null;
    }

    // internal - helper
    private static int[] flip(int[] uv) {
        switch (uv[0]*2 + uv[1]) {
            case 0: return new int[] {1,0};
            case 1: return new int[] {1,1};
            case 2: return new int[] {0,0};
            case 3: return new int[] {0,1};
        }
        return null;
    }


    private final static TextureManager textureManager;

    // load a texture to the world (from string)
    public static void loadTexture(String name, String url) {
        Image image = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                ClassLoader.getSystemResource(url)
        )).getImage();
        loadTexture(name, image);
    }

    // load a texture to the world (from image)
    public static void loadTexture(String name, Image image) {
        Texture texture = new Texture(image);
        if (textureManager.containsTexture(name)) {
            textureManager.replaceTexture(name, texture);
        } else {
            textureManager.addTexture(name, texture);
        }
        // make sure the id is assigned
        //textureManager.getNameByID(textureManager.getTextureID(name));
    }

    public static void loadTexture(String name, ImageIcon image) {
        // Create a texture from image
        BufferedImage text_top = new BufferedImage(image.getIconWidth(), image.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        text_top.getGraphics().drawImage(image.getImage(), 0, 0, null);
        loadTexture(name, text_top);
    }

    public static void removeTexture(String name) {
        textureManager.removeTexture(name);
    }

    // add a box to the world
    public static int addBoxSides (World world, SimpleVector pos, Color color,
                                   int[] rotation, boolean[] flip,
                                   int[] textureIds, String boxType, boolean culling) {

        Object3D box = boxTypes.get(boxType).cloneObject();
        // set other settings, build and add

        // select: color or texture
        if (textureIds == null) { // for color overlay
            box.setAdditionalColor(color);
        } else { // for texture overlay
            box.setAdditionalColor(Color.WHITE);
            PolygonManager polygonManager = box.getPolygonManager();
            char[] charArray = boxType.toCharArray();
            int polyCount = 0;
            boolean hasRotation = rotation != null;
            boolean hasFlip = flip != null;
            for (int i = 0; i < charArray.length; i++) {
                if (charArray[i] == '0') {
                    int id = textureManager.getTextureID(String.valueOf(textureIds[i]));
                    if ((hasRotation && rotation[i] != 0) || (hasFlip && flip[i])) {
                        int rotationValue = rotation == null ? 0 : rotation[i];
                        int flipValue = flip == null ? 0 : (flip[i] ? 1 : 0);
                        float[] uvMapping = uvRotation.get(i + "_" + rotationValue + "_" + flipValue);
                        polygonManager.setPolygonTexture(polyCount, new TextureInfo(id,
                                uvMapping[0],uvMapping[1],uvMapping[2],
                                uvMapping[3],uvMapping[4],uvMapping[5]
                        ));
                        polygonManager.setPolygonTexture(polyCount+1, new TextureInfo(id,
                                uvMapping[6],uvMapping[7],uvMapping[8],
                                uvMapping[9],uvMapping[10],uvMapping[11]
                        ));
                    } else {
                        // no rotation or flip on this side
                        polygonManager.setPolygonTexture(polyCount, id);
                        polygonManager.setPolygonTexture(polyCount+1, id);
                    }
                    polyCount+=2;
                }
            }
        }


        box.setOrigin(pos);
        box.setShadingMode(Object3D.SHADING_FAKED_FLAT);
        box.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
        box.setCulling(culling);

        if (boxType.equals("111111")) { // no need to show this object
            box.setVisibility(false);
        }
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
