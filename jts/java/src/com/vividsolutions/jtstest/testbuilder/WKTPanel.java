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
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import com.vividsolutions.jtstest.testbuilder.ui.dnd.FileDrop;
import com.vividsolutions.jtstest.util.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jtstest.testbuilder.model.*;
import com.vividsolutions.jtstest.testbuilder.ui.*;

/**
 * @version 1.7
 */
public class WKTPanel extends JPanel 
{
	TestBuilderModel tbModel;
	
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JPanel jPanel1 = new JPanel();
    JButton loadButton = new JButton();
    TitledBorder titledBorder1;
    JLabel jLabel2 = new JLabel();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JLabel jLabel1 = new JLabel();
    
    JPanel aPanel = new JPanel();
    JButton aCopyButton = new JButton();
    JButton aPasteButton = new JButton();
    JButton aCutButton = new JButton();
//    JPanel aButtonPanel = new JPanel();
    Box aButtonPanel = Box.createVerticalBox();
    FlowLayout aButtonPanelLayout = new FlowLayout();
    BorderLayout aPanelLayout = new BorderLayout();
    
    JPanel bPanel = new JPanel();
    JButton bCopyButton = new JButton();
    JButton bPasteButton = new JButton();
    JButton bCutButton = new JButton();
//    JPanel bButtonPanel = new JPanel();
    Box bButtonPanel = Box.createVerticalBox();
    FlowLayout bButtonPanelLayout = new FlowLayout();
    BorderLayout bPanelLayout = new BorderLayout();
    
    JScrollPane jScrollPane1 = new JScrollPane();
    JTextArea aTextArea = new JTextArea();
    JScrollPane jScrollPane2 = new JScrollPane();
    JTextArea bTextArea = new JTextArea();

    private final ImageIcon copyIcon = new ImageIcon(this.getClass().getResource("Copy.gif"));
    private final ImageIcon pasteIcon = new ImageIcon(this.getClass().getResource("Paste.gif"));
    private final ImageIcon cutIcon = new ImageIcon(this.getClass().getResource("Delete_small.png"));
    private final ImageIcon loadIcon = new ImageIcon(this.getClass().getResource("LoadWKTToTest.png"));

