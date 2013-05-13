package com.vitco.async;

import com.vitco.logic.console.ConsoleInterface;
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
import java.util.HashMap;
import java.util.concurrent.*;

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

    private ConsoleInterface console;
    // set the action handler
    @Autowired
    public final void setConsole(ConsoleInterface console) {
        this.console = console;
    }

    private ThreadManagerInterface threadManager;
    // set the action handler
    @Autowired
    public final void setThreadManager(ThreadManagerInterface threadManager) {
        this.threadManager = threadManager;
    }

    // list of new action
    private final ArrayList<AsyncAction> newActions = new ArrayList<AsyncAction>();

    // list of actions
    private final ArrayList<String> stack = new ArrayList<String>();
    // retry to execute when the main stack is empty
    private final ArrayList<String> idleStack = new ArrayList<String>();

    // list of current action names
    private final HashMap<String, AsyncAction> actionNames = new HashMap<String, AsyncAction>();

    public final void removeAsyncAction(String actionName) {
        if (null != actionNames.remove(actionName)) {
            stack.remove(actionName);
            idleStack.remove(actionName);
        }
    }

    // Note: re-adding an action does not ensure that the action
    // is at the end of the queue!
    public final void addAsyncAction(AsyncAction action) {
        synchronized (newActions) {
            newActions.add(action);
        }
        synchronized (workerThread) {
            workerThread.notify();
        }
    }

    // needs to be one as those tasks can not be executed in parallel!
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final LifeTimeThread workerThread = new LifeTimeThread() {

        @Override
        public void onBeforeStop() {
            executor.shutdown();
            // Wait until all threads are finish
            //noinspection StatementWithEmptyBody
            while (!executor.isTerminated()) {}
        }

        @Override
        public void loop() throws InterruptedException {
            // add new tasks
            synchronized (newActions) {
                if (!newActions.isEmpty()) {
                    for (AsyncAction action : newActions) {
                        String actionName = action.name;
                        if (null == actionNames.put(actionName, action)) {
                            stack.add(actionName);
                        }
                    }
                    newActions.clear();
                }
            }
            // handle stack execution
            if (!stack.isEmpty()) {
                // fetch action
                String actionName = stack.remove(0);
                final AsyncAction action = actionNames.get(actionName);
                if (action.ready()) {
                    lastAction = action;
                    // remove first in case the action adds
                    // itself to the cue again (e.g. for refreshWorld())
                    actionNames.remove(actionName);
                    //action.performAction();
                    executor.execute(action);
                } else {
                    idleStack.add(actionName);
                }
            } else {
                // add back to main stack
                while (!idleStack.isEmpty()) {
                    stack.add(idleStack.remove(0));
                }
                synchronized (workerThread) {
                    // sometimes notify "fails"(?), so we need a timeout here
                    wait(500);
                }
            }
        }
    };

    @PostConstruct
    public void init() {

        threadManager.manage(workerThread);

        // print the current stack
        actionManager.registerAction("aysnc_action_manager_print_stack_details", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                console.addLine("============");
                console.addLine("Async Action Manager");
                console.addLine("Printing Last Task: ");
                if (lastAction != null) {
                    console.addLine("------------");
                    console.addLine(lastAction.name);
                }
                console.addLine("============");
            }
        });

        // check that the manager is still alive and working
        actionManager.registerAction("aysnc_action_manager_alive_check", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                console.addLine("============");
                console.addLine("Testing Async Action Manager");
                console.addLine("Pending Tasks: " + (stack.size() + idleStack.size()) + " (" + idleStack.size() + ")");
                addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        final long time = System.currentTimeMillis();
                        SwingAsyncHelper.handle(new Runnable() {
                            @Override
                            public void run() {
                                console.addLine("Test Task executed in " + (System.currentTimeMillis() - time) + " ms");
                                console.addLine("============");
                            }
                        }, errorHandler);
                    }
                });
            }
        });

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

                            Thread.sleep(5000);
                        }
                    });
                    isRunning = true;
                }
            }
        });
    }

    private AsyncAction lastAction = null;
}
