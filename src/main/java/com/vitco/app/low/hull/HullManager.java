package com.vitco.app.low.hull;

import com.threed.jpct.SimpleVector;
import com.vitco.app.low.CubeIndexer;
import com.vitco.app.low.triangulate.util.Grid2PolyHelper;
import com.vitco.app.settings.VitcoSettings;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Efficient way to compute the hull for a group of
 * objects in 3D space (with short values as coordinates)
 */
public class HullManager<T> implements HullManagerInterface<T>, Serializable {
    private static final long serialVersionUID = 1L;

    // --------------

    // maps position to objects
    private final TIntObjectHashMap<T> id2obj = new TIntObjectHashMap<T>();

    // border
    private final TIntHashSet[] border = new TIntHashSet[]{
            new TIntHashSet(),new TIntHashSet(),new TIntHashSet(),
            new TIntHashSet(),new TIntHashSet(),new TIntHashSet()
    };

    // border changes
    @SuppressWarnings("unchecked")
    private final TIntObjectHashMap<T>[] borderAdded = new TIntObjectHashMap[]{
            new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>(),
            new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>()
    };
    @SuppressWarnings("unchecked")
    private final TIntObjectHashMap<T>[] borderRemoved = new TIntObjectHashMap[]{
            new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>(),
            new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>()
    };
    
    // add/remove buffer
    @SuppressWarnings("unchecked")
    private final TIntObjectHashMap<T>[] borderBufferAdded = new TIntObjectHashMap[]{
            new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>(),
            new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>()
    };

    @SuppressWarnings("unchecked")
    private final TIntObjectHashMap<T>[] borderBufferRemoved = new TIntObjectHashMap[]{
            new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>(),
            new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>(),new TIntObjectHashMap<T>()
    };

    // ---------------------

    @Override
    public final void clear() {
        id2obj.clear();
        for (int i = 0; i < 6; i++) {
            border[i].clear();
            borderAdded[i].clear();
            borderRemoved[i].clear();
            borderBufferAdded[i].clear();
            borderBufferRemoved[i].clear();
        }
    }

    @Override
    public final boolean contains(short[] pos) {
        return id2obj.containsKey(CubeIndexer.getId(pos));
    }

    @Override
    public final boolean contains(int posId) {
        return id2obj.containsKey(posId);
    }

    @Override
    public final boolean containsBorder(short[] pos, int orientation) {
        return border[orientation].contains(CubeIndexer.getId(pos));
    }

    @Override
    public final boolean containsBorder(int posId, int orientation) {
        return border[orientation].contains(posId);
    }

    @Override
    public final int[] getPosIds() {
        return id2obj.keys();
    }

    @Override
    public T get(short[] pos) {
        return id2obj.get(CubeIndexer.getId(pos));
    }

    @Override
    public final void update(short[] pos, T object) {
        //System.out.println("U " + pos[0] + "," + pos[1] + "," + pos[2]);
        update(CubeIndexer.getId(pos), object);
    }

