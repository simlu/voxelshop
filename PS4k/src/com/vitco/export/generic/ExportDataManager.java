package com.vitco.export.generic;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.export.collada.ColladaExportWrapper;
import com.vitco.export.generic.container.*;
import com.vitco.layout.content.console.ConsoleInterface;
import com.vitco.low.hull.HullManagerExt;
import com.vitco.low.triangulate.Grid2TriGreedyOptimal;
import com.vitco.low.triangulate.Grid2TriNaive;
import com.vitco.low.triangulate.Grid2TriPolyFast;
import com.vitco.low.triangulate.util.Grid2PolyHelper;
import com.vitco.settings.DynamicSettings;
import com.vitco.util.components.progressbar.ProgressDialog;
import com.vitco.util.components.progressbar.ProgressReporter;
import gnu.trove.list.array.TShortArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TObjectIntProcedure;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the voxel data and manages restructuring, preparing it for exporting.
 */
public class ExportDataManager extends ProgressReporter {

    // contains the current hull that we need for triangulation
    private final ArrayList<HullManagerExt<Voxel>> hullManagers = new ArrayList<HullManagerExt<Voxel>>();

    // used to access voxel data (for color generation)
    private final Data data;

    // true if texture padding is enabled
    private final boolean usePadding;

    // true if holes are removed
    private final boolean removeHoles;

    // false if z axis is up
    private final boolean useYUP;

    // the origin mode
    private final int originMode;

    // whether to export textured voxels
    private final boolean exportTexturedVoxels;

    // whether to use vertex coloring
    private final boolean triangulateByColor;

    // whether to fix tjunction problems
    private final boolean fixTJunctions;

    // center of this object
    private final float[] center;

    // -------------
    // Layer names of layers that are being exported
    private final ArrayList<String> layerNames = new ArrayList<String>();

    // getter for layer Names
    public final String[] getLayerNames() {
        String[] result = new String[layerNames.size()];
        layerNames.toArray(result);
        return result;
    }

    // Data Structure that manages the triangles
    private final ArrayList<TexTriangleManager> triangleManagers = new ArrayList<TexTriangleManager>();

    // getter for triangle manager
    public final TexTriangleManager[] getTriangleManager() {
        TexTriangleManager[] result = new TexTriangleManager[layerNames.size()];
        triangleManagers.toArray(result);
        return result;
    }

    // Data Structure that manages the textures
    private final TriTextureManager textureManager = new TriTextureManager(getProgressDialog(), getConsole());

    // getter for the texture manager
    public final TriTextureManager getTextureManager() {
        return textureManager;
    }

    // -------------

    // the algorithms static variables
    public static final int POLY2TRI_ALGORITHM = 0;
    public static final int MINIMAL_RECT_ALGORITHM = 1;
    public static final int NAIVE_ALGORITHM = 2;

