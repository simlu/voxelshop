package com.vitco.low.hull;

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
}
