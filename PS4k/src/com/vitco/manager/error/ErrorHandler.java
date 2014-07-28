package com.vitco.manager.error;

import com.vitco.layout.content.console.ConsoleInterface;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.misc.DateTools;
import com.vitco.manager.lang.LangSelectorInterface;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URLDecoder;

/**
 * Deals with all the exceptions in the program. Writes them to file and tries to upload
 * them to a server.
 */
public class ErrorHandler implements ErrorHandlerInterface {

    // var & setter
    private ConsoleInterface console;
    @Override
    public final void setConsole(ConsoleInterface console) {
        this.console = console;
        initOutMapping();
    }

    private void initOutMapping() {
        if (!debug) {
            // write error to console
            try {
                System.setErr(new PrintStream(new OutputStream() {
                    private String buffer = "";
                    @Override
                    public void write(int arg0) throws IOException {
                        if (((char)arg0) == '\n') {
                            console.addLine(buffer);
                            buffer = "";
                        } else {
                            buffer += (char)arg0;
                        }

                    }
                }, true, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                this.handle(e);
            }
            // write out to console
            try {
                System.setOut(new PrintStream(new OutputStream() {
                    private String buffer = "";
                    @Override
                    public void write(int arg0) throws IOException {
                        if (((char)arg0) == '\n') {
                            console.addLine(buffer);
                            buffer = "";
                        } else {
                            buffer += (char)arg0;
                        }

                    }
                }, true, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                this.handle(e);
            }
        }
    }

    // var & setter
    private String debugReportUrl;
    @Override
    public final void setDebugReportUrl(String debugReportUrl) {
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
    public final void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }

    private long lastErrorReport = 0;
    private final static long error_spam_timeout = 2*60*1000; // 2 minutes

    // handle exceptions
    @Override
    public void handle(Throwable e) {
        synchronized (VitcoSettings.SYNC) {
            if (debug) {
                // print the trace (debug)
                e.printStackTrace();
            } else {
                if (lastErrorReport + error_spam_timeout < System.currentTimeMillis()) {
                    lastErrorReport = System.currentTimeMillis();
                    Toolkit.getDefaultToolkit().beep(); // play beep
                    boolean result = false;

                    // print this error to file
                    String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                    try {
                        String appJarLocation = URLDecoder.decode(path, "UTF-8");
                        File appJar = new File(appJarLocation);
                        String absolutePath = appJar.getAbsolutePath();
                        String filePath = absolutePath.
                                substring(0, absolutePath.lastIndexOf(File.separator) + 1);
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(new FileOutputStream(filePath + "errorlog.txt", true),"UTF-8")));
                        out.println("===================");
                        out.println(DateTools.now("yyyy-MM-dd HH-mm-ss"));
                        out.println("-------------------");
                        e.printStackTrace(out);
                        out.println();
                        out.close();
                    } catch (UnsupportedEncodingException ex) {
                        // If this fails, the program is not reporting.
                        if (debug) {
                            ex.printStackTrace();
                        }
                    } catch (IOException ex) {
                        // If this fails, the program is not reporting.
                        if (debug) {
                            ex.printStackTrace();
                        }
                    }

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
                            PrintStream ps = new PrintStream(temp, "utf-8");
                            e.printStackTrace(ps);

                            // upload to server
                            if (uploadFile(temp, e.getMessage())) {
                                result = true;
                            }

                        } catch (FileNotFoundException e1) {
                            if (debug) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e1) {
                            if (debug) {
                                e1.printStackTrace();
                            }
                        }
                    }
                    if (!result) {
                        console.addLine(langSelector.getString("error_dialog_upload_failed"));
                        console.addLine(langSelector.getString("error_dialog_request_upload_manually"));
                    } else {
                        console.addLine(langSelector.getString("error_dialog_upload_ok"));
                    }
                }
            }
        }
    }

    // upload file to server
    private boolean uploadFile(File temp, String error) {
        boolean result = false;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(debugReportUrl);
        try {
            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("error", new StringBody(error));
            reqEntity.addPart("report", new FileBody(temp));
            httpPost.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            // do something useful with the response body
            if (EntityUtils.toString(entity).equals("1")) {
                // upload was successful
                result = true;
            }
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
        return result;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            handle(e);
        } catch (Exception ex) {
            // makes sure there will never be a loop!
        }
    }
}
