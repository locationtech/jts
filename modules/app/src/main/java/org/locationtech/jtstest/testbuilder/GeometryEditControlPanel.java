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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
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

import org.locationtech.jtstest.testbuilder.model.*;




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
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    Border border3;
    GridBagLayout gridBagLayout4 = new GridBagLayout();
    GridLayout gridLayout1 = new GridLayout();
    JButton btnEraseGeom = new JButton();
    JPanel jPanel7 = new JPanel();
    GridLayout gridLayout2 = new GridLayout();
    private GridBagLayout gridBagLayout5 = new GridBagLayout();
    private JPanel jPanelPM = new JPanel();
    JButton btnSetPrecisionModel = new JButton();
    //LayerControlList layerList = new LayerControlList();
    
    JPanel jPanel9 = new JPanel();
    JCheckBox cbMagnifyTopo = new JCheckBox();
    
    JPanel jPanelMagnify = new JPanel();
    JSpinner stretchDist = new JSpinner(new SpinnerNumberModel(5, 0, 99999, 1));
    
    public GeometryEditControlPanel() {
        //enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            uiInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setModel(TestBuilderModel model) {
      this.model = model;
      geomModel = model.getGeometryEditModel();
      geomModel
      .addGeometryListener(new org.locationtech.jtstest.testbuilder.model.GeometryListener() {
      public void geometryChanged(GeometryEvent e) {
          editPanel_geometryChanged(e);
      }
  });

    }

    /**Component initialization*/
    private void uiInit() throws Exception {
        titledBorder2 =
            new TitledBorder(BorderFactory.createLineBorder(Color.gray, 1), "Edit Mode");
        this.setLayout(borderLayout1);
        //this.setSize(new Dimension(194, 300));
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

        //jPanel1.setBorder(titledBorder2);

        btnEraseGeom.setPreferredSize(new Dimension(43, 20));
        btnEraseGeom.setMargin(new Insets(2, 2, 2, 2));
        btnEraseGeom.setText("Erase");
        btnEraseGeom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnEraseGeom_actionPerformed(e);
            }
        });
        jPanel7.setLayout(gridLayout2);
        
//        btnSetPrecisionModel.setMaximumSize(new Dimension(30, 27));
        btnSetPrecisionModel.setMinimumSize(new Dimension(120, 27));
        btnSetPrecisionModel.setPreferredSize(new Dimension(120, 21));
        btnSetPrecisionModel.setToolTipText("Set the Precision Model used by all Test Cases");
        btnSetPrecisionModel.setMargin(new Insets(2, 2, 2, 2));
        btnSetPrecisionModel.setMnemonic('0');
        btnSetPrecisionModel.setText("Precision Model...");

        // put box ahead of text
        //cbMagnifyTopo.setHorizontalTextPosition(AbstractButton.LEADING);
        cbMagnifyTopo.setText("Magnify Topology");
        cbMagnifyTopo.setToolTipText("Stretches portions of geometries to reveal fine topological detail");
        stretchDist.setToolTipText("Stretch Distance");
        
        /*
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
        */
        
        
        jPanelPM.setLayout(new FlowLayout());
        jPanelPM.add(btnSetPrecisionModel);
        
        //jPanel9.setLayout(new FlowLayout());
       // jPanel9.add(cbMagnifyTopo);
      
        jPanelMagnify.setLayout(new FlowLayout());
        jPanelMagnify.add(cbMagnifyTopo);
        jPanelMagnify.add(stretchDist);
        //jPanel10.add(new JLabel());
        //jPanel10.add(new JLabel("Stretch Distance"));
      
        //jPanel3.setLayout(gridBagLayout5);
        jPanel3.setLayout(new GridLayout(2, 1, 10, 2));
        jPanel3.add(jPanelPM);
        //jPanel3.add(jPanelMagnify);
        
        
        //this.add(jPanel1, BorderLayout.NORTH);
        this.add(jPanel3, BorderLayout.SOUTH);
        //this.add(layerList, BorderLayout.CENTER);
       
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
    
    /*
    public double getStretchSize()
    {
      Integer size = (Integer) stretchDist.getValue();
      return size.intValue();
    }
    */
}
