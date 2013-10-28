package com.canny;

import com.canny.CannyEdgeDetector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Canny example
 */
public class EdgeDetection {
    private static void findEdges(BufferedImage clusterImage, int i) {
        // find the edges (canny)
        //create the detector
        CannyEdgeDetector detector = new CannyEdgeDetector();
        //adjust its parameters as desired
        detector.setLowThreshold(1.0f);
        detector.setHighThreshold(1.0f);
        //apply it to an image
        detector.setSourceImage(clusterImage);
        detector.process();
        BufferedImage edges = detector.getEdgesImage();
        try {
            ImageIO.write(edges, "png", new File("result/cluster" + i + "_edges.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