    @Override
    public final void update(int posId, T object) {
        // store the object
        if (id2obj.put(posId, object) != null) {
            // the element was only updated (but existed already)
            T obj = id2obj.get(posId);
            for (int i = 0; i < 6; i++) {
                if (border[i].contains(posId)) {
                    // this does not use the buffer, because the side was not actually
                    // added this run and we want to allow for a potential remove (!)
                    borderAdded[i].put(posId, obj);
                }
                // also update the buffers
                if (borderBufferAdded[i].containsKey(posId)) {
                    borderBufferAdded[i].put(posId, obj);
                }
                if (borderBufferRemoved[i].containsKey(posId)) {
                    borderBufferRemoved[i].put(posId, obj);
                }
                if (borderRemoved[i].containsKey(posId)) {
                    borderRemoved[i].put(posId, obj);
                }
            }

        } else {

            T obj = id2obj.get(posId);

            // check borders
            int idOff = posId-1;
            if (id2obj.containsKey(idOff)) {
                border[0].remove(idOff);
                if (null == borderBufferAdded[0].remove(idOff)) {
                    borderRemoved[0].put(idOff, id2obj.get(idOff));
                } else {
                    borderAdded[0].remove(idOff);
                }
            } else {
                border[1].add(posId);
                if (null != borderBufferAdded[1].put(posId, obj)) {
                    borderAdded[1].put(posId, obj);
                }
            }
            // check borders
            idOff = posId+1;
            if (id2obj.containsKey(idOff)) {
                border[1].remove(idOff);
                if (null == borderBufferAdded[1].remove(idOff)) {
                    borderRemoved[1].put(idOff, id2obj.get(idOff));
                } else {
                    borderAdded[1].remove(idOff);
                }
            } else {
                border[0].add(posId);
                if (null != borderBufferAdded[0].put(posId, obj)) {
                    borderAdded[0].put(posId, obj);
                }
            }

            // check borders
            idOff = posId-CubeIndexer.widthwidth;
            if (id2obj.containsKey(idOff)) {
                border[2].remove(idOff);
                if (null == borderBufferAdded[2].remove(idOff)) {
                    borderRemoved[2].put(idOff, id2obj.get(idOff));
                } else {
                    borderAdded[2].remove(idOff);
                }
            } else {
                border[3].add(posId);
                if (null != borderBufferAdded[3].put(posId, obj)) {
                    borderAdded[3].put(posId, obj);
                }
            }
            // check borders
            idOff = posId+CubeIndexer.widthwidth;
            if (id2obj.containsKey(idOff)) {
                border[3].remove(idOff);
                if (null == borderBufferAdded[3].remove(idOff)) {
                    borderRemoved[3].put(idOff, id2obj.get(idOff));
                } else {
                    borderAdded[3].remove(idOff);
                }
            } else {
                border[2].add(posId);
                if (null != borderBufferAdded[2].put(posId, obj)) {
                    borderAdded[2].put(posId, obj);
                }
            }

            // check borders
            idOff = posId-CubeIndexer.width;
            if (id2obj.containsKey(idOff)) {
                border[4].remove(idOff);
                if (null == borderBufferAdded[4].remove(idOff)) {
                    borderRemoved[4].put(idOff, id2obj.get(idOff));
                } else {
                    borderAdded[4].remove(idOff);
                }
            } else {
                border[5].add(posId);
                if (null != borderBufferAdded[5].put(posId, obj)) {
                    borderAdded[5].put(posId, obj);
                }
            }
            // check borders
            idOff = posId+CubeIndexer.width;
            if (id2obj.containsKey(idOff)) {
                border[5].remove(idOff);
                if (null == borderBufferAdded[5].remove(idOff)) {
                    borderRemoved[5].put(idOff, id2obj.get(idOff));
                } else {
                    borderAdded[5].remove(idOff);
                }
            } else {
                border[4].add(posId);
                if (null != borderBufferAdded[4].put(posId, obj)) {
                    borderAdded[4].put(posId, obj);
                }
            }
        }
    }

    @Override
    public final boolean clearPosition(short[] pos) {
        //System.out.println("C " + pos[0] + "," + pos[1] + "," + pos[2]);
        return clearPosition(CubeIndexer.getId(pos));
    }

