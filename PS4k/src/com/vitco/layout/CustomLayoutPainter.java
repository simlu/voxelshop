package com.vitco.layout;

import com.jidesoft.plaf.basic.BasicPainter;
import com.vitco.settings.VitcoSettings;

import javax.swing.*;
import java.awt.*;

/**
 * Custom painter class for a custom layout.
 *
 * todo: look at sample files
 * https://github.com/jidesoft/jide-oss/blob/master/src/com/jidesoft/plaf/basic/BasicPainter.java
 */
public abstract class CustomLayoutPainter extends BasicPainter {

    // ======================

    @Override
    public java.awt.Color getGripperForeground() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getGripperForegroundLt() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getSeparatorForeground() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getSeparatorForegroundLt() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getCollapsiblePaneContentBackground() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getCollapsiblePaneTitleForeground() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getCollapsiblePaneTitleForegroundEmphasized() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getCollapsiblePaneFocusTitleForegroundEmphasized() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getCollapsiblePaneFocusTitleForeground() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getBackgroundDk() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getBackgroundLt() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getSelectionSelectedDk() {
        return VitcoSettings.TABBED_PANE_HEADER_ACTIVE_COLOR;
    }

    @Override
    public java.awt.Color getSelectionSelectedLt() {
        return VitcoSettings.TABBED_PANE_HEADER_ACTIVE_COLOR;
    }

    @Override
    public java.awt.Color getMenuItemBorderColor() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getMenuItemBackground() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getCommandBarTitleBarBackground() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getControl() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getControlLt() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getControlDk() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getControlShadow() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getTitleBarBackground() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getDockableFrameTitleBarActiveForeground() {
        return VitcoSettings.SOFT_WHITE;
    }

    @Override
    public java.awt.Color getDockableFrameTitleBarInactiveForeground() {
        return VitcoSettings.SOFT_WHITE;
    }

    @Override
    public java.awt.Color getTabbedPaneSelectDk() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getTabbedPaneSelectLt() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getTabAreaBackgroundDk() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getTabAreaBackgroundLt() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getOptionPaneBannerForeground() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getOptionPaneBannerDk() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public java.awt.Color getOptionPaneBannerLt() {
        return VitcoSettings.DEFAULT_BG_COLOR;
    }

    @Override
    public void paintSelectedMenu(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintMenuItemBackground(javax.swing.JComponent c, java.awt.Graphics g, java.awt.Rectangle rect, int orientation, int state) {
        paintButtonBackground(c, g, rect, orientation, state, true);
    }

    @Override
    public void paintMenuItemBackground(javax.swing.JComponent c, java.awt.Graphics g, java.awt.Rectangle rect, int orientation, int state, boolean b) {
        paintButtonBackground(c, g, rect, orientation, state, b);
    }

    @Override
    public void paintButtonBackground(JComponent c, Graphics g, Rectangle rect, int orientation, int state) {
        paintButtonBackground(c, g, rect, orientation, state, true);
    }

    @Override
    public void paintChevronBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintDividerBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintFloatingCommandBarBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintMenuShadow(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintContentBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.SOFT_BLACK);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintStatusBarBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCommandBarTitlePane(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintChevronOption(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintFloatingChevronOption(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintDockableFrameBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintDockableFrameTitlePane(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCollapsiblePaneTitlePaneBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCollapsiblePaneTitlePaneBackgroundEmphasized(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCollapsiblePanesBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCollapsiblePaneTitlePaneBackgroundPlainEmphasized(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCollapsiblePaneTitlePaneBackgroundPlain(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCollapsiblePaneTitlePaneBackgroundSeparatorEmphasized(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCollapsiblePaneTitlePaneBackgroundSeparator(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintTabAreaBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintTabBackground(JComponent c, Graphics g, Shape region, Color[] colors, int orientation, int state) {
        super.paintTabBackground(c, g, region, colors, orientation, state);
    }

    @Override
    public void paintTabContentBorder(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintSidePaneItemBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, java.awt.Color[] colors, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintHeaderBoxBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

//    @Override
//    public void paintToolBarSeparator(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
//        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
//        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
//    }

    @Override
    public void paintPopupMenuSeparator(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintStatusBarSeparator(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }


    @Override
    public void fillBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1, java.awt.Color color) {
        graphics.setColor(VitcoSettings.DEFAULT_BG_COLOR);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public java.awt.Insets getSortableTableHeaderColumnCellDecoratorInsets(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1, int i2, javax.swing.Icon icon, int i3, java.awt.Color color, boolean b) {
        return new Insets(0, 0, 0, 0);
    }

    @Override
    public void paintSortableTableHeaderColumn(JComponent c, Graphics g, Rectangle rect,
                                               int orientation, int state, int sortOrder, Icon sortIcon,
                                               int orderIndex, Color indexColor, boolean paintIndex) {
        g.setColor(Color.RED);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
    }


}