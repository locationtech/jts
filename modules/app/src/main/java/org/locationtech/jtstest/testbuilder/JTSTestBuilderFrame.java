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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.util.Assert;
import org.locationtech.jtstest.clean.CleanDuplicatePoints;
import org.locationtech.jtstest.testbuilder.controller.JTSTestBuilderController;
import org.locationtech.jtstest.testbuilder.controller.ResultController;
import org.locationtech.jtstest.testbuilder.event.SpatialFunctionPanelEvent;
import org.locationtech.jtstest.testbuilder.event.SpatialFunctionPanelListener;
import org.locationtech.jtstest.testbuilder.model.GeometryEvent;
import org.locationtech.jtstest.testbuilder.model.HtmlWriter;
import org.locationtech.jtstest.testbuilder.model.JavaTestWriter;
import org.locationtech.jtstest.testbuilder.model.TestBuilderModel;
import org.locationtech.jtstest.testbuilder.model.TestCaseEdit;
import org.locationtech.jtstest.testbuilder.model.XMLTestWriter;
import org.locationtech.jtstest.testbuilder.ui.ImageUtil;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.testbuilder.ui.dnd.FileDrop;
import org.locationtech.jtstest.testbuilder.ui.tools.DeleteVertexTool;
import org.locationtech.jtstest.testbuilder.ui.tools.EditVertexTool;
import org.locationtech.jtstest.testbuilder.ui.tools.ExtractComponentTool;
import org.locationtech.jtstest.testbuilder.ui.tools.InfoTool;
import org.locationtech.jtstest.testbuilder.ui.tools.LineStringTool;
import org.locationtech.jtstest.testbuilder.ui.tools.PanTool;
import org.locationtech.jtstest.testbuilder.ui.tools.PointTool;
import org.locationtech.jtstest.testbuilder.ui.tools.RectangleTool;
import org.locationtech.jtstest.testbuilder.ui.tools.StreamPolygonTool;
import org.locationtech.jtstest.testbuilder.ui.tools.ZoomTool;
import org.locationtech.jtstest.testrunner.GuiUtil;
import org.locationtech.jtstest.util.FileUtil;
import org.locationtech.jtstest.util.StringUtil;


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
  WKTPanel wktPanel = new WKTPanel(this);
  InspectorPanel inspectPanel = new InspectorPanel();
  TestListPanel testListPanel = new TestListPanel(this);
  //LayerListPanel layerListPanel = new LayerListPanel();
  LayerListPanel layerListPanel = new LayerListPanel();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  GridLayout gridLayout1 = new GridLayout();
  ResultWKTPanel resultWKTPanel = new ResultWKTPanel();
  ResultValuePanel resultValuePanel = new ResultValuePanel();
  StatsPanel statsPanel = new StatsPanel();
  InfoPanel logPanel = new InfoPanel();
  private ZoomTool zoomTool;
  private final ImageIcon appIcon = new ImageIcon(this.getClass().getResource("app-icon.gif"));

  private JFileChooser fileChooser = new JFileChooser();
  private JFileChooser pngFileChooser;
  private JFileChooser fileAndDirectoryChooser = new JFileChooser();
  private JFileChooser directoryChooser = new JFileChooser();
  
  TestBuilderModel tbModel;
  
  private TestCaseTextDialog testCaseTextDlg = new TestCaseTextDialog(this,
      "", true);
  private GeometryInspectorDialog geomInspectorDlg = new GeometryInspectorDialog(this);
  /*
  private LoadTestCasesDialog loadTestCasesDlg = new LoadTestCasesDialog(this,
      "Load Test Cases", true);
*/
  
  
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
      testCasePanel.spatialFunctionPanel.addSpatialFunctionPanelListener(
          new SpatialFunctionPanelListener() {
            public void functionExecuted(SpatialFunctionPanelEvent e) {
            	resultController.spatialFunctionPanel_functionExecuted(e);
            }
          });
      testCasePanel.scalarFunctionPanel.addSpatialFunctionPanelListener(
          new SpatialFunctionPanelListener() {
            public void functionExecuted(SpatialFunctionPanelEvent e) {
            	resultController.scalarFunctionPanel_functionExecuted(e);
            }
          });
      testCasePanel.editCtlPanel.btnSetPrecisionModel.addActionListener(
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              precisionModelMenuItem_actionPerformed(e);
            }
          });
      //testCasePanel.editCtlPanel.cbMagnifyTopo.addActionListener(
      testCasePanel.cbMagnifyTopo.addActionListener(
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              revealTopo_actionPerformed();
            }
          });
      //testCasePanel.editCtlPanel.stretchDist
      testCasePanel.spStretchDist
      .addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent e) {
          revealTopo_actionPerformed();
        }
      });

      zoomTool = new ZoomTool(2, AppCursors.ZOOM);
      showGeomsTab();
      initFileDrop(testCasePanel);
      testCasePanel.getGeometryEditPanel().setCurrentTool(RectangleTool.getInstance());
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
          SwingUtil.reportException(null, ex);
        }
      }
    });
  }
  private void initFileChoosers() {
    if (pngFileChooser == null) {
      pngFileChooser = new JFileChooser();
      pngFileChooser.addChoosableFileFilter(SwingUtil.PNG_FILE_FILTER);
      pngFileChooser.setDialogTitle("Save PNG");
      pngFileChooser.setSelectedFile(new File("geoms.png"));
    }
  }
  
  public static JTSTestBuilderFrame instance() {
    if (singleton == null) {
      new JTSTestBuilderFrame();
    }
    return singleton;
  }

  public static GeometryEditPanel getGeometryEditPanel()
  {
    return instance().getTestCasePanel().getGeometryEditPanel();
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
    inspectPanel.setModel(model);
    resultWKTPanel.setModel(model);
    resultValuePanel.setModel(model);
    statsPanel.setModel(model);
    
    model.getGeometryEditModel().addGeometryListener(
        new org.locationtech.jtstest.testbuilder.model.GeometryListener() {
          public void geometryChanged(GeometryEvent e) {
            model_geometryChanged(e);
          }
        });
    
    testListPanel.populateList();
    //layerListPanel.init(getModel().getLayers());
    layerListPanel.populateList();
    updateTestCaseView();
    updatePrecisionModelDescription();
  }
  
  public static void reportException(Exception e) {
  	SwingUtil.reportException(instance(), e);
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
    dlg.setVisible(true);
  }

  public void showTab(String name)
  {
    inputTabbedPane.setSelectedIndex(inputTabbedPane.indexOfTab(name));
  }
  
  public void showGeomsTab()
  {
    showTab(AppStrings.TAB_LABEL_INPUT);
  }
  
  public void showResultWKTTab()
  {
    showTab(AppStrings.TAB_LABEL_RESULT);
  }
  public void showResultValueTab()
  {
    showTab(AppStrings.TAB_LABEL_VALUE);
  }
  
  public void showInfoTab()
  {
    showTab(AppStrings.TAB_LABEL_LOG);
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

  void model_geometryChanged(GeometryEvent e) {
    //testCasePanel.relatePanel.clearResults();
    JTSTestBuilderController.geometryViewChanged();
    updateWktPanel();
  }

  void createNewCase() {
    tbModel.createNew();
    showGeomsTab();
    updateTestCases();
  }

  void moveToPrevCase(boolean isZoom) {
    tbModel.prevCase();
    updateTestCaseView();
    if (isZoom) JTSTestBuilderController.zoomToInput();
  }

  void moveToNextCase(boolean isZoom) {
    tbModel.nextCase();
    updateTestCaseView();
    if (isZoom) JTSTestBuilderController.zoomToInput();
  }

  void copyCase() {
    tbModel.copyCase();
    updateTestCases();
  }

  public void updateTestCases()
  {
    testListPanel.populateList();    
    updateTestCaseView();
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

  void menuViewText_actionPerformed(ActionEvent e) {
    testCaseTextDlg.setTestCase(tbModel.getCurrentTestCaseEdit());
    testCaseTextDlg.setVisible(true);
  }

  public void actionInspectGeometry() {
    int geomIndex = tbModel.getGeometryEditModel().getGeomIndex();
    String tag = geomIndex == 0 ? AppStrings.GEOM_LABEL_A : AppStrings.GEOM_LABEL_B;
    Geometry geometry = tbModel.getCurrentTestCaseEdit().getGeometry(geomIndex);
    inspectPanel.setGeometry( tag, geometry, geomIndex);
    showTab(AppStrings.TAB_LABEL_INSPECT);
    /*
    geomInspectorDlg.setGeometry(
        geomIndex == 0 ? AppStrings.GEOM_LABEL_A : AppStrings.GEOM_LABEL_B,
        tbModel.getCurrentTestCaseEdit().getGeometry(geomIndex));
        */
    //geomInspectorDlg.setVisible(true);
  }

  public void actionInspectGeometryDialog() {
    int geomIndex = tbModel.getGeometryEditModel().getGeomIndex();
    String tag = geomIndex == 0 ? AppStrings.GEOM_LABEL_A : AppStrings.GEOM_LABEL_B;
    Geometry geometry = tbModel.getCurrentTestCaseEdit().getGeometry(geomIndex);
    geomInspectorDlg.setGeometry(tag, geometry);
    geomInspectorDlg.setVisible(true);
  }

  void menuLoadXmlTestFile_actionPerformed(ActionEvent e) {
    try {
      fileChooser.removeChoosableFileFilter(SwingUtil.JAVA_FILE_FILTER);
      fileChooser.addChoosableFileFilter(SwingUtil.XML_FILE_FILTER);
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
      SwingUtil.reportException(this, x);
    }
  }

  void menuSaveAsXml_actionPerformed(ActionEvent e) {
    try {
      fileChooser.removeChoosableFileFilter(SwingUtil.JAVA_FILE_FILTER);
      fileChooser.addChoosableFileFilter(SwingUtil.XML_FILE_FILTER);
      fileChooser.setDialogTitle("Save XML Test File");
      if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
        File file = fileChooser.getSelectedFile();
        if (! SwingUtil.confirmOverwrite(this, file)) return;
        FileUtil.setContents(fileChooser.getSelectedFile().getPath(), 
        		XMLTestWriter.getRunXml(tbModel.getTestCaseList(), tbModel.getPrecisionModel()) );
      }
    }
    catch (Exception x) {
      SwingUtil.reportException(this, x);
    }
  }

  public String getRunXml() 
  {
  	return XMLTestWriter.getRunXml(tbModel.getTestCaseList(), tbModel.getPrecisionModel());
  }
  
  void menuSaveAsJava_actionPerformed(ActionEvent e) {
    try {
      fileChooser.removeChoosableFileFilter(SwingUtil.XML_FILE_FILTER);
      fileChooser.addChoosableFileFilter(SwingUtil.JAVA_FILE_FILTER);
      fileChooser.setDialogTitle("Save Java File");
      if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
        File file = fileChooser.getSelectedFile();
        if (! SwingUtil.confirmOverwrite(this, file)) return;
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
      SwingUtil.reportException(this, x);
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
      SwingUtil.reportException(this, x);
    }
  }

  void menuSaveAsPNG_actionPerformed(ActionEvent e) {
    initFileChoosers();
    try {
      String fullFileName = SwingUtil.chooseFilenameWithConfirm(this, pngFileChooser);  
      if (fullFileName == null) return;
        ImageUtil.writeImage(testCasePanel.getGeometryEditPanel(), 
            fullFileName,
            ImageUtil.IMAGE_FORMAT_NAME_PNG);
    }
    catch (Exception x) {
      SwingUtil.reportException(this, x);
    }
  }

  void menuSaveScreenToClipboard_actionPerformed(ActionEvent e) {
    try {
        ImageUtil.saveImageToClipboard(testCasePanel.getGeometryEditPanel(), 
        		ImageUtil.IMAGE_FORMAT_NAME_PNG);
    }
    catch (Exception x) {
      SwingUtil.reportException(this, x);
    }
  }

  void drawRectangleButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(RectangleTool.getInstance());
  }

  void drawPolygonButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(StreamPolygonTool.getInstance());
  }

  void drawLineStringButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(LineStringTool.getInstance());
  }

  void drawPointButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(PointTool.getInstance());
  }

  void infoButton_actionPerformed() {
    testCasePanel.getGeometryEditPanel().setCurrentTool(InfoTool.getInstance());
  }

  void actionExtractComponentButton() {
    testCasePanel.getGeometryEditPanel().setCurrentTool(ExtractComponentTool.getInstance());
  }

  void actionDeleteVertexButton() {
    testCasePanel.getGeometryEditPanel().setCurrentTool(DeleteVertexTool.getInstance());
  }

  void zoomInButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(zoomTool);
  }

  void oneToOneButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().getViewport().zoomToInitialExtent();
  }

  void zoomToFullExtentButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().zoomToFullExtent();
  }

  void zoomToResult_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().zoomToResult();
  }

  void zoomToInputButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().zoomToInput();
  }

  void zoomToInputA_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().zoomToGeometry(0);
  }

  void zoomToInputB_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().zoomToGeometry(1);
  }

  void panButton_actionPerformed(ActionEvent e) {
    testCasePanel.getGeometryEditPanel().setCurrentTool(PanTool.getInstance());
  }

  void deleteAllTestCasesMenuItem_actionPerformed(ActionEvent e) {
    tbModel.initTestCaseList();
    updateTestCaseView();
    testListPanel.populateList();
  }

  public void setShowingGrid(boolean showGrid) {
    testCasePanel.editPanel.setGridEnabled(showGrid);
    JTSTestBuilderController.geometryViewChanged();
  }

  public void setShowingStructure(boolean showStructure) {
    TestBuilderModel.setShowingStructure(showStructure);
    JTSTestBuilderController.geometryViewChanged();
  }

  public void setShowingOrientations(boolean showingOrientations) {
    TestBuilderModel.setShowingOrientation(showingOrientations);
    JTSTestBuilderController.geometryViewChanged();
  }

  public void setShowVertexIndices(boolean showVertexIndices) {
    TestBuilderModel.setShowingOrientation(showVertexIndices);
    JTSTestBuilderController.geometryViewChanged();
  }

  public void setShowingVertices(boolean showingVertices) {
    TestBuilderModel.setShowingVertices(showingVertices);
    JTSTestBuilderController.geometryViewChanged();
  }

  public void setShowingLabel(boolean showLabel) {
    TestBuilderModel.setShowingLabel(showLabel);
    JTSTestBuilderController.geometryViewChanged();
  }

  void showVertexIndicesMenuItem_actionPerformed(ActionEvent e) {
//    testCasePanel.editPanel.setShowVertexIndices(showVertexIndicesMenuItem.isSelected());
  }

  void menuLoadXmlTestFolder_actionPerformed(ActionEvent e) {
    try {
      directoryChooser.removeChoosableFileFilter(SwingUtil.JAVA_FILE_FILTER);
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
      SwingUtil.reportException(this, x);
    }
  }

  void precisionModelMenuItem_actionPerformed(ActionEvent e) {
    try {
      PrecisionModelDialog precisionModelDialog = new PrecisionModelDialog(
          this, "Edit Precision Model", true);
      GuiUtil.center(precisionModelDialog, this);
      precisionModelDialog.setPrecisionModel(tbModel.getPrecisionModel());
      precisionModelDialog.setVisible(true);
      tbModel.changePrecisionModel(precisionModelDialog.getPrecisionModel());
      updatePrecisionModelDescription();
      updateGeometry();
    }
    catch (ParseException pe) {
      SwingUtil.reportException(this, pe);
    }
  }
  void revealTopo_actionPerformed() {
    tbModel.setMagnifyingTopology(testCasePanel.cbMagnifyTopo.isSelected());
    tbModel.setTopologyStretchSize(testCasePanel.getStretchSize());
    //tbModel.setMagnifyingTopology(testCasePanel.editCtlPanel.cbMagnifyTopo.isSelected());
    //tbModel.setTopologyStretchSize(testCasePanel.editCtlPanel.getStretchSize());
    JTSTestBuilderController.geometryViewChanged();
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
    inputTabbedPane.add(testListPanel, AppStrings.TAB_LABEL_CASES);
    inputTabbedPane.add(wktPanel,  AppStrings.TAB_LABEL_INPUT);
    inputTabbedPane.add(resultWKTPanel, AppStrings.TAB_LABEL_RESULT);
    inputTabbedPane.add(resultValuePanel, AppStrings.TAB_LABEL_VALUE);
    inputTabbedPane.add(inspectPanel,  AppStrings.TAB_LABEL_INSPECT);
    inputTabbedPane.add(statsPanel, AppStrings.TAB_LABEL_STATS);
    inputTabbedPane.add(logPanel, AppStrings.TAB_LABEL_LOG);
    inputTabbedPane.add(layerListPanel, AppStrings.TAB_LABEL_LAYERS);
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

  public JTSTestBuilderToolBar getToolbar()
  {
    return tbToolBar;
  }
  
  private void updateStatsPanelIfVisible()
  {
    int index = inputTabbedPane.getSelectedIndex();
    if (index < 0) return;
    if (inputTabbedPane.getComponent(index) == statsPanel) {
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

  public void updateTestCaseView() {
    testCasePanel.setTestCase(tbModel.getCurrentTestCaseEdit());
    getTestCasePanel().setCurrentTestCaseIndex(tbModel.getCurrentTestIndex() + 1);
    getTestCasePanel().setMaxTestCaseIndex(tbModel.getTestListSize());
    updateWktPanel();
    updateStatsPanelIfVisible();
  }

  public void displayInfo(Coordinate modelPt)
  {
    displayInfo(
        testCasePanel.getGeometryEditPanel().getInfo(modelPt)
        );
  }
  
  public void displayInfo(String s)
  {
    displayInfo(s, true);
  }
  
  public void displayInfo(String s, boolean showTab)
  {
    logPanel.addInfo(s);
    if (showTab) showInfoTab();
  }
  
  private void reportProblemsParsingXmlTestFile(List parsingProblems) {
    if (parsingProblems.isEmpty()) {
      return;
    }
    for (Iterator i = parsingProblems.iterator(); i.hasNext(); ) {
      String problem = (String) i.next();
      System.out.println(problem);
    }
    JOptionPane.showMessageDialog(this, StringUtil.wrap(parsingProblems.size()
         + " problems occurred parsing the XML test file."
         + " The first problem was: " + parsingProblems.get(0), 80),
        "Error", JOptionPane.ERROR_MESSAGE);
  }

  void menuRemoveDuplicatePoints_actionPerformed(ActionEvent e) {
    CleanDuplicatePoints clean = new CleanDuplicatePoints();
    Geometry cleanGeom = clean.clean(tbModel.getGeometryEditModel().getGeometry(0));
    tbModel.getCurrentTestCaseEdit().setGeometry(0, cleanGeom);
    updateGeometry();
  }

  void menuChangeToLines_actionPerformed(ActionEvent e) {
    Geometry cleanGeom = LinearComponentExtracter.getGeometry(tbModel.getGeometryEditModel().getGeometry(0));
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

