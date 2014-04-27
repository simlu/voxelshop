package com.vitco.low.hull;

import com.threed.jpct.SimpleVector;
import com.vitco.low.CubeIndexer;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Efficient way to compute the hull for a group of
 * objects in 3D space (with short values as coordinates)
 */
public class HullManager<T> implements HullFinderInterface<T>, Serializable {
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
    public final boolean containsBorder(short[] pos, int orientation) {
        return border[orientation].contains(CubeIndexer.getId(pos));
    }

    @Override
    public final void update(short[] pos, T object) {
        //System.out.println("U " + pos[0] + "," + pos[1] + "," + pos[2]);
        int id = CubeIndexer.getId(pos);
        // store the object
        if (id2obj.put(id, object) != null) {
            // the element was only updated (but existed already)
            T obj = id2obj.get(id);
            for (int i = 0; i < 6; i++) {
                if (border[i].contains(id)) {
                    // this does not use the buffer, because the side was not actually
                    // added this run and we want to allow for a potential remove (!)
                    borderAdded[i].put(id, obj);
                }
                // also update the buffers
                if (borderBufferAdded[i].containsKey(id)) {
                    borderBufferAdded[i].put(id, obj);
                }
                if (borderBufferRemoved[i].containsKey(id)) {
                    borderBufferRemoved[i].put(id, obj);
                }
                if (borderRemoved[i].containsKey(id)) {
                    borderRemoved[i].put(id, obj);
                }
            }

        } else {

            T obj = id2obj.get(id);

            // check borders
            int idOff = id-1;
            if (id2obj.containsKey(idOff)) {
                border[0].remove(idOff);
                if (null == borderBufferAdded[0].remove(idOff)) {
                    borderRemoved[0].put(idOff, id2obj.get(idOff));
                } else {
                    borderAdded[0].remove(idOff);
                }
            } else {
                border[1].add(id);
                if (null != borderBufferAdded[1].put(id, obj)) {
                    borderAdded[1].put(id, obj);
                }
            }
            // check borders
            idOff = id+1;
            if (id2obj.containsKey(idOff)) {
                border[1].remove(idOff);
                if (null == borderBufferAdded[1].remove(idOff)) {
                    borderRemoved[1].put(idOff, id2obj.get(idOff));
                } else {
                    borderAdded[1].remove(idOff);
                }
            } else {
                border[0].add(id);
                if (null != borderBufferAdded[0].put(id, obj)) {
                    borderAdded[0].put(id, obj);
                }
            }

            // check borders
            idOff = id-CubeIndexer.widthwidth;
            if (id2obj.containsKey(idOff)) {
                border[2].remove(idOff);
                if (null == borderBufferAdded[2].remove(idOff)) {
                    borderRemoved[2].put(idOff, id2obj.get(idOff));
                } else {
                    borderAdded[2].remove(idOff);
                }
            } else {
                border[3].add(id);
                if (null != borderBufferAdded[3].put(id, obj)) {
                    borderAdded[3].put(id, obj);
                }
            }
            // check borders
            idOff = id+CubeIndexer.widthwidth;
            if (id2obj.containsKey(idOff)) {
                border[3].remove(idOff);
                if (null == borderBufferAdded[3].remove(idOff)) {
                    borderRemoved[3].put(idOff, id2obj.get(idOff));
                } else {
                    borderAdded[3].remove(idOff);
                }
            } else {
                border[2].add(id);
                if (null != borderBufferAdded[2].put(id, obj)) {
                    borderAdded[2].put(id, obj);
                }
            }

            // check borders
            idOff = id-CubeIndexer.width;
            if (id2obj.containsKey(idOff)) {
                border[4].remove(idOff);
                if (null == borderBufferAdded[4].remove(idOff)) {
                    borderRemoved[4].put(idOff, id2obj.get(idOff));
                } else {
                    borderAdded[4].remove(idOff);
                }
            } else {
                border[5].add(id);
                if (null != borderBufferAdded[5].put(id, obj)) {
                    borderAdded[5].put(id, obj);
                }
            }
            // check borders
            idOff = id+CubeIndexer.width;
            if (id2obj.containsKey(idOff)) {
                border[5].remove(idOff);
                if (null == borderBufferAdded[5].remove(idOff)) {
                    borderRemoved[5].put(idOff, id2obj.get(idOff));
                } else {
                    borderAdded[5].remove(idOff);
                }
            } else {
                border[4].add(id);
                if (null != borderBufferAdded[4].put(id, obj)) {
                    borderAdded[4].put(id, obj);
                }
            }
        }
    }

