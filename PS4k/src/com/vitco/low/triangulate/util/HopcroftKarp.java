package com.vitco.low.triangulate.util;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implementation of Hopcroft-Karp algorithm
 *
 * Reference: http://en.wikipedia.org/wiki/Hopcroft%E2%80%93Karp_algorithm
 *
 * Adapted from
 * https://github.com/pierre-dejoue/kart-match/blob/master/src/fr/neuf/perso/pdejoue/kart_match/HopcroftKarp.java
 */

// todo: make faster!

public class HopcroftKarp {

    // The Hopcroft-Karp algorithm
    public static HashMap<Integer, Integer> findMaximumMatching(HashMap<Integer, ArrayList<Integer>> graph) {
        // Local variables:
        // The first step of the Hopcroft-Karp algorithm consists in building a list alternating
        // U-layers and V-layers. The current U/V-layer being processed by the algorithm is stored in
        // hash maps current_layer_u and current_layer_v. All U-layers (respectively V-layers) shall
        // be disjoint from each other. Yet there is no need to store all the layers as they are built,
        // so the algorithm only keeps track of the union of the previous U-layers and V-layers in hash
        // maps all_layers_u and all_layers_v.
        // Finally, hash map matched_v contains the temporary matching built by the algorithm. Upon
        // completion of the algorithm, it is a maximum matching.
        TIntIntHashMap current_layer_u = new TIntIntHashMap(); // u --> v
        HashMap<Integer, ArrayList<Integer>> current_layer_v = new HashMap<Integer, ArrayList<Integer>>(); // v --> list of u
        TIntIntHashMap all_layers_u = new TIntIntHashMap(); // u --> v
        HashMap<Integer, ArrayList<Integer>> all_layers_v = new HashMap<Integer, ArrayList<Integer>>(); // v --> list of u
        TIntIntHashMap matched_v = new TIntIntHashMap(); // v --> u
        ArrayList<Integer> unmatched_v = new ArrayList<Integer>(); // list of v

        // Loop as long as we can find at least one minimal augmenting path
        while (true) {
            int k = 0; // U-layers have indexes n = 2*k ; V-layers have indexes n = 2*k+1.

            // The initial layer of vertices of U is equal to the set of u not in the current matching
            all_layers_u.clear();
            current_layer_u.clear();
            for(Integer u : graph.keySet()) {
                if(!matched_v.containsValue(u)) {
                    current_layer_u.put(u, 0);
                    all_layers_u.put(u, 0);
                }
            }

            all_layers_v.clear();
            unmatched_v.clear();

            // Use BFS to build alternating U and V layers, in which:
            // - The edges between U-layer 2*k and V-layer 2*k+1 are unmatched ones.
            // - The edges between V-layer 2*k+1 and U-layer 2*k+2 are matched ones.

            // While the current layer U is not empty and no unmatched V is encountered
            while(!current_layer_u.isEmpty() && unmatched_v.isEmpty()) {
                //Log.d("HopcroftKarp.Algo", "current_layer_u: " + current_layer_u.toString());

                // Build the layer of vertices of V with index n = 2*k+1
                current_layer_v.clear();
                for (TIntIterator it = current_layer_u.keySet().iterator(); it.hasNext();) {
                    Integer u = it.next();
                    for(Integer v : graph.get(u)) {
                        if(!all_layers_v.containsKey(v)) { // If not already in the previous partitions for V
                            ArrayList<Integer> entry = current_layer_v.get(v);
                            if (entry == null) {
                                entry = new ArrayList<Integer>();
                                current_layer_v.put(v, entry);
                            }
                            entry.add(u);
                            // Expand of all_layers_v is done in the next step, building the U-layer
                        }
                    }
                }

                k++;
                // Build the layer of vertices of U with index n = 2*k
                current_layer_u.clear();
                for(Integer v : current_layer_v.keySet()) {
                    all_layers_v.put(v, current_layer_v.get(v)); // Expand the union of all V-layers to include current_v_layer

                    // Is it a matched vertex in V?
                    if(matched_v.containsKey(v)) {
                        Integer u = matched_v.get(v);
                        current_layer_u.put(u, v);
                        all_layers_u.put(u, v); // Expand the union of all U-layers to include current_u_layer
                    } else {
                        // Found one unmatched vertex v. The algorithm will finish the current layer,
                        // then exit the while loop since it has found at least one augmenting path.
                        unmatched_v.add(v);
                    }
                }
            }

            // After the inner while loop has completed, either we found at least one augmenting path...
            if(!unmatched_v.isEmpty()) {
                for(Integer v : unmatched_v) {
                    // Use DFS to find one augmenting path ending with vertex V. The vertices from that path, if it
                    // exists, are removed from the all_layers_u and all_layers_v maps.
                    if(k >= 1) {
                        recFindAugmentingPath(v, all_layers_u, all_layers_v, matched_v, (k-1)); // Ignore return status
                    } else {
                        throw new ArithmeticException("k should not be equal to zero here.");
                    }
                }
            } else { // ... or we didn't, in which case we already got a maximum matching for that graph
                break;
            }
        } // end while(true)

        // compute the result (reversed)
        HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
        for (TIntIntIterator it = matched_v.iterator(); it.hasNext();) {
            it.advance();
            result.put(it.value(), it.key());
        }

        return result;
    }

    // Recursive function used to build an augmenting path starting from the end node v.
    // It relies on a DFS on the U and V layers built during the first phase of the algorithm.
    // This is by the way this function which is responsible for most of the randomization
    // of the output.
    // Returns true if an augmenting path is found.
    private static boolean recFindAugmentingPath(Integer v, TIntIntHashMap all_layers_u,
                                                 HashMap<Integer, ArrayList<Integer>> all_layers_v, TIntIntHashMap matched_v, int k) {
        if (all_layers_v.containsKey(v)) {
            ArrayList<Integer> list_u = all_layers_v.get(v);

            for(Integer u: list_u) {
                if(all_layers_u.containsKey(u)) {
                    Integer prev_v = all_layers_u.get(u);

                    // If the path ending with "prev_v -> u -> v" is an augmenting path
                    if(k == 0 || recFindAugmentingPath(prev_v, all_layers_u, all_layers_v, matched_v, (k-1))) {
                        matched_v.put(v, u); // Edge u -> v replaces the previous matched edge connected to v.
                        all_layers_v.remove(v); // Remove vertex v from all_layers_v
                        all_layers_u.remove(u); // Remove vertex u from all_layers_u
                        return true;
                    }
                }
            }
        }

        return false; // No augmenting path found
    }

}