package com.vitco.util.misc;

import javax.swing.*;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.io.InputStream;
import java.net.URL;

/**
 * Wraps the ClassLoader interface to access resources.
 */
public class SaveResourceLoader {
    private final String resourceName;
    private final URL resource;
    public final boolean error;

    public SaveResourceLoader(String resourceName) {
        this.resourceName = resourceName;
        resource = ClassLoader.getSystemResource(resourceName);
        error = resource == null;
    }

    public Image asImage() {

        return Toolkit.getDefaultToolkit().getImage(resource);
    }

    public ImageIcon asIconImage() {
        return new ImageIcon(asImage());
    }

    public StreamSource asStreamSource() {
        InputStream stream = asInputStream();
        return new StreamSource(stream);
    }

    public InputStream asInputStream() {
        InputStream stream = ClassLoader.getSystemResourceAsStream(resourceName);
        if (stream == null) {
            System.err.println("Resource \"" + resourceName + "\" is undefined.");
        }
        return stream;
    }

    public String asString() {
        java.util.Scanner s = new java.util.Scanner(asInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public String[] asLines() {
        return asString().split("\n");
    }
}
