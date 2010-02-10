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
package com.vividsolutions.jtstest.testbuilder;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.vividsolutions.jtstest.testrunner.GuiUtil;
import com.vividsolutions.jtstest.util.StringUtil;

/**
 * An indicator that the app is performing a long operation. Use instead of
 * an hourglass, as Java 1.3's #setCursor methods are buggy. Make sure
 * "images/Hourglass.gif" is on the classpath.
 *
 * @version 1.7
 */
public class BusyDialog extends JDialog {
    private static Frame owner = null;

    /**
     * Sets the Frame for which the BusyDialog is displayed.
     */
    public static void setOwner(Frame _owner) {
        owner = _owner;
    }

    public interface Executable {

        public void execute() throws Exception;
    }
    ////////////////////////////////////////////////////////////////////////////////
    private Executable executable;
    private Thread thread = null;
    private String description;
    private ImageIcon icon = new ImageIcon(this.getClass().getResource("Hourglass.gif"));
    private Exception exception = null;
    private String stackTrace = null;
    private javax.swing.Timer timer = new javax.swing.Timer(250, new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
            label.setText(description);
            if (!thread.isAlive()) {
                timer.stop();
                setVisible(false);
            }
        }
    });
    JLabel label = new JLabel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();

    /**
     * Creates a BusyDialog
     */
    public BusyDialog() {
        super(owner, "Busy", true);
        try {
            jbInit();
            pack();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private void jbInit() throws Exception {
        label.setText("Please wait . . .");
        label.setMaximumSize(new Dimension(400, 40));
        label.setMinimumSize(new Dimension(400, 40));
        label.setPreferredSize(new Dimension(400, 40));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setIcon(icon);
        this.setResizable(false);
        this.setModal(true);
        this.getContentPane().setLayout(gridBagLayout1);
        this.addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowOpened(WindowEvent e) {
                this_windowOpened(e);
            }
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.getContentPane().add(
            label,
            new GridBagConstraints(
                0,
                0,
                1,
                1,
                1.0,
                1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(4, 4, 4, 4),
                0,
                0));
    }

    /**
     * Runs the Executable and displays the BusyDialog.
     */
    public void execute(String description, Executable executable) throws Exception {
        this.executable = executable;
        this.description = description;
        exception = null;
        stackTrace = null;
        if (owner == null)
            GuiUtil.centerOnScreen(this);
        else
            GuiUtil.center(this, owner);
        setVisible(true);
        if (exception != null)
            throw exception;
    }

    void this_windowOpened(WindowEvent e) {
        label.setText(description);
        Runnable runnable = new Runnable() {

            public void run() {
                try {
                    executable.execute();
                } catch (Exception e) {
                    exception = e;
                    stackTrace = StringUtil.getStackTrace(e);
                }
            }
        };
        thread = new Thread(runnable);
        thread.start();
        timer.start();
    }

    /**
     * Sets the String displayed in the BusyDialog. Can be safely called
     * by the AWT event dispatching thread and threads other than the
     * AWT event dispatching thread.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String getStackTrace() {
        return stackTrace;
    }
}
