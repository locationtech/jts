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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
 import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.io.IOUtil;
import org.locationtech.jtstest.testbuilder.model.DisplayParameters;
import org.locationtech.jtstest.testbuilder.model.GeometryEditModel;
import org.locationtech.jtstest.testbuilder.model.GeometryType;
import org.locationtech.jtstest.testbuilder.model.TestBuilderModel;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.testbuilder.ui.dnd.FileDrop;
import org.locationtech.jtstest.util.GeometryTextCleaner;
import org.locationtech.jtstest.util.io.MultiFormatReader;


/**
 * @version 1.7
 */
public class WKTPanel extends JPanel 
{
	TestBuilderModel tbModel;
	
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JPanel panelAB = new JPanel();
    JButton loadButton = new JButton();
    JButton inspectButton = new JButton();
    JButton exchangeButton = new JButton();
    TitledBorder titledBorder1;
    JLabel bLabel = new JLabel();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JLabel aLabel = new JLabel();
    
    JPanel aPanel = new JPanel();
    JButton aCopyButton = new JButton();
    JButton aPasteButton = new JButton();
    JButton aCutButton = new JButton();
//    JPanel aButtonPanel = new JPanel();
    Box aLabelPanel = Box.createVerticalBox();
    Box aButtonPanel = Box.createVerticalBox();
    FlowLayout aButtonPanelLayout = new FlowLayout();
    BorderLayout aPanelLayout = new BorderLayout();
    JRadioButton aRB = new JRadioButton();
    
    JPanel bPanel = new JPanel();
    JButton bCopyButton = new JButton();
    JButton bPasteButton = new JButton();
    JButton bCutButton = new JButton();
//    JPanel bButtonPanel = new JPanel();
    Box bLabelPanel = Box.createVerticalBox();
    Box bButtonPanel = Box.createVerticalBox();
    FlowLayout bButtonPanelLayout = new FlowLayout();
    BorderLayout bPanelLayout = new BorderLayout();
    JRadioButton bRB = new JRadioButton();
    
    JScrollPane aScrollPane = new JScrollPane();
    JTextArea aTextArea = new JTextArea();
    JScrollPane bScrollPane = new JScrollPane();
    JTextArea bTextArea = new JTextArea();
    ButtonGroup editMode = new ButtonGroup();

    protected JTSTestBuilderFrame tbFrame;

    public WKTPanel(JTSTestBuilderFrame tbFrame) {
      this.tbFrame = tbFrame;
        try {
            uiInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        initFileDrop(aTextArea, 0);
        initFileDrop(bTextArea, 1);
    }

  public void setModel(TestBuilderModel tbModel) {
    this.tbModel = tbModel;
        setFocusGeometry(0);
  }

    void uiInit() throws Exception {
        titledBorder1 = new TitledBorder("");
        
        loadButton.setMaximumSize(new Dimension(38, 38));
        loadButton.setPreferredSize(new Dimension(38, 38));
        loadButton.setMargin(new Insets(8, 8, 8, 8));
//        loadButton.setText("Load");
        loadButton.setIcon(AppIcons.GEOM_LOAD);
        loadButton.setToolTipText(AppStrings.TIP_WKT_PANEL_LOAD_GEOMETRY);
        
        inspectButton.setMaximumSize(new Dimension(38, 30));
        inspectButton.setPreferredSize(new Dimension(24, 38));
        inspectButton.setToolTipText(AppStrings.TIP_INSPECT_GEOMETRY);
        inspectButton.setIcon(AppIcons.GEOM_INSPECT);
        
        exchangeButton.setMaximumSize(new Dimension(38, 30));
        exchangeButton.setPreferredSize(new Dimension(24, 38));
        exchangeButton.setToolTipText(AppStrings.TIP_EXCHANGE_A_B);
        exchangeButton.setIcon(AppIcons.GEOM_EXCHANGE);
        
        JButton btnUndo = SwingUtil.createButton(AppIcons.UNDO, "Undo", new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            tbModel.getGeometryEditModel().undo();
          }        
        });
        btnUndo.setMaximumSize(new Dimension(38, 30));
        Box panelButtons = Box.createVerticalBox();
        panelButtons.setPreferredSize(new java.awt.Dimension(30, 30));
        panelButtons.add(loadButton);
        panelButtons.add(Box.createVerticalStrut(20));
        panelButtons.add(exchangeButton);
        panelButtons.add(inspectButton);
        panelButtons.add(btnUndo);
        

