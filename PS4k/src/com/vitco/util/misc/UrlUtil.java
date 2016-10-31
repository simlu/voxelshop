package com.vitco.util.misc;

import com.vitco.layout.content.console.ConsoleInterface;
import com.vitco.manager.error.ErrorHandlerInterface;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class UrlUtil {

    // returns "" on error
    public static String readUrl(String url, ErrorHandlerInterface errorHandler) {
        URL url2;
        StringBuilder result = new StringBuilder();
        try {
            url2 = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(url2.openStream(), "utf-8"), 1024);

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine).append("\n");
            }
            in.close();
        } catch (UnknownHostException ignored) {
            // this can happen when there is no internet connection
        } catch (ConnectException ignored) {
            // this can happen when there is no internet connection
        } catch (IOException ignored) {
            // this can also happen when there is no internet connection
            // (if the isp provides an error page?)
            // errorHandler.handle(e);
        }
        return result.toString();
    }

    // open a url
    public static void openURL(ConsoleInterface console, String url) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI(url));
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
            }
        } else {
            console.addLine("Error: Can not find a valid Browser.");
            console.addLine("Please visit: " + url);
        }
    }
}
