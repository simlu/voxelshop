package com.vitco.util;

import com.vitco.logic.console.ConsoleInterface;
import com.vitco.res.VitcoSettings;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.thread.LifeTimeThread;
import com.vitco.util.thread.ThreadManagerInterface;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Handles update notification of the program
 */
public class Updater {

    private ThreadManagerInterface threadManager;
    // set the action handler
    @Autowired
    public final void setThreadManager(ThreadManagerInterface threadManager) {
        this.threadManager = threadManager;
    }

    // var & setter
    private ErrorHandlerInterface errorHandler;
    @Autowired
    public final void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    // var & setter
    private ConsoleInterface console;
    @Autowired
    public final void setConsole(ConsoleInterface console) {
        this.console = console;
    }

    private String digest = null;

    @PostConstruct
    public final void init() {
        // load the local digest
        String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            String appJarLocation = URLDecoder.decode(path, "UTF-8");
            File appJar = new File(appJarLocation);
            String absolutePath = appJar.getAbsolutePath();
            String filePath = absolutePath.
                    substring(0, absolutePath.lastIndexOf(File.separator) + 1);
            File digestFile = new File(filePath + "digest.txt");
            if (digestFile.exists()) {
                String localUpdaterInfo = FileTools.readFileAsString(digestFile, errorHandler);
                digest = FileTools.md5Hash(localUpdaterInfo, errorHandler);
            }
        } catch (UnsupportedEncodingException e) {
            errorHandler.handle(e);
        }

        // initialize the update checker
        threadManager.manage(new LifeTimeThread() {
            private int i = 0;
            private boolean notify = false;

            @Override
            public void loop() throws InterruptedException {
                if (++i >= 60) { // check for update every minute
                    if (notify) { // wait an additional minute before notifying
                        console.addLine("There is an update available. Please restart to apply it.");
                        stopThread();
                    } else {
                        String updaterInfo = UrlUtil.readUrl(VitcoSettings.PROGRAM_UPDATER_URL, errorHandler);
                        String newDigest = FileTools.md5Hash(updaterInfo, errorHandler);
                        if (digest != null) {
                            if (!digest.equals(newDigest)) {
                                notify = true;
                            }
                        }
                        digest = newDigest;
                    }

                    i = 0;
                }
                sleep(1000);
            }
        });
    }
}