    @Override
    public final boolean clearPosition(short[] pos) {
        //System.out.println("C " + pos[0] + "," + pos[1] + "," + pos[2]);
        int id = CubeIndexer.getId(pos);
        // remove the object (the actual removal needs to be done
        // last, because we still need the reference to the object
        if (id2obj.containsKey(id)) {

            T obj = id2obj.get(id);
            T objOff;

            // check borders
            int idOff = id-1;
            if (id2obj.containsKey(idOff)) {
                border[0].add(idOff);
                objOff = id2obj.get(idOff);
                if (null != borderBufferRemoved[0].put(idOff, objOff)) {
                    borderAdded[0].put(idOff, objOff);
                }
            } else {
                border[1].remove(id);
                if (null == borderBufferRemoved[1].remove(id)) {
                    borderRemoved[1].put(id, obj);
                } else {
                    borderAdded[1].remove(id);
                }
            }
            // check borders
            idOff = id+1;
            if (id2obj.containsKey(idOff)) {
                border[1].add(idOff);
                objOff = id2obj.get(idOff);
                if (null != borderBufferRemoved[1].put(idOff, objOff)) {
                    borderAdded[1].put(idOff, objOff);
                }
            } else {
                border[0].remove(id);
                if (null == borderBufferRemoved[0].remove(id)) {
                    borderRemoved[0].put(id, obj);
                } else {
                    borderAdded[0].remove(id);
                }
            }

            // check borders
            idOff = id-CubeIndexer.widthwidth;
            if (id2obj.containsKey(idOff)) {
                border[2].add(idOff);
                objOff = id2obj.get(idOff);
                if (null != borderBufferRemoved[2].put(idOff, objOff)) {
                    borderAdded[2].put(idOff, objOff);
                }
            } else {
                border[3].remove(id);
                if (null == borderBufferRemoved[3].remove(id)) {
                    borderRemoved[3].put(id, obj);
                } else {
                    borderAdded[3].remove(id);
                }
            }
            // check borders
            idOff = id+CubeIndexer.widthwidth;
            if (id2obj.containsKey(idOff)) {
                border[3].add(idOff);
                objOff = id2obj.get(idOff);
                if (null != borderBufferRemoved[3].put(idOff, objOff)) {
                    borderAdded[3].put(idOff, objOff);
                }
            } else {
                border[2].remove(id);
                if (null == borderBufferRemoved[2].remove(id)) {
                    borderRemoved[2].put(id, obj);
                } else {
                    borderAdded[2].remove(id);
                }
            }

            // check borders
            idOff = id-CubeIndexer.width;
            if (id2obj.containsKey(idOff)) {
                border[4].add(idOff);
                objOff = id2obj.get(idOff);
                if (null != borderBufferRemoved[4].put(idOff, objOff)) {
                    borderAdded[4].put(idOff, objOff);
                }
            } else {
                border[5].remove(id);
                if (null == borderBufferRemoved[5].remove(id)) {
                    borderRemoved[5].put(id, obj);
                } else {
                    borderAdded[5].remove(id);
                }
            }
            // check borders
            idOff = id+CubeIndexer.width;
            if (id2obj.containsKey(idOff)) {
                border[5].add(idOff);
                objOff = id2obj.get(idOff);
                if (null != borderBufferRemoved[5].put(idOff, objOff)) {
                    borderAdded[5].put(idOff, objOff);
                }
            } else {
                border[4].remove(id);
                if (null == borderBufferRemoved[4].remove(id)) {
                    borderRemoved[4].put(id, obj);
                } else {
                    borderAdded[4].remove(id);
                }
            }
            // remove the object
            id2obj.remove(id);
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

    @Override
    public final short[][] getHull(int direction) {
        short[][] result = new short[border[direction].size()][3]; // allocate with correct size
        int count = 0;
        for (TIntIterator it = border[direction].iterator(); it.hasNext();) {
            result[count++] = CubeIndexer.getPos(it.next());
        }
        return result;
    }

    // do a hit test against the voxels in this hull manager
    @Override
    public short[] hitTest(SimpleVector position, SimpleVector dir) {

        // todo: move origin into bounding box of hull manager
        // Note: This is only needed when camera is zoomed out far!)
        // Util3D provides a triangle ray intersection test that could be used
        // with a bounding box (needs to be modified to return "t", the distance value)

        // step direction
        short stepX = (short) Math.signum(dir.x);
        short stepY = (short) Math.signum(dir.y);
        short stepZ = (short) Math.signum(dir.z);

        boolean stepXB = stepX > 0;
        boolean stepYB = stepY > 0;
        boolean stepZB = stepZ > 0;

        short sideX = (short) (stepXB ? 1 : 0);
        short sideY = (short) (stepYB ? 3 : 2);
        short sideZ = (short) (stepZB ? 5 : 4);

        // starting grid coordinates
        short lastHitSide;
        int pos = CubeIndexer.getId(
                (short) Math.floor(position.x),
                (short) Math.floor(position.y),
                (short) Math.floor(position.z)
        );

        // compute the offsets
        double offX = stepX == Math.signum(position.x) ? (1 - Math.abs(position.x%1d)) : Math.abs(position.x%1d);
        double offY = stepY == Math.signum(position.y) ? (1 - Math.abs(position.y%1d)) : Math.abs(position.y%1d);
        double offZ = stepZ == Math.signum(position.z) ? (1 - Math.abs(position.z%1d)) : Math.abs(position.z%1d);
        offX = (double)Math.round(offX * 1000000000) / 1000000000;
        offY = (double)Math.round(offY * 1000000000) / 1000000000;
        offZ = (double)Math.round(offZ * 1000000000) / 1000000000;
        if (offX == 0) {
            offX = 1;
        }
        if (offY == 0) {
            offY = 1;
        }
        if (offZ == 0) {
            offZ = 1;
        }

        // the "progress" value
        double valYX = Math.abs(dir.y / dir.x);
        double valZX = Math.abs(dir.z / dir.x);
        double valZY = Math.abs(dir.z / dir.y);

        int tMaxX = 0;
        int tMaxY = 0;
        int tMaxZ = 0;

        // only check for nearby voxels (ray length)
        int i = 0;
        while (i++ < 400) {

            double diffYX = valYX * (tMaxX + offX) - (tMaxY + offY);

            if (diffYX < 0) {
                double diffZX = valZX * (tMaxX + offX) - (tMaxZ + offZ);
                if (diffZX < 0) {
                    tMaxX++;
                    pos = CubeIndexer.changeX(pos, stepXB);
                    lastHitSide = sideX;
                } else {
                    tMaxZ++;
                    pos = CubeIndexer.changeZ(pos, stepZB);
                    lastHitSide = sideZ;
                }
            } else {
                double diffZY = valZY * (tMaxY + offY) - (tMaxZ + offZ);
                if (diffZY < 0) {
                    tMaxY++;
                    pos = CubeIndexer.changeY(pos, stepYB);
                    lastHitSide = sideY;
                } else {
                    tMaxZ++;
                    pos = CubeIndexer.changeZ(pos, stepZB);
                    lastHitSide = sideZ;
                }
            }

            // check for containment
            if (border[lastHitSide].contains(pos)) {
                short[] result = CubeIndexer.getPos(pos);
                return new short[] {result[0], result[1], result[2], lastHitSide};
            }
        }
        return null;
    }
}