    @Override
    public final boolean clearPosition(int posId) {

        // remove the object (the actual removal needs to be done
        // last, because we still need the reference to the object
        if (id2obj.containsKey(posId)) {

            T obj = id2obj.get(posId);
            T objOff;

            // check borders
            int idOff = posId-1;
            if (id2obj.containsKey(idOff)) {
                border[0].add(idOff);
                objOff = id2obj.get(idOff);
                if (null != borderBufferRemoved[0].put(idOff, objOff)) {
                    borderAdded[0].put(idOff, objOff);
                }
            } else {
                border[1].remove(posId);
                if (null == borderBufferRemoved[1].remove(posId)) {
                    borderRemoved[1].put(posId, obj);
                } else {
                    borderAdded[1].remove(posId);
                }
            }
            // check borders
            idOff = posId+1;
            if (id2obj.containsKey(idOff)) {
                border[1].add(idOff);
                objOff = id2obj.get(idOff);
                if (null != borderBufferRemoved[1].put(idOff, objOff)) {
                    borderAdded[1].put(idOff, objOff);
                }
            } else {
                border[0].remove(posId);
                if (null == borderBufferRemoved[0].remove(posId)) {
                    borderRemoved[0].put(posId, obj);
                } else {
                    borderAdded[0].remove(posId);
                }
            }

            // check borders
            idOff = posId-CubeIndexer.widthwidth;
            if (id2obj.containsKey(idOff)) {
                border[2].add(idOff);
                objOff = id2obj.get(idOff);
                if (null != borderBufferRemoved[2].put(idOff, objOff)) {
                    borderAdded[2].put(idOff, objOff);
                }
            } else {
                border[3].remove(posId);
                if (null == borderBufferRemoved[3].remove(posId)) {
                    borderRemoved[3].put(posId, obj);
                } else {
                    borderAdded[3].remove(posId);
                }
            }
            // check borders
            idOff = posId+CubeIndexer.widthwidth;
            if (id2obj.containsKey(idOff)) {
                border[3].add(idOff);
                objOff = id2obj.get(idOff);
                if (null != borderBufferRemoved[3].put(idOff, objOff)) {
                    borderAdded[3].put(idOff, objOff);
                }
            } else {
                border[2].remove(posId);
                if (null == borderBufferRemoved[2].remove(posId)) {
                    borderRemoved[2].put(posId, obj);
                } else {
                    borderAdded[2].remove(posId);
                }
            }

            // check borders
            idOff = posId-CubeIndexer.width;
            if (id2obj.containsKey(idOff)) {
                border[4].add(idOff);
                objOff = id2obj.get(idOff);
                if (null != borderBufferRemoved[4].put(idOff, objOff)) {
                    borderAdded[4].put(idOff, objOff);
                }
            } else {
                border[5].remove(posId);
                if (null == borderBufferRemoved[5].remove(posId)) {
                    borderRemoved[5].put(posId, obj);
                } else {
                    borderAdded[5].remove(posId);
                }
            }
            // check borders
            idOff = posId+CubeIndexer.width;
            if (id2obj.containsKey(idOff)) {
                border[5].add(idOff);
                objOff = id2obj.get(idOff);
                if (null != borderBufferRemoved[5].put(idOff, objOff)) {
                    borderAdded[5].put(idOff, objOff);
                }
            } else {
                border[4].remove(posId);
                if (null == borderBufferRemoved[4].remove(posId)) {
                    borderRemoved[4].put(posId, obj);
                } else {
                    borderAdded[4].remove(posId);
                }
            }
            // remove the object
            id2obj.remove(posId);
            return true;
        }
        return false;
    }

    @Override
    public final Set<T> getHullAdditions(int direction) {

        // add pending changes
        borderAdded[direction].putAll(borderBufferAdded[direction]);
        borderAdded[direction].putAll(borderBufferRemoved[direction]);

        // remove the values that are pending as remove (remove is stronger!)
        for (TIntIterator it = borderRemoved[direction].keySet().iterator(); it.hasNext();) {
            borderAdded[direction].remove(it.next());
        }

        // generate result
        Set<T> result = new HashSet<T>(borderAdded[direction].valueCollection());

        // clear buffer and changes
        borderBufferAdded[direction].clear();
        borderBufferRemoved[direction].clear();
        borderAdded[direction].clear();

        return result;
    }

    @Override
    public final Set<T> getHullRemovals(int direction) {

        // generate result
        Set<T> result = new HashSet<T>(borderRemoved[direction].valueCollection());

        // remove the values that are pending as remove (remove is stronger!)
        for (TIntIterator it = borderRemoved[direction].keySet().iterator(); it.hasNext();) {
            borderAdded[direction].remove(it.next());
        }

        // clear buffer and changes
        borderRemoved[direction].clear();

        return result;
    }

