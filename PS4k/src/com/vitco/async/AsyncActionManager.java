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
    private final List<String> stack = Collections.synchronizedList(new ArrayList<String>());
    // retry to execute when the main stack is empty
    private final List<String> idleStack = Collections.synchronizedList(new ArrayList<String>());

    // list of current action names
    private final ConcurrentHashMap<String, AsyncAction> actionNames = new ConcurrentHashMap<String, AsyncAction>();

    public final void addAsyncAction(AsyncAction action) {
        String actionName = action.getName();
        if (null == actionNames.put(actionName, action)) {
            stack.add(actionName);
        }
    }

    @PostConstruct
    public void init() {
        // create new thread that deals with things
        threadManager.manage(new LifeTimeThread() {
            @Override
            public void loop() throws InterruptedException {
                if (stack.size() > 0) {
                    // fetch action
                    String actionName = stack.remove(0);
                    AsyncAction action = actionNames.get(actionName);
                    if (action.ready()) {
                        action.performAction();
                        actionNames.remove(actionName);
                    } else {
                        idleStack.add(actionName);
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
