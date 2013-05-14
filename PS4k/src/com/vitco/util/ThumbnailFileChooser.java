package com.vitco.util;

import javax.swing.*;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

// reference:
// http://stackoverflow.com/questions/4096433/making-jfilechooser-show-image-thumbnails

public class ThumbnailFileChooser extends JFileChooser {

    /** All preview icons will be this width and height */
    private final int ICON_SIZE_X;
    private final int ICON_SIZE_Y;

    /** This blank icon will be used while previews are loading */
    private transient final Image LOADING_IMAGE;

    /** Edit this to determine what file types will be previewed. */
    private final Pattern imageFilePattern = Pattern.compile(".+?\\.(png|jpe?g|gif|tiff?)$", Pattern.CASE_INSENSITIVE);

    /** Use a weak hash map to cache images until the next garbage collection (saves memory) */
    private transient final Map<File, ImageIcon> imageCache = new WeakHashMap<File, ImageIcon>();

    // constructor
    public ThumbnailFileChooser(int x, int y) {
        super();
        ICON_SIZE_X = x;
        ICON_SIZE_Y = y;
        LOADING_IMAGE = new BufferedImage(ICON_SIZE_X, ICON_SIZE_Y, BufferedImage.TYPE_INT_ARGB);
    }

    // --- Override the other constructors as needed ---

    {
        // This initializer block is always executed after any constructor call.
        setFileView(new ThumbnailView());
    }

    private class ThumbnailView extends FileView {
        /** This thread pool is where the thumnnail icon loaders run */
        private final ExecutorService executor = Executors.newCachedThreadPool();

        public Icon getIcon(File file) {
            if (!imageFilePattern.matcher(file.getName()).matches()) {
                return null;
            }

            // Our cache makes browsing back and forth lightning-fast! :D
            synchronized (imageCache) {
                ImageIcon icon = imageCache.get(file);

                if (icon == null) {
                    // Create a new icon with the default image
                    icon = new ImageIcon(LOADING_IMAGE);

                    // Add to the cache
                    imageCache.put(file, icon);

                    // Submit a new task to load the image and update the icon
                    executor.submit(new ThumbnailIconLoader(icon, file));
                }

                return icon;
            }
        }
    }

    private class ThumbnailIconLoader implements Runnable {
        private final ImageIcon icon;
        private final File file;

        public ThumbnailIconLoader(ImageIcon i, File f) {
            icon = i;
            file = f;
        }

        public void run() {
            //System.out.println("Loading image: " + file);

            // Load and scale the image down, then replace the icon's old image with the new one.
            ImageIcon newIcon = new ImageIcon(file.getAbsolutePath());
            Image img = newIcon.getImage().getScaledInstance(ICON_SIZE_X, ICON_SIZE_Y, Image.SCALE_SMOOTH);
            icon.setImage(img);

            // Repaint the dialog so we see the new icon.
            SwingUtilities.invokeLater(new Runnable() {public void run() {
                repaint();
            }});
        }
    }

}