    public WKTPanel() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        initFileDrop(aTextArea, 0);
        initFileDrop(bTextArea, 1);
    }

  	public void setModel(TestBuilderModel tbModel)
  	{
  		this.tbModel = tbModel;
  	}

    void jbInit() throws Exception {
        titledBorder1 = new TitledBorder("");
        this.setLayout(gridBagLayout1);
        this.setPreferredSize(new java.awt.Dimension(394, 176));
        loadButton.setPreferredSize(new Dimension(38, 38));
        loadButton.setMargin(new Insets(8, 8, 8, 8));
//        loadButton.setText("Load");
        loadButton.setIcon(loadIcon);
        loadButton.setToolTipText(AppStrings.WKT_PANEL_LOAD_GEOMETRY_TIP);
        jPanel1.setLayout(gridBagLayout2);
        jLabel2.setFont(new java.awt.Font("Dialog", 1, 12));
        jLabel2.setForeground(Color.red);
        jLabel2.setText("B");
        jLabel1.setFont(new java.awt.Font("Dialog", 1, 12));
        jLabel1.setForeground(Color.blue);
        jLabel1.setText("A");
        jScrollPane1.setBorder(BorderFactory.createLoweredBevelBorder());
        aTextArea.setWrapStyleWord(true);
        aTextArea.setLineWrap(true);
        aTextArea.setBackground(Color.white);
        aTextArea.setFont(new java.awt.Font("Monospaced", 0, 12));
        aTextArea.setToolTipText(AppStrings.TEXT_ENTRY_TIP);

        jScrollPane2.setBorder(BorderFactory.createLoweredBevelBorder());
        bTextArea.setWrapStyleWord(true);
        bTextArea.setLineWrap(true);
        bTextArea.setBackground(Color.white);
        bTextArea.setFont(new java.awt.Font("Monospaced", 0, 12));
        bTextArea.setToolTipText(AppStrings.TEXT_ENTRY_TIP);
        
        aCopyButton.setToolTipText("Copy WKT (Ctl-click for formatted)");
        aCopyButton.setIcon(copyIcon);
        aCopyButton.setMargin(new Insets(0, 0, 0, 0));

        aPasteButton.setToolTipText("Paste WKT, WKB, or GML");
        aPasteButton.setIcon(pasteIcon);
        aPasteButton.setMargin(new Insets(0, 0, 0, 0));

        aCutButton.setToolTipText("Clear");
        aCutButton.setIcon(cutIcon);
        aCutButton.setMargin(new Insets(0, 0, 0, 0));

        aButtonPanelLayout.setVgap(1);
        aButtonPanelLayout.setHgap(1);
//        aButtonPanel.setLayout(aButtonPanelLayout);
        aButtonPanel.add(aPasteButton);
        aButtonPanel.add(aCopyButton);
        aButtonPanel.add(aCutButton);
        aPanel.setLayout(aPanelLayout);
        aPanel.add(jScrollPane1, BorderLayout.CENTER);
        aPanel.add(aButtonPanel, BorderLayout.EAST);
        
        bCopyButton.setToolTipText("Copy WKT (Ctl-click for formatted)");
        bCopyButton.setIcon(copyIcon);
        bCopyButton.setMargin(new Insets(0, 0, 0, 0));

        bPasteButton.setToolTipText("Paste WKT, WKB, or GML");
        bPasteButton.setIcon(pasteIcon);
        bPasteButton.setMargin(new Insets(0, 0, 0, 0));

        bCutButton.setToolTipText("Clear");
        bCutButton.setIcon(cutIcon);
        bCutButton.setMargin(new Insets(0, 0, 0, 0));

        bButtonPanelLayout.setVgap(1);
        bButtonPanelLayout.setHgap(1);
//        bButtonPanel.setLayout(bButtonPanelLayout);
        bButtonPanel.add(bPasteButton);
        bButtonPanel.add(bCopyButton);
        bButtonPanel.add(bCutButton);
        bPanel.setLayout(bPanelLayout);
        bPanel.add(jScrollPane2, BorderLayout.CENTER);
        bPanel.add(bButtonPanel, BorderLayout.EAST);
        
        this.add(
            jPanel1,
            new GridBagConstraints(
                0,
                1,
                1,
                2,
                1.0,
                1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
                0,
                0));
        jPanel1.add(
            jLabel1,
            new GridBagConstraints(
                0,
                0,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.NORTH,
                GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2),
                0,
                0));
        jPanel1.add(
        		aPanel,
            new GridBagConstraints(
                1,
                0,
                1,
                1,
                1.0,
                1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
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
                GridBagConstraints.NORTH,
                GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2),
                0,
                0));
        jPanel1.add(
            bPanel,
            new GridBagConstraints(
                1,
                1,
                1,
                1,
                1.0,
                1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
                0,
                0));
        jScrollPane2.getViewport().add(bTextArea, null);
        jScrollPane1.getViewport().add(aTextArea, null);
        this.add(
            loadButton,
            new GridBagConstraints(
                1,
                1,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE,
                new Insets(2, 2, 0, 2),
                0,
                0));
        
        loadButton.addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                loadButton_actionPerformed(e);
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

    }

    public void setText(Geometry g, int geomIndex)
    {
      String txt = null;
      if (g == null)
        txt = "";
      else if (g.getNumPoints() > TestBuilderModel.MAX_DISPLAY_POINTS)
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
      boolean isFormatted = 0 != (e.getModifiers() & ActionEvent.CTRL_MASK);
      Geometry g = tbModel.getCurrentTestCaseEdit().getGeometry(geomIndex);
      if (g != null)
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
          } catch (Exception ex) {
            SwingUtil.reportException(null, ex);
          }
        }
      });
    }
  
}
