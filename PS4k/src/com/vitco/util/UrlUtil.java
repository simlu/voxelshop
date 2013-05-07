package com.vitco.util;

import com.vitco.util.error.ErrorHandlerInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;

public class UrlUtil {

    // returns "" on error
    public static String readUrl(String url, ErrorHandlerInterface errorHandler) {
        URL url2;
        StringBuilder result = new StringBuilder();
        try {
            url2 = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(url2
                    .openStream()), 1024);

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
}
