package com.vitco.app.manager.updater;

import com.vitco.app.layout.content.console.ConsoleInterface;
import com.vitco.app.manager.action.ActionManager;
import com.vitco.app.manager.error.ErrorHandlerInterface;
import com.vitco.app.manager.lang.LangSelectorInterface;
import com.vitco.app.manager.thread.LifeTimeThread;
import com.vitco.app.manager.thread.ThreadManagerInterface;
import com.vitco.app.settings.VitcoSettings;
import com.vitco.app.util.file.FileTools;
import com.vitco.app.util.misc.UrlUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.URLDecoder;

/**
 * Handles
 *
 * - update notification of the program
 * - updating of the updater
 *
 * Note: The updated updater only takes effect for the next start.
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
                    String newDigest = DigestUtils.md5Hex(updaterInfo);
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

            // ---------
            // check if the updater has updated
            upgradeGetdown(
                    new File(filePath + "getdown-client-old.jar"),
                    new File(filePath + "getdown-client.jar"),
                    new File(filePath + "getdown-client-new.jar")
            );
            // ---------

            File digestFile = new File(filePath + "digest.txt");
            if (digestFile.exists()) {
                String localUpdaterInfo = FileTools.readFileAsString(digestFile, errorHandler);
                digest = DigestUtils.md5Hex(localUpdaterInfo);
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

    /**
     * Copies the contents of the supplied input stream to the supplied output stream.
     */
    public static <T extends OutputStream> T copy (InputStream in, T out) throws IOException {
        byte[] buffer = new byte[4096];
        for (int read; (read = in.read(buffer)) > 0; ) {
            out.write(buffer, 0, read);
        }
        return out;
    }

    /**
     * Upgrades Getdown by moving an installation managed copy of the Getdown jar file over the
     * non-managed copy (which would be used to run Getdown itself).
     *
     * <p> If the upgrade fails for a variety of reasons, there's not much else one
     * can do other than try again next time around.
     */
    public void upgradeGetdown (File oldgd, File curgd, File newgd) {
        // we assume getdown's jar file size changes with every upgrade, this is not guaranteed,
        // but in reality it will, and it allows us to avoid pointlessly upgrading getdown every
        // time the client is updated which is unnecessarily flirting with danger
        if (!newgd.exists() || newgd.length() == curgd.length()) {
            return;
        }

        // clear out any old getdown
        if (oldgd.exists()) {
            if (!oldgd.delete()) {
                console.addLine("Error: Failed to remove old file version (1).");
            }
        }

        // now try updating using renames
        if (!curgd.exists() || curgd.renameTo(oldgd)) {
            if (newgd.renameTo(curgd)) {
                if (oldgd.exists()) {
                    if (!oldgd.delete()) {
                        console.addLine("Error: Failed to remove old file version (2).");
                    }
                }
                try {
                    // copy the moved file back to getdown-dop-new.jar so that we don't end up
                    // downloading another copy next time
                    copy(new FileInputStream(curgd), new FileOutputStream(newgd));
                } catch (IOException e) {
                    errorHandler.handle(e);
                }
                return;
            }

            // try to unfuck ourselves
            if (!oldgd.renameTo(curgd)) {
                console.addLine("Error: Failed to revert renaming.");
            }
        }

        // that didn't work, let's try copying it
        try {
            copy(new FileInputStream(newgd), new FileOutputStream(curgd));
        } catch (IOException ioe) {
            errorHandler.handle(ioe);
        }
    }
}
