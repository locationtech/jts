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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.locationtech.jtstest.testbuilder.controller.JTSTestBuilderController;

public class JTSTestBuilderToolBar {

  JTSTestBuilderFrame tbFrame;

  JToolBar toolbar = new JToolBar();
  ButtonGroup toolButtonGroup = new ButtonGroup();
  
  JToggleButton drawRectangleButton;
  JToggleButton drawPolygonButton;
  JToggleButton drawLineStringButton;
  JToggleButton drawPointButton;
  JToggleButton zoomButton;
  JToggleButton infoButton;
  JToggleButton panButton;
  JToggleButton btnEditVertex;
  JToggleButton extractComponentButton;
  JToggleButton deleteVertexButton;
  JToggleButton btnMove;

  private final ImageIcon leftIcon = new ImageIcon(this.getClass().getResource("Left.png"));
  private final ImageIcon rightIcon = new ImageIcon(this.getClass().getResource("Right.png"));
  private final ImageIcon plusIcon = new ImageIcon(this.getClass().getResource("Plus.png"));
  private final ImageIcon copyCaseIcon = new ImageIcon(this.getClass().getResource("CopyCase.png"));
  private final ImageIcon deleteIcon = new ImageIcon(this.getClass().getResource("Delete.png"));
  private final ImageIcon zoomIcon = new ImageIcon(this.getClass().getResource("MagnifyCursor.gif"));
  private final ImageIcon drawRectangleIcon = new ImageIcon(this.getClass().getResource("DrawRectangle.png"));
  private final ImageIcon drawRectangleBIcon = new ImageIcon(this.getClass().getResource("DrawRectangleB.png"));
  private final ImageIcon drawPolygonIcon = new ImageIcon(this.getClass().getResource("DrawPolygon.png"));
  private final ImageIcon drawPolygonBIcon = new ImageIcon(this.getClass().getResource("DrawPolygonB.png"));
  private final ImageIcon drawLineStringIcon = new ImageIcon(this.getClass().getResource("DrawLineString.png"));
  private final ImageIcon drawLineStringBIcon = new ImageIcon(this.getClass().getResource("DrawLineStringB.png"));
  private final ImageIcon drawPointIcon = new ImageIcon(this.getClass().getResource("DrawPoint.png"));
  private final ImageIcon drawPointBIcon = new ImageIcon(this.getClass().getResource("DrawPointB.png"));
  private final ImageIcon infoIcon = new ImageIcon(this.getClass().getResource("Info.png"));
  private final ImageIcon zoomOneToOneIcon = new ImageIcon(this.getClass().getResource("ZoomOneToOne.png"));
  private final ImageIcon zoomToInputIcon = new ImageIcon(this.getClass().getResource("ZoomInput.png"));
  private final ImageIcon zoomToInputAIcon = new ImageIcon(this.getClass().getResource("ZoomInputA.png"));
  private final ImageIcon zoomToInputBIcon = new ImageIcon(this.getClass().getResource("ZoomInputB.png"));
  private final ImageIcon zoomToResultIcon = new ImageIcon(this.getClass().getResource("ZoomResult.png"));
  private final ImageIcon zoomToFullExtentIcon = new ImageIcon(this.getClass().getResource("ZoomAll.png"));
  private final ImageIcon moveVertexIcon = new ImageIcon(this.getClass().getResource("MoveVertex.png"));
  private final ImageIcon panIcon = new ImageIcon(this.getClass().getResource("Hand.gif"));


  public JTSTestBuilderToolBar(JTSTestBuilderFrame tbFrame) 
  {
    this.tbFrame = tbFrame;
  }

  public void clearToolButtons()
  {
    // this only works in JSE 1.6
    // In 1.5, need to add an invisible button and select it
    toolButtonGroup.clearSelection();
  }
  
  public void selectZoomButton()
  {
    zoomButton.setSelected(true);
    toolButtonGroup.setSelected(zoomButton.getModel(), true);
  }
  
