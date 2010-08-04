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

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.util.*;
import com.vividsolutions.jtstest.*;
import com.vividsolutions.jtstest.clean.*;
import com.vividsolutions.jtstest.test.*;
import com.vividsolutions.jtstest.testbuilder.controller.ResultController;
import com.vividsolutions.jtstest.testbuilder.geom.*;
import com.vividsolutions.jtstest.testbuilder.model.*;
import com.vividsolutions.jtstest.testbuilder.ui.tools.*;
import com.vividsolutions.jtstest.testbuilder.ui.dnd.*;
import com.vividsolutions.jtstest.testrunner.*;
import com.vividsolutions.jtstest.util.*;

import java.awt.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

import com.vividsolutions.jtstest.testbuilder.ui.*;

/**
 * The main frame for the JTS Test Builder.
 * 
 * @version 1.7
 */
public class JTSTestBuilderFrame extends JFrame 
{
  private static JTSTestBuilderFrame singleton = null;
  private ResultController resultController = new ResultController(this);
  private JTSTestBuilderMenuBar tbMenuBar = new JTSTestBuilderMenuBar(this);
  private JTSTestBuilderToolBar tbToolBar = new JTSTestBuilderToolBar(this);
  //---------------------------------------------
  JPanel contentPane;
  BorderLayout borderLayout1 = new BorderLayout();
  Border border4;
  JSplitPane jSplitPane1 = new JSplitPane();
  JPanel jPanel1 = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();
  TestCasePanel testCasePanel = new TestCasePanel();
  JPanel jPanel2 = new JPanel();
  JTabbedPane inputTabbedPane = new JTabbedPane();
  BorderLayout borderLayout3 = new BorderLayout();
  JPanel testPanel = new JPanel();
  WKTPanel wktPanel = new WKTPanel();
  TestListPanel testListPanel = new TestListPanel(this);
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  GridLayout gridLayout1 = new GridLayout();
  ResultWKTPanel resultWKTPanel = new ResultWKTPanel();
  ResultValuePanel resultValuePanel = new ResultValuePanel();
  StatsPanel statsPanel = new StatsPanel();
  InfoPanel infoPanel = new InfoPanel();
  private ZoomToClickTool zoomInTool;
  private final ImageIcon appIcon = new ImageIcon(this.getClass().getResource("app-icon.gif"));

  private JFileChooser fileChooser = new JFileChooser();
  private JFileChooser fileAndDirectoryChooser = new JFileChooser();
  private JFileChooser directoryChooser = new JFileChooser();
  
  TestBuilderModel tbModel;
  
  private FileFilter xmlFileFilter =
    new FileFilter() {

      public String getDescription() {
        return "JTS Test XML File (*.xml)";
      }

      public boolean accept(File f) {
        return f.isDirectory() || f.toString().toLowerCase().endsWith(".xml");
      }
    };
  private FileFilter javaFileFilter =
    new FileFilter() {

      public String getDescription() {
        return "Java File (*.java)";
      }

      public boolean accept(File f) {
        return f.isDirectory() || f.toString().toLowerCase().endsWith(".java");
      }
    };
  private TextViewDialog textViewDlg = new TextViewDialog(this, "", true);
  private TestCaseTextDialog testCaseTextDlg = new TestCaseTextDialog(this,
      "", true);
  /*
  private LoadTestCasesDialog loadTestCasesDlg = new LoadTestCasesDialog(this,
      "Load Test Cases", true);
*/
  
  private ArrayList wktABeforePMChange = new ArrayList();
  private ArrayList wktBBeforePMChange = new ArrayList();
  
