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
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

import com.vividsolutions.jtstest.testbuilder.ui.*;
import com.vividsolutions.jtstest.testbuilder.ui.SwingWorker;



/**
 * @version 1.7
 */
public class JTSTestBuilderFrame extends JFrame {
  private static JTSTestBuilderFrame singleton = null;
  private ResultController resultController = new ResultController(this);
  //---------------------------------------------
  JPanel contentPane;
  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu jMenuFile = new JMenu();
  JMenu jMenuHelp = new JMenu();
  JMenuItem jMenuHelpAbout = new JMenuItem();
  ImageIcon image1;
  ImageIcon image2;
  ImageIcon image3;
  BorderLayout borderLayout1 = new BorderLayout();
  Border border4;
  JMenu jMenu1 = new JMenu();
  JSplitPane jSplitPane1 = new JSplitPane();
  JPanel jPanel1 = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();
  TestCasePanel testCasePanel = new TestCasePanel();
  JMenuItem jMenuFileExit = new JMenuItem();
  JMenu jMenu2 = new JMenu();
  JMenuItem menuExchangeGeom = new JMenuItem();
  JMenuItem menuTestText = new JMenuItem();
  JMenuItem menuLoadXmlTestFile = new JMenuItem();
  JPanel jPanel2 = new JPanel();
  JTabbedPane inputTabbedPane = new JTabbedPane();
  BorderLayout borderLayout3 = new BorderLayout();
  JPanel testPanel = new JPanel();
  WKTPanel wktPanel = new WKTPanel();
  TestListPanel testListPanel = new TestListPanel(this);
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  GridLayout gridLayout1 = new GridLayout();
  JMenuItem saveAsXmlMenuItem = new JMenuItem();
  JToolBar jToolBar1 = new JToolBar();
  JButton previousButton = new JButton();
  JButton nextButton = new JButton();
  JButton newButton = new JButton();
  JButton copyButton = new JButton();
  JButton deleteButton = new JButton();
  JButton exchangeButton = new JButton();
  Component component1;
  Component component2;
  Component component5;
  ResultWKTPanel resultWKTPanel = new ResultWKTPanel();
  ResultValuePanel resultValuePanel = new ResultValuePanel();
  StatsPanel statsPanel = new StatsPanel();
  InfoPanel infoPanel = new InfoPanel();
  JMenuItem saveAsHtmlMenuItem = new JMenuItem();
  JMenuItem saveAsPNGMenuItem = new JMenuItem();
  JMenuItem saveToClipboardMenuItem = new JMenuItem();
  Component component3;
  JToggleButton drawPolygonButton = new JToggleButton();
  JToggleButton drawLineStringButton = new JToggleButton();
  JToggleButton drawPointButton = new JToggleButton();
  JToggleButton zoomInButton = new JToggleButton();
  JToggleButton infoButton = new JToggleButton();
  JButton oneToOneButton = new JButton();
  ButtonGroup buttonGroup = new ButtonGroup();
  JButton zoomToFullExtentButton = new JButton();
  JButton zoomToInputButton = new JButton();
  Component component4;
  JToggleButton panButton = new JToggleButton();
  JMenuItem deleteAllTestCasesMenuItem = new JMenuItem();
  JMenu jMenu3 = new JMenu();
  JCheckBoxMenuItem showVerticesMenuItem = new JCheckBoxMenuItem();
  JCheckBoxMenuItem showGridMenuItem = new JCheckBoxMenuItem();
  JCheckBoxMenuItem showOrientationsMenuItem = new JCheckBoxMenuItem();
  JCheckBoxMenuItem showVertexIndicesMenuItem = new JCheckBoxMenuItem();
  JMenuItem menuLoadXmlTestFolder = new JMenuItem();
  JMenuItem precisionModelMenuItem = new JMenuItem();
  private ZoomToClickTool zoomInTool;
  private final ImageIcon leftIcon = new ImageIcon(this.getClass().getResource("Left.gif"));
  private final ImageIcon rightIcon = new ImageIcon(this.getClass().getResource("Right.gif"));
  private final ImageIcon plusIcon = new ImageIcon(this.getClass().getResource("Plus.gif"));
  private final ImageIcon copyCaseIcon = new ImageIcon(this.getClass().getResource("CopyCase.gif"));
  private final ImageIcon deleteIcon = new ImageIcon(this.getClass().getResource("Delete.gif"));
  private final ImageIcon exchangeGeomsIcon = new ImageIcon(this.getClass().getResource("ExchangeGeoms.png"));
  private final ImageIcon executeIcon = new ImageIcon(this.getClass().getResource("ExecuteProject.gif"));
  private final ImageIcon zoomInIcon = new ImageIcon(this.getClass().getResource("MagnifyCursor.gif"));
  private final ImageIcon drawPolygonIcon = new ImageIcon(this.getClass().getResource("DrawPolygon.png"));
  private final ImageIcon drawLineStringIcon = new ImageIcon(this.getClass().getResource("DrawLineString.png"));
  private final ImageIcon drawPointIcon = new ImageIcon(this.getClass().getResource("DrawPoint.png"));
  private final ImageIcon infoIcon = new ImageIcon(this.getClass().getResource("Info.png"));
  private final ImageIcon zoomOneToOneIcon = new ImageIcon(this.getClass().getResource("ZoomOneToOne.png"));
  private final ImageIcon zoomToInputIcon = new ImageIcon(this.getClass().getResource("ZoomInput.png"));
  private final ImageIcon zoomToFullExtentIcon = new ImageIcon(this.getClass().getResource("ZoomAll.png"));
  private final ImageIcon selectIcon = new ImageIcon(this.getClass().getResource("Select.gif"));
  private final ImageIcon moveVertexIcon = new ImageIcon(this.getClass().getResource("MoveVertex.png"));
  private final ImageIcon panIcon = new ImageIcon(this.getClass().getResource("Hand.gif"));
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
  JMenu jMenu4 = new JMenu();
  JMenuItem removeDuplicatePoints = new JMenuItem();
  JMenuItem changeToLines = new JMenuItem();
  JToggleButton btnSelectPoint = new JToggleButton();
  private JMenuItem generateExpectedValuesMenuItem = new JMenuItem();
  
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
            public void functionChanged(SpatialFunctionPanelEvent e) {
            	resultController.spatialFunctionPanel_functionChanged(e);
            }
          });
      testCasePanel.scalarFunctionPanel.addSpatialFunctionPanelListener(
          new SpatialFunctionPanelListener() {
            public void functionChanged(SpatialFunctionPanelEvent e) {
            	resultController.scalarFunctionPanel_functionChanged(e);
            }
          });
      testCasePanel.editCtlPanel.btnSetPrecisionModel.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            precisionModelMenuItem_actionPerformed(e);
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
  
  public static JTSTestBuilderFrame getInstance() {
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
    refreshNavBar();
    updatePrecisionModelDescription();
  }
  
  public static void reportException(Component c, Exception e) {
    JOptionPane.showMessageDialog(c, StringUtil.split(e.toString(), 80), "Exception",
        JOptionPane.ERROR_MESSAGE);
    e.printStackTrace(System.out);
  }

  public static void reportException(Exception e) {
  	reportException(getInstance(), e);
  }

  public void setCurrentTestCase(TestCaseEdit testCase) {
    tbModel.setCurrentTestCase(testCase);
    refreshNavBar();
  }

  public TestCasePanel getTestCasePanel() {
    return testCasePanel;
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
    refreshNavBar();
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

  void wktPanel_actionPerformed(ActionEvent e) {
    try {
      loadGeometryText(false);
    }
    catch (Exception ex) {
      reportException(this, ex);
    }
  }

  void btnNewCase_actionPerformed(ActionEvent e) {
    tbModel.createNew();
    showGeomsTab();
    refreshNavBar();
    testListPanel.populateList();
  }

  void refreshNavBar() {
    testCasePanel.setTestCase(tbModel.getCurrentTestCaseEdit());
    getTestCasePanel().setCurrentTestCaseIndex(tbModel.getCurrentTestIndex() + 1);
    getTestCasePanel().setMaxTestCaseIndex(tbModel.getTestListSize());
    updateWktPanel();
  }

  void btnPrevCase_actionPerformed(ActionEvent e) {
    tbModel.prevCase();
    refreshNavBar();
  }

  void btnNextCase_actionPerformed(ActionEvent e) {
    tbModel.nextCase();
     refreshNavBar();
  }

  void btnCopyCase_actionPerformed(ActionEvent e) {
    tbModel.copyCase();
    refreshNavBar();
    testListPanel.populateList();
  }

  public void copyResultToTest() 
  {
    Object currResult = resultWKTPanel.getResult();
    if (! (currResult instanceof Geometry))
      return;
    tbModel.addCase(new Geometry[] { (Geometry) currResult, null });
    refreshNavBar();
    testListPanel.populateList();  
  }
  
  void btnExchangeGeoms_actionPerformed(ActionEvent e) {
    tbModel.getCurrentTestCaseEdit().exchange();
    testCasePanel.setTestCase(tbModel.getCurrentTestCaseEdit());
  }

  void btnDeleteCase_actionPerformed(ActionEvent e) {
    tbModel.deleteCase();
    refreshNavBar();
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
    refreshNavBar();
    testListPanel.populateList();
  }

  void showVerticesMenuItem_actionPerformed(ActionEvent e) {
    setShowingVertices(showVerticesMenuItem.isSelected());
  }

  void showOrientationsMenuItem_actionPerformed(ActionEvent e) {
    setShowingOrientations(showOrientationsMenuItem.isSelected());
  }

  void showGridMenuItem_actionPerformed(ActionEvent e) {
    testCasePanel.editPanel.setGridEnabled(showGridMenuItem.isSelected());
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

  void moveToOriginButton_actionPerformed(ActionEvent e) {
    try {
      loadGeometryText(true);
    }
    catch (Exception ex) {
      reportException(this, ex);
    }
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
    component1 = Box.createHorizontalStrut(8);
    component2 = Box.createHorizontalStrut(8);
    component3 = Box.createHorizontalStrut(8);
    component4 = Box.createHorizontalStrut(8);
    component5 = Box.createHorizontalStrut(8);
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
    jMenuFile.setText("File");
    jMenuHelp.setText("Help");
    jMenuHelpAbout.setText("About");
    jMenuHelpAbout.addActionListener(
      new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          jMenuHelpAbout_actionPerformed(e);
        }
      });
    jMenu1.setText("View");
    
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
    jMenuFileExit.setText("Exit");
    jMenuFileExit.addActionListener(
      new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          jMenuFileExit_actionPerformed(e);
        }
      });
    jMenu2.setText("Edit");
    menuExchangeGeom.setText("Exchange Geometries");
    menuExchangeGeom.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          menuExchangeGeom_actionPerformed(e);
        }
      });
    menuTestText.setText("Test Case Text...");
    menuTestText.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          menuTestText_actionPerformed(e);
        }
      });
    menuLoadXmlTestFile.setText("Open XML File(s)...");
    menuLoadXmlTestFile.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          menuLoadXmlTestFile_actionPerformed(e);
        }
      });
    inputTabbedPane.setTabPlacement(JTabbedPane.LEFT);
    jPanel2.setLayout(borderLayout3);
    wktPanel.setMinimumSize(new Dimension(111, 0));
    wktPanel.setPreferredSize(new Dimension(600, 100));
    wktPanel.setToolTipText(AppStrings.TEXT_ENTRY_TIP);
    previousButton.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          btnPrevCase_actionPerformed(e);
        }
      });
    nextButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          btnNextCase_actionPerformed(e);
        }
      });
    copyButton.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            btnCopyCase_actionPerformed(e);
          }
        });
    deleteButton.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            btnDeleteCase_actionPerformed(e);
          }
        });
    exchangeButton.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            btnExchangeGeoms_actionPerformed(e);
          }
        });
    newButton.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          btnNewCase_actionPerformed(e);
        }
      });
    testPanel.setLayout(gridBagLayout2);
    gridLayout1.setRows(4);
    gridLayout1.setColumns(1);
    saveAsXmlMenuItem.setText("Save As XML...");
    saveAsXmlMenuItem.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          menuSaveAsXml_actionPerformed(e);
        }
      });
    previousButton.setFont(new java.awt.Font("SansSerif", 0, 10));
    previousButton.setMaximumSize(new Dimension(30, 30));
    previousButton.setMinimumSize(new Dimension(30, 30));
    previousButton.setPreferredSize(new Dimension(30, 30));
    previousButton.setToolTipText("Previous Case");
    previousButton.setHorizontalTextPosition(SwingConstants.CENTER);
    previousButton.setIcon(leftIcon);
    previousButton.setMargin(new Insets(0, 0, 0, 0));
    previousButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    nextButton.setMargin(new Insets(0, 0, 0, 0));
    nextButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    nextButton.setFont(new java.awt.Font("SansSerif", 0, 10));
    nextButton.setMaximumSize(new Dimension(30, 30));
    nextButton.setMinimumSize(new Dimension(30, 30));
    nextButton.setPreferredSize(new Dimension(30, 30));
    nextButton.setToolTipText("Next Case");
    nextButton.setHorizontalTextPosition(SwingConstants.CENTER);
    nextButton.setIcon(rightIcon);
    newButton.setMargin(new Insets(0, 0, 0, 0));
    newButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    newButton.setFont(new java.awt.Font("SansSerif", 0, 10));
    newButton.setMaximumSize(new Dimension(30, 30));
    newButton.setMinimumSize(new Dimension(30, 30));
    newButton.setPreferredSize(new Dimension(30, 30));
    newButton.setToolTipText("New Case");
    newButton.setHorizontalTextPosition(SwingConstants.CENTER);
    newButton.setIcon(plusIcon);
    
    copyButton.setMargin(new Insets(0, 0, 0, 0));
    copyButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    copyButton.setFont(new java.awt.Font("SansSerif", 0, 10));
    copyButton.setMaximumSize(new Dimension(30, 30));
    copyButton.setMinimumSize(new Dimension(30, 30));
    copyButton.setPreferredSize(new Dimension(30, 30));
    copyButton.setToolTipText("Copy Current Case");
    copyButton.setHorizontalTextPosition(SwingConstants.CENTER);
    copyButton.setIcon(copyCaseIcon);
    
    deleteButton.setMargin(new Insets(0, 0, 0, 0));
    deleteButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    deleteButton.setFont(new java.awt.Font("SansSerif", 0, 10));
    deleteButton.setMaximumSize(new Dimension(30, 30));
    deleteButton.setMinimumSize(new Dimension(30, 30));
    deleteButton.setPreferredSize(new Dimension(30, 30));
    deleteButton.setToolTipText("Delete Current Case");
    deleteButton.setHorizontalTextPosition(SwingConstants.CENTER);
    deleteButton.setIcon(deleteIcon);
    
    exchangeButton.setMargin(new Insets(0, 0, 0, 0));
    exchangeButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    exchangeButton.setFont(new java.awt.Font("SansSerif", 0, 10));
    exchangeButton.setMaximumSize(new Dimension(30, 30));
    exchangeButton.setMinimumSize(new Dimension(30, 30));
    exchangeButton.setPreferredSize(new Dimension(30, 30));
    exchangeButton.setToolTipText("Exchange A & B");
    exchangeButton.setHorizontalTextPosition(SwingConstants.CENTER);
    exchangeButton.setIcon(exchangeGeomsIcon);
    
    saveAsHtmlMenuItem.setText("Save As HTML...");
    saveAsHtmlMenuItem.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          menuSaveAsHtml_actionPerformed(e);
        }
      });
    saveAsPNGMenuItem.setText("Save As PNG...");
    saveAsPNGMenuItem.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          menuSaveAsPNG_actionPerformed(e);
        }
      });
    saveToClipboardMenuItem.setText("Save Screen To Clipboard");
    saveToClipboardMenuItem.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
        	menuSaveScreenToClipboard_actionPerformed(e);
        }
      });
    drawPolygonButton.setMargin(new Insets(0, 0, 0, 0));
    drawPolygonButton.setPreferredSize(new Dimension(30, 30));
    drawPolygonButton.setIcon(drawPolygonIcon);
    drawPolygonButton.setMinimumSize(new Dimension(30, 30));
    drawPolygonButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    drawPolygonButton.setSelected(true);
    drawPolygonButton.setToolTipText("Draw Polyogn");
    drawPolygonButton.setHorizontalTextPosition(SwingConstants.CENTER);
    drawPolygonButton.setFont(new java.awt.Font("SansSerif", 0, 10));
    drawPolygonButton.setMaximumSize(new Dimension(30, 30));
    drawPolygonButton.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          drawPolygonButton_actionPerformed(e);
        }
      });
    drawLineStringButton.setMargin(new Insets(0, 0, 0, 0));
    drawLineStringButton.setPreferredSize(new Dimension(30, 30));
    drawLineStringButton.setIcon(drawLineStringIcon);
    drawLineStringButton.setMinimumSize(new Dimension(30, 30));
    drawLineStringButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    drawLineStringButton.setSelected(true);
    drawLineStringButton.setToolTipText("Draw LineString");
    drawLineStringButton.setHorizontalTextPosition(SwingConstants.CENTER);
    drawLineStringButton.setFont(new java.awt.Font("SansSerif", 0, 10));
    drawLineStringButton.setMaximumSize(new Dimension(30, 30));
    drawLineStringButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          drawLineStringButton_actionPerformed(e);
        }
      });
    drawPointButton.setMargin(new Insets(0, 0, 0, 0));
    drawPointButton.setPreferredSize(new Dimension(30, 30));
    drawPointButton.setIcon(drawPointIcon);
    drawPointButton.setMinimumSize(new Dimension(30, 30));
    drawPointButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    drawPointButton.setSelected(true);
    drawPointButton.setToolTipText("Draw Point");
    drawPointButton.setHorizontalTextPosition(SwingConstants.CENTER);
    drawPointButton.setFont(new java.awt.Font("SansSerif", 0, 10));
    drawPointButton.setMaximumSize(new Dimension(30, 30));
    drawPointButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          drawPointButton_actionPerformed(e);
        }
      });
    infoButton.setMargin(new Insets(0, 0, 0, 0));
    infoButton.setPreferredSize(new Dimension(30, 30));
    infoButton.setIcon(infoIcon);
    infoButton.setMinimumSize(new Dimension(30, 30));
    infoButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    infoButton.setSelected(false);
    infoButton.setToolTipText("Info");
    infoButton.setHorizontalTextPosition(SwingConstants.CENTER);
    infoButton.setFont(new java.awt.Font("SansSerif", 0, 10));
    infoButton.setMaximumSize(new Dimension(30, 30));
    infoButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          infoButton_actionPerformed(e);
        }
      });
    zoomInButton.setMaximumSize(new Dimension(30, 30));
    zoomInButton.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          zoomInButton_actionPerformed(e);
        }
      });
    zoomInButton.setToolTipText("<html>Zoom In/Out<br><br>In = Left-Btn<br>Out = Right-Btn</html>");
    zoomInButton.setHorizontalTextPosition(SwingConstants.CENTER);
    zoomInButton.setFont(new java.awt.Font("Serif", 0, 10));
    zoomInButton.setMinimumSize(new Dimension(30, 30));
    zoomInButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    zoomInButton.setPreferredSize(new Dimension(30, 30));
    zoomInButton.setIcon(zoomInIcon);
    zoomInButton.setMargin(new Insets(0, 0, 0, 0));
    
    oneToOneButton.setMargin(new Insets(0, 0, 0, 0));
    oneToOneButton.setIcon(zoomOneToOneIcon);
    oneToOneButton.setPreferredSize(new Dimension(30, 30));
    oneToOneButton.setMinimumSize(new Dimension(30, 30));
    oneToOneButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    oneToOneButton.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          oneToOneButton_actionPerformed(e);
        }
      });
    oneToOneButton.setFont(new java.awt.Font("SansSerif", 0, 10));
    oneToOneButton.setToolTipText("Zoom 1:1");
    oneToOneButton.setHorizontalTextPosition(SwingConstants.CENTER);
    oneToOneButton.setMaximumSize(new Dimension(30, 30));
    
    zoomToInputButton.setMargin(new Insets(0, 0, 0, 0));
    zoomToInputButton.setIcon(zoomToInputIcon);
    zoomToInputButton.setPreferredSize(new Dimension(30, 30));
    zoomToInputButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    zoomToInputButton.setMinimumSize(new Dimension(30, 30));
    zoomToInputButton.setFont(new java.awt.Font("SansSerif", 0, 10));
    zoomToInputButton.setHorizontalTextPosition(SwingConstants.CENTER);
    zoomToInputButton.setToolTipText("Zoom To Input");
    zoomToInputButton.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          zoomToInputButton_actionPerformed(e);
        }
      });
    zoomToInputButton.setMaximumSize(new Dimension(30, 30));
    
    zoomToFullExtentButton.setMargin(new Insets(0, 0, 0, 0));
    zoomToFullExtentButton.setIcon(zoomToFullExtentIcon);
    zoomToFullExtentButton.setPreferredSize(new Dimension(30, 30));
    zoomToFullExtentButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    zoomToFullExtentButton.setMinimumSize(new Dimension(30, 30));
    zoomToFullExtentButton.setFont(new java.awt.Font("SansSerif", 0, 10));
    zoomToFullExtentButton.setHorizontalTextPosition(SwingConstants.CENTER);
    zoomToFullExtentButton.setToolTipText("Zoom To Full Extent");
    zoomToFullExtentButton.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          zoomToFullExtentButton_actionPerformed(e);
        }
      });
    zoomToFullExtentButton.setMaximumSize(new Dimension(30, 30));
    
    panButton.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          panButton_actionPerformed(e);
        }
      });
    panButton.setMaximumSize(new Dimension(30, 30));
    panButton.setFont(new java.awt.Font("SansSerif", 0, 10));
    panButton.setHorizontalTextPosition(SwingConstants.CENTER);
    panButton.setToolTipText("Pan");
    panButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    panButton.setMinimumSize(new Dimension(30, 30));
    panButton.setIcon(panIcon);
    panButton.setPreferredSize(new Dimension(30, 30));
    panButton.setMargin(new Insets(0, 0, 0, 0));
    
    btnSelectPoint.setMaximumSize(new Dimension(30, 30));
    btnSelectPoint.setMinimumSize(new Dimension(30, 30));
    btnSelectPoint.setToolTipText("<html>Move/Add/Delete Vertex<br><br>Move = Left-Btn<br>Add = Right-Btn<br>Delete = Ctl-Right-Btn</html>");
    btnSelectPoint.setIcon(moveVertexIcon);
    btnSelectPoint.setMargin(new Insets(0, 0, 0, 0));
    btnSelectPoint.setMnemonic('0');
    btnSelectPoint.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnSelectPoint_actionPerformed(e);
      }
    });
    
    deleteAllTestCasesMenuItem.setText("Delete All Test Cases");
    deleteAllTestCasesMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          deleteAllTestCasesMenuItem_actionPerformed(e);
        }
      });
    jMenu3.setText("Options");
    showVerticesMenuItem.setText("Show Vertices");
    showVerticesMenuItem.setSelected(true);
    showVerticesMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          showVerticesMenuItem_actionPerformed(e);
        }
      });
    showGridMenuItem.setText("Show Grid");
    showGridMenuItem.setSelected(true);
    showGridMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          showGridMenuItem_actionPerformed(e);
        }
      });
    showOrientationsMenuItem.setText("Show Orientations");
    showOrientationsMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          showOrientationsMenuItem_actionPerformed(e);
        }
      });
    showVertexIndicesMenuItem.setText("Show Vertex Indices");
    showVertexIndicesMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	showVertexIndicesMenuItem_actionPerformed(e);
        }
      });
    menuLoadXmlTestFolder.setText("Open XML Folder(s)...");
    menuLoadXmlTestFolder.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          menuLoadXmlTestFolder_actionPerformed(e);
        }
      });
    precisionModelMenuItem.setText("Precision Model...");
    precisionModelMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          precisionModelMenuItem_actionPerformed(e);
        }
      });
    jMenu4.setText("Tools");
    removeDuplicatePoints.setText("Remove Duplicate Points");
    removeDuplicatePoints.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        menuRemoveDuplicatePoints_actionPerformed(e);
      }
    });
    changeToLines.setText("Change to Lines");
    changeToLines.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        menuChangeToLines_actionPerformed(e);
      }
    });
    jToolBar1.setFloatable(false);
    generateExpectedValuesMenuItem.setText("Generate Expected Values");
    generateExpectedValuesMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //generateExpectedValuesMenuItem_actionPerformed(e);
      }
    });
    jMenuFile.add(menuLoadXmlTestFile);
    jMenuFile.add(menuLoadXmlTestFolder);
    jMenuFile.add(saveAsPNGMenuItem);
    jMenuFile.add(saveToClipboardMenuItem);
    jMenuFile.add(saveAsXmlMenuItem);
    jMenuFile.add(saveAsHtmlMenuItem);

    jMenuFile.addSeparator();
    jMenuFile.add(generateExpectedValuesMenuItem);
    jMenuFile.addSeparator();
    jMenuFile.add(jMenuFileExit);
    jMenuHelp.add(jMenuHelpAbout);
    jMenuBar1.add(jMenuFile);
    jMenuBar1.add(jMenu1);
    jMenuBar1.add(jMenu2);
    jMenuBar1.add(jMenu3);
    jMenuBar1.add(jMenu4);
    jMenuBar1.add(jMenuHelp);
    this.setJMenuBar(jMenuBar1);
    jMenu1.add(menuTestText);
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
        if (inputTabbedPane.getComponent(inputTabbedPane.getSelectedIndex()) == statsPanel) {
          statsPanel.refresh();        	
        }  	
      }
    });
    contentPane.add(jToolBar1, BorderLayout.NORTH);
    jToolBar1.add(newButton, null);
    jToolBar1.add(copyButton, null);
    jToolBar1.add(deleteButton, null);
    jToolBar1.add(component2, null);
    jToolBar1.add(previousButton, null);
    jToolBar1.add(nextButton, null);
    jToolBar1.add(component1, null);

    jToolBar1.add(exchangeButton, null);
    jToolBar1.add(component5, null);
