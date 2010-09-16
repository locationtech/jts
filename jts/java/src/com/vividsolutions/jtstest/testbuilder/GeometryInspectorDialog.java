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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jtstest.testbuilder.model.TestCaseEdit;


/**
 * @version 1.7
 */
public class GeometryInspectorDialog extends JDialog {
    private TestCaseEdit test;
    //----------------------------------
    JPanel dialogPanel = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    GeometryTreePanel geomTree = new GeometryTreePanel();
    JPanel jPanel1 = new JPanel();
    JPanel cmdButtonPanel = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    JButton btnCopy = new JButton();
    JButton btnOk = new JButton();
    JPanel textFormatPanel = new JPanel();
    JPanel functionsPanel = new JPanel();
    BoxLayout boxLayout1 = new BoxLayout(functionsPanel, BoxLayout.Y_AXIS);

    public GeometryInspectorDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            jbInit();
            pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public GeometryInspectorDialog() {
        this(null, "", false);
    }

    void jbInit() throws Exception {
        jPanel1.setLayout(borderLayout2);
        
        btnCopy.setEnabled(true);
        btnCopy.setText("Copy");
        btnOk.setToolTipText("");
        btnOk.setText("Close");
        btnOk.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                btnOk_actionPerformed(e);
            }
        });
        cmdButtonPanel.add(btnCopy, null);
        cmdButtonPanel.add(btnOk, null);
        jPanel1.add(cmdButtonPanel, BorderLayout.SOUTH);
        
        dialogPanel.setLayout(borderLayout1);
        geomTree.setPreferredSize(new Dimension(500, 300));

        dialogPanel.add(geomTree, BorderLayout.CENTER);
        dialogPanel.add(jPanel1, BorderLayout.SOUTH);
        getContentPane().add(dialogPanel);
    }

    public void setGeometry(Geometry g) {
    	geomTree.populate(g);
    }

    void btnOk_actionPerformed(ActionEvent e) {
        setVisible(false);
    }


}
