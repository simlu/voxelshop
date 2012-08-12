package com.vitco.util.error;

import com.vitco.logic.frames.console.ConsoleViewInterface;
import com.vitco.util.DateTools;
import com.vitco.util.FileTools;
import com.vitco.util.lang.LangSelectorInterface;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Deals with all the exceptions in the program. Writes them to file and tries to upload
 * them to a server.
 */
public class ErrorHandler implements ErrorHandlerInterface {

    // var & setter
    private ConsoleViewInterface consoleView;
    @Override
    public void setConsoleView(ConsoleViewInterface consoleView) {
        this.consoleView = consoleView;
    }

    // var & setter
    private String debugReportUrl;
    @Override
    public void setDebugReportUrl(String debugReportUrl) {
        this.debugReportUrl = debugReportUrl;
    }

    // to check if we are in debug mode
    private static boolean debug = false;
    public static void setDebugMode() {
        ErrorHandler.debug = true;
    }

    // var & setter
    private LangSelectorInterface langSelector;
    @Override
    public void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }

    // handle exceptions
    @Override
    public void handle(Throwable e) {
        if (debug) {
            // print the trace (debug)
            e.printStackTrace();
        } else {
            Toolkit.getDefaultToolkit().beep(); // play beep
            // show dialog
            if (JOptionPane.showOptionDialog(null, langSelector.getString("error_dialog_text"),
                    langSelector.getString("error_dialog_caption"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    new String[]{langSelector.getString("error_dialog_clc_yes"),
                            langSelector.getString("error_dialog_clc_no")},
                    0) == JOptionPane.YES_OPTION
                    ) {
                try {
                    // write temporary file with stack-trace
                    File temp = File.createTempFile("PS4k_" + DateTools.now("yyyy-MM-dd_HH-mm-ss_"), ".error");
                    temp.deleteOnExit();
                    PrintStream ps = new PrintStream(temp);
                    e.printStackTrace(ps);

                    // upload to server
                    uploadFile(temp);

                } catch (FileNotFoundException e1) {
                    //e1.printStackTrace();
                } catch (IOException e1) {
                    //e1.printStackTrace();
                }
            }
        }

    }

    // upload file to server
    private void uploadFile(File temp) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(debugReportUrl);
        try {
            FileBody body = new FileBody(temp);
            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("report", body);
            httpPost.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (FileTools.inputStreamToString(entity.getContent()).equals("1")) {
                // upload was successful, notify the user
                consoleView.addLine(langSelector.getString("error_dialog_upload_ok"));
            } else {
                consoleView.addLine(langSelector.getString("error_dialog_upload_failed"));
            }
            // do something useful with the response body
            // and ensure it is fully consumed
            EntityUtils.consume(entity);
        } catch (IOException e) {
            // If this fails, the program is not reporting.
            if (debug) {
                e.printStackTrace();
            }
        } finally {
            httpPost.releaseConnection();
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            handle(e);
        } catch (Exception ex) {
            // make sure there will never be a loop!
        }
    }
}