//    jToolBar1.add(runAllTestsButton, null);
//    jToolBar1.add(component3, null);
    jToolBar1.add(zoomInButton, null);
    jToolBar1.add(panButton, null);
    jToolBar1.add(oneToOneButton, null);
    jToolBar1.add(zoomToInputButton, null);
    jToolBar1.add(zoomToFullExtentButton, null);
    jToolBar1.add(component4, null);
    jToolBar1.add(drawPolygonButton, null);
    jToolBar1.add(drawLineStringButton, null);
    jToolBar1.add(drawPointButton, null);
    jToolBar1.add(btnSelectPoint, null);
    jToolBar1.add(component4, null);
    jToolBar1.add(infoButton, null);
    
    jMenu2.add(deleteAllTestCasesMenuItem);
    jMenu2.add(menuExchangeGeom);
    jMenu2.addSeparator();
    jMenu2.add(precisionModelMenuItem);
    
    jMenu3.add(showVerticesMenuItem);
    jMenu3.add(showVertexIndicesMenuItem);
    jMenu3.add(showOrientationsMenuItem);
    jMenu3.add(showGridMenuItem);
    
    jMenu4.add(removeDuplicatePoints);
    jMenu4.add(changeToLines);
    
    jSplitPane1.setDividerLocation(500);
    wktPanel.addActionListener(
      new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          wktPanel_actionPerformed(e);
        }
      });
    buttonGroup.add(drawPolygonButton);
    buttonGroup.add(drawLineStringButton);
    buttonGroup.add(drawPointButton);
    buttonGroup.add(panButton);
    buttonGroup.add(zoomInButton);
    buttonGroup.add(btnSelectPoint);
    buttonGroup.add(infoButton);
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

  private double offsetNumber(double number, Coordinate offset, boolean xValue) {
    return number - (xValue ? offset.x : offset.y);
  }

  private String offset(String wellKnownText, Coordinate offset) throws IOException {
    String offsetWellKnownText = "";
    StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(wellKnownText));
    boolean xValue = false;
    int type = tokenizer.nextToken();
    while (type != StreamTokenizer.TT_EOF) {
      offsetWellKnownText += " ";
      switch (type) {
        case StreamTokenizer.TT_EOL:
          break;
        case StreamTokenizer.TT_NUMBER:
          xValue = ! xValue;
          offsetWellKnownText += offsetNumber(tokenizer.nval, offset, xValue);
          break;
        case StreamTokenizer.TT_WORD:
          offsetWellKnownText += tokenizer.sval;
          break;
        case '(':
          offsetWellKnownText += "(";
          break;
        case ')':
          offsetWellKnownText += ")";
          break;
        case ',':
          offsetWellKnownText += ",";
          break;
        default:
          Assert.shouldNeverReachHere();
      }
      type = tokenizer.nextToken();
    }
    return offsetWellKnownText;
  }

  private void loadGeometryText(boolean moveToOrigin) throws ParseException, IOException {
  	MultiFormatReader reader = new MultiFormatReader(new GeometryFactory(tbModel.getPrecisionModel(),0));
  	
  	// read geom A
    Geometry g0 = null;
    String text0 = wktPanel.getGeometryTextClean(0);
    if (text0.length() > 0) {
      g0 = reader.read(text0);
    }
    
    // read geom B
    Geometry g1 = null;
    String text1 = wktPanel.getGeometryTextClean(1);
    if (text1.length() > 0) {
      g1 = reader.read(text1);
    }
    
    if (moveToOrigin) {
      Coordinate offset = pickOffset(g0, g1);
      if (offset == null) { return; }
      if (g0 != null) {
        g0 = reader.read(offset(wktPanel.getGeometryTextA(), offset));
      }
      if (g1 != null) {
        g1 = reader.read(offset(wktPanel.getGeometryTextB(), offset));
      }
    }
    TestCaseEdit testCaseEdit = (TestCaseEdit) tbModel.getCurrentTestCaseEdit();
    testCaseEdit.setGeometry(0, g0);
    testCaseEdit.setGeometry(1, g1);
    tbModel.getGeometryEditModel().setTestCase(testCaseEdit);
  }


  private void updatePrecisionModelDescription() {
    testCasePanel.setPrecisionModelDescription(tbModel.getPrecisionModel().toString());
  }

  private void menuRemoveDuplicatePoints_actionPerformed(ActionEvent e) {
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

  void btnSelectPoint_actionPerformed(ActionEvent e) {
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