  private JTSTestBuilderController controller() {
    return JTSTestBuilder.controller();
  }
  
  public JToolBar getToolBar()
  {
    toolbar.setFloatable(false);

    /**--------------------------------------------------
     * Buttons
     * --------------------------------------------------
     */
    JButton previousButton = createButton(
        AppStrings.TIP_PREV, leftIcon,
        new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            boolean isZoom = 0 == (e.getModifiers() & ActionEvent.CTRL_MASK);
            controller().caseMoveTo(-1, isZoom);
          }
        });
    JButton nextButton = createButton(
        AppStrings.TIP_NEXT, rightIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              boolean isZoom = 0 == (e.getModifiers() & ActionEvent.CTRL_MASK);
              controller().caseMoveTo(1, isZoom);
            }
          });
    JButton newButton = createButton(
        AppStrings.TIP_CASE_ADD_NEW, plusIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().caseCreateNew();
            }
          });
    JButton copyButton = createButton(
        AppStrings.TIP_CASE_DUP, copyCaseIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().caseCopy();
            }
          });
    JButton deleteButton = createButton(
        AppStrings.TIP_CASE_DELETE, deleteIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().caseDelete();
            }
          });
    JButton oneToOneButton = createButton(
        AppStrings.TIP_ZOOM_1_1, zoomOneToOneIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().zoomOneToOne();
            }
          });
    JButton zoomToInputButton = createButton(
        "Zoom To Input", zoomToInputIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().zoomToInput();
            }
          });
    JButton zoomToInputAButton = createButton(
        AppStrings.TIP_ZOOM_TO_A, zoomToInputAIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().zoomToInputA();
            }
          });
    JButton zoomToInputBButton = createButton(
        AppStrings.TIP_ZOOM_TO_B, zoomToInputBIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().zoomToInputB();
            }
          });
    JButton zoomToResultButton = createButton(
        AppStrings.TIP_ZOOM_TO_RESULT, zoomToResultIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().zoomToResult();
            }
          });
    JButton zoomToFullExtentButton = createButton(
        AppStrings.TIP_ZOOM_TO_FULL_EXTENT, zoomToFullExtentIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().zoomToFullExtent();
            }
          });
      
      drawRectangleButton = createToggleButton(
          AppStrings.TIP_DRAW_RECTANGLE, drawRectangleIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().modeDrawRectangle();
            }
          });
      drawPolygonButton = createToggleButton(
          AppStrings.TIP_DRAW_POLY, drawPolygonIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().modeDrawPolygon();
            }
          });
      drawLineStringButton = createToggleButton(
          AppStrings.TIP_DRAW_LINE, drawLineStringIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().modeDrawLineString();
            }
          });
      drawPointButton = createToggleButton(
          AppStrings.TIP_DRAW_POINT, drawPointIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().modeDrawPoint();
            }
          });
      infoButton = createToggleButton(
          AppStrings.TIP_INFO, infoIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().modeInfo();
            }
          });
      zoomButton = createToggleButton(
          AppStrings.TIP_ZOOM, zoomIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().modeZoomIn();
            }
          });
      panButton = createToggleButton(
          AppStrings.TIP_PAN, panIcon,
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().modePan();
            }
          });
      
      btnEditVertex = createToggleButton(
          AppStrings.TIP_MOVE_VERTEX, moveVertexIcon,
        new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            controller().modeEditVertex();
          }
        });

      btnMove = createToggleButton(
          AppStrings.TIP_MOVE, AppIcons.MOVE,
        new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            controller().modeMove();
          }
        });

      extractComponentButton = createToggleButton(
          AppStrings.TIP_EXTRACT_ELEMENTS,
          new ImageIcon(this.getClass().getResource("ExtractComponent.png")), 
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
              controller().modeExtractComponent();
            }
          });
      
      JToggleButton selectComponentButton = createToggleButton(
          AppStrings.TIP_SELECT_ELEMENTS,
          new ImageIcon(this.getClass().getResource("Select.png")), 
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
              controller().modeSelectComponent();
            }
          });
      
      deleteVertexButton = createToggleButton(
          AppStrings.TIP_DELETE_VERTEX_ELEMENT,
          new ImageIcon(this.getClass().getResource("DeleteVertex.png")), 
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              controller().modeDeleteVertex();
          }});

      group(toolButtonGroup,
          drawRectangleButton
          ,drawPolygonButton
          ,drawLineStringButton
          ,drawPointButton
          ,panButton
          ,zoomButton
          ,btnEditVertex
          ,btnMove
          ,deleteVertexButton
          ,infoButton
          ,selectComponentButton
          ,extractComponentButton
      );

      add(toolbar,
        newButton, copyButton, previousButton, nextButton,
        strut(8),
        deleteButton,
        strut(8),
        oneToOneButton,
        zoomToInputAButton, zoomToInputBButton, zoomToInputButton,
        zoomToResultButton, zoomToFullExtentButton,
        strut(20),
        zoomButton,
        infoButton,
        selectComponentButton,
        extractComponentButton,
        
        strut(20),
        drawRectangleButton,drawPolygonButton,drawLineStringButton,
        drawPointButton, 
        strut(20),
        btnMove, btnEditVertex,
        deleteVertexButton
      );
      
      drawRectangleButton.setSelected(true);

      return toolbar;
  }

  private static Component strut(int width) {
    return Box.createHorizontalStrut(width);
  }
  
  private static void add(JToolBar toolbar, Component ...  comps) {
    for (Component comp : comps) {
      toolbar.add(comp);
    }
  }
  private static void group(ButtonGroup group, AbstractButton ...  btns) {
    for (AbstractButton btn : btns) {
      group.add(btn);
    }
  }
  public void setFocusGeometry(int index)
  {
    drawRectangleButton.setIcon(index == 0 ? drawRectangleIcon : drawRectangleBIcon);
    drawPolygonButton.setIcon(index == 0 ? drawPolygonIcon : drawPolygonBIcon);
    drawLineStringButton.setIcon(index == 0 ? drawLineStringIcon : drawLineStringBIcon);
    drawPointButton.setIcon(index == 0 ? drawPointIcon : drawPointBIcon);
  }
  
  private static JToggleButton createToggleButton(String toolTipText, 
      ImageIcon icon, 
      java.awt.event.ActionListener actionListener)
  {
    JToggleButton btn = new JToggleButton();
    btn.setMargin(new Insets(0, 0, 0, 0));
    btn.setPreferredSize(new Dimension(30, 30));
    btn.setIcon(icon);
    btn.setMinimumSize(new Dimension(30, 30));
    btn.setVerticalTextPosition(SwingConstants.BOTTOM);
    btn.setSelected(false);
    btn.setToolTipText(toolTipText);
    btn.setHorizontalTextPosition(SwingConstants.CENTER);
    btn.setFont(new java.awt.Font("SansSerif", 0, 10));
    btn.setMaximumSize(new Dimension(30, 30));
    btn.setFocusable(false);
    btn.addActionListener(actionListener);
    return btn;
  }
  
  private static JButton createButton(String toolTipText, 
      ImageIcon icon, 
      java.awt.event.ActionListener actionListener)
  {
    JButton btn = new JButton();
    btn.setMargin(new Insets(0, 0, 0, 0));
    btn.setPreferredSize(new Dimension(30, 30));
    btn.setIcon(icon);
    btn.setMinimumSize(new Dimension(30, 30));
    btn.setVerticalTextPosition(SwingConstants.BOTTOM);
    btn.setSelected(false);
    btn.setToolTipText(toolTipText);
    btn.setHorizontalTextPosition(SwingConstants.CENTER);
    btn.setFont(new java.awt.Font("SansSerif", 0, 10));
    btn.setMaximumSize(new Dimension(30, 30));
    btn.addActionListener(actionListener);
    btn.setFocusable(false);
    btn.setFocusPainted(false);
    return btn;
  }
  
}