    // constructor
    public ExportDataManager(ProgressDialog dialog, ConsoleInterface console, Data data, boolean usePadding, boolean removeHoles, int algorithm, boolean useYUP,
                             int originMode, boolean forcePOT, boolean useLayers, boolean triangulateByColor, boolean useVertexColoring, boolean fixTJunctions,
                             boolean exportTexturedVoxels, boolean useOverlappingUvs) {
        super(dialog, console);

        // create hull manager that exposes hull information
        setActivity("Computing Hull...", true);
        int minx = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxy = Integer.MIN_VALUE;
        int minz = Integer.MAX_VALUE;
        int maxz = Integer.MIN_VALUE;

        if (useLayers) {
            // handle layers separately
            for (Integer layerId : data.getLayers()) {
                if (data.getLayerVisible(layerId)) {
                    Voxel[] layerVoxel = data.getLayerVoxels(layerId);
                    if (layerVoxel.length != 0) {
                        HullManagerExt<Voxel> hullManager = new HullManagerExt<Voxel>();
                        for (Voxel voxel : data.getLayerVoxels(layerId)) {
                            hullManager.update(voxel.posId, voxel);
                            minx = Math.min(minx, voxel.x);
                            maxx = Math.max(maxx, voxel.x);
                            miny = Math.min(miny, voxel.y);
                            maxy = Math.max(maxy, voxel.y);
                            minz = Math.min(minz, voxel.z);
                            maxz = Math.max(maxz, voxel.z);
                        }
                        layerNames.add(data.getLayerName(layerId));
                        hullManagers.add(hullManager);
                    }
                }
            }
        } else {
            // merge all layers
            HullManagerExt<Voxel> hullManager = new HullManagerExt<Voxel>();
            for (Voxel voxel : data.getVisibleLayerVoxel()) {
                hullManager.update(voxel.posId, voxel);
                minx = Math.min(minx, voxel.x);
                maxx = Math.max(maxx, voxel.x);
                miny = Math.min(miny, voxel.y);
                maxy = Math.max(maxy, voxel.y);
                minz = Math.min(minz, voxel.z);
                maxz = Math.max(maxz, voxel.z);
            }
            layerNames.add("Merged");
            hullManagers.add(hullManager);
        }
        center = new float[] {
            (minx + maxx) / 2f,
            (miny + maxy) / 2f,
            (minz + maxz) / 2f
        };

        // store references
        this.data = data;
        this.usePadding = usePadding;
        this.removeHoles = removeHoles;
        this.useYUP = useYUP;
        this.originMode = originMode;
        this.triangulateByColor = triangulateByColor;
        this.fixTJunctions = fixTJunctions;
        this.exportTexturedVoxels = exportTexturedVoxels;

        // pre-compute exterior hole if necessary
        if (removeHoles) {
            for (HullManagerExt<Voxel> hullManager : hullManagers) {
                hullManager.computeExterior();
            }
        }

        // extract information
        extract(algorithm);

        if (!useVertexColoring) {
            // combine the textures
            if (useOverlappingUvs) {
                textureManager.combine();
            } else {
                textureManager.combineNonOverlapping();
            }

            if (forcePOT) { // make textures power of two
                textureManager.resizeToPowOfTwo();
            }

            // validate uv mappings
            setActivity("Validating UV Mappings...", true);
            textureManager.validateUVMappings();
        }
    }