        panelAB.setLayout(gridBagLayout2);
        
        aLabel.setFont(new java.awt.Font("Dialog", 1, 16));
        aLabel.setForeground(Color.blue);
        aLabel.setText("A");
        aLabel.setPreferredSize(new Dimension(20, 20));
        aLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        
        bLabel.setFont(new java.awt.Font("Dialog", 1, 16));
        bLabel.setForeground(Color.red);
        bLabel.setText("B");
        bLabel.setPreferredSize(new Dimension(20, 20));
        
        aScrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        aTextArea.setWrapStyleWord(true);
        aTextArea.setLineWrap(true);
        aTextArea.setBackground(Color.white);
        aTextArea.setFont(new java.awt.Font("Monospaced", 0, 12));
        aTextArea.setToolTipText(AppStrings.TIP_TEXT_ENTRY);
        aTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent arg0) {
            setFocusGeometry(0);
          }
        });

        bScrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        bTextArea.setWrapStyleWord(true);
        bTextArea.setLineWrap(true);
        bTextArea.setBackground(Color.white);
        bTextArea.setFont(new java.awt.Font("Monospaced", 0, 12));
        bTextArea.setToolTipText(AppStrings.TIP_TEXT_ENTRY);
        bTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent arg0) {
            setFocusGeometry(1);
          }
        });
        
        aCopyButton.setToolTipText(AppStrings.TIP_COPY_DATA);
        aCopyButton.setIcon(AppIcons.COPY);
        aCopyButton.setMargin(new Insets(0, 0, 0, 0));

        aPasteButton.setToolTipText(AppStrings.TIP_PASTE_DATA);
        aPasteButton.setIcon(AppIcons.PASTE);
        aPasteButton.setMargin(new Insets(0, 0, 0, 0));

        aCutButton.setToolTipText("Clear");
        aCutButton.setIcon(AppIcons.CUT);
        aCutButton.setMargin(new Insets(0, 0, 0, 0));

        aButtonPanelLayout.setVgap(1);
        aButtonPanelLayout.setHgap(1);
//        aButtonPanel.setLayout(aButtonPanelLayout);
        aButtonPanel.add(aPasteButton);
        aButtonPanel.add(aCopyButton);
        aButtonPanel.add(aCutButton);
        
        aLabel.setAlignmentX(LEFT_ALIGNMENT);
        aRB.setAlignmentX(LEFT_ALIGNMENT);
        aRB.setSelected(true);
        aRB.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setFocusGeometry(0);
          }
        });
        //aLabelPanel.add(aRB);
        aLabelPanel.add(aLabel);
        aLabelPanel.add(aButtonPanel);
        
        aPanel.setLayout(aPanelLayout);
        aPanel.add(aLabelPanel, BorderLayout.WEST);
        aPanel.add(aScrollPane, BorderLayout.CENTER);
        //aPanel.add(aButtonPanel, BorderLayout.EAST);
        
        bCopyButton.setToolTipText(AppStrings.TIP_COPY_DATA);
        bCopyButton.setIcon(AppIcons.COPY);
        bCopyButton.setMargin(new Insets(0, 0, 0, 0));

        bPasteButton.setToolTipText(AppStrings.TIP_PASTE_DATA);
        bPasteButton.setIcon(AppIcons.PASTE);
        bPasteButton.setMargin(new Insets(0, 0, 0, 0));

        bCutButton.setToolTipText("Clear");
        bCutButton.setIcon(AppIcons.CUT);
        bCutButton.setMargin(new Insets(0, 0, 0, 0));

        bButtonPanelLayout.setVgap(1);
        bButtonPanelLayout.setHgap(1);
