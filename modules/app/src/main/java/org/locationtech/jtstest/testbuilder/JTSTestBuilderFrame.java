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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.Assert;
import org.locationtech.jtstest.testbuilder.io.XMLTestWriter;
import org.locationtech.jtstest.testbuilder.model.DisplayParameters;
import org.locationtech.jtstest.testbuilder.model.GeometryEvent;
import org.locationtech.jtstest.testbuilder.model.TestBuilderModel;
import org.locationtech.jtstest.testbuilder.model.TestCaseEdit;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.testbuilder.ui.dnd.FileDrop;
import org.locationtech.jtstest.testbuilder.ui.tools.RectangleTool;
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
  static boolean isSavingIndicators = false;
  
  TestBuilderModel tbModel;

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
  LayerListPanel layerListPanel = new LayerListPanel();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  GridLayout gridLayout1 = new GridLayout();
  ResultWKTPanel resultWKTPanel = new ResultWKTPanel();
  ResultValuePanel resultValuePanel = new ResultValuePanel();
  StatsPanel statsPanel = new StatsPanel();
  InfoPanel logPanel = new InfoPanel();

  private JFileChooser fileChooser = new JFileChooser();
  private JFileChooser pngFileChooser;
  private JFileChooser fileAndDirectoryChooser = new JFileChooser();
  private JFileChooser directoryChooser = new JFileChooser();
  
  /**
   *  Construct the frame
   */
  public JTSTestBuilderFrame() {
    try {
      Assert.isTrue(singleton == null);
      singleton = this;
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      setIconImage(AppIcons.APP.getImage());
      jbInit();
      testCasePanel.cbRevealTopo.addActionListener(
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
              displayRevealTopo();
            }
          });
      //testCasePanel.editCtlPanel.stretchDist
      testCasePanel.spStretchDist
      .addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent e) {
          displayRevealTopo();
        }
      });

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
  public static boolean isSavingIndicators() {
    return isRunning() && isSavingIndicators;
  }
  public static GeometryEditPanel getGeometryEditPanel()
  {
    return instance().getTestCasePanel().getGeometryEditPanel();
  }

  public static SpatialFunctionPanel getSpatialFunctionPanel()
  {
    return instance().getTestCasePanel().spatialFunctionPanel;
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
    JTSTestBuilder.controller().geometryChanged();
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
  
  public void inspectResult() 
  {
    Object currResult = tbModel.getResult();
    if (! (currResult instanceof Geometry))
      return;
    inspectGeometry("R", (Geometry) currResult);
  }

  public void inspectGeometry() {
    int geomIndex = tbModel.getGeometryEditModel().getGeomIndex();
    String tag = geomIndex == 0 ? AppStrings.GEOM_LABEL_A : AppStrings.GEOM_LABEL_B;
    Geometry geometry = currentCase().getGeometry(geomIndex);
    inspectGeometry(geometry, geomIndex, tag, true);
  }

  public void inspectGeometry(String tag, Geometry geometry) {
    inspectPanel.setGeometry( tag, geometry, 0, false);
    showTab(AppStrings.TAB_LABEL_INSPECT);
  }

  private void inspectGeometry(Geometry geometry, int geomIndex, String tag, boolean isEditable) {
    inspectPanel.setGeometry( tag, geometry, geomIndex, isEditable);
    showTab(AppStrings.TAB_LABEL_INSPECT);
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

  public String getRunXml() 
  {
  	return XMLTestWriter.getRunXml(tbModel.getTestCaseList(), tbModel.getPrecisionModel());
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

  private void displayRevealTopo() {
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
    //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    
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
    inputTabbedPane.add(layerListPanel, AppStrings.TAB_LABEL_LAYERS);
    inputTabbedPane.add(wktPanel,  AppStrings.TAB_LABEL_INPUT);
    inputTabbedPane.add(resultWKTPanel, AppStrings.TAB_LABEL_RESULT);
    inputTabbedPane.add(resultValuePanel, AppStrings.TAB_LABEL_VALUE);
    inputTabbedPane.add(inspectPanel,  AppStrings.TAB_LABEL_INSPECT);
    inputTabbedPane.add(statsPanel, AppStrings.TAB_LABEL_STATS);
    inputTabbedPane.add(logPanel, AppStrings.TAB_LABEL_LOG);
    inputTabbedPane.add(commandPanel,  AppStrings.TAB_LABEL_COMMAND);
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
  
  public void geometryChanged() {
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

  void updatePrecisionModelDescription() {
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
  
  public void refreshLayerList() {
    layerListPanel.populateList();
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

