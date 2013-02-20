package com.vitco;

import com.threed.jpct.*;
import com.vitco.res.VitcoSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * The main class.
 */
public class TextureTest extends WindowAdapter implements MouseMotionListener {

    static FrameBuffer buffer;
    static World world;
    Object3D box;
    JFrame frame;
    Object3D cube;

    public TextureTest() {
        frame = new JFrame("Hello world");
        frame.setSize(800, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        world = new World();
        world.setAmbientLight(255, 255, 255);
        //world.addLight( new SimpleVector( 500,-500,500),new Color(20,20,20) );
        Config.fadeoutLight = false;

        // Create a texture from image
        {
            BufferedImage text_top = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Image image = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                    ClassLoader.getSystemResource("resource/tex1.png")
            )).getImage();
            text_top.getGraphics().drawImage(image, 0, 0, null);
            Texture texture = new Texture(text_top, true);
            texture.setMipmap(true);
            TextureManager.getInstance().addTexture("tex1", texture);
        }
        {
            BufferedImage text_top = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Image image = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                    ClassLoader.getSystemResource("resource/tex2.png")
            )).getImage();
            text_top.getGraphics().drawImage(image, 0, 0, null);
            Texture texture = new Texture(text_top, true);
            texture.setMipmap(true);
            TextureManager.getInstance().addTexture("tex2", texture);
        }

//        try {
//            ImageIO.write(text_top, "png", new File("test.png"));
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }


        cube = new Object3D(12);

        //cube.setTransparency(0);

        float cdis = VitcoSettings.VOXEL_SIZE/2;
        SimpleVector upperLeftFront=new SimpleVector(-cdis,-cdis,-cdis);
        SimpleVector upperRightFront=new SimpleVector(cdis,-cdis,-cdis);
        SimpleVector lowerLeftFront=new SimpleVector(-cdis,cdis,-cdis);
        SimpleVector lowerRightFront=new SimpleVector(cdis,cdis,-cdis);

        SimpleVector upperLeftBack = new SimpleVector( -cdis, -cdis, cdis);
        SimpleVector upperRightBack = new SimpleVector(cdis, -cdis, cdis);
        SimpleVector lowerLeftBack = new SimpleVector( -cdis, cdis, cdis);
        SimpleVector lowerRightBack = new SimpleVector(cdis, cdis, cdis);

        // add the triangles
        // Front
        cube.addTriangle(upperLeftFront,0,0, lowerLeftFront,0,1, upperRightFront,1,0);
        cube.addTriangle(upperRightFront,1,0, lowerLeftFront,0,1, lowerRightFront,1,1);

        // Back
        cube.addTriangle(upperLeftBack,0,0, upperRightBack,1,0, lowerLeftBack,0,1);
        cube.addTriangle(upperRightBack,1,0, lowerRightBack,1,1, lowerLeftBack,0,1);

        // Upper
        cube.addTriangle(upperLeftBack,0,0, upperLeftFront,0,1, upperRightBack,1,0);
        cube.addTriangle(upperRightBack,1,0, upperLeftFront,0,1, upperRightFront,1,1);

        // Lower
        cube.addTriangle(lowerLeftBack,0,0, lowerRightBack,1,0, lowerLeftFront,0,1);
        cube.addTriangle(lowerRightBack,1,0, lowerRightFront,1,1, lowerLeftFront,0,1);

        // Left
        cube.addTriangle(upperLeftFront,0,0, upperLeftBack,1,0, lowerLeftFront,0,1);
        cube.addTriangle(upperLeftBack,1,0, lowerLeftBack,1,1, lowerLeftFront,0,1);

        // Right
        cube.addTriangle(upperRightFront,0,0, lowerRightFront,0,1, upperRightBack,1,0);
        cube.addTriangle(upperRightBack,1,0, lowerRightFront, 0,1, lowerRightBack,1,1);

        cube.setSpecularLighting(true);
        cube.setAdditionalColor(Color.ORANGE);
        cube.setTexture("tex1");
        cube.getPolygonManager().setPolygonTexture(0, TextureManager.getInstance().getTextureID("tex2"));
        cube.compile();
        cube.strip();
        cube.build();

        world.addObject(cube);

        //add cube to the world
//        cube = Primitives.getPlane(10,0.3f);
//
//        cube.setSpecularLighting(true);
//        cube.setTexture("texture");
//        cube.compile();
//        cube.strip();
//        cube.build();
//        world.addObject(cube);

//        // add data to the world
       /* box = Primitives.getBox(10f, 1f);
        box.setAdditionalColor(Color.RED);
        box.setEnvmapped(Object3D.ENVMAP_ENABLED);
        box.setShadingMode( Object3D.SHADING_FAKED_FLAT );
        box.build();
        world.addObject(box);         */


//        Enumeration objects = world.getObjects();
//        ArrayList<float[]> meshList = new ArrayList<float[]>();
//        while (objects.hasMoreElements()) {
//            Object3D obj = (Object3D) objects.nextElement();
//            meshList.add(obj.getMesh().getBoundingBox());
//        }
//
//        MeshData meshData = new MeshData(meshList.get(0), null , null);

        //Skeleton skeleton = new Skeleton();

        world.getCamera().setPosition(50, -50, -5);
        world.getCamera().lookAt(this.cube.getTransformedCenter());

        buffer = new FrameBuffer(800, 600, FrameBuffer.SAMPLINGMODE_NORMAL);

        frame.addMouseMotionListener(this);
        frame.addWindowListener(this);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        buffer.disableRenderer(IRenderer.RENDERER_OPENGL);
        buffer.dispose();
        frame.dispose();
        System.exit(0);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // nothing to do
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        cube.rotateY(-0.01f);
        buffer.clear(java.awt.Color.BLUE);
        world.renderScene(buffer);
        world.draw(buffer);
        buffer.update();
        buffer.display(frame.getGraphics());
    }

    public static void main (String[] args) {
        new TextureTest();
    }


}
