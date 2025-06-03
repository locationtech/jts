/*
 * Copyright (c) 2021 Martin Davis, and others.
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

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.util.Assert;
import org.locationtech.jtstest.testbuilder.io.HtmlSvgTestWriter;
import org.locationtech.jtstest.testbuilder.io.HtmlWriter;
import org.locationtech.jtstest.testbuilder.io.JavaTestWriter;
import org.locationtech.jtstest.testbuilder.io.XMLTestWriter;
import org.locationtech.jtstest.testbuilder.model.TestBuilderModel;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.testrunner.GuiUtil;
import org.locationtech.jtstest.util.FileUtil;

public class TestBuilderDialogs {

  private static JFileChooser directoryChooser = new JFileChooser();;
  private static JFileChooser fileChooser = new JFileChooser();;
  
  public static void saveAsXML(JTSTestBuilderFrame tbFrame, TestBuilderModel tbModel) {
    try {
      fileChooser.removeChoosableFileFilter(SwingUtil.JAVA_FILE_FILTER);
      fileChooser.addChoosableFileFilter(SwingUtil.XML_FILE_FILTER);
      fileChooser.setDialogTitle("Save XML Test File");
      if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(tbFrame)) {
        File file = fileChooser.getSelectedFile();
        if (! SwingUtil.confirmOverwrite(tbFrame, file)) return;
        FileUtil.setContents(fileChooser.getSelectedFile().getPath(), 
            XMLTestWriter.getRunXml(tbModel.getTestCaseList(), tbModel.getPrecisionModel()) );
      }
    }
    catch (Exception x) {
      SwingUtil.reportException(tbFrame, x);
    }
  }

  private static JFileChooser htmlFileChooser = null;

  private static String chooseSVGFile(JTSTestBuilderFrame tbFrame) {
    if (htmlFileChooser == null) {
      htmlFileChooser = new JFileChooser();
      htmlFileChooser.addChoosableFileFilter(SwingUtil.HTML_FILE_FILTER);
      htmlFileChooser.setDialogTitle("Save HTML-SVG Test File");
      htmlFileChooser.setSelectedFile(new File("geoms.html"));
    }
    if (JFileChooser.APPROVE_OPTION != htmlFileChooser.showSaveDialog(tbFrame)) 
      return null;
    File file = htmlFileChooser.getSelectedFile();
    if (! SwingUtil.confirmOverwrite(tbFrame, file)) 
      return null;
    return htmlFileChooser.getSelectedFile().getPath();
  }
  
  public static void saveAsHtmlSVG(JTSTestBuilderFrame tbFrame, TestBuilderModel tbModel) {
    try {
      String path = chooseSVGFile(tbFrame);
      if (path == null) return;
      FileUtil.setContents(path, 
          HtmlSvgTestWriter.writeTestSVG(tbModel.getTestCaseList()) );
    }
    catch (Exception x) {
      SwingUtil.reportException(tbFrame, x);
    }
  }

  public static void saveAsHtml(JTSTestBuilderFrame tbFrame, TestBuilderModel tbModel) {
    try {
      directoryChooser.setDialogTitle("Select Folder In Which To Save HTML and GIF Files");
      if (JFileChooser.APPROVE_OPTION == directoryChooser.showSaveDialog(tbFrame)) {
        int choice = JOptionPane.showConfirmDialog(tbFrame,
            "Would you like the spatial function images "
             + "to show the A and B geometries?", "Confirmation",
            JOptionPane.YES_NO_CANCEL_OPTION);
        final HtmlWriter writer = new HtmlWriter();
        switch (choice) {
          case JOptionPane.CANCEL_OPTION:
            return;
          case JOptionPane.YES_OPTION:
            writer.setShowingABwithSpatialFunction(true);
            break;
          case JOptionPane.NO_OPTION:
            writer.setShowingABwithSpatialFunction(false);
            break;
        }
        final File directory = directoryChooser.getSelectedFile();
        Assert.isTrue(directory.exists());
        //        BusyDialog.setOwner(this);
        //        BusyDialog busyDialog = new BusyDialog();
        //        writer.setBusyDialog(busyDialog);
        //        try {
        //          busyDialog.execute("Saving .html and .gif files", new BusyDialog.Executable() {
        //            public void execute() throws Exception {
        writer.write(directory, tbModel.getTestCaseList(), tbModel.getPrecisionModel());
        //            }
        //          });
        //        }
        //        catch (Exception e2) {
        //          System.out.println(busyDialog.getStackTrace());
        //          throw e2;
        //        }
      }
    }
    catch (Exception x) {
      SwingUtil.reportException(tbFrame, x);
    }
  }
  
  public static void saveAsJava(JTSTestBuilderFrame tbFrame, TestBuilderModel tbModel) {
    try {
      fileChooser.removeChoosableFileFilter(SwingUtil.XML_FILE_FILTER);
      fileChooser.addChoosableFileFilter(SwingUtil.JAVA_FILE_FILTER);
      fileChooser.setDialogTitle("Save Java File");
      if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(tbFrame)) {
        File file = fileChooser.getSelectedFile();
        if (! SwingUtil.confirmOverwrite(tbFrame, file)) return;
        String className = fileChooser.getSelectedFile().getName();
        int extensionIndex = className.lastIndexOf(".");
        if (extensionIndex > 0) {
          className = className.substring(0, extensionIndex);
        }
        ;
        FileUtil.setContents(fileChooser.getSelectedFile().getPath(), JavaTestWriter.getRunJava(className, tbModel));
      }
    }
    catch (Exception x) {
      SwingUtil.reportException(tbFrame, x);
    }
  }
  
  public static void precisionModel(JTSTestBuilderFrame tbFrame) {
    try {
      PrecisionModelDialog precisionModelDialog = new PrecisionModelDialog(
          tbFrame, "Edit Precision Model", true);
      GuiUtil.center(precisionModelDialog, tbFrame);
      precisionModelDialog.setPrecisionModel(JTSTestBuilder.model().getPrecisionModel());
      precisionModelDialog.setVisible(true);
      
      JTSTestBuilder.model().changePrecisionModel(precisionModelDialog.getPrecisionModel());
      tbFrame.updatePrecisionModelDescription();
      tbFrame.geometryChanged();
    }
    catch (ParseException pe) {
      JTSTestBuilderFrame.reportException(pe);
    }
  }

  private static JFileChooser pngFileChooser;
  
  public static JFileChooser getSavePNGFileChooser() {
    if (pngFileChooser == null) {
      pngFileChooser = new JFileChooser();
      pngFileChooser.addChoosableFileFilter(SwingUtil.PNG_FILE_FILTER);
      pngFileChooser.setDialogTitle("Save PNG");
      pngFileChooser.setSelectedFile(new File("geoms.png"));
    }
    return pngFileChooser;
  }
  
  private static GeometryInspectorDialog geomInspectorDlg;

  public static void inspectGeometry(JTSTestBuilderFrame tbFrame, int geomIndex, Geometry geometry) {
    if (geomInspectorDlg == null) {
      geomInspectorDlg = new GeometryInspectorDialog(tbFrame);
    }
    geomInspectorDlg.setGeometry(geomIndex, geometry);
    geomInspectorDlg.setVisible(true);
  }
  
  public static void inspectGeometry(JTSTestBuilderFrame tbFrame, String tag, Geometry geometry) {
    if (geomInspectorDlg == null) {
      geomInspectorDlg = new GeometryInspectorDialog(tbFrame);
    }
    geomInspectorDlg.setGeometry(tag, geometry, 0, false);
    geomInspectorDlg.setVisible(true);
  }
  
  private static TestCaseTextDialog testCaseTextDlg;
  
  public static void viewCaseText(JTSTestBuilderFrame tbFrame) {
    if (testCaseTextDlg == null) {
      testCaseTextDlg = new TestCaseTextDialog(tbFrame,
          "", true);
    }
    testCaseTextDlg.setTestCase(JTSTestBuilder.model().getCurrentCase());
    testCaseTextDlg.setVisible(true);
  }
  /**
   *  Help | About action performed
   */
  public static void showAbout(JTSTestBuilderFrame tbFrame) {
    JTSTestBuilder_AboutBox dlg = new JTSTestBuilder_AboutBox(tbFrame);
    java.awt.Dimension dlgSize = dlg.getPreferredSize();
    java.awt.Dimension frmSize = tbFrame.getSize();
    java.awt.Point loc = tbFrame.getLocation();
    dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height
         - dlgSize.height) / 2 + loc.y);
    dlg.setModal(true);
    dlg.setVisible(true);
  }
}
