package com.vitco.export;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.export.container.*;
import com.vitco.low.hull.HullManager;
import com.vitco.low.triangulate.Grid2TriPolyFast;
import com.vitco.low.triangulate.util.Grid2PolyHelper;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the voxel data and manages restructuring, preparing it for exporting.
 */
public class ExportDataManager {

    // contains the current hull that we need for triangulation
    private final HullManager<Voxel> hullManager;

    // used to access voxel data (for color generation)
    private final Data data;

    // -------------
    // Data Structure that manages the triangles
    private final TexTriangleManager triangleManager = new TexTriangleManager();

    // getter for triangle manager
    public final TexTriangleManager getTriangleManager() {
        return triangleManager;
    }

    // Data Structure that manages the textures
    private final TriTextureManager textureManager = new TriTextureManager();

    // getter for the texture manager
    public final TriTextureManager getTextureManager() {
        return textureManager;
    }

    // -------------

    // constructor
    public ExportDataManager(Data data) {

        // create hull manager that exposes hull information
        Voxel[] voxels = data.getVisibleLayerVoxel();
        HullManager<Voxel> hullManager = new HullManager<Voxel>();
        for (Voxel voxel : voxels) {
            hullManager.update(new short[]{(short) voxel.x, (short) voxel.y, (short) voxel.z}, voxel);
        }

        // store references
        this.hullManager = hullManager;
        this.data = data;

        // extract information
        extract();
    }

    // extract the necessary information from the hull manager
    private void extract() {
        // loop over all sides
        for (int i = 0; i < 6; i++) {
            // get borders into specific direction and
            // calculate orientation related variables
            short[][] hull = hullManager.getHull(i);
            final int directionId = i/2;
            final boolean orientationPositive = i%2 != (directionId == 1 ? 1 : 0);
            final int offset = i%2 != 1 ? 1 : 0;

            // extract planes
            HashMap<Short, ArrayList<short[]>> planes = new HashMap<Short, ArrayList<short[]>>();
            for (short[] border : hull) {
                ArrayList<short[]> plane = planes.get(border[directionId]);
                if (plane == null) {
                    plane = new ArrayList<short[]>();
                    planes.put(border[directionId], plane);
                }
                plane.add(border);
            }

            // select the corresponding ids for the orientation
            int id1;
            int id2;
            switch (directionId) {
                case 0:
                    id1 = 1;
                    id2 = 2;
                    break;
                case 1:
                    id1 = 0;
                    id2 = 2;
                    break;
                default:
                    id1 = 0;
                    id2 = 1;
                    break;
            }

            // loop over planes
            for (Map.Entry<Short, ArrayList<short[]>> entries : planes.entrySet()) {
                // generate mesh
                short minA = Short.MAX_VALUE;
                short minB = Short.MAX_VALUE;
                short maxA = Short.MIN_VALUE;
                short maxB = Short.MIN_VALUE;
                for (short[] entry : entries.getValue()) {
                    minA = (short) Math.min(minA, entry[id1]);
                    minB = (short) Math.min(minB, entry[id2]);
                    maxA = (short) Math.max(maxA, entry[id1]);
                    maxB = (short) Math.max(maxB, entry[id2]);
                }
                boolean[][] data = new boolean[maxA - minA + 1][maxB - minB + 1];
                for (short[] entry : entries.getValue()) {
                    data[entry[id1]-minA][entry[id2]-minB] = true;
                }

                // generate triangles
                short[][][] polys = Grid2PolyHelper.convert(data);
                for (DelaunayTriangle tri : Grid2TriPolyFast.triangulate(polys)) {

                    TexTriangle texTri = new TexTriangle(tri, triangleManager);

                    // compute the texture for this triangle
                    TriTexture triTexture = new TriTexture(
                            texTri.getUV(0), minA + tri.points[0].getXf(), minB + tri.points[0].getYf(),
                            texTri.getUV(1), minA + tri.points[1].getXf(), minB + tri.points[1].getYf(),
                            texTri.getUV(2), minA + tri.points[2].getXf(), minB + tri.points[2].getYf(),
                            texTri, this.data
                    );

                    // set the texture of this triangle
                    texTri.setTexture(triTexture);

                    // add to the texture manager
                    textureManager.addTexture(triTexture);

                    // translate to triangle in 3D space
                    for (int p = 0; p < 3; p++) {
                        TexTriPoint point = texTri.getPoint(p);
                        float[] coord = point.getCoords();
                        point.set(directionId, entries.getKey() + offset);
                        point.set(id1, minA + coord[0]);
                        point.set(id2, minB + coord[1]);
                    }

                    // invert the triangle when necessary
                    if (orientationPositive) {
                        texTri.invert();
                    }

                    // change positions so that the exported file is accurate
                    texTri.swap(1, 2);
                    texTri.invert(0);
                    texTri.invert(1);
                    texTri.invert(2);

                    // move one up
                    texTri.move(0.5f,0.5f,0.5f);

                    // scale to create integers
                    texTri.scale(2);

                    // convert to integer values
                    texTri.round();

                    // add to known triangles
                    triangleManager.addTriangle(texTri);
                }
            }
        }
    }

}
