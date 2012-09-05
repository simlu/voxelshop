package com.vitco.util.thread;

import com.vitco.util.action.ActionManagerInterface;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * Helps with creating threads.
 */
public interface ThreadManagerInterface {
    // set the action handler
    @Autowired
    void setActionManager(ActionManagerInterface actionManager);

    void manage(LifeTimeThread thread);

    @PostConstruct
    void init();

    void remove(LifeTimeThread thread);
}
