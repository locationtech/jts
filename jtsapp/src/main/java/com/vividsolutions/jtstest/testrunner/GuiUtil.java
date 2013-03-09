/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jtstest.testrunner;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import com.vividsolutions.jtstest.util.StringUtil;

/**
 * Useful GUI utilities
 *
 * @version 1.7
 */
public class GuiUtil {

    /**
     * Centers the first component on the second
     */
    public static void center(Component componentToMove, Component componentToCenterOn) {
        Dimension componentToCenterOnSize = componentToCenterOn.getSize();
        componentToMove.setLocation(
            componentToCenterOn.getX()
                + ((componentToCenterOnSize.width - componentToMove.getWidth()) / 2),
            componentToCenterOn.getY()
                + ((componentToCenterOnSize.height - componentToMove.getHeight()) / 2));
    }

    /**
     * Centers the component on the screen
     */
    public static void centerOnScreen(Component componentToMove) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        componentToMove.setLocation(
            (screenSize.width - componentToMove.getWidth()) / 2,
            (screenSize.height - componentToMove.getHeight()) / 2);
    }

    /**
     * Centers the component on its window
     */
    public static void centerOnWindow(Component componentToMove) {
        center(componentToMove, SwingUtilities.windowForComponent(componentToMove));
    }

    //Save the contents of the cell that the user is in the middle of editing
    //From Question of the Week No. 23
    //http://developer.java.sun.com/developer/qow/archive/23/
    public static void commitChanges(JTable table) {
        if (table.isEditing()) {
            String text = ((JTextComponent) table.getEditorComponent()).getText();
            table.setValueAt(text, table.getEditingRow(), table.getEditingColumn());
            table.getCellEditor().cancelCellEditing();
        }
    }

    /**
     * Workaround for bug: can't re-show internal frames. See bug parade 4138031.
     */
    public static void show(JInternalFrame internalFrame, JDesktopPane desktopPane)
        throws PropertyVetoException {
        if (!desktopPane.isAncestorOf(internalFrame))
            desktopPane.add(internalFrame);
        internalFrame.setClosed(false);
        internalFrame.setVisible(true);
        internalFrame.toFront();
    }

    /**
     * Workaround for Swing bug: JFileChooser does not support multi-file selection
     * See Sun bug database 4218431.
     * http://manning.spindoczine.com/sbe/files/uts2/Chapter14html/Chapter14.htm)
     */
    public static File[] getSelectedFiles(JFileChooser chooser) {
        // Although JFileChooser won't give us this information,
        // we need it...
        Container c1 = (Container) chooser.getComponent(3);
        JList list = null;
        while (c1 != null) {
            Container c = (Container) c1.getComponent(0);
            if (c instanceof JList) {
                list = (JList) c;
                break;
            }
            c1 = c;
        }
        Object[] entries = list.getSelectedValues();
        File[] files = new File[entries.length];
        for (int k = 0; k < entries.length; k++) {
            if (entries[k] instanceof File)
                files[k] = (File) entries[k];
        }
        return files;
    }

    /**
     * Changes the tooltip text of each component in the Container to be
     * multiline HTML. Modifies all descendants (children, grandchildren, etc.).
     */
    public static void formatTooltips(Container container) {
        for (int i = 0; i < container.getComponentCount(); i++) {
            Component component = container.getComponent(i);
            if (component instanceof JComponent)
                formatTooltip((JComponent) component);
            if (component instanceof Container)
                formatTooltips((Container) component);
        }
    }

    /**
     * Changes the tooltip text of the JComponent to be multiline HTML.
     */
    public static void formatTooltip(JComponent jcomponent) {
        String tip = jcomponent.getToolTipText();
        if (tip == null || tip.length() == 0)
            return;
        if (tip.toLowerCase().indexOf("<html>") > -1)
            return;
        tip = StringUtil.wrap(tip, 50);
        tip = StringUtil.replaceAll(tip, "\n", "<p>");
        tip = "<html>" + tip + "</html>";
        jcomponent.setToolTipText(tip);
    }

    /**
     * Runs r in the event dispatch thread, which may be the current thread.
     * Waits for r to finish before returning.
     */
    public static void invokeAndWait(Runnable r)
        throws InterruptedException, java.lang.reflect.InvocationTargetException {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeAndWait(r);
        }
    }
}
