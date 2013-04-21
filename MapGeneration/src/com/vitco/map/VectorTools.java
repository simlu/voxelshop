package com.vitco.map;

import com.vividsolutions.jts.geom.Polygon;
import org.jaitools.media.jai.vectorize.VectorizeDescriptor;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import java.awt.image.RenderedImage;
import java.util.Collection;
import java.util.Collections;

/**
 * Represents
 */
public class VectorTools {
    // converts "black and white" image into vector representation
    public static Collection<Polygon> doVectorize(RenderedImage src) {
        ParameterBlockJAI pb = new ParameterBlockJAI("Vectorize");
        pb.setSource("source0", src);

        pb.setParameter("outsideValues", Collections.singleton(0));

        // Get the desintation image: this is the unmodified source image data
        // plus a property for the generated vectors
        RenderedOp dest = JAI.create("Vectorize", pb);

        // Get the vectors
        Object property = dest.getProperty(VectorizeDescriptor.VECTOR_PROPERTY_NAME);
        return (Collection<Polygon>) property;
    }

}
