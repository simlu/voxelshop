package com.vitco.map;

import com.vitco.export.ColladaFile;
import com.vitco.export.container.UVPoint;
import com.vitco.export.container.Vertex;
import com.vitco.util.ImgTools;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import javax.media.jai.TiledImage;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Clusters an image by colors
 */
public class TextureColorCluster {

    public void findRegions(String textureName, String writeTo) {
        // load the input texture
        BufferedImage texture = ImgTools.loadImage(textureName);
        if (texture != null) {
            // get the clusters
            TiledImage[] clusters = new TextureClusterer(texture, 3).supervisedClustering();

            // manages planes and creates dae file
            PlaneManager planeManager = new PlaneManager(texture.getWidth(), texture.getHeight());

            // feed all the clusters
            float height = 0;
            for (TiledImage cluster : clusters) {
                for (Polygon poly : VectorTools.doVectorize(cluster)) {
                    planeManager.addPlane(poly, height);
                }
                height += 3f;
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
