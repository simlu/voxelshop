package com.vitco.frames.engine.mainview;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 8/20/12
 * Time: 12:40 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MainViewInterface {

    @PostConstruct
    void init();

    @PreDestroy
    void finish();

    void build(JComponent frame);
}
