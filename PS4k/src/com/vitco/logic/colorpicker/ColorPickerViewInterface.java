package com.vitco.logic.colorpicker;

import com.vitco.util.thread.ThreadManagerInterface;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;

/**
 * Builds the color picker.
 */
public interface ColorPickerViewInterface {
    JPanel build();

    // set the action handler
    @Autowired
    void setThreadManager(ThreadManagerInterface threadManager);

    @PreDestroy
    void finish();

    @PostConstruct
    void init();
}
