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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.locationtech.jtstest.test.TestCaseList;


/**
 * MD - Probably obsolete
 * 
 * @author
 * @version 1.7
 */
public class LoadTestCasesDialog extends JDialog {
    TestCaseList testCaseList; // if non-null, the instance created by this dialog
    //==============================
    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JButton btnCancel = new JButton();
    JButton btnOk = new JButton();
    JPanel jPanel2 = new JPanel();
    JTextField txtClassname = new JTextField();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    JLabel jLabel3 = new JLabel();

    public LoadTestCasesDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            jbInit();
            pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public LoadTestCasesDialog() {
        this(null, "", false);
    }

    void jbInit() throws Exception {
        panel1.setLayout(borderLayout1);
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                btnCancel_actionPerformed(e);
            }
        });
        btnOk.setText("Ok");
        btnOk.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                btnOk_actionPerformed(e);
            }
        });
        jPanel2.setLayout(gridBagLayout1);
        jLabel1.setToolTipText("");
        jLabel1.setText("Class name");
        jPanel2.setPreferredSize(new Dimension(300, 21));
        panel1.setPreferredSize(new Dimension(300, 200));
        jLabel2.setFont(new java.awt.Font("Dialog", 2, 10));
        jLabel2.setToolTipText("");
        jLabel2.setText(
            "Enter the fully-qualified classname of a class that extends TestCaseList.");
        jLabel3.setFont(new java.awt.Font("Dialog", 2, 10));
        jLabel3.setText("(E.g. \"com.vividsolutions.jtstest.testsuite.TestRelateAA\")");
        txtClassname.setText("com.vividsolutions.jtstest.testsuite.TestRelatePP");
        getContentPane().add(panel1);
        panel1.add(jPanel1, BorderLayout.SOUTH);
        jPanel1.add(btnOk, null);
        jPanel1.add(btnCancel, null);
        panel1.add(jPanel2, BorderLayout.CENTER);
        jPanel2.add(
            txtClassname,
            new GridBagConstraints(
                1,
                0,
                1,
                1,
                1.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5),
                0,
                0));
        jPanel2.add(
            jLabel1,
            new GridBagConstraints(
                0,
                0,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5),
                0,
                0));
        jPanel2.add(
            jLabel2,
            new GridBagConstraints(
                1,
                1,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(0, 5, 0, 0),
                0,
                0));
        jPanel2.add(
            jLabel3,
            new GridBagConstraints(
                1,
                2,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(0, 5, 0, 0),
                0,
                0));
    }

    void btnOk_actionPerformed(ActionEvent e) {
        testCaseList = null;
        String errMsg = null;
        try {
            Class cls = Class.forName(txtClassname.getText());
            testCaseList = (TestCaseList) cls.newInstance();
        } catch (ClassNotFoundException ex) {
            errMsg = "This class cannot be found.  Check that it is on your CLASSPATH";
            // TODO: alert user to this error
        } catch (IllegalAccessException ex) {
            errMsg = "This class cannot be loaded.  Check that it has public access";
        } catch (Exception ex) {
            errMsg = ex.getMessage();
        }
        if (errMsg != null) {
            JOptionPane.showMessageDialog(
                this,
                errMsg,
                "Class Load Error",
                JOptionPane.ERROR_MESSAGE);
        }
        // TODO: check that class is a subclass of TestClassList
        setVisible(false);
    }

    public TestCaseList getList() {
        return testCaseList;
    }

    void btnCancel_actionPerformed(ActionEvent e) {
        setVisible(false);
    }
}
