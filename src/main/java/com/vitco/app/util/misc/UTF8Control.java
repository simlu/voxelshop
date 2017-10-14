package com.vitco.app.util.misc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

// Reference: https://stackoverflow.com/questions/4659929/how-to-use-utf-8-in-resource-properties-with-resourcebundle

public class UTF8Control extends Control {

    private final Locale locale;

    public UTF8Control(Locale locale) {
        this.locale = locale;
    }

    public ResourceBundle newBundle(String baseName, Locale ignored, String format, ClassLoader loader, boolean reload)
            throws IllegalAccessException, InstantiationException, IOException
    {
        // The below is a copy of the default implementation.
        String bundleName = toBundleName(baseName, this.locale);
        String resourceName = toResourceName(bundleName, "properties");
        ResourceBundle bundle = null;
        InputStream stream = null;
        if (reload) {
            URL url = loader.getResource(resourceName);
            if (url != null) {
                URLConnection connection = url.openConnection();
                if (connection != null) {
                    connection.setUseCaches(false);
                    stream = connection.getInputStream();
                }
            }
        } else {
            stream = loader.getResourceAsStream(resourceName);
        }
        if (stream != null) {
            try (
                InputStreamReader inputStreamReader = new InputStreamReader(stream, "UTF-8")
            ){
                // Read properties files as user defined and if not provided then use "UTF-8" encoding.
                bundle = new PropertyResourceBundle(inputStreamReader);
            }
        }
        return bundle;
    }
}