    // get the current hull
    @Override
    public final short[][] getHull(int direction) {
        short[][] result = new short[border[direction].size()][3]; // allocate with correct size
        int count = 0;
        for (TIntIterator it = border[direction].iterator(); it.hasNext();) {
            short[] val = CubeIndexer.getPos(it.next());
            result[count][0] = val[0];
            result[count][1] = val[1];
            result[count][2] = val[2];
            count++;
        }
        return result;
    }

    // get the visible voxel ids
    @Override
    public final TIntHashSet getVisibleVoxelsIds() {
        TIntHashSet visibleVoxels = new TIntHashSet();
        for (int i = 0; i < 6; i++) {
            visibleVoxels.addAll(border[i]);
        }
        return visibleVoxels;
    }

    // get the current hull as ids
    @Override
    public final int[] getHullAsIds(int direction) {
        return border[direction].toArray();
    }

    // get the outline of all voxels into one direction
    @Override
    public SimpleVector[][] getOutline(int side) {
        // compute the correct orientation (w.r.t. the side)
        int orientation = side == 0 ? 5 : (side == 1 ? 3 : 1);
        if (!border[orientation].isEmpty()) {
            // find correct id mappings
            int id1, id2;
            switch (side) {
                case 0:
                    id1 = 0;
                    id2 = 1;
                    break;
                case 1:
                    id1 = 0;
                    id2 = 2;
                    break;
                default:
                    id1 = 1;
                    id2 = 2;
                    break;
            }
            // find minimum and range of the data
            short minX = Short.MAX_VALUE;
            short maxX = Short.MIN_VALUE;
            short minY = Short.MAX_VALUE;
            short maxY = Short.MIN_VALUE;
            short[][] voxelPositions = this.getHull(orientation);
            for (short[] pos : voxelPositions) {
                minX = (short) Math.min(minX, pos[id1]);
                maxX = (short) Math.max(maxX, pos[id1]);
                minY = (short) Math.min(minY, pos[id2]);
                maxY = (short) Math.max(maxY, pos[id2]);
            }
            // convert to boolean array
            boolean[][] data = new boolean[maxX - minX + 1][maxY - minY + 1];
            for (short[] pos : voxelPositions) {
                data[pos[id1] - minX][pos[id2] - minY] = true;
            }
            // convert to polygon
            short[][][] polys = Grid2PolyHelper.convert(data);
            // translate outline
            ArrayList<SimpleVector[]> lines = new ArrayList<SimpleVector[]>();
            for (short[][] poly : polys) {
                for (short[] outline : poly) {
                    SimpleVector p1, p2;
                    switch (side) {
                        case 2: p1 = new SimpleVector(0, minX + outline[0], minY + outline[1]); break;
                        case 1: p1 = new SimpleVector(minX + outline[0], 0, minY + outline[1]); break;
                        default: p1 = new SimpleVector(minX + outline[0], minY + outline[1], 0); break;
                    }
                    p1.scalarMul(VitcoSettings.VOXEL_SIZE);
                    p1.sub(VitcoSettings.VOXEL_WORLD_OFFSET);

                    for (int i = 2; i < outline.length;) {
                        switch (side) {
                            case 2: p2 = new SimpleVector(0, minX + outline[i++], minY + outline[i++]); break;
                            case 1: p2 = new SimpleVector(minX + outline[i++], 0, minY + outline[i++]); break;
                            default: p2 = new SimpleVector(minX + outline[i++], minY + outline[i++], 0); break;
                        }
                        p2.scalarMul(VitcoSettings.VOXEL_SIZE);
                        p2.sub(VitcoSettings.VOXEL_WORLD_OFFSET);
                        lines.add(new SimpleVector[]{p1, p2});
                        p1 = p2;
                    }
                }
            }
            // generate result array
            SimpleVector[][] result = new SimpleVector[lines.size()][];
            lines.toArray(result);
            return result;
        }
        return new SimpleVector[0][];
    }
}
