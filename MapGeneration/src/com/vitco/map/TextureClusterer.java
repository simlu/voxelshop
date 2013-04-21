package com.vitco.map;

import com.vitco.export.ColladaFile;
import com.vitco.map.container.MapColor;
import com.vitco.util.ColorTools;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.jaitools.imageutils.ImageUtils;

import javax.media.jai.TiledImage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Clusters a texture according to colors
 */
public class TextureClusterer {
    private final BufferedImage texture;

    public TextureClusterer(BufferedImage texture) {
        this.texture = texture;
    }

    public TiledImage[] supervisedClustering() {
        // extract the colors from the image
        KMeansPlusPlusClusterer<MapColor> transformer =
                new KMeansPlusPlusClusterer<MapColor>(3, 100, new DistanceMeasure() {
                    @Override
                    public double compute(double[] rgb1, double[] rgb2) {
                        return ColorTools.colorDistanceNatural(rgb1, rgb2);
                    }
                });
        ArrayList<MapColor> toCluster = new ArrayList<MapColor>();
        for (int y = 0; y < texture.getHeight(); y++) {
            for (int x = 0; x < texture.getWidth(); x++) {
                toCluster.add(new MapColor(new Color(texture.getRGB(x, y)),new int[]{x, y}));
            }
        }

        // cluster the image
        java.util.List<CentroidCluster<MapColor>> clusters = transformer.cluster(toCluster);

        final int IMAGE_WIDTH = texture.getWidth();
        final int IMAGE_HEIGHT = texture.getHeight();

        // the "black and white" images of the clusters
        ArrayList<TiledImage> resultList = new ArrayList<TiledImage>();

        // loop over all the clusters
        for (CentroidCluster<MapColor> cluster : clusters) {
            TiledImage src = ImageUtils.createConstantImage(IMAGE_WIDTH, IMAGE_HEIGHT, 0);
            for (MapColor mapColor : cluster.getPoints()) {
                src.setSample(mapColor.getPos()[0], mapColor.getPos()[1], 0, 1);
            }
            resultList.add(src);
        }

        TiledImage[] result = new TiledImage[resultList.size()];
        resultList.toArray(result);
        return result;
    }

}
