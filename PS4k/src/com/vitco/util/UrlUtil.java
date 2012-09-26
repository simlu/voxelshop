package com.vitco.util;

import com.vitco.util.error.ErrorHandlerInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UrlUtil {

    // read the content of a url and return as string
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
        } catch (IOException e) {
            errorHandler.handle(e);
        }

        return result.toString();

    }
}
