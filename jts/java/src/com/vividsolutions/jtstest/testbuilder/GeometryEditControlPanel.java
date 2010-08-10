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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.vividsolutions.jtstest.testbuilder.model.*;



/**
 * @version 1.7
 */
public class GeometryEditControlPanel extends JPanel 
{
  private TestBuilderModel model;
  private GeometryEditModel geomModel;

  
    //---------------------------------------------
    BorderLayout borderLayout1 = new BorderLayout();
    ButtonGroup geometryType = new ButtonGroup();
    JPanel jPanel3 = new JPanel();
    JRadioButton rbNoEdit = new JRadioButton();
    JRadioButton rbA = new JRadioButton();
    JRadioButton rbB = new JRadioButton();
    JPanel jPanel1 = new JPanel();
    TitledBorder titledBorder2;
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    ButtonGroup editMode = new ButtonGroup();
    JPanel jPanel4 = new JPanel();
    JLabel gridLabel = new JLabel();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    Border border3;
    GridBagLayout gridBagLayout4 = new GridBagLayout();
    JTextField txtGridSize = new JTextField();
    GridLayout gridLayout1 = new GridLayout();
    JButton btnEraseGeom = new JButton();
    JPanel jPanel7 = new JPanel();
    GridLayout gridLayout2 = new GridLayout();
    JButton setGridSizeButton = new JButton();
    private GridBagLayout gridBagLayout5 = new GridBagLayout();
    private JPanel jPanel8 = new JPanel();
    JButton btnSetPrecisionModel = new JButton();
    
    JPanel jPanel9 = new JPanel();
    JCheckBox cbRevealTopo = new JCheckBox();
    
    JPanel jPanel10 = new JPanel();
    JSpinner stretchDist = new JSpinner(new SpinnerNumberModel(5, 0, 99999, 1));
    
    public GeometryEditControlPanel() {
        //enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setModel(TestBuilderModel model) {
      this.model = model;
      geomModel = model.getGeometryEditModel();
      
      geomModel
      .addGeometryListener(new com.vividsolutions.jtstest.testbuilder.model.GeometryListener() {

      public void geometryChanged(GeometryEvent e) {
          editPanel_geometryChanged(e);
      }
  });

    }


    /**Component initialization*/
    private void jbInit() throws Exception {
        titledBorder2 =
            new TitledBorder(BorderFactory.createLineBorder(Color.gray, 1), "Edit Mode");
        this.setLayout(borderLayout1);
        this.setSize(new Dimension(194, 464));
        rbNoEdit.setMargin(new Insets(0, 0, 0, 0));
        rbNoEdit.setPreferredSize(new Dimension(61, 16));
        rbNoEdit.setText("No Edit");
        rbNoEdit.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                rbNoEdit_actionPerformed(e);
            }
        });
        rbNoEdit.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                rbNoEdit_actionPerformed(e);
            }
        });
        rbB.setForeground(Color.red);
        rbB.setMargin(new Insets(0, 0, 0, 0));
        rbB.setPreferredSize(new Dimension(66, 16));
        rbB.setText("Edit B");
        rbB.setFont(new java.awt.Font("Dialog", 1, 14));
        rbB.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                rbB_actionPerformed(e);
            }
        });
        jPanel1.setLayout(gridBagLayout1);
        rbA.setMargin(new Insets(0, 0, 0, 0));
        rbA.setPreferredSize(new Dimension(66, 16));
        rbA.setText("Edit A");
        rbA.setForeground(Color.blue);
        rbA.setFont(new java.awt.Font("Dialog", 1, 14));
        rbA.setSelected(true);
        rbA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rbA_actionPerformed(e);
            }
        });

        jPanel3.setLayout(gridBagLayout5);
        jPanel1.setBorder(titledBorder2);

        btnEraseGeom.setPreferredSize(new Dimension(43, 20));
        btnEraseGeom.setMargin(new Insets(2, 2, 2, 2));
        btnEraseGeom.setText("Erase");
        btnEraseGeom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnEraseGeom_actionPerformed(e);
            }
        });
        jPanel7.setLayout(gridLayout2);
        setGridSizeButton.setMaximumSize(new Dimension(29, 21));
        setGridSizeButton.setMinimumSize(new Dimension(29, 21));
        setGridSizeButton.setPreferredSize(new Dimension(29, 21));
        setGridSizeButton.setMargin(new Insets(2, 2, 2, 2));
        setGridSizeButton.setMnemonic('0');
        setGridSizeButton.setText("Set");
        setGridSizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setGridSizeButton_actionPerformed(e);
            }
        });
        
