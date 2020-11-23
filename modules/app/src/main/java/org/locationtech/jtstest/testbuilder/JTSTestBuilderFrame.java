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
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.util.Assert;
import org.locationtech.jtstest.clean.CleanDuplicatePoints;
import org.locationtech.jtstest.testbuilder.controller.ResultController;
import org.locationtech.jtstest.testbuilder.event.SpatialFunctionPanelEvent;
import org.locationtech.jtstest.testbuilder.event.SpatialFunctionPanelListener;
import org.locationtech.jtstest.testbuilder.io.HtmlWriter;
import org.locationtech.jtstest.testbuilder.io.JavaTestWriter;
import org.locationtech.jtstest.testbuilder.io.XMLTestWriter;
import org.locationtech.jtstest.testbuilder.model.DisplayParameters;
import org.locationtech.jtstest.testbuilder.model.GeometryEvent;
import org.locationtech.jtstest.testbuilder.model.TestBuilderModel;
import org.locationtech.jtstest.testbuilder.model.TestCaseEdit;
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
import org.locationtech.jtstest.testbuilder.ui.tools.Tool;
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
  static boolean isShowingIndicators = true;
  
  private ResultController resultController = new ResultController(this);
  private JTSTestBuilderMenuBar tbMenuBar = new JTSTestBuilderMenuBar(this);
  private JTSTestBuilderToolBar tbToolBar = new JTSTestBuilderToolBar(this);
  //---------------------------------------------
  JPanel contentPane;
  BorderLayout contentLayout = new BorderLayout();
  Border border4;
  JSplitPane jSplitPane1 = new JSplitPane();
  JPanel panelTop = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();
  TestCasePanel testCasePanel = new TestCasePanel();
  JPanel panelBottom = new JPanel();
  JTabbedPane inputTabbedPane = new JTabbedPane();
  BorderLayout borderLayout3 = new BorderLayout();
  JPanel testPanel = new JPanel();
  WKTPanel wktPanel = new WKTPanel(this);
  CommandPanel commandPanel = new CommandPanel();
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

      testCasePanel.spatialFunctionPanel.addSpatialFunctionPanelListener(
          new SpatialFunctionPanelListener() {
            public void functionExecuted(SpatialFunctionPanelEvent e) {
            	resultController.spatialFunctionPanel_functionExecuted(e);
            }
          });
      testCasePanel.scalarFunctionPanel.addSpatialFunctionPanelListener(
          new SpatialFunctionPanelListener() {
            public void functionExecuted(SpatialFunctionPanelEvent e) {
            	resultController.executeScalarFunction();
            }
          });
      testCasePanel.cbRevealTopo.addActionListener(
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
  /**
   * Tests if the TestBuilder is running.
   * Useful to allow functions to decide whether to show indicators 
   * (if functions are running under JtsOpCmd, they should not show indicators
   * since that seriously impacts performance).
   * 
   * @return true if there is a TestBuilder instance running
   */
  public static boolean isRunning() {
    return singleton != null;
  }
  public static boolean isShowingIndicators() {
    return isRunning() && isShowingIndicators;
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

  public void setCursorWait() {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  }
  
  public void setCursorNormal() {
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }
  
  public void setCurrentTestCase(TestCaseEdit testCase) {
    tbModel.cases().setCurrent(testCase);
    updateTestCaseView();
    JTSTestBuilder.controller().zoomToInput();
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
  
  public InfoPanel getLogPanel() {
    return logPanel;
  }
  
  public CommandPanel getCommandPanel() {
    return commandPanel;
  }
  
  /**
   *  File | Exit action performed
   */
  public void actionExit() {
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
    JTSTestBuilder.controller().zoomToInput();
  }

  /**
   *  Overridden so we can exit when window is closed
   */
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      actionExit();
    }
  }

  void model_geometryChanged(GeometryEvent e) {
    //testCasePanel.relatePanel.clearResults();
    JTSTestBuilder.controller().geometryViewChanged();
    updateWktPanel();
  }

  TestCaseEdit currentCase() {
    return tbModel.cases().getCurrentCase();
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
  
  public void inspectResult() 
  {
    Object currResult = tbModel.getResult();
    if (! (currResult instanceof Geometry))
      return;
    inspectGeometry((Geometry) currResult, 0, "R");
  }
  
  void menuViewText_actionPerformed(ActionEvent e) {
    testCaseTextDlg.setTestCase(currentCase());
    testCaseTextDlg.setVisible(true);
  }

  public void actionInspectGeometry() {
    int geomIndex = tbModel.getGeometryEditModel().getGeomIndex();
    String tag = geomIndex == 0 ? AppStrings.GEOM_LABEL_A : AppStrings.GEOM_LABEL_B;
    Geometry geometry = currentCase().getGeometry(geomIndex);
    inspectGeometry(geometry, geomIndex, tag);
    /*
    geomInspectorDlg.setGeometry(
        geomIndex == 0 ? AppStrings.GEOM_LABEL_A : AppStrings.GEOM_LABEL_B,
        tbModel.getCurrentTestCaseEdit().getGeometry(geomIndex));
        */
    //geomInspectorDlg.setVisible(true);
  }

  public void inspectGeometry(Geometry geometry, int geomIndex, String tag) {
    inspectPanel.setGeometry( tag, geometry, geomIndex);
    showTab(AppStrings.TAB_LABEL_INSPECT);
  }

  public void actionInspectGeometryDialog() {
    int geomIndex = tbModel.getGeometryEditModel().getGeomIndex();
    String tag = geomIndex == 0 ? AppStrings.GEOM_LABEL_A : AppStrings.GEOM_LABEL_B;
    Geometry geometry = currentCase().getGeometry(geomIndex);
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

  public void actionSaveImageAsPNG() {
    initFileChoosers();
    try {
      String fullFileName = SwingUtil.chooseFilenameWithConfirm(this, pngFileChooser);  
      if (fullFileName == null) return;
        ImageUtil.writeImage(testCasePanel.getGeometryEditPanel(), 
            fullFileName,
            ImageUtil.IMAGE_FORMAT_NAME_PNG);
    }
    catch (Exception x) {
      reportException(x);
    }
  }

  public void actionSaveImageToClipboard() {
    try {
        ImageUtil.saveImageToClipboard(testCasePanel.getGeometryEditPanel(), 
        		ImageUtil.IMAGE_FORMAT_NAME_PNG);
    }
    catch (Exception x) {
      reportException(x);
    }
  }

  void actionDeleteAllTestCases() {
    tbModel.cases().init();
    updateTestCaseView();
    testListPanel.populateList();
  }

  void actionLoadXmlTestFolder() {
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
      reportException(x);
    }
  }

  void precisionModelMenuItem_actionPerformed() {
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
      reportException(pe);
    }
  }
  void revealTopo_actionPerformed() {
    DisplayParameters.setRevealingTopology(testCasePanel.cbRevealTopo.isSelected());
    DisplayParameters.setTopologyStretchSize(testCasePanel.getStretchSize());
    JTSTestBuilder.controller().geometryViewChanged();
  }


  /**
   *  Component initialization
   */
  private void jbInit() throws Exception {
    this.setSize(new Dimension(800, 800));
    this.setTitle("JTS TestBuilder");
    this.setJMenuBar(tbMenuBar.getMenuBar());
   
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);
    fileAndDirectoryChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    fileAndDirectoryChooser.setMultiSelectionEnabled(true);
    directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    directoryChooser.setMultiSelectionEnabled(false);
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    
    //---------------------------------------------------
    
    wktPanel.setMinimumSize(new Dimension(111, 0));
    wktPanel.setPreferredSize(new Dimension(600, 100));
    testPanel.setLayout(gridBagLayout2);
    gridLayout1.setRows(4);
    gridLayout1.setColumns(1);
    
    panelTop.setLayout(borderLayout2);
    panelTop.setMinimumSize(new Dimension(431, 0));
    panelTop.add(testCasePanel, BorderLayout.CENTER);
    
    panelBottom.setLayout(borderLayout3);
    panelBottom.setMinimumSize(new Dimension(431, 0));
    panelBottom.add(tbToolBar.getToolBar(), BorderLayout.NORTH);
    panelBottom.add(inputTabbedPane, BorderLayout.CENTER);
      
    //---- Input tabs
    inputTabbedPane.setTabPlacement(JTabbedPane.LEFT);
    inputTabbedPane.add(testListPanel, AppStrings.TAB_LABEL_CASES);
    inputTabbedPane.add(wktPanel,  AppStrings.TAB_LABEL_INPUT);
    inputTabbedPane.add(resultWKTPanel, AppStrings.TAB_LABEL_RESULT);
    inputTabbedPane.add(resultValuePanel, AppStrings.TAB_LABEL_VALUE);
    inputTabbedPane.add(commandPanel,  AppStrings.TAB_LABEL_COMMAND);
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
    
    //--- main frame
  
    jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
    jSplitPane1.setPreferredSize(new Dimension(601, 690));
    jSplitPane1.setBorder(new EmptyBorder(2,2,2,2));
    jSplitPane1.setResizeWeight(1);
    jSplitPane1.setDividerLocation(500);
    jSplitPane1.add(panelTop, JSplitPane.TOP);
    jSplitPane1.add(panelBottom, JSplitPane.BOTTOM);
    
    /*
    border4 = BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white,
        Color.white, new Color(93, 93, 93), new Color(134, 134, 134));
        */
    contentPane = (JPanel) this.getContentPane();
    contentPane.setLayout(contentLayout);
    contentPane.setPreferredSize(new Dimension(601, 690));
    contentPane.add(jSplitPane1, BorderLayout.CENTER);

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
    testCasePanel.setTestCase(currentCase());
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
    testCasePanel.setTestCase(currentCase());
    getTestCasePanel().setCurrentTestCaseIndex(tbModel.getCurrentCaseIndex() + 1);
    getTestCasePanel().setMaxTestCaseIndex(tbModel.getCasesSize());
    updateWktPanel();
    updateStatsPanelIfVisible();
  }

  public void updateLayerList() {
    layerListPanel.updateList();
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
    currentCase().setGeometry(0, cleanGeom);
    updateGeometry();
  }

  void menuChangeToLines_actionPerformed(ActionEvent e) {
    Geometry cleanGeom = LinearComponentExtracter.getGeometry(tbModel.getGeometryEditModel().getGeometry(0));
    currentCase().setGeometry(0, cleanGeom);
    updateGeometry();
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

