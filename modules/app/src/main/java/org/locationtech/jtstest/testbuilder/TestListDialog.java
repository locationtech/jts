/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testbuilder;

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

import org.locationtech.jtstest.test.Testable;
import org.locationtech.jtstest.testbuilder.model.TestCaseEdit;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;



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