  /**
   *  Construct the frame
   */
  public JTSTestBuilderFrame() {
    try {
      Assert.isTrue(singleton == null);
      singleton = this;
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      setIconImage(appIcon.getImage());
      jbInit();
      //#setRollover was introduced in Java 1.4 and is not present in 1.3.1. [Jon Aquino]
      //jToolBar1.setRollover(true);
 //     initList(tcList);
      //loadEditList(testpp);
//      testCasePanel.setModel(tbModel);
      testCasePanel.editCtlPanel.setGridSizeButton.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setGridSizeButton_actionPerformed(e);
          }
        });
      testCasePanel.spatialFunctionPanel.addSpatialFunctionPanelListener(
          new SpatialFunctionPanelListener() {
            public void functionExecuted(SpatialFunctionPanelEvent e) {
            	resultController.spatialFunctionPanel_functionExecuted(e);
            }
          });
      testCasePanel.scalarFunctionPanel.addSpatialFunctionPanelListener(
          new SpatialFunctionPanelListener() {
            public void functionExecuted(SpatialFunctionPanelEvent e) {
            	resultController.scalarFunctionPanel_functionChanged(e);
            }
          });
      testCasePanel.editCtlPanel.btnSetPrecisionModel.addActionListener(
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              precisionModelMenuItem_actionPerformed(e);
            }
          });
      testCasePanel.editCtlPanel.cbRevealTopo.addActionListener(
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	revealTopo_actionPerformed(e);
            }
          });
      Cursor zoomInCursor = Toolkit.getDefaultToolkit().createCustomCursor(
      		new ImageIcon(this.getClass().getResource("MagnifyCursor.gif")).getImage(),
          new java.awt.Point(16, 16), "Zoom In");
      zoomInTool = new ZoomToClickTool(2, zoomInCursor);
      showGeomsTab();
      initFileDrop(testCasePanel);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void initFileDrop(Component comp) {
    new FileDrop(comp, new FileDrop.Listener() {
      public void filesDropped(java.io.File[] files) {
        try {
          openXmlFilesAndDirectories(files);
        } catch (Exception ex) {
          reportException(null, ex);
        }
      }
    });
  }
  
  public static JTSTestBuilderFrame instance() {
    if (singleton == null) {
      new JTSTestBuilderFrame();
    }
    return singleton;
  }

  public TestBuilderModel getModel()
  {
    return tbModel;
  }
  
  public void setModel(TestBuilderModel model)
  {
  	tbModel = model;
    testCasePanel.setModel(tbModel);
    wktPanel.setModel(model);
    resultWKTPanel.setModel(model);
    resultValuePanel.setModel(model);
    statsPanel.setModel(model);
    
    model.getGeometryEditModel().addGeometryListener(
        new com.vividsolutions.jtstest.testbuilder.model.GeometryListener() {
          public void geometryChanged(GeometryEvent e) {
            model_geometryChanged(e);
          }
        });
    
    testListPanel.populateList();
    updateTestCaseView();
    updatePrecisionModelDescription();
  }
  
  public static void reportException(Component c, Exception e) {
    JOptionPane.showMessageDialog(c, StringUtil.split(e.toString(), 80), "Exception",
        JOptionPane.ERROR_MESSAGE);
    e.printStackTrace(System.out);
  }

  public static void reportException(Exception e) {
  	reportException(instance(), e);
  }

  public void setCurrentTestCase(TestCaseEdit testCase) {
    tbModel.setCurrentTestCase(testCase);
    updateTestCaseView();
  }

  public TestCasePanel getTestCasePanel() {
    return testCasePanel;
  }

  public ResultWKTPanel getResultWKTPanel() {
    return resultWKTPanel;
  }

  public ResultValuePanel getResultValuePanel() {
    return resultValuePanel;
  }

  public String getTestJava(TestCaseList tcList) {
    StringBuffer java = new StringBuffer();
    for (int i = 0; i < tcList.getList().size(); i++) {
      java.append((new JavaTestWriter()).write((Testable) tcList.getList().get(i)));
    }
    return java.toString();
  }

  /*
  public String getRunXml() {
    String runXML = "<run>" + StringUtil.newLine;
    runXML += getRunDescription(tcList);
    runXML += getRunWorkspace(tcList);
    runXML += xml(tbModel.getPrecisionModel()) + StringUtil.newLine;
    runXML += (new XMLTestWriter()).getTestXML(tcList) + "</run>";
    return runXML;
  }
*/
  
  /**
   *  File | Exit action performed
   */
  public void jMenuFileExit_actionPerformed(ActionEvent e) {
    System.exit(0);
  }

  /**
   *  Help | About action performed
   */
  public void jMenuHelpAbout_actionPerformed(ActionEvent e) {
    JTSTestBuilder_AboutBox dlg = new JTSTestBuilder_AboutBox(this);
    java.awt.Dimension dlgSize = dlg.getPreferredSize();
    java.awt.Dimension frmSize = getSize();
    java.awt.Point loc = getLocation();
    dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height
         - dlgSize.height) / 2 + loc.y);
    dlg.setModal(true);
    dlg.show();
  }

  public void showGeomsTab()
  {
    inputTabbedPane.setSelectedIndex(inputTabbedPane.indexOfTab("Input"));
  }
  
  public void showResultWKTTab()
  {
    inputTabbedPane.setSelectedIndex(inputTabbedPane.indexOfTab("Result"));
  }
  public void showResultValueTab()
  {
    inputTabbedPane.setSelectedIndex(inputTabbedPane.indexOfTab("Value"));
  }
  public void showInfoTab()
  {
    inputTabbedPane.setSelectedIndex(inputTabbedPane.indexOfTab("Info"));
  }
  
  public void openXmlFilesAndDirectories(File[] files) throws Exception {
    if (files.length == 1) {
      fileChooser.setSelectedFile(files[0]);
    }
    tbModel.openXmlFilesAndDirectories(files);
    reportProblemsParsingXmlTestFile(tbModel.getParsingProblems());
    updateTestCaseView();
    testListPanel.populateList();
    updatePrecisionModelDescription();
  }

  /**
   *  Overridden so we can exit when window is closed
   */
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      jMenuFileExit_actionPerformed(null);
    }
  }

  void setGridSizeButton_actionPerformed(ActionEvent e) {
    try {
      testCasePanel.editPanel.setGridSize(Double.parseDouble(testCasePanel.editCtlPanel.txtGridSize.getText()));
    }
    catch (Exception x) {
      JTSTestBuilderFrame.reportException(this, x);
    }
  }

  void model_geometryChanged(GeometryEvent e) {
    //testCasePanel.relatePanel.clearResults();
    testCasePanel.editPanel.updateView();
    updateWktPanel();
//    updateTestableGeometries();
  }

  void btnNewCase_actionPerformed(ActionEvent e) {
    tbModel.createNew();
    showGeomsTab();
    updateTestCaseView();
    testListPanel.populateList();
  }

  void btnPrevCase_actionPerformed(ActionEvent e) {
    tbModel.prevCase();
    updateTestCaseView();
  }

  void btnNextCase_actionPerformed(ActionEvent e) {
    tbModel.nextCase();
     updateTestCaseView();
  }

  void btnCopyCase_actionPerformed(ActionEvent e) {
    tbModel.copyCase();
    updateTestCaseView();
    testListPanel.populateList();
  }

  public void copyResultToTest() 
  {
    Object currResult = tbModel.getResult();
    if (! (currResult instanceof Geometry))
      return;
    tbModel.addCase(new Geometry[] { (Geometry) currResult, null }, 
    		"Result of " + tbModel.getOpName());
    updateTestCaseView();
    testListPanel.populateList();  
  }
  
  void btnExchangeGeoms_actionPerformed(ActionEvent e) {
    tbModel.getCurrentTestCaseEdit().exchange();
    testCasePanel.setTestCase(tbModel.getCurrentTestCaseEdit());
  }

  void btnDeleteCase_actionPerformed(ActionEvent e) {
    tbModel.deleteCase();
    updateTestCaseView();
    testListPanel.populateList();
  }

  /*
  void menuLoadTestCases_actionPerformed(ActionEvent e) {
    try {
      loadTestCasesDlg.show();
      TestCaseList tcl = loadTestCasesDlg.getList();
      loadTestCaseList(tcl, new PrecisionModel());
      refreshNavBar();
    }
    catch (Exception x) {
      reportException(this, x);
    }
  }

  void loadTestCaseList(TestCaseList tcl, PrecisionModel precisionModel) throws Exception {
    tbModel.setPrecisionModel(precisionModel);
    if (tcl != null) {
      loadEditList(tcl);
    }
    testListPanel.populateList();
  }
*/
  
  void menuExchangeGeom_actionPerformed(ActionEvent e) {
    tbModel.getCurrentTestCaseEdit().exchange();
    testCasePanel.setTestCase(tbModel.getCurrentTestCaseEdit());
  }

  void menuTestText_actionPerformed(ActionEvent e) {
    testCaseTextDlg.setTestCase(tbModel.getCurrentTestCaseEdit());
    testCaseTextDlg.show();
  }

  void menuLoadXmlTestFile_actionPerformed(ActionEvent e) {
    try {
      fileChooser.removeChoosableFileFilter(javaFileFilter);
      fileChooser.addChoosableFileFilter(xmlFileFilter);
      fileChooser.setDialogTitle("Open XML Test File(s)");
      fileChooser.setMultiSelectionEnabled(true);
      if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(this)) {
        File[] files = fileChooser.getSelectedFiles();
        if (files.length == 0) {
          files = new File[]{fileChooser.getSelectedFile()};
        }
        openXmlFilesAndDirectories(files);
      }
    }
    catch (Exception x) {
      reportException(this, x);
    }
  }

  void menuSaveAsXml_actionPerformed(ActionEvent e) {
    try {
      fileChooser.removeChoosableFileFilter(javaFileFilter);
      fileChooser.addChoosableFileFilter(xmlFileFilter);
      fileChooser.setDialogTitle("Save XML Test File");
      if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
        File file = fileChooser.getSelectedFile();
        if (file.exists()) {
          int decision = JOptionPane.showConfirmDialog(this, file.getName()
               + " exists. Overwrite?", "Confirmation", JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE);
          if (decision == JOptionPane.NO_OPTION) {
            return;
          }
        }
        FileUtil.setContents(fileChooser.getSelectedFile().getPath(), 
        		XMLTestWriter.getRunXml(tbModel.getTestCaseList(), tbModel.getPrecisionModel()) );
      }
    }
    catch (Exception x) {
      reportException(this, x);
    }
  }

  public String getRunXml() 
  {
  	return XMLTestWriter.getRunXml(tbModel.getTestCaseList(), tbModel.getPrecisionModel());
  }
  
  void menuSaveAsJava_actionPerformed(ActionEvent e) {
    try {
      fileChooser.removeChoosableFileFilter(xmlFileFilter);
      fileChooser.addChoosableFileFilter(javaFileFilter);
      fileChooser.setDialogTitle("Save Java File");
      if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
        File file = fileChooser.getSelectedFile();
        if (file.exists()) {
          int decision = JOptionPane.showConfirmDialog(this, file.getName()
               + " exists. Overwrite?", "Confirmation", JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE);
          if (decision == JOptionPane.NO_OPTION) {
            return;
          }
        }
        String className = fileChooser.getSelectedFile().getName();
        int extensionIndex = className.lastIndexOf(".");
        if (extensionIndex > 0) {
          className = className.substring(0, extensionIndex);
        }
        ;
        FileUtil.setContents(fileChooser.getSelectedFile().getPath(), getRunJava(className));
      }
    }
    catch (Exception x) {
      reportException(this, x);
    }
  }

  void menuSaveAsHtml_actionPerformed(ActionEvent e) {
    try {
      directoryChooser.setDialogTitle("Select Folder In Which To Save HTML and GIF Files");
      if (JFileChooser.APPROVE_OPTION == directoryChooser.showSaveDialog(this)) {
        int choice = JOptionPane.showConfirmDialog(this,
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
      reportException(this, x);
    }
  }

  void menuSaveAsPNG_actionPerformed(ActionEvent e) {
    try {
      directoryChooser.setDialogTitle("Select Folder In Which To Save PNG");
      if (JFileChooser.APPROVE_OPTION == directoryChooser.showSaveDialog(this)) {
        /*
        final PNGWriter writer = new PNGWriter();
        final File directory = directoryChooser.getSelectedFile();
        Assert.isTrue(directory.exists());
        writer.write(directory, tbModel.getCurrentTestCaseEdit(), tbModel.getPrecisionModel());
        */
        final File directory = directoryChooser.getSelectedFile();
        String filenameWithPath = directory.getPath() + "\\" + "geoms";
        ImageUtil.writeImage(testCasePanel.getGeometryEditPanel(), 
        		filenameWithPath + ".png",
        		ImageUtil.IMAGE_FORMAT_NAME_PNG);
        
        //saveImageToClipboard(testCasePanel.getGeometryEditPanel(), filenameWithPath);
      }
    }
    catch (Exception x) {
      reportException(this, x);
    }
  }

  void menuSaveScreenToClipboard_actionPerformed(ActionEvent e) {
    try {
        ImageUtil.saveImageToClipboard(testCasePanel.getGeometryEditPanel(), 
        		ImageUtil.IMAGE_FORMAT_NAME_PNG);
    }
    catch (Exception x) {
      reportException(this, x);
    }
  }

  void drawRectangleButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(RectangleTool.getInstance());
  }

  void drawPolygonButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(PolygonTool.getInstance());
  }

  void drawLineStringButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(LineStringTool.getInstance());
  }

  void drawPointButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(PointTool.getInstance());
  }

  void infoButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(InfoTool.getInstance());
  }

  void zoomInButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(zoomInTool);
  }

  void oneToOneButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().getViewport().zoomToInitialExtent();
  }

  void zoomToFullExtentButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().zoomToFullExtent();
  }

  void zoomToInputButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().zoomToInput();
  }

  void panButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(PanTool.getInstance());
  }

  void deleteAllTestCasesMenuItem_actionPerformed(ActionEvent e) {
    tbModel.initTestCaseList();
    updateTestCaseView();
    testListPanel.populateList();
  }

  void showVerticesMenuItem_actionPerformed(ActionEvent e) {
    setShowingVertices(tbMenuBar.showVerticesMenuItem.isSelected());
  }

  void showOrientationsMenuItem_actionPerformed(ActionEvent e) {
    setShowingOrientations(tbMenuBar.showOrientationsMenuItem.isSelected());
  }

  void showGridMenuItem_actionPerformed(ActionEvent e) {
    testCasePanel.editPanel.setGridEnabled(tbMenuBar.showGridMenuItem.isSelected());
    testCasePanel.editPanel.updateView();  }

  public void setShowingOrientations(boolean showingOrientations) {
    TestBuilderModel.setShowingOrientations(showingOrientations);
    testCasePanel.editPanel.updateView();
  }

  public void setShowVertexIndices(boolean showVertexIndices) {
    TestBuilderModel.setShowingOrientations(showVertexIndices);
    testCasePanel.editPanel.updateView();
  }

  public void setShowingVertices(boolean showingVertices) {
    TestBuilderModel.setShowingVertices(showingVertices);
    testCasePanel.editPanel.updateView();
  }

  void showVertexIndicesMenuItem_actionPerformed(ActionEvent e) {
//    testCasePanel.editPanel.setShowVertexIndices(showVertexIndicesMenuItem.isSelected());
  }

  void menuLoadXmlTestFolder_actionPerformed(ActionEvent e) {
    try {
      directoryChooser.removeChoosableFileFilter(javaFileFilter);
      directoryChooser.setDialogTitle("Open Folder(s) Containing XML Test Files");
      directoryChooser.setMultiSelectionEnabled(true);
      if (JFileChooser.APPROVE_OPTION == directoryChooser.showOpenDialog(this)) {
        File[] files = directoryChooser.getSelectedFiles();
        if (files.length == 0) {
          files = new File[]{fileChooser.getSelectedFile()};
        }
        openXmlFilesAndDirectories(files);
      }
    }
    catch (Exception x) {
      reportException(this, x);
    }
  }

  void precisionModelMenuItem_actionPerformed(ActionEvent e) {
    try {
      PrecisionModelDialog precisionModelDialog = new PrecisionModelDialog(
          this, "Edit Precision Model", true);
      GuiUtil.center(precisionModelDialog, this);
      precisionModelDialog.setPrecisionModel(tbModel.getPrecisionModel());
      precisionModelDialog.setVisible(true);
      saveWKTBeforePMChange();
      tbModel.setPrecisionModel(precisionModelDialog.getPrecisionModel());
      loadWKTAfterPMChange();
      updatePrecisionModelDescription();
      updateGeometry();
    }
    catch (ParseException pe) {
      reportException(this, pe);
    }
  }
  void revealTopo_actionPerformed(ActionEvent e) {
  	tbModel.setRevealingTopology(testCasePanel.editCtlPanel.cbRevealTopo.isSelected());
  	testCasePanel.editPanel.updateView();
  }

    private String getRunJava(String className) {
    return
        "package com.vividsolutions.jtstest.testsuite;" + StringUtil.newLine
         + "" + StringUtil.newLine
         + "import com.vividsolutions.jtstest.test.*;" + StringUtil.newLine
         + "" + StringUtil.newLine
         + "public class " + className + " extends TestCaseList {" + StringUtil.newLine
         + "  public static void main(String[] args) {" + StringUtil.newLine
         + "    " + className + " test = new " + className + "();" + StringUtil.newLine
         + "    test.run();" + StringUtil.newLine
         + "  }" + StringUtil.newLine
         + "" + StringUtil.newLine
         + "  public " + className + "() {" + StringUtil.newLine
         + getTestJava(tbModel.getTestCaseList())
         + "  }" + StringUtil.newLine
         + "}";
  }


  /**
   *  Component initialization
   */
  private void jbInit() throws Exception {
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);
    fileAndDirectoryChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    fileAndDirectoryChooser.setMultiSelectionEnabled(true);
    directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    directoryChooser.setMultiSelectionEnabled(false);
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = textViewDlg.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    textViewDlg.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height
         - frameSize.height) / 2);
    /*
    loadTestCasesDlg.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height
         - frameSize.height) / 2);
         */
    
    //---------------------------------------------------
    contentPane = (JPanel) this.getContentPane();
    border4 = BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white,
        Color.white, new Color(93, 93, 93), new Color(134, 134, 134));
    contentPane.setLayout(borderLayout1);
    this.setSize(new Dimension(800, 800));
    this.setTitle("JTS TestBuilder");
    
    /*
    testCasePanel.editPanel.addGeometryListener(
      new com.vividsolutions.jtstest.testbuilder.model.GeometryListener() {

        public void geometryChanged(GeometryEvent e) {
          editPanel_geometryChanged(e);
        }
      });
*/    
    
    jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
    jSplitPane1.setPreferredSize(new Dimension(601, 690));
    jPanel1.setLayout(borderLayout2);
    jPanel1.setMinimumSize(new Dimension(431, 0));
    contentPane.setPreferredSize(new Dimension(601, 690));
    inputTabbedPane.setTabPlacement(JTabbedPane.LEFT);
    jPanel2.setLayout(borderLayout3);
    wktPanel.setMinimumSize(new Dimension(111, 0));
    wktPanel.setPreferredSize(new Dimension(600, 100));
    wktPanel.setToolTipText(AppStrings.TEXT_ENTRY_TIP);
    testPanel.setLayout(gridBagLayout2);
    gridLayout1.setRows(4);
    gridLayout1.setColumns(1);
    
    contentPane.add(jSplitPane1, BorderLayout.CENTER);
    jSplitPane1.add(jPanel1, JSplitPane.TOP);
    jPanel1.add(testCasePanel, BorderLayout.CENTER);
    jSplitPane1.add(jPanel2, JSplitPane.BOTTOM);
    jPanel2.add(inputTabbedPane, BorderLayout.CENTER);
    jSplitPane1.setBorder(new EmptyBorder(2,2,2,2));
    jSplitPane1.setResizeWeight(0.5);
    inputTabbedPane.add(testPanel, "Tests");
    testPanel.add(testListPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0,
        0, 0), 0, 0));
    inputTabbedPane.add(wktPanel,  "Input");
    inputTabbedPane.add(resultWKTPanel, "Result");
    inputTabbedPane.add(resultValuePanel, "Value");
    inputTabbedPane.add(statsPanel, "Stats");
    inputTabbedPane.add(infoPanel, "Info");
    inputTabbedPane.setSelectedIndex(1);
    inputTabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e)
      {
        updateStatsPanelIfVisible();
        }
    });
    
    jSplitPane1.setDividerLocation(500);
    this.setJMenuBar(tbMenuBar.getMenuBar());
    contentPane.add(tbToolBar.getToolBar(), BorderLayout.NORTH);
  }

  private void updateStatsPanelIfVisible()
  {
    if (inputTabbedPane.getComponent(inputTabbedPane.getSelectedIndex()) == statsPanel) {
      statsPanel.refresh();         
    }   
  }
  
  private void updateGeometry() {
    testCasePanel.relatePanel.clearResults();
    testCasePanel.setTestCase(tbModel.getCurrentTestCaseEdit());
    updateWktPanel();
  }

  private void updateWktPanel() {
    Geometry g0 = tbModel.getGeometryEditModel().getGeometry(0);
    wktPanel.setText(g0, 0);
    Geometry g1 = tbModel.getGeometryEditModel().getGeometry(1);
    wktPanel.setText(g1, 1);
  }

  private void updatePrecisionModelDescription() {
    testCasePanel.setPrecisionModelDescription(tbModel.getPrecisionModel().toString());
  }

  void updateTestCaseView() {
    testCasePanel.setTestCase(tbModel.getCurrentTestCaseEdit());
    getTestCasePanel().setCurrentTestCaseIndex(tbModel.getCurrentTestIndex() + 1);
    getTestCasePanel().setMaxTestCaseIndex(tbModel.getTestListSize());
    updateWktPanel();
    updateStatsPanelIfVisible();
  }

  public void displayInfo(Coordinate modelPt)
  {
    infoPanel.setInfo(
        testCasePanel.getGeometryEditPanel().getInfo(modelPt)
        );
    showInfoTab();
  }
  
  private void reportProblemsParsingXmlTestFile(List parsingProblems) {
    if (parsingProblems.isEmpty()) {
      return;
    }
    for (Iterator i = parsingProblems.iterator(); i.hasNext(); ) {
      String problem = (String) i.next();
      System.out.println(problem);
    }
    JOptionPane.showMessageDialog(this, StringUtil.split(parsingProblems.size()
         + " problems occurred parsing the XML test file."
         + " The first problem was: " + parsingProblems.get(0), 80),
        "Error", JOptionPane.ERROR_MESSAGE);
  }

  private void saveWKTBeforePMChange() {
    wktABeforePMChange.clear();
    wktBBeforePMChange.clear();
    for (Iterator i = tbModel.getTestCaseList().getList().iterator(); i.hasNext(); ) {
      Testable testable = (Testable) i.next();
      Geometry a = testable.getGeometry(0);
      Geometry b = testable.getGeometry(1);
      wktABeforePMChange.add(a != null ? a.toText() : null);
      wktBBeforePMChange.add(b != null ? b.toText() : null);
    }
  }

  private void loadWKTAfterPMChange() throws ParseException {
    WKTReader reader = new WKTReader(new GeometryFactory(tbModel.getPrecisionModel(), 0));
    for (int i = 0; i < tbModel.getTestCaseList().getList().size(); i++) {
      Testable testable = (Testable) tbModel.getTestCaseList().getList().get(i);
      String wktA = (String) wktABeforePMChange.get(i);
      String wktB = (String) wktBBeforePMChange.get(i);
      testable.setGeometry(0, wktA != null ? reader.read(wktA) : null);
      testable.setGeometry(1, wktB != null ? reader.read(wktB) : null);
    }
  }

  void menuRemoveDuplicatePoints_actionPerformed(ActionEvent e) {
    CleanDuplicatePoints clean = new CleanDuplicatePoints();
    Geometry cleanGeom = clean.clean(tbModel.getCurrentTestCaseEdit().getGeometry(0));
    tbModel.getCurrentTestCaseEdit().setGeometry(0, cleanGeom);
    updateGeometry();
  }

  void menuChangeToLines_actionPerformed(ActionEvent e) {
    LineStringExtracter lse = new LineStringExtracter();
    Geometry cleanGeom = lse.extract(tbModel.getCurrentTestCaseEdit().getGeometry(0));
    tbModel.getCurrentTestCaseEdit().setGeometry(0, cleanGeom);
    updateGeometry();
  }

  void btnEditVertex_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(EditVertexTool.getInstance());
  }

  private Coordinate pickOffset(Geometry a, Geometry b) {
    if (a != null && ! a.isEmpty()) {
      return a.getCoordinates()[0];
    }
    if (b != null && ! b.isEmpty()) {
      return b.getCoordinates()[0];
    }
    return null;
  }

}

