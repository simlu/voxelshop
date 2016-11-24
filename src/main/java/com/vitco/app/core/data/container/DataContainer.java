package com.vitco.app.core.data.container;

import com.vitco.app.core.container.HackedObjectInputStream;
import com.vitco.app.manager.error.ErrorHandlerInterface;
import com.vitco.app.util.misc.AutoFileCloser;

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
    public int selectedTexture = -1;

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

    private static final class TmpData {
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
    }

    // constructor
    @SuppressWarnings("unchecked")
    public DataContainer(final File file, ErrorHandlerInterface errorHandler) {

        final TmpData tmpData = new TmpData();

        if (file != null && file.exists()) {
            try {
                new AutoFileCloser() {
                    @Override protected void doWork() throws Throwable {
                        try {
                            // declare variables for the readers and "watch" them
                            InputStream inputStream = autoClose(new FileInputStream( file ));
                            InputStream buffer = autoClose(new BufferedInputStream( inputStream ));
                            HackedObjectInputStream input = autoClose(new HackedObjectInputStream( buffer ));

                            if (input.available() > 0 && input.readUTF().equals("**VSD2013**")) {
                                while (input.available() > 0) {
                                    String token = input.readUTF();
                                    if (token.equals("#textures#")) {
                                        tmpData.textures = (HashMap<Integer, ImageIcon>) input.readObject();
                                    } else if (token.equals("#selectedTexture#")) {
                                        tmpData.selectedTexture = (Integer) input.readObject();
                                    } else if (token.equals("#selectedLayer#")) {
                                        tmpData.selectedLayer = (Integer) input.readObject();
                                    } else if (token.equals("#layers#")) {
                                        tmpData.layers = (HashMap<Integer, VoxelLayer>) input.readObject();
                                    } else if (token.equals("#layerOrder#")) {
                                        tmpData.layerOrder = (ArrayList<Integer>) input.readObject();
                                    } else if (token.equals("#voxels#")) {
                                        tmpData.voxels = (HashMap<Integer, Voxel>) input.readObject();
                                    } else if (token.equals("#points#")) {
                                        tmpData.points = (HashMap<Integer, ExtendedVector>) input.readObject();
                                    } else if (token.equals("#lines#")) {
                                        tmpData.lines = (HashMap<String, ExtendedLine>) input.readObject();
                                    } else if (token.equals("#pointsToLines#")) {
                                        tmpData.pointsToLines = (HashMap<Integer, ArrayList<ExtendedLine>>) input.readObject();
                                    } else if (token.equals("#activeFrame#")) {
                                        tmpData.activeFrame = (Integer) input.readObject();
                                    } else if (token.equals("#frames#")) {
                                        tmpData.frames = (HashMap<Integer, Frame>) input.readObject();
                                    }
                                }
                                tmpData.result = true;
                            }
                        } catch (StreamCorruptedException ignored) {} // caused if the file format is invalid
                    }
                };
            } catch (RuntimeException e) {
                errorHandler.handle(e);
            }
        }


        // ############# assign temporary vars to vars
        this.textures = tmpData.textures;
        this.selectedTexture = tmpData.selectedTexture;
        // ###################### DATA (Voxel)
        this.selectedLayer = tmpData.selectedLayer;
        this.layers = tmpData.layers;
        this.layerOrder = tmpData.layerOrder;
        this.voxels = tmpData.voxels;
        // ####################### DATA (Animation)
        this.points = tmpData.points;
        this.lines = tmpData.lines;
        this.pointsToLines = tmpData.pointsToLines;
        this.activeFrame = tmpData.activeFrame;
        this.frames = tmpData.frames;

        hasLoaded = tmpData.result;
    }

    // save to file function
    public final boolean saveToVsdFile(final File file, ErrorHandlerInterface errorHandler) {
        final boolean[] result = {false};
        try {
            new AutoFileCloser() {
                @Override protected void doWork() throws Throwable {
                    OutputStream outputStream = autoClose(new FileOutputStream( file ));
                    OutputStream buffer = autoClose(new BufferedOutputStream( outputStream ));
                    ObjectOutputStream output = autoClose(new ObjectOutputStream( buffer ));

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

                    result[0] = true;
                }
            };
        } catch (RuntimeException e) {
            errorHandler.handle(e);
        }
        return result[0];
    }
}
