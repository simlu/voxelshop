package com.vitco;

import com.threed.jpct.*;
import com.threed.jpct.util.Light;
import com.vitco.settings.VitcoSettings;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Simple test program.
 */
public class Test {

    private World world;
    private FrameBuffer buffer;
    private Object3D box;
    private JFrame frame;

    public static void main(String[] args) throws Exception {
        new Test().loop();
    }

    public Object3D createBox() {
        // create object (container)
        Object3D box = new Object3D(12);

        // add triangles
        SimpleVector upperLeftFront=new SimpleVector(-1,-1,-1);
        SimpleVector upperRightFront=new SimpleVector(1,-1,-1);
        SimpleVector lowerLeftFront=new SimpleVector(-1,1,-1);
        SimpleVector lowerRightFront=new SimpleVector(1,1,-1);

        SimpleVector upperLeftBack = new SimpleVector( -1, -1, 1);
        SimpleVector upperRightBack = new SimpleVector(1, -1, 1);
        SimpleVector lowerLeftBack = new SimpleVector( -1, 1, 1);
        SimpleVector lowerRightBack = new SimpleVector(1, 1, 1);

        // Front
        box.addTriangle(upperLeftFront,0,0, lowerLeftFront,0,1, upperRightFront,1,0);
        box.addTriangle(upperRightFront,1,0, lowerLeftFront,0,1, lowerRightFront,1,1);
        // Back
        box.addTriangle(upperLeftBack,0,0, upperRightBack,1,0, lowerLeftBack,0,1);
        box.addTriangle(upperRightBack,1,0, lowerRightBack,1,1, lowerLeftBack,0,1);
        // Upper
        box.addTriangle(upperLeftBack,0,0, upperLeftFront,0,1, upperRightBack,1,0);
        box.addTriangle(upperRightBack,1,0, upperLeftFront,0,1, upperRightFront,1,1);
        // Lower
        box.addTriangle(lowerLeftBack,0,0, lowerRightBack,1,0, lowerLeftFront,0,1);
        box.addTriangle(lowerRightBack,1,0, lowerRightFront,1,1, lowerLeftFront,0,1);
        // Left
        box.addTriangle(upperLeftFront,0,0, upperLeftBack,1,0, lowerLeftFront,0,1);
        box.addTriangle(upperLeftBack,1,0, lowerLeftBack,1,1, lowerLeftFront,0,1);
        // Right
        box.addTriangle(upperRightFront,0,0, lowerRightFront,0,1, upperRightBack,1,0);
        box.addTriangle(upperRightBack,1,0, lowerRightFront, 0,1, lowerRightBack,1,1);

        // set texture
        box.setAdditionalColor(Color.WHITE);
        box.setTexture("box");

        box.setShadingMode(Object3D.SHADING_FAKED_FLAT);

        // scale and place correctly
        box.scale(10f);
        box.setOrigin(new SimpleVector(0,-VitcoSettings.VOXEL_GROUND_PLANE_SIZE/2 + VitcoSettings.VOXEL_GROUND_DISTANCE, 0));

        box.build();

        return box;
    }

    public Test() throws Exception {

        Config.fadeoutLight = false;

        Config.useMultipleThreads = true;
        Config.maxNumberOfCores = Runtime.getRuntime().availableProcessors();
        Config.loadBalancingStrategy = 1; // default 0
        // usually not worth it (http://www.jpct.net/doc/com/threed/jpct/Config.html#useMultiThreadedBlitting)
        Config.useMultiThreadedBlitting = true;   //default false

        // disable anti-aliasing for textures
        Config.texelFilter = false;

        frame=new JFrame("Hello world");
        frame.setSize(800, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        world = new World();
        Light light = new Light(world);
        light.setPosition(new SimpleVector( -20,-5,-25));
        light.setIntensity(new SimpleVector(-10, -10, -10));

        TextureManager.getInstance().addTexture("box", new Texture(64,64, Color.red));

        box = createBox();

        world.addObject(box);

        world.getCamera().setPosition(5, -5, -2);
        world.getCamera().lookAt(box.getTransformedCenter());
    }

    private void loop() throws Exception {
        buffer = new FrameBuffer(800, 600, FrameBuffer.SAMPLINGMODE_OGSS);

        int i = 0;

        while (frame.isShowing()) {

            if ((i%200) == 0) {
                world.removeObject(box);
                TextureManager.getInstance().removeTexture("box");
                TextureManager.getInstance().addTexture("box", new Texture(64,64, new Color(new Random().nextInt())));
                box = createBox();
                world.addObject(box);
            }
            i = (i+1)%200;

            box.rotateX(0.001f);
            box.rotateY(0.002f);
            box.rotateZ(0.003f);
            buffer.clear(java.awt.Color.BLUE);
            world.renderScene(buffer);
            world.draw(buffer);
            buffer.update();
            buffer.display(frame.getGraphics());
            //Thread.sleep(10);
        }
        buffer.disableRenderer(IRenderer.RENDERER_OPENGL);
        buffer.dispose();
        frame.dispose();
        System.exit(0);
    }
}
