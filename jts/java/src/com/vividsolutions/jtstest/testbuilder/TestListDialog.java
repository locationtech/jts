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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.vividsolutions.jtstest.test.Testable;
import com.vividsolutions.jtstest.testbuilder.model.TestCaseEdit;
import com.vividsolutions.jtstest.testbuilder.ui.SwingUtil;


/**
 * @version 1.7
 */
public class TestListDialog extends JDialog {
    private JTSTestBuilderFrame testBuilderFrame;
    private DefaultListModel listModel = new DefaultListModel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    JScrollPane jScrollPane1 = new JScrollPane();
    JList list = new JList(listModel);
    JButton closeButton = new JButton();
    JButton runAllTestsButton = new JButton();

    private class TestListCellRenderer extends JLabel implements ListCellRenderer {
        private final ImageIcon tickIcon =
            new ImageIcon(this.getClass().getResource("tickShaded.gif"));
        private final ImageIcon crossIcon =
            new ImageIcon(this.getClass().getResource("crossShaded.gif"));
        private final ImageIcon clearIcon = new ImageIcon(this.getClass().getResource("clear.gif"));

        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
            Testable testCase = (Testable) value;
            String name = testCase.getName();
            if ((name == null || name.length() == 0) && testCase instanceof TestCaseEdit) {
                name = ((TestCaseEdit) testCase).getDescription();
            }
            if (name == null || name.length() == 0) {
                name = "Test";
            }
            setText(name);
            setIcon(testCase.isPassed() ? tickIcon : (testCase.isFailed() ? crossIcon : clearIcon));
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            return this;
        }
    }

    public TestListDialog() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TestListDialog(JTSTestBuilderFrame testBuilderFrame) {
        super(testBuilderFrame, "Tests", false);
        this.testBuilderFrame = testBuilderFrame;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        list.setCellRenderer(new TestListCellRenderer());
        registerListSelectionListener();
        populateList();
    }

    public void setVisible(boolean isVisible) {
        if (isVisible)
            populateList();
        super.setVisible(isVisible);
    }

    private void registerListSelectionListener() {
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                testBuilderFrame.setCurrentTestCase((TestCaseEdit) list.getSelectedValue());
            }
        });
    }

    public void populateList() {
        listModel.clear();
        for (Iterator i = testBuilderFrame.getModel().getTestCases().iterator(); i.hasNext();) {
            Testable testCase = (Testable) i.next();
            listModel.addElement(testCase);
        }
    }

    private void jbInit() throws Exception {
        setSize(200, 250);
        this.getContentPane().setLayout(borderLayout1);
        jPanel1.setLayout(flowLayout1);
        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                closeButton_actionPerformed(e);
            }
        });
        runAllTestsButton.setText("Run All Tests");
        runAllTestsButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                runAllTestsButton_actionPerformed(e);
            }
        });
        list.setBackground(Color.lightGray);
        list.setBorder(BorderFactory.createLoweredBevelBorder());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.getContentPane().add(jPanel1, BorderLayout.SOUTH);
        jPanel1.add(runAllTestsButton, null);
        jPanel1.add(closeButton, null);
        this.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(list, null);
    }

    void closeButton_actionPerformed(ActionEvent e) {
        setVisible(false);
    }

    void runAllTestsButton_actionPerformed(ActionEvent e) {
        try {
            for (Iterator i = testBuilderFrame.getModel().getTestCases().iterator(); i.hasNext();) {
                Testable testCase = (Testable) i.next();
                if (testCase.getWellKnownText(0) != null && testCase.getWellKnownText(1) != null) {
                    testCase.runTest();
                }
            }
            testBuilderFrame.testCasePanel.relatePanel.runTests();
            list.repaint();
        } catch (Exception x) {
            SwingUtil.reportException(this, x);
        }
    }
}
