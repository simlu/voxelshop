package com.vitco;

import com.vitco.map.TextureColorCluster;

/**
 * Main Class
 */
public class Main {
    public static void main(String[] arg) {
        TextureColorCluster cluster = new TextureColorCluster();
        cluster.findRegions("data/voxel_ground_TXT05.png", "data/voxel_ground_TXT05_BW.png", "result");
    }
}