//        btnSetPrecisionModel.setMaximumSize(new Dimension(30, 27));
        btnSetPrecisionModel.setMinimumSize(new Dimension(120, 27));
        btnSetPrecisionModel.setPreferredSize(new Dimension(120, 21));
        btnSetPrecisionModel.setToolTipText("Set the Precision Model used by all Test Cases");
        btnSetPrecisionModel.setMargin(new Insets(2, 2, 2, 2));
        btnSetPrecisionModel.setMnemonic('0');
        btnSetPrecisionModel.setText("Set Precision Model...");

        cbRevealTopo.setText("Magnify Topology");
        cbRevealTopo.setToolTipText("Stretches portions of geometries to reveal fine topological detail");
        
        this.add(jPanel3, BorderLayout.CENTER);
        jPanel3.add(
            jPanel1,
            new GridBagConstraints(
                0,
                0,
                1,
                1,
                1.0,
                0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
                0,
                0));
        
        jPanel1.add(
            rbNoEdit,
            new GridBagConstraints(
                0,
                0,
                1,
                1,
                1.0,
                0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 5, 0, 0),
                0,
                0));
        jPanel1.add(
            rbA,
            new GridBagConstraints(
                0,
                1,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 5, 0, 0),
                0,
                0));
        jPanel1.add(
            rbB,
            new GridBagConstraints(
                0,
                2,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 5, 0, 0),
                0,
                0));
        jPanel1.add(
            btnEraseGeom,
            new GridBagConstraints(
                1,
                1,
                1,
                2,
                0.0,
                0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0),
                0,
                0));
        
        
        gridLabel.setText("Grid Spacing");
        txtGridSize.setText("20");
        txtGridSize.setHorizontalAlignment(SwingConstants.RIGHT);

        jPanel4.setLayout(new GridLayout(1, 3));
        jPanel4.add(gridLabel);
        jPanel4.add(txtGridSize);
        jPanel4.add(setGridSizeButton);
        
//      jPanel4.setLayout(gridBagLayout2);
        /*
        jPanel4.add(
            gridLabel,
            new GridBagConstraints(
                0,
                0,
                1,
                1,
                0.2,
                0.0,
                GridBagConstraints.EAST,
                GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2),
                0,
                0));
        jPanel4.add(
            txtGridSize,
            new GridBagConstraints(
                2,
                0,
                1,
                1,
                0.5,
                0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2),
                0,
                0));
        jPanel4.add(
            setGridSizeButton,
            new GridBagConstraints(
                10,
                0,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2),
                0,
                0));
                */
        
        jPanel8.setLayout(new FlowLayout());
        jPanel8.add(btnSetPrecisionModel);
        
        jPanel9.setLayout(new FlowLayout());
        jPanel9.add(cbRevealTopo);
      
        jPanel10.setLayout(new GridLayout(2, 2, 10, 2));
        jPanel10.add(cbRevealTopo);
        jPanel10.add(new JLabel());
        jPanel10.add(new JLabel("Stretch Distance"));
        jPanel10.add(stretchDist);
      
        jPanel3.add(
            jPanel4,
            new GridBagConstraints(
                0,
                4,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
                0,
                0));
        jPanel3.add(
            jPanel8,
            new GridBagConstraints(
                0,
                5,
                1,
                1,
                0.0,
                0.2,
                GridBagConstraints.CENTER,
                GridBagConstraints.CENTER,
                new Insets(0, 0, 0, 0),
                0,
                0));
        jPanel3.add(
            jPanel10,
            new GridBagConstraints(
                0,
                6,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.VERTICAL,
                new Insets(0, 0, 0, 0),
                0,
                0));
        jPanel3.add(
            jPanel9,
            new GridBagConstraints(
                0,
                7,
                1,
                1,
                0.0,
                1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.VERTICAL,
                new Insets(0, 0, 0, 0),
                0,
                0));
//        this.add(jPanel7, BorderLayout.SOUTH);
//        jPanel7.add(lblAction, null);
        editMode.add(rbNoEdit);
        editMode.add(rbA);
        editMode.add(rbB);
    }

    void editPanel_geometryChanged(GeometryEvent e) {
        updatePanel(false);
    }

    void rbA_actionPerformed(ActionEvent e) {
        setEditMode(0);
    }

    void rbB_actionPerformed(ActionEvent e) {
        setEditMode(1);
    }

    void setEditMode(int geomIndex) {
        geomModel.setReadOnly(false);
        geomModel.setEditGeomIndex(geomIndex);
//        setGeometryTypeState();
    }

    void rbNoEdit_actionPerformed(ActionEvent e) {
      geomModel.setReadOnly(true);
    }

    void cbStretchTopo_actionPerformed(ActionEvent e) {
      
    }

    private void updatePanel(boolean partClosed) 
    {
    }

    void rbGeomLineString_actionPerformed(ActionEvent e) {
      geomModel.setGeometryType(GeometryType.LINESTRING);
    }

    void rbGeomPolygon_actionPerformed(ActionEvent e) {
      geomModel.setGeometryType(GeometryType.POLYGON);
    }

    void rbGeomPoint_actionPerformed(ActionEvent e) {
      geomModel.setGeometryType(GeometryType.POINT);
    }

    void btnEraseGeom_actionPerformed(ActionEvent e) {
        geomModel.clear();
//        setGeometryTypeState();
    }

    void setGridSizeButton_actionPerformed(ActionEvent e) {}
    
    public double getStretchSize()
    {
      Integer size = (Integer) stretchDist.getValue();
      return size.intValue();
    }
}
