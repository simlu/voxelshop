package com.vitco.map;

import com.vitco.util.ImgTools;
import com.vividsolutions.jts.geom.Polygon;
import org.jaitools.imageutils.ImageUtils;

import javax.media.jai.TiledImage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Clusters an image by colors
 */
public class TextureColorCluster {

    public void findRegions(String textureName, String heightmapName, String writeTo) {
        // load the input texture
        BufferedImage texture = ImgTools.loadImage(textureName);

        // load the heightmap
        BufferedImage heightmap = ImgTools.loadImage(heightmapName);

        if (texture != null &&
                heightmap != null &&
                heightmap.getHeight() == texture.getHeight() &&
                heightmap.getWidth() == texture.getWidth()) {
            // get the clusters
            //TiledImage[] clusters = new TextureClusterer(texture, 3).cluster();

            HashMap<TiledImage, Float> clusters = new HashMap<TiledImage, Float>();
            // get the clusters from the heightmap
            {
                HashSet<Integer> heights = new HashSet<Integer>();
                // fetch all the colors in the heightmap
                for (int x = 0; x < heightmap.getWidth(); x++) {
                    for (int y = 0; y < heightmap.getHeight(); y++) {
                        heights.add(heightmap.getRGB(x,y));
                    }
                }
                for (Integer rgb : heights) {
                    // generate cluster
                    TiledImage src = ImageUtils.createConstantImage(
                            heightmap.getWidth(), heightmap.getHeight(), 0);
                    for (int x = 0; x < heightmap.getWidth(); x++) {
                        for (int y = 0; y < heightmap.getHeight(); y++) {
                            if (heightmap.getRGB(x,y) == rgb) {
                                src.setSample(x, y, 0, 1);
                            }
                        }
                    }
                    // add to clusters
                    clusters.put(src, new Color(rgb).getBlue()/25f);
                }

            }

            // manages planes and creates dae file
            PlaneManager planeManager = new PlaneManager(texture.getWidth(), texture.getHeight());

            // feed all the clusters
            float height = 0;
            for (Map.Entry<TiledImage, Float> cluster : clusters.entrySet()) {
                height = cluster.getValue();
                for (Polygon poly : VectorTools.doVectorize(cluster.getKey())) {
                    planeManager.addPlane(poly, height);
                }
            }

            planeManager.generateSides();

            // write the texture file
            ImgTools.writeAsPNG(texture, "result/" + writeTo + ".png");

            // finish up
            planeManager.finish(writeTo + ".png");
            planeManager.writeToFile("result/ " + writeTo + ".dae");

        }
    }
}