//        bButtonPanel.setLayout(bButtonPanelLayout);
        bButtonPanel.add(bPasteButton);
        bButtonPanel.add(bCopyButton);
        bButtonPanel.add(bCutButton);

        bLabel.setAlignmentX(LEFT_ALIGNMENT);
        //bLabelPanel.add(bRB);
        bRB.setAlignmentX(LEFT_ALIGNMENT);
        bRB.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setFocusGeometry(1);
          }
        });
        bLabelPanel.add(bLabel);
        bLabelPanel.add(bButtonPanel);

        bPanel.setLayout(bPanelLayout);
        bPanel.add(bLabelPanel, BorderLayout.WEST);
        bPanel.add(bScrollPane, BorderLayout.CENTER);
        //bPanel.add(bButtonPanel, BorderLayout.EAST);
        
        panelAB.add(
        		aPanel,
            new GridBagConstraints(1, 0, 1, 1,
                1.0, 1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
                0, 0));
        panelAB.add(
            bPanel,
            new GridBagConstraints(1, 1, 1, 1,
                1.0, 1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
                0, 0));
        bScrollPane.getViewport().add(bTextArea, null);
        aScrollPane.getViewport().add(aTextArea, null);
        
        /*
        this.setLayout(gridBagLayout1);
        this.setPreferredSize(new java.awt.Dimension(394, 176));
        this.add(
            panelAB,
            new GridBagConstraints(0, 1, 1, 2,
                1.0, 1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
                0, 0));
        this.add(
            panelButtons,
            new GridBagConstraints( 1, 1, 1, 1,
                0.0, 0.0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE,
                new Insets(2, 2, 0, 2),
                0, 0));
        */
        
        this.setLayout(new BorderLayout());
        this.add(panelAB, BorderLayout.CENTER);
        this.add(panelButtons, BorderLayout.EAST);
        
        loadButton.addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                loadButton_actionPerformed(e);
              }
            });
        inspectButton.addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                JTSTestBuilder.controller().inspectGeometry();
              }
            });
        exchangeButton.addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                JTSTestBuilder.controller().exchangeGeometry();
              }
            });
       aCopyButton.addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
              	aCopyButton_actionPerformed(e);
              }
            });
        aPasteButton.addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
              	aPasteButton_actionPerformed(e);
              }
            });
        aCutButton.addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
              	aCutButton_actionPerformed(e);
              }
            });
        bCopyButton.addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
              	bCopyButton_actionPerformed(e);
              }
            });
        bPasteButton.addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
              	bPasteButton_actionPerformed(e);
              }
            });
        bCutButton.addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
              	bCutButton_actionPerformed(e);
              }
            });
        editMode.add(aRB);
        editMode.add(bRB);
    }

    public void setText(Geometry g, int geomIndex)
    {
      String txt = null;
      if (g == null)
        txt = "";
      else if (g.getNumPoints() > DisplayParameters.MAX_DISPLAY_POINTS)
        txt = GeometryEditModel.toStringVeryLarge(g);
      else
        txt = GeometryEditModel.getText(g, GeometryType.WELLKNOWNTEXT);
      
      switch (geomIndex) {
      case 0: aTextArea.setText(txt); break;
      case 1: bTextArea.setText(txt); break;
      }
    }
    
    public String getGeometryTextA() {
        return aTextArea.getText();
    }

    public String getGeometryTextB() {
        return bTextArea.getText();
    }

    public String getGeometryText(int geomIndex)
    {
    	if (geomIndex == 0) return aTextArea.getText();
    	return bTextArea.getText();
    }
    
    public String getGeometryTextClean(int geomIndex)
    {
    	String text = getGeometryText(geomIndex);
    	String textTrim = text.trim();
    	if (textTrim.length() == 0) return textTrim;
    	String textClean = textTrim;
    	switch (MultiFormatReader.format(textTrim))
    	{
    	case MultiFormatReader.FORMAT_WKT:
    		textClean = GeometryTextCleaner.cleanWKT(textTrim);
    		break;
    	}
    	return textClean;
    }
    
    void aTextArea_keyTyped(KeyEvent e) {
        loadButton.setEnabled(true);
    }

    void bTextArea_keyTyped(KeyEvent e) {
        loadButton.setEnabled(true);
    }

    void loadButton_actionPerformed(ActionEvent e) {
      try {
        tbModel.loadGeometryText(
            getGeometryTextClean(0), 
            getGeometryTextClean(1));
        JTSTestBuilder.controller().zoomToInput();
      }
      catch (Exception ex) {
        SwingUtil.reportException(this, ex);
      }
    }

    void aCopyButton_actionPerformed(ActionEvent e) {
      copy(e, 0);
    }
    
    void bCopyButton_actionPerformed(ActionEvent e) {
      copy(e, 1);
    }
    
    void copy(ActionEvent e, int geomIndex)
    {
      Geometry g = tbModel.getCurrentCase().getGeometry(geomIndex);
      if (g == null) return;
      
      if (SwingUtil.isShiftKeyPressed(e)) {
        String wkb = IOUtil.toWKBHex(g);
        SwingUtil.copyToClipboard(wkb, false);
        return;
      }
      boolean isFormatted = SwingUtil.isCtlKeyPressed(e);
      SwingUtil.copyToClipboard(g, isFormatted);
    }
    
    void aPasteButton_actionPerformed(ActionEvent e)
    {
      paste(0);
    }
    void bPasteButton_actionPerformed(ActionEvent e) {
      paste(1);
    }
    
    void paste(int geomIndex) {
      try {
        tbModel.pasteGeometry(geomIndex);
        JTSTestBuilder.controller().zoomToInput();
      }
      catch (Exception ex) {
        JTSTestBuilderFrame.reportException(ex);
      }
    }
    
    void aCutButton_actionPerformed(ActionEvent e) {
      aTextArea.setText("");
      tbModel.getGeometryEditModel().clear(0);
    }
    void bCutButton_actionPerformed(ActionEvent e) {
    	bTextArea.setText("");
      tbModel.getGeometryEditModel().clear(1);
    }

    private void initFileDrop(Component comp, int index) 
    {
      final int geomIndex = index;
      
      new FileDrop(comp, new FileDrop.Listener() {
        public void filesDropped(java.io.File[] files) {
          try {
            tbModel.loadMultipleGeometriesFromFile(geomIndex, files[0].getCanonicalPath());
            //(textArea).setText(FileUtil.readText(files[0]));
            JTSTestBuilder.controller().zoomToInput();
          } catch (Exception ex) {
            SwingUtil.reportException(null, ex);
          }
        }
      });
    }

    Border focusBorder = BorderFactory.createMatteBorder(0, 2, 0, 0, Color.green);
    //Border otherBorder = BorderFactory.createEmptyBorder();
    Border otherBorder = BorderFactory.createMatteBorder(0, 2, 0, 0, Color.white);
    
    private static Color focusBackgroundColor = Color.white; //new Color(240,255,250);
    private static Color otherBackgroundColor = AppColors.BACKGROUND;
    
    private void setFocusGeometry(int index) {
      JTSTestBuilder.controller().setFocusGeometry(index);
      
      JTextArea focusTA = index == 0 ? aTextArea : bTextArea;
      JTextArea otherTA = index == 0 ? bTextArea : aTextArea;
      //focusTA.setBorder(focusBorder);
      //otherTA.setBorder(otherBorder);
      
      focusTA.setBackground(focusBackgroundColor);
      otherTA.setBackground(otherBackgroundColor);
      repaint();
    }
  
}
