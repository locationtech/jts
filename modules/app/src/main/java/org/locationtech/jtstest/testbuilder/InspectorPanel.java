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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.controller.JTSTestBuilderController;
import org.locationtech.jtstest.testbuilder.geom.GeometryComponentDeleter;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;


public class InspectorPanel extends TestBuilderPanel  {
  
  private static final int BOX_SPACER = 5;

  private static final ImageIcon downIcon = AppIcons.DOWN;
  private static final ImageIcon upIcon = AppIcons.UP;
  private static final ImageIcon zoomIcon = AppIcons.ZOOM;
  private static final ImageIcon copyIcon = AppIcons.COPY;

  GeometryTreePanel geomTreePanel;
  
  JButton btnZoom = new JButton();
  JButton btnCopy = new JButton();
  JButton btnNext = new JButton();
  JButton btnPrev = new JButton();
  JButton btnExpand = new JButton();

  JLabel lblGeom = new JLabel();

  private boolean showExpand = true;

  private int source;

  private Geometry geometry;

  private Comparator sorterArea;

  private Comparator sorterLen;

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
        btnZoom_actionPerformed(e);
      }
    });
    JButton btnCopy = SwingUtil.createButton(AppIcons.COPY, "Copy (Ctl-click to copy formatted", new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnCopy_actionPerformed(e);
      }
    });
    JButton btnNext = SwingUtil.createButton(AppIcons.DOWN, "Zoom to Next", new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnZoomNext_actionPerformed(e, 1);
      }
    });
    JButton btnPrev = SwingUtil.createButton(AppIcons.UP, "Zoom to Previous", new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnZoomNext_actionPerformed(e, -1);
      }
    });
    JButton btnDelete = SwingUtil.createButton(AppIcons.DELETE, "Delete", new java.awt.event.ActionListener() {
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
    
    if (showExpand) {
      JPanel btn2Panel = new JPanel();
      btn2Panel.setLayout(new BoxLayout(btn2Panel, BoxLayout.PAGE_AXIS));
      btn2Panel.setPreferredSize(new java.awt.Dimension(30, 30));
      btnExpand.setEnabled(true);
      btnExpand.setMaximumSize(new Dimension(30, 30));
      btnExpand.setText("...");
      btnExpand.setToolTipText("Display in window");
      btnExpand.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e)
        {
          btnExpand_actionPerformed();
        }
      });
      btn2Panel.add(btnExpand);
      this.add(btn2Panel, BorderLayout.EAST);
    }
    
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
    
    JPanel sortPanel = new JPanel();
    sortPanel.setLayout(new BoxLayout(sortPanel, BoxLayout.LINE_AXIS));
    sortPanel.add(Box.createRigidArea(new Dimension(160, 0)));
    sortPanel.add(new JLabel("Sort"));
    sortPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    sortPanel.add(btnSortNone);
    //sortPanel.add(new JLabel(AppIcons.ICON_LINESTRING));
    sortPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    sortPanel.add(btnSortByLen);
    //sortPanel.add(new JLabel(AppIcons.ICON_POLYGON));
    sortPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    sortPanel.add(btnSortByArea);
    this.add(sortPanel, BorderLayout.NORTH);

  }
  private void btnExpand_actionPerformed() {
    JTSTestBuilder.controller().inspectGeometryDialogForCurrentCase();
  }
  private void btnZoom_actionPerformed(ActionEvent e) {
    JTSTestBuilderFrame.getGeometryEditPanel().zoom(geomTreePanel.getSelectedGeometry());
  }
  private void btnZoomNext_actionPerformed(ActionEvent e, int direction) {
    geomTreePanel.moveToNextNode(direction);
    JTSTestBuilderFrame.getGeometryEditPanel().zoom(geomTreePanel.getSelectedGeometry());
  }
  private void btnCopy_actionPerformed(ActionEvent e) {
    boolean isFormatted = 0 != (e.getModifiers() & ActionEvent.CTRL_MASK);
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

  public void setGeometry(String tag, Geometry geom, int source)
  {
    this.source = source;
    this.geometry = geom;
    
    lblGeom.setText(tag);
    lblGeom.setForeground(source == 0 ? Color.BLUE : Color.RED);
    
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
    geomTreePanel.populate(geometry, source);
  }
  
  public void sortByArea()
  {
    sorterLen = null;
    
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
    if (sorterLen == GeometryTreeModel.SORT_LEN_ASC) {
      sorterLen = GeometryTreeModel.SORT_LEN_DESC;
    }
    else {
      sorterLen = GeometryTreeModel.SORT_LEN_ASC;
    }
    geomTreePanel.populate(geometry, source, sorterLen);
  }
  
}
