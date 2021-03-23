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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

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
  JMenuItem menuViewText = new JMenuItem();
  JMenuItem menuViewGeometry = new JMenuItem();
  JMenuItem menuLoadXmlTestFile = new JMenuItem();
  JMenuItem saveAsXmlMenuItem = new JMenuItem();
  JMenuItem saveAsHtmlMenuItem = new JMenuItem();
  JMenuItem saveAsPNGMenuItem = new JMenuItem();
  JMenuItem saveToClipboardMenuItem = new JMenuItem();
  JMenuItem deleteAllTestCasesMenuItem = new JMenuItem();
  JMenuItem menuLoadXmlTestFolder = new JMenuItem();
  JMenuItem precisionModelMenuItem = new JMenuItem();
  JMenuItem removeDuplicatePoints = new JMenuItem();
  JMenuItem changeToLines = new JMenuItem();

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
          TestBuilderDialogs.showAbout(tbFrame);
        }
      });

    jMenuFileExit.setText("Exit");
    jMenuFileExit.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.actionExit();
        }
      });
    menuViewText.setText("Test Case Text...");
    menuViewText.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          TestBuilderDialogs.viewCaseText(tbFrame);
        }
      });
    menuViewGeometry.setText("Geometry Inspector...");
    menuViewGeometry.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JTSTestBuilder.controller().inspectGeometryDialogForCurrentCase();
        }
      });
    JMenuItem menuShowIndicators = menuItemCheck("ShowIndicators",
      JTSTestBuilderFrame.isShowingIndicators,
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JTSTestBuilderFrame.isShowingIndicators = ! JTSTestBuilderFrame.isShowingIndicators;
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
          TestBuilderDialogs.saveAsXML(tbFrame, JTSTestBuilder.model());
        }
      });
    saveAsHtmlMenuItem.setText("Save As HTML...");
    saveAsHtmlMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          TestBuilderDialogs.saveAsHtml(tbFrame, JTSTestBuilder.model());
        }
      });
    JMenuItem saveAsSvgMenuItem = new JMenuItem();
    saveAsSvgMenuItem.setText("Save As HTML+SVG...");
    saveAsSvgMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          TestBuilderDialogs.saveAsHtmlSVG(tbFrame, JTSTestBuilder.model());
        }
      });
    saveAsPNGMenuItem.setText("Save As PNG...");
    saveAsPNGMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JTSTestBuilder.controller().saveImageAsPNG();
        }
      });
    saveToClipboardMenuItem.setText("Save Screen To Clipboard");
    saveToClipboardMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JTSTestBuilder.controller().saveImageToClipboard();
        }
      });
    deleteAllTestCasesMenuItem.setText("Delete All Test Cases");
    deleteAllTestCasesMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.actionDeleteAllTestCases();
        }
      });

    menuLoadXmlTestFolder.setText("Open XML Folder(s)...");
    menuLoadXmlTestFolder.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tbFrame.actionLoadXmlTestFolder();
        }
      });
    precisionModelMenuItem.setText("Precision Model...");
    precisionModelMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          TestBuilderDialogs.precisionModel(tbFrame);
        }
      });
    removeDuplicatePoints.setText("Remove Duplicate Points");
    removeDuplicatePoints.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JTSTestBuilder.controller().removeDuplicatePoints();
      }
    });
    changeToLines.setText("Change to Lines");
    changeToLines.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JTSTestBuilder.controller().changeToLines();
      }
    });
    jMenuFile.setText("File");
    //jMenuOptions.setText("Options");
    //jMenuTools.setText("Tools");

    jMenuFile.add(menuLoadXmlTestFile);
    jMenuFile.add(menuLoadXmlTestFolder);
    jMenuFile.add(saveAsXmlMenuItem);
    jMenuFile.add(saveAsSvgMenuItem);
    //-----------------------
    jMenuFile.addSeparator();
    jMenuFile.add(saveAsPNGMenuItem);
    jMenuFile.add(saveToClipboardMenuItem);
    jMenuFile.addSeparator();
    jMenuFile.add(jMenuFileExit);
    //==========================
    
    jMenuHelp.setText("Help");
    jMenuHelp.add(jMenuAbout);
    //==========================
    jMenuView.setText("View");

    jMenuView.add(menuViewText);
    jMenuView.add(menuViewGeometry);
    //-----------------------
    jMenuEdit.addSeparator();
    jMenuView.add(menuShowIndicators);
    
    //==========================    
    jMenuEdit.setText("Edit");
    jMenuEdit.add(deleteAllTestCasesMenuItem);
    jMenuEdit.add(precisionModelMenuItem);
    //-----------------------
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

  JMenuItem menuItemCheck(String name, boolean init) {
    return createMenuItemSelectable(new JCheckBoxMenuItem(), name, init, null);
  }
  JMenuItem menuItemCheck(String name, boolean init, ActionListener listener) {
    return createMenuItemSelectable(new JCheckBoxMenuItem(), name, init, listener);
  }
  JMenuItem menuItemRadio(String name, boolean init) {
    return createMenuItemSelectable(new JRadioButtonMenuItem(), name, init, null);
  }
  JMenuItem menuItemRadio(String name, boolean init, ActionListener listener) {
    return createMenuItemSelectable(new JRadioButtonMenuItem(), name, init, listener);
  }
  
  JMenuItem createMenuItemSelectable(JMenuItem item, String name, boolean init, ActionListener listener) {
    item.setText(name);
    item.setSelected(init);
    if (listener != null) {
      item.addActionListener(listener);
    }
    return item;
  }
}
