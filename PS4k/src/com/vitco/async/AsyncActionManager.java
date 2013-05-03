package com.vitco.async;

import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.thread.LifeTimeThread;
import com.vitco.util.thread.ThreadManagerInterface;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages Async Actions
 */
public class AsyncActionManager {

    // var & setter
    private ErrorHandlerInterface errorHandler;
    @Autowired
    public final void setErrorHandler(ErrorHandlerInterface errorHandler) {
        this.errorHandler = errorHandler;
    }

    private ThreadManagerInterface threadManager;
    // set the action handler
    @Autowired
    public final void setThreadManager(ThreadManagerInterface threadManager) {
        this.threadManager = threadManager;
    }

    // list of actions
    private final List<AsyncAction> stack = Collections.synchronizedList(new ArrayList<AsyncAction>());
    // retry to execute when the main stack is empty
    private final List<AsyncAction> idleStack = Collections.synchronizedList(new ArrayList<AsyncAction>());

    // list of current action names
    private final ConcurrentHashMap<String, String> actionNames = new ConcurrentHashMap<String, String>();

    public final void addAsyncAction(AsyncAction action) {
        String actionName = action.getName();
        if (!actionNames.containsKey(actionName)) {
            stack.add(action);
            actionNames.put(actionName, "blank");
        }
    }

    @PostConstruct
    public void init() {
        // create new thread that deals with things
        threadManager.manage(new LifeTimeThread() {
            @Override
            public void loop() throws InterruptedException {
                if (stack.size() > 0) {
                    //System.out.println(stack.size());
                    // fetch action
                    final AsyncAction action = stack.remove(0);
                    if (action.ready()) {
//                            SwingUtilities.invokeAndWait(new Runnable() {
//                                @Override
//                                public void run() {
//                                    action.performAction();
//                                }
//                            });
                        action.performAction();
                        actionNames.remove(action.getName());
                    } else {
                        idleStack.add(action);
                    }
                } else {
                    // add back to main stack
                    if (idleStack.size() > 0) {
                        stack.add(idleStack.remove(0));
                    }
                    sleep(50);
                }
            }
        });
    }
}
