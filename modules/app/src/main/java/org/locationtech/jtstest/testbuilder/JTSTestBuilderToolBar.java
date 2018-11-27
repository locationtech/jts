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

import org.locationtech.jtstest.testbuilder.ui.SwingUtil;

public class JTSTestBuilderToolBar {

  JTSTestBuilderFrame tbFrame;

  JToolBar toolbar = new JToolBar();
  ButtonGroup toolButtonGroup = new ButtonGroup();
  
  JButton previousButton = new JButton();
  JButton nextButton = new JButton();
  JButton newButton = new JButton();
  JButton copyButton = new JButton();
  JButton deleteButton = new JButton();
  JButton exchangeButton = new JButton();

  JButton oneToOneButton = new JButton();
  JButton zoomToFullExtentButton = new JButton();
  JButton zoomToInputButton = new JButton();
  JButton zoomToInputAButton = new JButton();
  JButton zoomToInputBButton = new JButton();
  JButton zoomToResultButton = new JButton();
  
  JToggleButton drawRectangleButton = new JToggleButton();
  JToggleButton drawPolygonButton = new JToggleButton();
  JToggleButton drawLineStringButton = new JToggleButton();
  JToggleButton drawPointButton = new JToggleButton();
  JToggleButton zoomButton = new JToggleButton();
  JToggleButton infoButton = new JToggleButton();
  JToggleButton panButton = new JToggleButton();
  JToggleButton btnEditVertex = new JToggleButton();
  JToggleButton extractComponentButton;

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
  private final ImageIcon selectIcon = new ImageIcon(this.getClass().getResource("Select.gif"));
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
    //toolButtonGroup.clearSelection();
  }
  
  public void unselectExtractComponentButton()
  {
    extractComponentButton.setSelected(false);
    toolButtonGroup.setSelected(extractComponentButton.getModel(), false);
  }
  
  public JToolBar getToolBar()
  {
    toolbar.setFloatable(false);

    /**--------------------------------------------------
     * Buttons
     * --------------------------------------------------
     */
      previousButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      previousButton.setMaximumSize(new Dimension(30, 30));
      previousButton.setMinimumSize(new Dimension(30, 30));
      previousButton.setPreferredSize(new Dimension(30, 30));
      previousButton.setToolTipText(AppStrings.TIP_PREV);
      previousButton.setHorizontalTextPosition(SwingConstants.CENTER);
      previousButton.setIcon(leftIcon);
      previousButton.setMargin(new Insets(0, 0, 0, 0));
      previousButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      previousButton.addActionListener(
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              boolean isZoom = 0 == (e.getModifiers() & ActionEvent.CTRL_MASK);
              tbFrame.moveToPrevCase(isZoom);
            }
          });
      
      nextButton.setMargin(new Insets(0, 0, 0, 0));
      nextButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      nextButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      nextButton.setMaximumSize(new Dimension(30, 30));
      nextButton.setMinimumSize(new Dimension(30, 30));
      nextButton.setPreferredSize(new Dimension(30, 30));
      nextButton.setToolTipText(AppStrings.TIP_NEXT);
      nextButton.setHorizontalTextPosition(SwingConstants.CENTER);
      nextButton.setIcon(rightIcon);
      nextButton.addActionListener(
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
             boolean isZoom = 0 == (e.getModifiers() & ActionEvent.CTRL_MASK);
             tbFrame.moveToNextCase(isZoom);
            }
          });
      
      newButton.setMargin(new Insets(0, 0, 0, 0));
      newButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      newButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      newButton.setMaximumSize(new Dimension(30, 30));
      newButton.setMinimumSize(new Dimension(30, 30));
      newButton.setPreferredSize(new Dimension(30, 30));
      newButton.setToolTipText(AppStrings.TIP_CASE_ADD_NEW);
      newButton.setHorizontalTextPosition(SwingConstants.CENTER);
      newButton.setIcon(plusIcon);
      newButton.addActionListener(
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              tbFrame.createNewCase();
            }
          });
      
      copyButton.setMargin(new Insets(0, 0, 0, 0));
      copyButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      copyButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      copyButton.setMaximumSize(new Dimension(30, 30));
      copyButton.setMinimumSize(new Dimension(30, 30));
      copyButton.setPreferredSize(new Dimension(30, 30));
      copyButton.setToolTipText(AppStrings.TIP_CASE_DUP);
      copyButton.setHorizontalTextPosition(SwingConstants.CENTER);
      copyButton.setIcon(copyCaseIcon);
      copyButton.addActionListener(
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              tbFrame.copyCase();
            }
          });
      
      deleteButton.setMargin(new Insets(0, 0, 0, 0));
      deleteButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      deleteButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      deleteButton.setMaximumSize(new Dimension(30, 30));
      deleteButton.setMinimumSize(new Dimension(30, 30));
      deleteButton.setPreferredSize(new Dimension(30, 30));
      deleteButton.setToolTipText(AppStrings.TIP_CASE_DELETE);
      deleteButton.setHorizontalTextPosition(SwingConstants.CENTER);
      deleteButton.setIcon(deleteIcon);
      deleteButton.addActionListener(
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              tbFrame.actionDeleteCase();
            }
          });
           
      drawRectangleButton.setMargin(new Insets(0, 0, 0, 0));
      drawRectangleButton.setPreferredSize(new Dimension(30, 30));
      drawRectangleButton.setIcon(drawRectangleIcon);
      drawRectangleButton.setMinimumSize(new Dimension(30, 30));
      drawRectangleButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      drawRectangleButton.setSelected(true);
      drawRectangleButton.setToolTipText(AppStrings.TIP_DRAW_RECTANGLE);
      drawRectangleButton.setHorizontalTextPosition(SwingConstants.CENTER);
      drawRectangleButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      drawRectangleButton.setMaximumSize(new Dimension(30, 30));
      drawRectangleButton.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            tbFrame.modeDrawRectangle();
          }
        });
      
      drawPolygonButton.setMargin(new Insets(0, 0, 0, 0));
      drawPolygonButton.setPreferredSize(new Dimension(30, 30));
      drawPolygonButton.setIcon(drawPolygonIcon);
      drawPolygonButton.setMinimumSize(new Dimension(30, 30));
      drawPolygonButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      drawPolygonButton.setSelected(true);
      drawPolygonButton.setToolTipText(AppStrings.TIP_DRAW_POLY);
      drawPolygonButton.setHorizontalTextPosition(SwingConstants.CENTER);
      drawPolygonButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      drawPolygonButton.setMaximumSize(new Dimension(30, 30));
      drawPolygonButton.addActionListener(
        new java.awt.event.ActionListener() {

          public void actionPerformed(ActionEvent e) {
            tbFrame.modeDrawPolygon();
          }
        });
      
      drawLineStringButton.setMargin(new Insets(0, 0, 0, 0));
      drawLineStringButton.setPreferredSize(new Dimension(30, 30));
      drawLineStringButton.setIcon(drawLineStringIcon);
      drawLineStringButton.setMinimumSize(new Dimension(30, 30));
      drawLineStringButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      drawLineStringButton.setSelected(true);
      drawLineStringButton.setToolTipText(AppStrings.TIP_DRAW_LINE);
      drawLineStringButton.setHorizontalTextPosition(SwingConstants.CENTER);
      drawLineStringButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      drawLineStringButton.setMaximumSize(new Dimension(30, 30));
      drawLineStringButton.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            tbFrame.modeDrawLineString();
          }
        });
      drawPointButton.setMargin(new Insets(0, 0, 0, 0));
      drawPointButton.setPreferredSize(new Dimension(30, 30));
      drawPointButton.setIcon(drawPointIcon);
      drawPointButton.setMinimumSize(new Dimension(30, 30));
      drawPointButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      drawPointButton.setSelected(true);
      drawPointButton.setToolTipText(AppStrings.TIP_DRAW_POINT);
      drawPointButton.setHorizontalTextPosition(SwingConstants.CENTER);
      drawPointButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      drawPointButton.setMaximumSize(new Dimension(30, 30));
      drawPointButton.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            tbFrame.modeDrawPoint();
          }
        });
      infoButton.setMargin(new Insets(0, 0, 0, 0));
      infoButton.setPreferredSize(new Dimension(30, 30));
      infoButton.setIcon(infoIcon);
      infoButton.setMinimumSize(new Dimension(30, 30));
      infoButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      infoButton.setSelected(false);
      infoButton.setToolTipText(AppStrings.TIP_INFO);
      infoButton.setHorizontalTextPosition(SwingConstants.CENTER);
      infoButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      infoButton.setMaximumSize(new Dimension(30, 30));
      infoButton.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            tbFrame.modeInfo();
          }
        });
      zoomButton.setMaximumSize(new Dimension(30, 30));
      zoomButton.addActionListener(
        new java.awt.event.ActionListener() {

          public void actionPerformed(ActionEvent e) {
            tbFrame.modeZoomIn();
          }
        });
      zoomButton.setToolTipText(AppStrings.TIP_ZOOM);
      zoomButton.setHorizontalTextPosition(SwingConstants.CENTER);
      zoomButton.setFont(new java.awt.Font("Serif", 0, 10));
      zoomButton.setMinimumSize(new Dimension(30, 30));
      zoomButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      zoomButton.setPreferredSize(new Dimension(30, 30));
      zoomButton.setIcon(zoomIcon);
      zoomButton.setMargin(new Insets(0, 0, 0, 0));
      
      oneToOneButton.setMargin(new Insets(0, 0, 0, 0));
      oneToOneButton.setIcon(zoomOneToOneIcon);
      oneToOneButton.setPreferredSize(new Dimension(30, 30));
      oneToOneButton.setMinimumSize(new Dimension(30, 30));
      oneToOneButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      oneToOneButton.addActionListener(
        new java.awt.event.ActionListener() {

          public void actionPerformed(ActionEvent e) {
            tbFrame.zoomOneToOne();
          }
        });
      oneToOneButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      oneToOneButton.setToolTipText(AppStrings.TIP_ZOOM_1_1);
      oneToOneButton.setHorizontalTextPosition(SwingConstants.CENTER);
      oneToOneButton.setMaximumSize(new Dimension(30, 30));
      
      zoomToInputButton.setMargin(new Insets(0, 0, 0, 0));
      zoomToInputButton.setIcon(zoomToInputIcon);
      zoomToInputButton.setPreferredSize(new Dimension(30, 30));
      zoomToInputButton.setMaximumSize(new Dimension(30, 30));
      zoomToInputButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      zoomToInputButton.setMinimumSize(new Dimension(30, 30));
      zoomToInputButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      zoomToInputButton.setHorizontalTextPosition(SwingConstants.CENTER);
      zoomToInputButton.setToolTipText("Zoom To Input");
      zoomToInputButton.addActionListener(
        new java.awt.event.ActionListener() {

          public void actionPerformed(ActionEvent e) {
            tbFrame.zoomToInput();
          }
        });

      zoomToInputAButton.setMargin(new Insets(0, 0, 0, 0));
      zoomToInputAButton.setIcon(zoomToInputAIcon);
      zoomToInputAButton.setPreferredSize(new Dimension(30, 30));
      zoomToInputAButton.setMaximumSize(new Dimension(30, 30));
      zoomToInputAButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      zoomToInputAButton.setMinimumSize(new Dimension(30, 30));
      zoomToInputAButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      zoomToInputAButton.setHorizontalTextPosition(SwingConstants.CENTER);
      zoomToInputAButton.setToolTipText(AppStrings.TIP_ZOOM_TO_A);
      zoomToInputAButton.addActionListener(
        new java.awt.event.ActionListener() {

          public void actionPerformed(ActionEvent e) {
            tbFrame.zoomToInputA();
          }
        });
      
      zoomToInputBButton.setMargin(new Insets(0, 0, 0, 0));
      zoomToInputBButton.setIcon(zoomToInputBIcon);
      zoomToInputBButton.setPreferredSize(new Dimension(30, 30));
      zoomToInputBButton.setMaximumSize(new Dimension(30, 30));
      zoomToInputBButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      zoomToInputBButton.setMinimumSize(new Dimension(30, 30));
      zoomToInputBButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      zoomToInputBButton.setHorizontalTextPosition(SwingConstants.CENTER);
      zoomToInputBButton.setToolTipText(AppStrings.TIP_ZOOM_TO_B);
      zoomToInputBButton.addActionListener(
        new java.awt.event.ActionListener() {

          public void actionPerformed(ActionEvent e) {
            tbFrame.zoomToInputB();
          }
        });
      zoomToInputButton.setMaximumSize(new Dimension(30, 30));
      
      zoomToResultButton.setMargin(new Insets(0, 0, 0, 0));
      zoomToResultButton.setIcon(zoomToResultIcon);
      zoomToResultButton.setPreferredSize(new Dimension(30, 30));
      zoomToResultButton.setMaximumSize(new Dimension(30, 30));
      zoomToResultButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      zoomToResultButton.setMinimumSize(new Dimension(30, 30));
      zoomToResultButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      zoomToResultButton.setHorizontalTextPosition(SwingConstants.CENTER);
      zoomToResultButton.setToolTipText(AppStrings.TIP_ZOOM_TO_RESULT);
      zoomToResultButton.addActionListener(
        new java.awt.event.ActionListener() {

          public void actionPerformed(ActionEvent e) {
            tbFrame.zoomToResult();
          }
        });
      zoomToResultButton.setMaximumSize(new Dimension(30, 30));
      
      zoomToFullExtentButton.setMargin(new Insets(0, 0, 0, 0));
      zoomToFullExtentButton.setIcon(zoomToFullExtentIcon);
      zoomToFullExtentButton.setPreferredSize(new Dimension(30, 30));
      zoomToFullExtentButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      zoomToFullExtentButton.setMinimumSize(new Dimension(30, 30));
      zoomToFullExtentButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      zoomToFullExtentButton.setHorizontalTextPosition(SwingConstants.CENTER);
      zoomToFullExtentButton.setToolTipText(AppStrings.TIP_ZOOM_TO_FULL_EXTENT);
      zoomToFullExtentButton.addActionListener(
        new java.awt.event.ActionListener() {

          public void actionPerformed(ActionEvent e) {
            tbFrame.zoomToFullExtent();
          }
        });
      zoomToFullExtentButton.setMaximumSize(new Dimension(30, 30));
      
      panButton.addActionListener(
        new java.awt.event.ActionListener() {

          public void actionPerformed(ActionEvent e) {
            tbFrame.modePan();
          }
        });
      panButton.setMaximumSize(new Dimension(30, 30));
      panButton.setFont(new java.awt.Font("SansSerif", 0, 10));
      panButton.setHorizontalTextPosition(SwingConstants.CENTER);
      panButton.setToolTipText(AppStrings.TIP_PAN);
      panButton.setVerticalTextPosition(SwingConstants.BOTTOM);
      panButton.setMinimumSize(new Dimension(30, 30));
      panButton.setIcon(panIcon);
      panButton.setPreferredSize(new Dimension(30, 30));
      panButton.setMargin(new Insets(0, 0, 0, 0));
      
      btnEditVertex.setMaximumSize(new Dimension(30, 30));
      btnEditVertex.setMinimumSize(new Dimension(30, 30));
      btnEditVertex.setToolTipText(AppStrings.TIP_MOVE_VERTEX);
      btnEditVertex.setIcon(moveVertexIcon);
      btnEditVertex.setMargin(new Insets(0, 0, 0, 0));
      btnEditVertex.setMnemonic('0');
      btnEditVertex.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.modeEditVertex();
        }
      });

      extractComponentButton = createToggleButton(
          AppStrings.TIP_EXTRACT_COMPONENTS,
          new ImageIcon(this.getClass().getResource("ExtractComponent.png")), 
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
              tbFrame.modeExtractComponent();
            }
          });

      JToggleButton deleteVertexButton = createToggleButton(
          AppStrings.TIP_DELETE_VERTEX_COMPONENT,
          new ImageIcon(this.getClass().getResource("DeleteVertex.png")), 
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tbFrame.modeDeleteVertex();
          }});

      group(drawRectangleButton,drawPolygonButton
        ,drawLineStringButton
        ,drawPointButton
        ,panButton
        ,zoomButton
        ,btnEditVertex
        ,deleteVertexButton
        ,infoButton
        ,extractComponentButton
      );


      add(
        newButton, copyButton, previousButton, nextButton,
        strut(8),
        deleteButton,
        strut(8),
        exchangeButton,
        strut(8),
        oneToOneButton,
        zoomToInputAButton, zoomToInputBButton, zoomToInputButton,
        zoomToResultButton, zoomToFullExtentButton,
        strut(20),
        zoomButton,
        //jToolBar1.add(panButton  // remove in favour of using Zoom tool right-drag
        infoButton,
        extractComponentButton,
        
        strut(20),
        drawRectangleButton,drawPolygonButton,drawLineStringButton,
        drawPointButton, btnEditVertex,
        deleteVertexButton
      );
      
      return toolbar;
  }

  private Component strut(int width) {
    return Box.createHorizontalStrut(width);
  }
  
  private void add(Component ...  comps) {
    for (Component comp : comps) {
      toolbar.add(comp);
    }
  }
  private void group(AbstractButton ...  btns) {
    for (AbstractButton btn : btns) {
      toolButtonGroup.add(btn);
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
