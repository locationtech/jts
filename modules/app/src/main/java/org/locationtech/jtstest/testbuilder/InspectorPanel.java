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
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.controller.JTSTestBuilderController;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;


public class InspectorPanel extends TestBuilderPanel  {
  
  private static final int BOX_SPACER = 5;

  private final ImageIcon downIcon = IconLoader.icon("Down.gif");
  private final ImageIcon upIcon = IconLoader.icon("Up.gif");
  private final ImageIcon zoomIcon = IconLoader.icon("MagnifyCursor.gif");
  private final ImageIcon copyIcon = IconLoader.icon("Copy.gif");

  GeometryTreePanel geomTreePanel;
  
  JButton btnZoom = new JButton();
  JButton btnCopy = new JButton();
  JButton btnNext = new JButton();
  JButton btnPrev = new JButton();
  JButton btnExpand = new JButton();

  JLabel lblGeom = new JLabel();

  private boolean showExpand = true;

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
    
    btnZoom.setEnabled(true);
    btnZoom.setMaximumSize(new Dimension(30, 26));
    //btnZoom.setText("Zoom");
    btnZoom.setIcon(zoomIcon);
    btnZoom.setToolTipText("Zoom to component");
    btnZoom.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnZoom_actionPerformed(e);
      }
    });
    btnCopy.setEnabled(true);
    btnCopy.setMaximumSize(new Dimension(30, 30));
    //btnCopy.setText("Copy");
    btnCopy.setIcon(copyIcon);
    btnCopy.setToolTipText("Copy (Ctl-click to copy formatted");
    btnCopy.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnCopy_actionPerformed(e);
      }
    });
    btnNext.setEnabled(true);
    btnNext.setMaximumSize(new Dimension(30, 30));
    //btnNext.setText("Next");
    btnNext.setIcon(downIcon);
    btnNext.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnZoomNext_actionPerformed(e, 1);
      }
    });
    btnPrev.setEnabled(true);
    btnPrev.setMaximumSize(new Dimension(30, 30));
    //btnPrev.setText("Prev");
    btnPrev.setIcon(upIcon);
    btnPrev.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnZoomNext_actionPerformed(e, -1);
      }
    });
    
    lblGeom.setFont(new java.awt.Font("Dialog", 1, 16));
    lblGeom.setText(" ");
    lblGeom.setMaximumSize(new Dimension(30, 30));
    lblGeom.setHorizontalAlignment(JLabel.CENTER);

    JPanel btnPanel = new JPanel();
    btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.PAGE_AXIS));
    btnPanel.add(lblGeom);
    btnPanel.add(Box.createRigidArea(new Dimension(0, BOX_SPACER)));
    btnPanel.add(btnZoom);
    btnPanel.add(Box.createRigidArea(new Dimension(0, BOX_SPACER)));
    btnPanel.add(btnPrev);
    btnPanel.add(btnNext);
    btnPanel.add(Box.createRigidArea(new Dimension(0, BOX_SPACER)));
    btnPanel.add(btnCopy);
    this.add(btnPanel, BorderLayout.WEST);
    
    if (showExpand) {
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
      JPanel btn2Panel = new JPanel();
      btn2Panel.setLayout(new BoxLayout(btn2Panel, BoxLayout.PAGE_AXIS));
      btn2Panel.add(btnExpand);
      this.add(btn2Panel, BorderLayout.EAST);
    }

  }
  private void btnExpand_actionPerformed() {
    JTSTestBuilderController.inspectGeometryDialog();
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

  public void setGeometry(String tag, Geometry a, int source)
  {
    lblGeom.setText(tag);
    lblGeom.setForeground(source == 0 ? Color.BLUE : Color.RED);
    geomTreePanel.populate(a, source);
  }
}
