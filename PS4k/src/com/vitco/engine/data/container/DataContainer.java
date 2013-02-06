package com.vitco.engine.data.container;

import com.vitco.util.error.ErrorHandlerInterface;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Collection of all the persistent data
 *
 * IMPORTANT: This class should never be changes!
 */
public final class DataContainer implements Serializable {
    // this is just for legacy (vsd format)
    private static final long serialVersionUID = 1L;
    // ###################### DATA (Texture)
    // all existing texture maps
    // todo make this final again once legacy support is removed
    public HashMap<Integer, ImageIcon> textures;
    // the selected texture
    public int selectedTexture;

    // ###################### DATA (Voxel)
    // holds all layers (layers map to voxel ids)
    public int selectedLayer;
    public final HashMap<Integer, VoxelLayer> layers;
    // order of the layers
    public final ArrayList<Integer> layerOrder;
    // holds all voxels (maps id to voxel)
    public final HashMap<Integer, Voxel> voxels;

    // ####################### DATA (Animation)
    // holds the points
    public final HashMap<Integer, ExtendedVector> points;
    // holds the lines
    public final HashMap<String, ExtendedLine> lines;
    // maps the points to lines
    public final HashMap<Integer, ArrayList<ExtendedLine>> pointsToLines;
    // currently active frame
    public int activeFrame;
    // all frames
    public final HashMap<Integer, Frame> frames;

    // true iff a file was successfully loaded into this container
    public final boolean hasLoaded;

    // constructor without file
    public DataContainer() {
        this(null, null);
    }

    // constructor
    @SuppressWarnings("unchecked")
    public DataContainer(File file, ErrorHandlerInterface errorHandler) {

        boolean result = false;

        // ############# create temporary to read from file
        HashMap<Integer, ImageIcon> textures = new HashMap<Integer, ImageIcon>();
        int selectedTexture = -1;
        // ###################### DATA (Voxel)
        int selectedLayer = -1;
        HashMap<Integer, VoxelLayer> layers = new HashMap<Integer, VoxelLayer>();
        ArrayList<Integer> layerOrder = new ArrayList<Integer>();
        HashMap<Integer, Voxel> voxels = new HashMap<Integer, Voxel>();
        // ####################### DATA (Animation)
        HashMap<Integer, ExtendedVector> points = new HashMap<Integer, ExtendedVector>();
        HashMap<String, ExtendedLine> lines = new HashMap<String, ExtendedLine>();
        HashMap<Integer, ArrayList<ExtendedLine>> pointsToLines = new HashMap<Integer, ArrayList<ExtendedLine>>();
        int activeFrame = -1;
        HashMap<Integer, Frame> frames = new HashMap<Integer, Frame>();

        if (file != null) {
            try{
                InputStream inputStream = new FileInputStream( file );
                InputStream buffer = new BufferedInputStream( inputStream );
                ObjectInput input = new ObjectInputStream ( buffer );
                try {
                    try {
                        if (input.available() > 0 && input.readUTF().equals("**VSD2013**")) {
                            while (input.available() > 0) {
                                String token = input.readUTF();
                                if (token.equals("#textures#")) {
                                    textures = (HashMap<Integer, ImageIcon>) input.readObject();
                                } else
                                if (token.equals("#selectedTexture#")) {
                                    selectedTexture = (Integer)input.readObject();
                                } else
                                if (token.equals("#selectedLayer#")) {
                                    selectedLayer = (Integer)input.readObject();
                                } else
                                if (token.equals("#layers#")) {
                                    layers = (HashMap<Integer, VoxelLayer>) input.readObject();
                                } else
                                if (token.equals("#layerOrder#")) {
                                    layerOrder = (ArrayList<Integer>) input.readObject();
                                } else
                                if (token.equals("#voxels#")) {
                                    voxels = (HashMap<Integer, Voxel>) input.readObject();
                                } else
                                if (token.equals("#points#")) {
                                    points = (HashMap<Integer, ExtendedVector>) input.readObject();
                                } else
                                if (token.equals("#lines#")) {
                                    lines = (HashMap<String, ExtendedLine>) input.readObject();
                                } else
                                if (token.equals("#pointsToLines#")) {
                                    pointsToLines = (HashMap<Integer, ArrayList<ExtendedLine>>) input.readObject();
                                } else
                                if (token.equals("#activeFrame#")) {
                                    activeFrame = (Integer)input.readObject();
                                } else
                                if (token.equals("#frames#")) {
                                    frames = (HashMap<Integer, Frame>) input.readObject();
                                }
                            }
                            result = true;
                        }
                    } catch (EOFException e) {
                        errorHandler.handle(e);
                    }
                }
                finally{
                    input.close();
                }
            }
            catch(ClassNotFoundException ex){
                errorHandler.handle(ex);
            }
            catch(FileNotFoundException ex){
                errorHandler.handle(ex);
            }
            catch(IOException ex){
                errorHandler.handle(ex);
            }
        }


        // ############# assign temporary vars to vars
        this.textures = textures;
        this.selectedTexture = selectedTexture;
        // ###################### DATA (Voxel)
        this.selectedLayer = selectedLayer;
        this.layers = layers;
        this.layerOrder = layerOrder;
        this.voxels = voxels;
        // ####################### DATA (Animation)
        this.points = points;
        this.lines = lines;
        this.pointsToLines = pointsToLines;
        this.activeFrame = activeFrame;
        this.frames = frames;

        hasLoaded = result;
    }

    // save to file function
    public final boolean saveToVsdFile(File file, ErrorHandlerInterface errorHandler) {
        boolean result = false;
        try{
            OutputStream outputStream = new FileOutputStream( file );
            OutputStream buffer = new BufferedOutputStream( outputStream );
            ObjectOutput output = new ObjectOutputStream( buffer );
            try{
                // write identifier
                output.writeUTF("**VSD2013**");

                if (textures != null) {
                    output.writeUTF("#textures#");
                    output.writeObject(textures);
                }

                output.writeUTF("#selectedTexture#");
                output.writeObject(selectedTexture);

                output.writeUTF("#selectedLayer#");
                output.writeObject(selectedLayer);

                if (layers != null) {
                    output.writeUTF("#layers#");
                    output.writeObject(layers);
                }
                if (layerOrder != null) {
                    output.writeUTF("#layerOrder#");
                    output.writeObject(layerOrder);
                }
                if (voxels != null) {
                    output.writeUTF("#voxels#");
                    output.writeObject(voxels);
                }
                if (points != null) {
                    output.writeUTF("#points#");
                    output.writeObject(points);
                }
                if (lines != null) {
                    output.writeUTF("#lines#");
                    output.writeObject(lines);
                }
                if (pointsToLines != null) {
                    output.writeUTF("#pointsToLines#");
                    output.writeObject(pointsToLines);
                }

                output.writeUTF("#activeFrame#");
                output.writeObject(activeFrame);

                if (frames != null) {
                    output.writeUTF("#frames#");
                    output.writeObject(frames);
                }

                result = true;
            }
            finally{
                output.close();
            }
        }
        catch(IOException ex){
            errorHandler.handle(ex);
        }
        return result;
    }
}
