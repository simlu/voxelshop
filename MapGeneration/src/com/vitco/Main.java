package com.vitco;

import com.vitco.map.TextureColorCluster;

/**
 * Main Class
 */
public class Main {
    public static void main(String[] arg) {
        TextureColorCluster cluster = new TextureColorCluster();
        cluster.findRegions("data/voxel_ground3_TXT.png",
                "data/heightmap cave.png", "result");
    }
}
