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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class JTSTestBuilderMenuBar 
{
  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu jMenuFile = new JMenu();
  JMenu jMenuHelp = new JMenu();
  JMenuItem jMenuAbout = new JMenuItem();
  JMenu jMenuView = new JMenu();
  JMenuItem jMenuFileExit = new JMenuItem();
  JMenu jMenuEdit = new JMenu();
  //JMenu jMenuTools = new JMenu();
  //JMenu jMenuOptions = new JMenu();
  JMenuItem menuExchangeGeom = new JMenuItem();
  JMenuItem menuViewText = new JMenuItem();
  JMenuItem menuViewGeometry = new JMenuItem();
  JMenuItem menuLoadXmlTestFile = new JMenuItem();
  JMenuItem saveAsXmlMenuItem = new JMenuItem();
  JMenuItem saveAsHtmlMenuItem = new JMenuItem();
  JMenuItem saveAsPNGMenuItem = new JMenuItem();
  JMenuItem saveToClipboardMenuItem = new JMenuItem();
  JMenuItem deleteAllTestCasesMenuItem = new JMenuItem();
  JCheckBoxMenuItem showVerticesMenuItem = new JCheckBoxMenuItem();
  JCheckBoxMenuItem showGridMenuItem = new JCheckBoxMenuItem();
  JCheckBoxMenuItem showOrientationsMenuItem = new JCheckBoxMenuItem();
  JCheckBoxMenuItem showStructureMenuItem = new JCheckBoxMenuItem();
  JCheckBoxMenuItem showVertexIndicesMenuItem = new JCheckBoxMenuItem();
  JMenuItem menuLoadXmlTestFolder = new JMenuItem();
  JMenuItem precisionModelMenuItem = new JMenuItem();
  JMenuItem removeDuplicatePoints = new JMenuItem();
  JMenuItem changeToLines = new JMenuItem();
  private JMenuItem generateExpectedValuesMenuItem = new JMenuItem();

  JTSTestBuilderFrame tbFrame;
  
  public JTSTestBuilderMenuBar(JTSTestBuilderFrame tbFrame) 
  {
    this.tbFrame = tbFrame;
  }

  public JMenuBar getMenuBar()
  {
    jMenuAbout.setText("About");
    jMenuAbout.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.jMenuHelpAbout_actionPerformed(e);
        }
      });
    jMenuView.setText("View");

    jMenuFileExit.setText("Exit");
    jMenuFileExit.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.jMenuFileExit_actionPerformed(e);
        }
      });
    jMenuEdit.setText("Edit");
    menuExchangeGeom.setText("Exchange Geometries");
    menuExchangeGeom.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.menuExchangeGeom_actionPerformed(e);
        }
      });
    menuViewText.setText("Test Case Text...");
    menuViewText.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.menuViewText_actionPerformed(e);
        }
      });
    menuViewGeometry.setText("Geometry Inspector...");
    menuViewGeometry.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.actionInspectGeometry();
        }
      });
    menuLoadXmlTestFile.setText("Open XML File(s)...");
    menuLoadXmlTestFile.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.menuLoadXmlTestFile_actionPerformed(e);
        }
      });
    saveAsXmlMenuItem.setText("Save As XML...");
    saveAsXmlMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.menuSaveAsXml_actionPerformed(e);
        }
      });
    saveAsHtmlMenuItem.setText("Save As HTML...");
    saveAsHtmlMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.menuSaveAsHtml_actionPerformed(e);
        }
      });
    saveAsPNGMenuItem.setText("Save As PNG...");
    saveAsPNGMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.menuSaveAsPNG_actionPerformed(e);
        }
      });
    saveToClipboardMenuItem.setText("Save Screen To Clipboard");
    saveToClipboardMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.menuSaveScreenToClipboard_actionPerformed(e);
        }
      });
    deleteAllTestCasesMenuItem.setText("Delete All Test Cases");
    deleteAllTestCasesMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.deleteAllTestCasesMenuItem_actionPerformed(e);
        }
      });
    showVerticesMenuItem.setText("Vertices");
    showVerticesMenuItem.setSelected(true);
    showVerticesMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.setShowingVertices(showVerticesMenuItem.isSelected());
        }
      });
    final JCheckBoxMenuItem showLabelMenuItem = menuItem("Labels", true);
    showLabelMenuItem.addActionListener(  new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.setShowingLabel(showLabelMenuItem.isSelected());
        }
      });
    showGridMenuItem.setText("Grid");
    showGridMenuItem.setSelected(true);
    showGridMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.setShowingGrid(showGridMenuItem.isSelected());
        }
      });
    showStructureMenuItem.setText("Geometry Structure");
    showStructureMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.setShowingStructure(showStructureMenuItem.isSelected());
        }
      });
    showOrientationsMenuItem.setText("Orientations");
    showOrientationsMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.setShowingOrientations(showOrientationsMenuItem.isSelected());
        }
      });
    showVertexIndicesMenuItem.setText("Vertex Indices");
    showVertexIndicesMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.setShowingVertices(showVerticesMenuItem.isSelected());
        }
      });
    menuLoadXmlTestFolder.setText("Open XML Folder(s)...");
    menuLoadXmlTestFolder.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.menuLoadXmlTestFolder_actionPerformed(e);
        }
      });
    precisionModelMenuItem.setText("Precision Model...");
    precisionModelMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.precisionModelMenuItem_actionPerformed(e);
        }
      });
    removeDuplicatePoints.setText("Remove Duplicate Points");
    removeDuplicatePoints.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        tbFrame.menuRemoveDuplicatePoints_actionPerformed(e);
      }
    });
    changeToLines.setText("Change to Lines");
    changeToLines.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        tbFrame.menuChangeToLines_actionPerformed(e);
      }
    });
    generateExpectedValuesMenuItem.setText("Generate Expected Values");
    generateExpectedValuesMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //generateExpectedValuesMenuItem_actionPerformed(e);
      }
    });
    jMenuFile.setText("File");
    jMenuHelp.setText("Help");
    //jMenuOptions.setText("Options");
    //jMenuTools.setText("Tools");

    
    jMenuFile.add(menuLoadXmlTestFile);
    jMenuFile.add(menuLoadXmlTestFolder);
    jMenuFile.add(saveAsPNGMenuItem);
    jMenuFile.add(saveToClipboardMenuItem);
    jMenuFile.add(saveAsXmlMenuItem);
    jMenuFile.add(saveAsHtmlMenuItem);
    jMenuFile.add(generateExpectedValuesMenuItem);
    jMenuFile.addSeparator();
    jMenuFile.add(jMenuFileExit);
    
    jMenuHelp.add(jMenuAbout);
    
    jMenuView.add(showVerticesMenuItem);
    //jMenuOptions.add(showVertexIndicesMenuItem);
    jMenuView.add(showStructureMenuItem);
    jMenuView.add(showOrientationsMenuItem);
    jMenuView.add(showLabelMenuItem);
    jMenuView.addSeparator();
    jMenuView.add(showGridMenuItem);
    jMenuView.addSeparator();
    jMenuView.add(menuViewText);
    jMenuView.add(menuViewGeometry);
    
    jMenuEdit.add(deleteAllTestCasesMenuItem);
    jMenuEdit.add(menuExchangeGeom);
    jMenuEdit.addSeparator();
    jMenuEdit.add(precisionModelMenuItem);
    jMenuEdit.addSeparator();
    jMenuEdit.add(removeDuplicatePoints);
    jMenuEdit.add(changeToLines);
    
    jMenuBar1.add(jMenuFile);
    jMenuBar1.add(jMenuView);
    jMenuBar1.add(jMenuEdit);
    //jMenuBar1.add(jMenuOptions);
    //jMenuBar1.add(jMenuTools);
    jMenuBar1.add(jMenuHelp);

    return jMenuBar1;
  }
  
  JCheckBoxMenuItem menuItem(String name, boolean init) {
    JCheckBoxMenuItem item = new JCheckBoxMenuItem();
    item.setText("Labels");
    item.setSelected(init);
    return item;
  }
}
