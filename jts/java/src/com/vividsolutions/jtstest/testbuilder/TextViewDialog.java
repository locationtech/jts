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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.vividsolutions.jtstest.test.Testable;
import com.vividsolutions.jtstest.testbuilder.model.JavaTestWriter;
import com.vividsolutions.jtstest.testbuilder.model.XMLTestWriter;


/**
 * @version 1.7
 */
public class TextViewDialog extends JDialog {
    Testable test;
    //----------------------------------
    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JScrollPane jScrollPane1 = new JScrollPane();
    JTextArea txtGeomView = new JTextArea();
    JPanel jPanel1 = new JPanel();
    JPanel jPanel2 = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JButton btnSelect = new JButton();
    JButton btnOk = new JButton();
    ButtonGroup textType = new ButtonGroup();

    public TextViewDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            jbInit();
            pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public TextViewDialog() {
        this(null, "", false);
    }

    void jbInit() throws Exception {
        panel1.setLayout(borderLayout1);
        jScrollPane1.setPreferredSize(new Dimension(500, 300));
        txtGeomView.setLineWrap(true);
        jPanel1.setLayout(borderLayout2);
        btnSelect.setEnabled(false);
        btnSelect.setText("Select");
        btnSelect.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                btnSelect_actionPerformed(e);
            }
        });
        btnOk.setToolTipText("");
        btnOk.setText("Close");
        btnOk.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                btnOk_actionPerformed(e);
            }
        });
        getContentPane().add(panel1);
        panel1.add(jScrollPane1, BorderLayout.CENTER);
        panel1.add(jPanel1, BorderLayout.SOUTH);
        jPanel1.add(jPanel2, BorderLayout.SOUTH);
        jPanel2.add(btnSelect, null);
        jPanel2.add(btnOk, null);
        jScrollPane1.getViewport().add(txtGeomView, null);
    }

    public void setText(String txt) {
        txtGeomView.setText(txt);
    }

    void btnOk_actionPerformed(ActionEvent e) {
        setVisible(false);
    }

    void btnSelect_actionPerformed(ActionEvent e) {
        txtGeomView.selectAll();
    }

    void rbTestCaseJava_actionPerformed(ActionEvent e) {
        txtGeomView.setText((new JavaTestWriter()).write(test));
    }

    void rbXML_actionPerformed(ActionEvent e) {
        txtGeomView.setText((new XMLTestWriter()).getTestXML(test));
    }
}
