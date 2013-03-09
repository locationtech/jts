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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.7
 */
public class GeometryInputDialog extends JDialog {
    Geometry[] geom = new Geometry[2];
    boolean parseError;
    //=================================================
    JPanel panel1 = new JPanel();
    JLabel jLabel1 = new JLabel();
    JPanel jPanel1 = new JPanel();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel2 = new JPanel();
    JButton btnLoad = new JButton();
    JButton btnCancel = new JButton();
    JLabel jLabel2 = new JLabel();
    JLabel lblError = new JLabel();
    JTextArea txtError = new JTextArea();
    Border border1;
    JScrollPane jScrollPane1 = new JScrollPane();
    JTextArea txtA = new JTextArea();
    JScrollPane jScrollPane2 = new JScrollPane();
    JTextArea txtB = new JTextArea();

    public GeometryInputDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            jbInit();
            pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public GeometryInputDialog() {
        this(null, "", false);
    }

    void jbInit() throws Exception {
        border1 = BorderFactory.createLineBorder(Color.gray, 2);
        panel1.setLayout(borderLayout1);
        jLabel1.setFont(new java.awt.Font("Dialog", 1, 12));
        jLabel1.setForeground(Color.blue);
        jLabel1.setToolTipText("");
        jLabel1.setText("A");
        jPanel1.setLayout(gridBagLayout2);
        btnLoad.setToolTipText("");
        btnLoad.setText("Load");
        btnLoad.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                btnLoad_actionPerformed(e);
            }
        });
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                btnCancel_actionPerformed(e);
            }
        });
        jLabel2.setFont(new java.awt.Font("Dialog", 1, 12));
        jLabel2.setForeground(Color.red);
        jLabel2.setText("B");
        lblError.setToolTipText("");
        txtError.setLineWrap(true);
        txtError.setBorder(BorderFactory.createEtchedBorder());
        txtError.setToolTipText("");
        txtError.setBackground(Color.lightGray);
        panel1.setPreferredSize(new java.awt.Dimension(300, 300));
        txtA.setLineWrap(true);
        txtB.setLineWrap(true);
        jScrollPane1.setBorder(BorderFactory.createLoweredBevelBorder());
        jScrollPane2.setBorder(BorderFactory.createLoweredBevelBorder());
        getContentPane().add(panel1);
        panel1.add(jPanel1, BorderLayout.CENTER);
        jPanel1.add(
            jLabel1,
            new GridBagConstraints(
                0,
                0,
                1,
                1,
                0.1,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5),
                0,
                0));
        jPanel1.add(
            jLabel2,
            new GridBagConstraints(
                0,
                1,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5),
                0,
                0));
        jPanel1.add(
            lblError,
            new GridBagConstraints(
                1,
                2,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
                0,
                0));
        jPanel1.add(
            txtError,
            new GridBagConstraints(
                1,
                3,
                1,
                1,
                0.0,
                0.2,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
                0,
                0));
        jPanel1.add(
            jScrollPane1,
            new GridBagConstraints(
                1,
                0,
                1,
                1,
                1.0,
                0.3,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
                0,
                0));
        jPanel1.add(
            jScrollPane2,
            new GridBagConstraints(
                1,
                1,
                1,
                1,
                1.0,
                0.3,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
                0,
                0));
        jScrollPane2.getViewport().add(txtB, null);
        jScrollPane1.getViewport().add(txtA, null);
        panel1.add(jPanel2, BorderLayout.SOUTH);
        jPanel2.add(btnLoad, null);
        jPanel2.add(btnCancel, null);
    }

    void btnCancel_actionPerformed(ActionEvent e) {
        setVisible(false);
    }

    void btnLoad_actionPerformed(ActionEvent e) {
        parseError = false;
        geom[0] = parseGeometry(txtA, Color.blue);
        if (!parseError)
            geom[1] = parseGeometry(txtB, Color.red);
        if (!parseError)
            setVisible(false);
    }

    Geometry parseGeometry(JTextComponent txt, Color clr) {
        try {
            WKTReader rdr =
                new WKTReader(
                    new GeometryFactory(JTSTestBuilder.model().getPrecisionModel(), 0));
            Geometry g = rdr.read(txt.getText());
            txtError.setText("");
            return g;
        } catch (Exception ex) {
            txtError.setText(ex.getMessage());
            txtError.setForeground(clr);
            parseError = true;
            // TODO: display this exception
        }
        return null;
    }

    Geometry getGeometry(int index) {
        return geom[index];
    }
}
