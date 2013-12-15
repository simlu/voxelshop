package com.vitco.manager.updater;

import com.vitco.layout.content.console.ConsoleInterface;
import com.vitco.manager.action.ActionManager;
import com.vitco.manager.error.ErrorHandlerInterface;
import com.vitco.manager.lang.LangSelectorInterface;
import com.vitco.manager.thread.LifeTimeThread;
import com.vitco.manager.thread.ThreadManagerInterface;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.misc.FileTools;
import com.vitco.util.misc.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Handles update notification of the program
 */
public class Updater {

    // var & setter
    protected LangSelectorInterface langSelector;
    @Autowired(required=true)
    public final void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }

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

    // var & setter
    protected ActionManager actionManager;
    @Autowired(required=true)
    public final void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    private String digest = null;

    private final LifeTimeThread updaterThread = new LifeTimeThread() {
        private boolean notify = false;

        @Override
        public void loop() throws InterruptedException {
            synchronized (updaterThread) {
                updaterThread.wait(60000);
            }
            if (notify) { // wait an additional minute before notifying
                console.addLine(langSelector.getString("update_available_please_restart"));
                stopThread();
            } else {
                String updaterInfo = UrlUtil.readUrl(VitcoSettings.PROGRAM_UPDATER_URL, errorHandler);
                if (updaterInfo != null && !updaterInfo.equals("")) {
                    //console.addLine("===" + updaterInfo + "===");
                    String newDigest = FileTools.md5Hash(updaterInfo, errorHandler);
                    if (digest != null) {
                        if (!digest.equals(newDigest) && !newDigest.equals("")) {
                            notify = true;
                        }
                    }
                    digest = newDigest;
                }
            }
        }
    };

    @PostConstruct
    public final void init() {
        // todo check if this works on mac & linux
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
                //console.addLine("===" + localUpdaterInfo + "===");
            }
        } catch (UnsupportedEncodingException e) {
            errorHandler.handle(e);
        }

        actionManager.registerAction("force_update_check", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (updaterThread) {
                    updaterThread.notify();
                }
            }
        });

        // initialize the update checker
        threadManager.manage(updaterThread);
    }
}