    // make sure that the polygon has no 3D t-junction problems
    private short[][][] fix3DTJunctionProblems(HullManagerExt<Voxel> hullManager, short[][][] polys, short plane, short planeAbove, int id1, int id2, int id3, int minA, int minB) {
        // result array
        short[][][] result = new short[polys.length][][];
        // temporary arrays to do comparisons
        short[] pos1 = new short[] {planeAbove, planeAbove, planeAbove};
        short[] pos2 = new short[] {planeAbove, planeAbove, planeAbove};
        // loop over all polygons
        for (int i1 = 0; i1 < polys.length; i1++) {
            // create corresponding result part
            short[][] poly = polys[i1];
            result[i1] = new short[poly.length][];
            // loop over outlines (poly + holes)
            for (int i2 = 0; i2 < poly.length; i2++) {
                short[] outline = poly[i2];
                // create dynamic list that we can later convert to result
                TShortArrayList list = new TShortArrayList(outline.length + 2);
                // loop over all points
                for (int i = 0, len = outline.length - 2; i < len; i += 2) {
                    // add current point
                    list.add(outline[i]);
                    list.add(outline[i+1]);
                    // check the type of line segment
                    if (outline[i + 2] == outline[i]) { // x values are equal
                        // compute the move direction
                        int step = (outline[i + 3] > outline[i + 1]) ? 1 : -1;
                        // move over all whole "in between" steps between this and the next point
                        for (short y = (short) (outline[i + 1] + step); y != outline[i + 3]; y += step) {
                            short x = (short) (outline[i] + (step == 1 ? -1 : 0) + minA);
                            pos1[id1] = x;
                            pos1[id2] = (short) (y + minB);
                            pos2[id1] = x;
                            pos2[id2] = (short) (y-1 + minB);
                            Voxel obj1 = hullManager.get(pos1);
                            Voxel obj2 = hullManager.get(pos2);
                            if ((obj1 == null) != (obj2 == null) || (
                                triangulateByColor && obj1 != null && obj1.getColor().getRGB() != obj2.getColor().getRGB()
                            )) {
                                // the "in between" point needs to be used for triangle generation
                                list.add(outline[i]);
                                list.add(y);
                            } else
                            // fix t-junction issues for vertex coloring
                            if (triangulateByColor) {
                                pos1[id3] = plane;
                                pos2[id3] = plane;
                                obj1 = hullManager.get(pos1);
                                obj2 = hullManager.get(pos2);
                                if ((obj1 == null) != (obj2 == null) || (
                                    obj1 != null && obj1.getColor().getRGB() != obj2.getColor().getRGB()
                                )) {
                                    // the "in between" point needs to be used for triangle generation
                                    list.add(outline[i]);
                                    list.add(y);
                                }
                                pos1[id3] = planeAbove;
                                pos2[id3] = planeAbove;
                            }
                        }
                    } else { // y values are equal
                        // compute the move direction
                        int step = (outline[i + 2] > outline[i]) ? 1 : -1;
                        // move over all whole "in between" steps between this and the next point
                        for (short x = (short) (outline[i] + step); x != outline[i + 2]; x += step) {
                            short y = (short) (outline[i + 1] + (step == -1 ? -1 : 0) + minB);
                            pos1[id1] = (short) (x + minA);
                            pos1[id2] = y;
                            pos2[id1] = (short) (x - 1 + minA);
                            pos2[id2] = y;
                            Voxel obj1 = hullManager.get(pos1);
                            Voxel obj2 = hullManager.get(pos2);
                            if ((obj1 == null) != (obj2 == null) || (
                                triangulateByColor && obj1 != null && obj1.getColor().getRGB() != obj2.getColor().getRGB()
                            )) {
                                // the "in between" point needs to be used for triangle generation
                                list.add(x);
                                list.add(outline[i + 1]);
                            } else
                            // fix t-junction issues for vertex coloring
                            if (triangulateByColor) {
                                pos1[id3] = plane;
                                pos2[id3] = plane;
                                obj1 = hullManager.get(pos1);
                                obj2 = hullManager.get(pos2);
                                if ((obj1 == null) != (obj2 == null) || (
                                    obj1 != null && obj1.getColor().getRGB() != obj2.getColor().getRGB()
                                )) {
                                    // the "in between" point needs to be used for triangle generation
                                    list.add(x);
                                    list.add(outline[i + 1]);
                                }
                                pos1[id3] = planeAbove;
                                pos2[id3] = planeAbove;
                            }
                        }
                    }
                }
                // add last point (same as first)
                list.add(outline[0]);
                list.add(outline[1]);
                // add to result
                short[] shortsList = new short[list.size()];
                list.toArray(shortsList);
                result[i1][i2] = shortsList;
            }
        }
        return result;
    }

