/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtstest.testbuilder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.controller.JTSTestBuilderController;
import org.locationtech.jtstest.testbuilder.geom.GeometryComponentDeleter;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;


public class InspectorPanel extends TestBuilderPanel  {
  
  private static final int BOX_SPACER = 5;

  GeometryTreePanel geomTreePanel;
  
  private JButton btnExpand = new JButton();
  private JButton btnDelete;

  JLabel lblGeom = new JLabel();

  private boolean showExpand = true;

  private int source;

  private Geometry geometry;
  private boolean isEditable;
  private String name;
  
  private Comparator<GeometricObjectNode> sorterArea;
  private Comparator<GeometricObjectNode> sorterLen;
  private Comparator<GeometricObjectNode> sorterNumPoints;

  public InspectorPanel() {
    this(true);
   }

  public InspectorPanel(boolean showExpand) {
    super();
    this.showExpand  = showExpand;
    uiInit();
   }

  protected void uiInit() {
    this.setLayout(new BorderLayout());
    geomTreePanel = new GeometryTreePanel();
    
    geomTreePanel.setPreferredSize(new Dimension(300, 500));
    this.add(geomTreePanel, BorderLayout.CENTER);
    
    JButton btnZoom = SwingUtil.createButton(AppIcons.ZOOM, "Zoom to component", new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionZoom(e);
      }
    });
    JButton btnCopy = SwingUtil.createButton(AppIcons.COPY, "Copy (Ctl-click to Copy formatted", new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionCopy(e);
      }
    });
    JButton btnNext = SwingUtil.createButton(AppIcons.DOWN, "Next (Ctl-click to Zoom)", new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
         actionZoomNext(e, 1);
      }
    });
    JButton btnPrev = SwingUtil.createButton(AppIcons.UP, "Previous (Ctl-click to Zoom)", new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionZoomNext(e, -1);
      }
    });
    btnDelete = SwingUtil.createButton(AppIcons.DELETE, "Delete", new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteGeom();
      }
    });    
    
    lblGeom.setFont(new java.awt.Font("Dialog", 1, 16));
    lblGeom.setText(" ");
    lblGeom.setMaximumSize(new Dimension(30, 30));
    lblGeom.setHorizontalAlignment(JLabel.CENTER);

    JPanel btnPanel = new JPanel();
    btnPanel.setPreferredSize(new java.awt.Dimension(30, 30));

    btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.PAGE_AXIS));
    btnPanel.add(lblGeom);
    btnPanel.add(Box.createRigidArea(new Dimension(0, BOX_SPACER)));
    btnPanel.add(btnZoom);
    btnPanel.add(Box.createRigidArea(new Dimension(0, BOX_SPACER)));
    btnPanel.add(btnPrev);
    btnPanel.add(btnNext);
    btnPanel.add(Box.createRigidArea(new Dimension(0, BOX_SPACER)));
    btnPanel.add(btnCopy);
    btnPanel.add(btnDelete);
    this.add(btnPanel, BorderLayout.WEST);
    
    JButton btnSortNone = SwingUtil.createButton(AppIcons.CLEAR, "Unsorted", new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sortNone();
      }
    });
    JButton btnSortByArea = SwingUtil.createButton(AppIcons.ICON_POLYGON, "Sort by Area (Asc/Desc)", new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sortByArea();
      }
    });
    JButton btnSortByLen = SwingUtil.createButton(AppIcons.ICON_LINESTRING, "Sort by Length (Asc/Desc)", new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sortByLen();
      }
    });
    JButton btnSortByNumPts = SwingUtil.createButton(AppIcons.ICON_POINT, "Sort by Num Points (Asc/Desc)", new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sortByNumPoints();
      }
    });
    
    JPanel btn2Panel = new JPanel();
    btn2Panel.setLayout(new BoxLayout(btn2Panel, BoxLayout.PAGE_AXIS));
    btn2Panel.setPreferredSize(new java.awt.Dimension(30, 30));
    btnExpand.setMaximumSize(new Dimension(30, 30));
    btnExpand.setText("...");
    btnExpand.setToolTipText("Display in window");
    btnExpand.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        btnExpand_actionPerformed();
      }
    });
    if (showExpand) {
      btnExpand.setEnabled(true);
    }
    btn2Panel.add(btnExpand);
    
    btn2Panel.add(Box.createRigidArea(new Dimension(0, 10)));
    btn2Panel.add(new JLabel("Sort"));
    btn2Panel.add(btnSortByNumPts);
    btn2Panel.add(btnSortByLen);
    btn2Panel.add(btnSortByArea);
    btn2Panel.add(btnSortNone);
    this.add(btn2Panel, BorderLayout.EAST);
  }
  
  private void btnExpand_actionPerformed() {
    if (isEditable) {
      JTSTestBuilder.controller().inspectGeometryDialogForCurrentCase();
    }
    else {
      JTSTestBuilder.controller().inspectGeometryDialog(name, geometry);      
    }
  }
  
  private void actionZoom(ActionEvent e) {
    Geometry geom = geomTreePanel.getSelectedGeometry();
    JTSTestBuilderFrame.getGeometryEditPanel().zoom(geom);
    //-- would be nice to flash, but zoom is too slow
    //JTSTestBuilder.controller().flash(geom);
  }
  private void actionZoomNext(ActionEvent e, int direction) {
    boolean isZoom = SwingUtil.isCtlKeyPressed(e);
    geomTreePanel.moveToNextNode(direction);
    Geometry geom = geomTreePanel.getSelectedGeometry();
    if (geom == null)
      return;
    if (isZoom) {
      JTSTestBuilderFrame.getGeometryEditPanel().zoom(geom);
      //-- would be nice to flash, but zoom is too slow
    }
    else {
      JTSTestBuilder.controller().flash(geom);
    }
  }
  private void actionCopy(ActionEvent e) {
    boolean isFormatted = SwingUtil.isCtlKeyPressed(e);
    Geometry geom = geomTreePanel.getSelectedGeometry();
    if (geom == null) return;
    SwingUtil.copyToClipboard(geom, isFormatted);
  }

  private void deleteGeom() {
    Geometry geomComp = geomTreePanel.getSelectedGeometry();
    if (geomComp == null) return;
    Geometry geomEdit = GeometryComponentDeleter.deleteComponent(geometry, geomComp);
    JTSTestBuilderController.model().getGeometryEditModel().setGeometry(source, geomEdit);
    updateGeometry(geomEdit);
  }

  public void setGeometry(String name, Geometry geom, int source, boolean isEditable)
  {
    this.source = source;
    this.geometry = geom;
    this.name = name;
    this.isEditable = isEditable;

    btnDelete.setEnabled(isEditable);
    lblGeom.setText(name);
    lblGeom.setToolTipText(name);
    lblGeom.setForeground(source == 0 ? AppColors.GEOM_A : AppColors.GEOM_B);
    
    sortNone();
  }

  private void updateGeometry(Geometry geom)
  {
    this.geometry = geom;
    geomTreePanel.populate(geometry, source);
  }

  public void sortNone()
  {
    sorterLen = null;
    sorterArea = null;
    sorterNumPoints = null;
    geomTreePanel.populate(geometry, source);
  }
  
  public void sortByArea()
  {
    sorterLen = null;
    sorterNumPoints = null;
    
    if (sorterArea == GeometryTreeModel.SORT_AREA_ASC) {
      sorterArea = GeometryTreeModel.SORT_AREA_DESC;
    }
    else {
      sorterArea = GeometryTreeModel.SORT_AREA_ASC;
    }
    geomTreePanel.populate(geometry, source, sorterArea);
  }
  
  public void sortByLen()
  {
    sorterArea = null;
    sorterNumPoints = null;
    
    if (sorterLen == GeometryTreeModel.SORT_LEN_ASC) {
      sorterLen = GeometryTreeModel.SORT_LEN_DESC;
    }
    else {
      sorterLen = GeometryTreeModel.SORT_LEN_ASC;
    }
    geomTreePanel.populate(geometry, source, sorterLen);
  }

  public void sortByNumPoints()
  {
    sorterArea = null;
    sorterLen = null;
    
    if (sorterNumPoints == GeometryTreeModel.SORT_NUMPTS_ASC) {
      sorterNumPoints = GeometryTreeModel.SORT_NUMPTS_DESC;
    }
    else {
      sorterNumPoints = GeometryTreeModel.SORT_NUMPTS_ASC;
    }
    geomTreePanel.populate(geometry, source, sorterNumPoints);
  }
  
}
