package com.vitco.layout;

import com.jidesoft.plaf.basic.BasicPainter;

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
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getGripperForegroundLt() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getSeparatorForeground() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getSeparatorForegroundLt() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getCollapsiblePaneContentBackground() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getCollapsiblePaneTitleForeground() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getCollapsiblePaneTitleForegroundEmphasized() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getCollapsiblePaneFocusTitleForegroundEmphasized() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getCollapsiblePaneFocusTitleForeground() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getBackgroundDk() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getBackgroundLt() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getSelectionSelectedDk() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getSelectionSelectedLt() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getMenuItemBorderColor() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getMenuItemBackground() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getCommandBarTitleBarBackground() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getControl() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getControlLt() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getControlDk() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getControlShadow() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getTitleBarBackground() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getDockableFrameTitleBarActiveForeground() {
        return new Color(255, 255, 255);
    }

    @Override
    public java.awt.Color getDockableFrameTitleBarInactiveForeground() {
        return new Color(255, 255, 255);
    }

    @Override
    public java.awt.Color getTabbedPaneSelectDk() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getTabbedPaneSelectLt() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getTabAreaBackgroundDk() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getTabAreaBackgroundLt() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getOptionPaneBannerForeground() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getOptionPaneBannerDk() {
        return new Color(83, 83, 83);
    }

    @Override
    public java.awt.Color getOptionPaneBannerLt() {
        return new Color(83, 83, 83);
    }

    @Override
    public void paintSelectedMenu(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintMenuItemBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintMenuItemBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1, boolean b) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintButtonBackground(JComponent c, Graphics g, Rectangle rect, int orientation, int state) {
        paintButtonBackground(c, g, rect, orientation, state, true);
    }

    @Override
    public void paintChevronBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintDividerBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCommandBarBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintFloatingCommandBarBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintMenuShadow(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintContentBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(38, 38, 38));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintStatusBarBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCommandBarTitlePane(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintGripper(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintChevronMore(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintChevronOption(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintFloatingChevronOption(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintDockableFrameBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintDockableFrameTitlePane(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCollapsiblePaneTitlePaneBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCollapsiblePaneTitlePaneBackgroundEmphasized(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCollapsiblePanesBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCollapsiblePaneTitlePaneBackgroundPlainEmphasized(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCollapsiblePaneTitlePaneBackgroundPlain(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCollapsiblePaneTitlePaneBackgroundSeparatorEmphasized(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintCollapsiblePaneTitlePaneBackgroundSeparator(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintTabAreaBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintTabBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Shape shape, java.awt.Color[] colors, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(0, 0, jComponent.getWidth(), jComponent.getHeight());
    }

    @Override
    public void paintTabContentBorder(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintSidePaneItemBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, java.awt.Color[] colors, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintHeaderBoxBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintToolBarSeparator(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintPopupMenuSeparator(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void paintStatusBarSeparator(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }


    @Override
    public void fillBackground(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1, java.awt.Color color) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public java.awt.Insets getSortableTableHeaderColumnCellDecoratorInsets(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1, int i2, javax.swing.Icon icon, int i3, java.awt.Color color, boolean b) {
        return new Insets(0, 0, 0, 0);
    }

    @Override
    public void paintSortableTableHeaderColumn(javax.swing.JComponent jComponent, java.awt.Graphics graphics, java.awt.Rectangle rectangle, int i, int i1, int i2, javax.swing.Icon icon, int i3, java.awt.Color color, boolean b) {
        graphics.setColor(new Color(83, 83, 83));
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }


}