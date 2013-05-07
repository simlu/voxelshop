package com.vitco.async;

import com.vitco.util.DateTools;
import com.vitco.util.SwingAsyncHelper;
import com.vitco.util.action.ActionManager;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.thread.LifeTimeThread;
import com.vitco.util.thread.ThreadManagerInterface;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages Async Actions
 */
public class AsyncActionManager {

    // var & setter
    protected ErrorHandlerInterface errorHandler;
    @Autowired(required=true)
    public final void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    protected ActionManager actionManager;
    @Autowired(required=true)
    public final void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    private ThreadManagerInterface threadManager;
    // set the action handler
    @Autowired
    public final void setThreadManager(ThreadManagerInterface threadManager) {
        this.threadManager = threadManager;
    }

    // list of actions
    private final List<String> stack = Collections.synchronizedList(new ArrayList<String>());
    // retry to execute when the main stack is empty
    private final List<String> idleStack = Collections.synchronizedList(new ArrayList<String>());

    // list of current action names
    private final ConcurrentHashMap<String, AsyncAction> actionNames = new ConcurrentHashMap<String, AsyncAction>();

    public final void removeAsyncAction(String actionName) {
        if (null != actionNames.remove(actionName)) {
            stack.remove(actionName);
            idleStack.remove(actionName);
        }
    }

    // Note: re-adding an action does not ensure that the action
    // is at the end of the queue!
    public final void addAsyncAction(AsyncAction action) {
        String actionName = action.getName();
        if (null == actionNames.put(actionName, action)) {
            stack.add(actionName);
        }
    }

    @PostConstruct
    public void init() {

        // register debug mode (console)
        actionManager.registerAction("initialize_deadlock_debug", new AbstractAction() {
            private boolean isRunning = false;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isRunning) {
                    // create watchdog thread that checks for deadlocks
                    threadManager.manage(new LifeTimeThread() {
                        @Override
                        public void loop() throws InterruptedException {
                            ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
                            long[] ids = tmx.findDeadlockedThreads();
                            if (ids != null) {
                                // obtain thread info
                                ThreadInfo[] infos = tmx.getThreadInfo(ids, true, true);
                                StringBuilder errorMsg = new StringBuilder();
                                errorMsg.append("The following threads are deadlocked:\n");
                                for (ThreadInfo ti : infos) {
                                    errorMsg.append(ti);
                                }
                                String error = errorMsg.toString();

                                // print info to file
                                String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                                try {
                                    String appJarLocation = URLDecoder.decode(path, "UTF-8");
                                    File appJar = new File(appJarLocation);
                                    String absolutePath = appJar.getAbsolutePath();
                                    String filePath = absolutePath.
                                            substring(0, absolutePath.lastIndexOf(File.separator) + 1);
                                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath + "errorlog.txt", true)));
                                    out.println("===================");
                                    out.println(DateTools.now("yyyy-MM-dd HH-mm-ss"));
                                    out.println("-------------------");
                                    out.println(error);
                                    out.println();
                                    out.close();
                                } catch (UnsupportedEncodingException ex) {
                                    errorHandler.handle(ex);
                                } catch (IOException ex) {
                                    errorHandler.handle(ex);
                                }
                                // print info the error handler
                                errorHandler.handle(new Exception(error));
                                interrupt();
                            }
                            SwingAsyncHelper.handle(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println("xxx");
                                }
                            }, errorHandler);

                            Thread.sleep(5000);
                        }
                    });
                    isRunning = true;
                }
            }
        });

        // create new thread that deals with things
        threadManager.manage(new LifeTimeThread() {
            @Override
            public void loop() throws InterruptedException {
                if (stack.size() > 0) {
                    // fetch action
                    String actionName = stack.remove(0);
                    AsyncAction action = actionNames.get(actionName);
                    if (action.ready()) {
                        // remove first in case the action adds
                        // itself to the cue again (e.g. for refreshWorld())
                        actionNames.remove(actionName);
                        action.performAction();
                    } else {
                        idleStack.add(actionName);
                    }
                } else {
                    // add back to main stack
                    while (idleStack.size() > 0) {
                        stack.add(idleStack.remove(0));
                    }
                    sleep(50);
                }
            }
        });
    }
}
