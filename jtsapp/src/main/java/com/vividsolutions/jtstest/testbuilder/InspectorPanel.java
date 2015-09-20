package com.vividsolutions.jtstest.testbuilder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jtstest.testbuilder.ui.SwingUtil;

public class InspectorPanel extends TestBuilderPanel  {
  
  GeometryTreePanel geomTreePanel;
  
  JButton btnZoom = new JButton();
  JButton btnCopy = new JButton();
  JButton btnNext = new JButton();
  JButton btnPrev = new JButton();

  JLabel lblGeom = new JLabel();

  public InspectorPanel() {
   super();
   uiInit();
  }

  protected void uiInit() {
    this.setLayout(new BorderLayout());
    geomTreePanel = new GeometryTreePanel();
    
    geomTreePanel.setPreferredSize(new Dimension(300, 500));
    this.add(geomTreePanel, BorderLayout.CENTER);
    
    btnZoom.setEnabled(true);
    btnZoom.setText("Zoom");
    btnZoom.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        btnZoom_actionPerformed(e);
      }

    });
    btnCopy.setEnabled(true);
    btnCopy.setText("Copy");
    btnCopy.setToolTipText("Copy (Ctl-click to copy formatted");
    btnCopy.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        btnCopy_actionPerformed(e);
      }

    });
    btnNext.setEnabled(true);
    btnNext.setText("Next");
    btnNext.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        btnZoomNext_actionPerformed(e, 1);
      }

    });
    btnPrev.setEnabled(true);
    btnPrev.setText("Prev");
    btnPrev.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        btnZoomNext_actionPerformed(e, -1);
      }
    });
    
    lblGeom.setText(" ");
    lblGeom.setHorizontalAlignment(JLabel.CENTER);

    JPanel btnPanel = new JPanel();
    btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.PAGE_AXIS));
    btnPanel.add(lblGeom);
    btnPanel.add(btnZoom);
    btnPanel.add(btnNext);
    btnPanel.add(btnPrev);
    btnPanel.add(btnCopy);
    
    this.add(btnPanel, BorderLayout.WEST);
    

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

  public void setGeometry(String tag, Geometry a)
  {
    lblGeom.setText(tag);
    geomTreePanel.populate(a);
  }
}