    // extract the necessary information from the hull manager
    private void extract(final int algorithm) {
        setActivity("Extracting Mesh...", false);
        // loop over all managers
        for (final HullManagerExt<Voxel> hullManager : hullManagers) {
            final TexTriangleManager triangleManager =  new TexTriangleManager();
            triangleManagers.add(triangleManager);
            // loop over all sides
            for (int side = 0; side < 6; side++) {
                final int finalSide = side;

                // get borders into specific direction and
                // calculate orientation related variables
                short[][] hull = removeHoles ? hullManager.getExteriorHull(side) : hullManager.getHull(side);
                final int directionId = side / 2;
                final boolean orientationPositive = side % 2 != (directionId == 1 ? 1 : 0);
                final int offset = side % 2 != 1 ? 1 : 0;

                // extract planes
                HashMap<Short, TObjectIntHashMap<short[]>> planes = new HashMap<Short, TObjectIntHashMap<short[]>>();
                for (short[] border : hull) {
                    TObjectIntHashMap<short[]> plane = planes.get(border[directionId]);
                    if (plane == null) {
                        plane = new TObjectIntHashMap<short[]>();
                        planes.put(border[directionId], plane);
                    }

                    // if we use textures we use "0" as placeholder for all colors
                    plane.put(border, triangulateByColor ? hullManager.get(border).getColor().getRGB() : 0);
                }

                // select the corresponding ids for the orientation
                final int id1;
                final int id2;
                final int id3;
                switch (directionId) {
                    case 0:
                        id1 = 1;
                        id2 = 2;
                        id3 = 0;
                        break;
                    case 1:
                        id1 = 0;
                        id2 = 2;
                        id3 = 1;
                        break;
                    default: //case 2
                        id1 = 0;
                        id2 = 1;
                        id3 = 2;
                        break;
                }

                // loop over planes
                int progressCount = 0;
                float elementCount = planes.size();
                for (final Map.Entry<Short, TObjectIntHashMap<short[]>> entries : planes.entrySet()) {
                    setProgress((side / 6f) * 100 + ((progressCount / elementCount) / 6f) * 100);
                    progressCount++;
                    // maps colors to min/max (minA, minB, maxA, maxB)
                    final TIntObjectHashMap<short[]> minMaxMap = new TIntObjectHashMap<short[]>();
                    // remove the values that are pending as remove (remove is stronger!)
                    entries.getValue().forEachEntry(new TObjectIntProcedure<short[]>() {
                        @Override
                        public boolean execute(short[] position, int rgb) {
                            short[] minMax = minMaxMap.get(rgb);
                            if (minMax == null) {
                                minMax = new short[] {
                                        Short.MAX_VALUE, Short.MAX_VALUE, Short.MIN_VALUE, Short.MIN_VALUE
                                };
                                minMaxMap.put(rgb, minMax);
                            }
                            minMax[0] = (short) Math.min(minMax[0], position[id1]);
                            minMax[1] = (short) Math.min(minMax[1], position[id2]);
                            minMax[2] = (short) Math.max(minMax[2], position[id1]);
                            minMax[3] = (short) Math.max(minMax[3], position[id2]);
                            return true;
                        }
                    });

                    // maps colors to data sets
                    final TIntObjectHashMap<boolean[][]> dataArray = new TIntObjectHashMap<boolean[][]>();

                    entries.getValue().forEachEntry(new TObjectIntProcedure<short[]>() {

                        @Override
                        public boolean execute(short[] position, int rgb) {
                            short[] minMax = minMaxMap.get(rgb);
                            boolean[][] data = dataArray.get(rgb);
                            if (data == null) {
                                data = new boolean[minMax[2] - minMax[0] + 1][minMax[3] - minMax[1] + 1];
                                dataArray.put(rgb, data);
                            }
                            data[position[id1] - minMax[0]][position[id2] - minMax[1]] = true;
                            return true;
                        }
                    });

                    final TIntObjectHashMap<Collection<DelaunayTriangle>> trisArray = new TIntObjectHashMap<Collection<DelaunayTriangle>>();

                    dataArray.forEachEntry(new TIntObjectProcedure<boolean[][]>() {
                        @Override
                        public boolean execute(int rgb, boolean[][] data) {
                            short[] minMax = minMaxMap.get(rgb);
                            Collection<DelaunayTriangle> tris;
                            switch (algorithm) {
                                case ExportDataManager.MINIMAL_RECT_ALGORITHM:
                                    tris = Grid2TriGreedyOptimal.triangulate(data);
                                    break;
                                case ExportDataManager.NAIVE_ALGORITHM:
                                    tris = Grid2TriNaive.triangulate(data);
                                    break;
                                default:
                                    // generate triangles
                                    short[][][] polys = Grid2PolyHelper.convert(data);
                                    if (fixTJunctions) {
                                        // fix 3D t-junction problems
                                        int planeAbove = entries.getKey() + (finalSide % 2 == 0 ? 1 : -1);
                                        // Note: This *should* work the same if only outside is used (i.e. holes are removed)
                                        polys = fix3DTJunctionProblems(hullManager, polys, entries.getKey(), (short) planeAbove, id1, id2, id3, minMax[0], minMax[1]);
                                    }
                                    // extract triangles
                                    tris = Grid2TriPolyFast.triangulate(polys);
                                    break;
                            }
                            trisArray.put(rgb, tris);
                            return true;
                        }
                    });

                    trisArray.forEachEntry(new TIntObjectProcedure<Collection<DelaunayTriangle>>() {
                        @Override
                        public boolean execute(int rgb, Collection<DelaunayTriangle> tris) {
                            short[] minMax = minMaxMap.get(rgb);

                            for (DelaunayTriangle tri : tris) {

                                // create the triangle
                                TexTriangle texTri = new TexTriangle(tri, triangleManager, finalSide);

                                // create the texture (wrapper) for this triangle
                                TexTriUV[] uvs = texTri.getUVs();
                                TriTexture triTexture = new TriTexture(
                                        // Note: The triangulation points might have rounding errors (!)
                                        // So we <need> to round these values (casting to int is not sufficient!)
                                        uvs[0], Math.round(minMax[0] + tri.points[0].getXf()), Math.round(minMax[1] + tri.points[0].getYf()),
                                        uvs[1], Math.round(minMax[0] + tri.points[1].getXf()), Math.round(minMax[1] + tri.points[1].getYf()),
                                        uvs[2], Math.round(minMax[0] + tri.points[2].getXf()), Math.round(minMax[1] + tri.points[2].getYf()),
                                        finalSide,
                                        entries.getKey(),
                                        usePadding,
                                        texTri, data,
                                        textureManager,
                                        exportTexturedVoxels
                                );

                                // set the texture for this triangle
                                texTri.setTexture(triTexture);

                                // add to the texture manager
                                textureManager.addTexture(triTexture);

                                // translate to triangle in 3D space
                                for (int p = 0; p < 3; p++) {
                                    TexTriPoint point = texTri.getPoint(p);
                                    float[] coord = point.getCoords();
                                    point.set(directionId, entries.getKey() + offset);
                                    point.set(id1, minMax[0] + coord[0]);
                                    point.set(id2, minMax[1] + coord[1]);
                                }

                                // invert the triangle when necessary (correct back-face culling)
                                if (orientationPositive) {
                                    texTri.invert();
                                }

                                // change positions so that the exported file is accurate
                                texTri.swap(1, 2);
                                texTri.invert(0);
                                texTri.invert(1);
                                texTri.invert(2);

                                if (originMode == ColladaExportWrapper.ORIGIN_CROSS) {
                                    texTri.move(0.5f, 0.5f, 0.5f); // move one up
                                } else if (originMode == ColladaExportWrapper.ORIGIN_CENTER) {
                                    texTri.move(center[0] + 0.5f, center[2] + 0.5f, center[1] + 0.5f);
                                } else if (originMode == ColladaExportWrapper.ORIGIN_PLANE_CENTER) {
                                    texTri.move(center[0] + 0.5f, center[2] + 0.5f, 1f);
                                } else if (originMode == ColladaExportWrapper.ORIGIN_BOX_CENTER) {
                                    texTri.move(
                                            DynamicSettings.VOXEL_PLANE_SIZE_X % 2 == 0 ? 0f : 0.5f,
                                            DynamicSettings.VOXEL_PLANE_SIZE_Z % 2 == 0 ? 0f : 0.5f,
                                            1f - DynamicSettings.VOXEL_PLANE_RANGE_Y
                                    );
                                } else if (originMode == ColladaExportWrapper.ORIGIN_BOX_PLANE_CENTER) {
                                    texTri.move(
                                            DynamicSettings.VOXEL_PLANE_SIZE_X % 2 == 0 ? 0f : 0.5f,
                                            DynamicSettings.VOXEL_PLANE_SIZE_Z % 2 == 0 ? 0f : 0.5f,
                                            1f
                                    );
                                }

                                if (useYUP) {
                                    texTri.swap(1, 2);
                                    texTri.invert(2);
                                }

                                // scale to create integers
                                texTri.scale(2);

                                // convert to integer values
                                texTri.round();

                                // add to known triangles
                                triangleManager.addTriangle(texTri);
                            }

                            return true;
                        }
                    });
                }
            }
        }
    }

}
