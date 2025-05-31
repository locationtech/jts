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

package org.locationtech.jtstest.testbuilder.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.math.MathUtil;
import org.locationtech.jts.util.Assert;
import org.locationtech.jtstest.test.TestCaseList;
import org.locationtech.jtstest.test.Testable;
import org.locationtech.jtstest.testbuilder.AppColors;
import org.locationtech.jtstest.testbuilder.AppConstants;
import org.locationtech.jtstest.testbuilder.AppStrings;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.testbuilder.ui.style.BasicStyle;
import org.locationtech.jtstest.testrunner.TestReader;
import org.locationtech.jtstest.testrunner.TestRun;
import org.locationtech.jtstest.util.ExceptionFormatter;
import org.locationtech.jtstest.util.StringUtil;
import org.locationtech.jtstest.util.io.IOUtil;
import org.locationtech.jtstest.util.io.MultiFormatReader;


public class TestBuilderModel 
{
  private PrecisionModel precisionModel = new PrecisionModel();
  private GeometryFactory geometryFactory = null;
	private GeometryEditModel geomEditModel;
	
  private LayerList layerList = LayerList.createFixed();
  private LayerList layerListTop = new LayerList();
  private LayerList layerListBase = new LayerList();
  private Layer layerSelect = new Layer(AppStrings.LYR_LABEL_SELECTION, false);
  
  private WKTWriter writer = new WKTWriter();
  private Object currResult = null;
  private String opName = "";

	public TestBuilderModel()
	{
		geomEditModel = new GeometryEditModel();
    initLayers();
    caseList.init();
	}
	
	public GeometryEditModel getGeometryEditModel() { return geomEditModel; }
	
	public PrecisionModel getPrecisionModel() { return precisionModel; }
	
  public void setPrecisionModel(PrecisionModel precisionModel)
  {
    this.precisionModel = precisionModel;
    geometryFactory = null;
  }
  
  public GeometryFactory getGeometryFactory()
  {
    if (geometryFactory == null)
      geometryFactory = new GeometryFactory(getPrecisionModel());
    return geometryFactory;
  }
  
  
	public String getResultDisplayString(Geometry g)
	{
		if (g == null)
			return "";
    if (g.getNumPoints() > DisplayParameters.MAX_DISPLAY_POINTS)
      return GeometryEditModel.toStringVeryLarge(g);
		return writer.writeFormatted(g);
	}
	
  public Layer getLayer(int i) { return layerList.getLayer(i); }
  public LayerList getLayers() { return layerList; }
  public LayerList getLayersTop() { return layerListTop; }
  public LayerList getLayersBase() { return layerListBase; }
  public LayerList getLayersAll() { 
    LayerList layers = LayerList.create(
        layerListTop,
        layerList,
        getLayersFloating(),
        layerListBase
        );
    return layers;
  }
  public Layer getLayerSelect() {
    return layerSelect;
  }

  public LayerList getLayersFloating() {
    LayerList list = new LayerList();
    if (layerSelect.hasGeometry())
      list.addBottom(layerSelect);
    return list;
  }

  public List<Layer> getLayersLegend() {
    List<Layer> layers = new ArrayList<Layer>();
    addLegendLayers(layerList, layers);
    addLegendLayers(layerListTop, layers);
    addLegendLayers(layerListBase, layers);
    return layers;
  }
  
  private void addLegendLayers(LayerList layerList, List<Layer> layers) {
    for (int i = 0; i < layerList.size(); i++) {
      if (layerList.getLayer(i).hasGeometry() 
          && layerList.getLayer(i).isEnabled())
        layers.add(layerList.getLayer(i));
    }
  }
  
  public Layer getLayerIndicators() {
    Layer ind = layerListTop.find(AppStrings.LYR_INDICATORS);
    if (ind == null)
      ind = layerListBase.find(AppStrings.LYR_INDICATORS);
    if (ind == null) {
      ind = createIndicatorLayer();
      layerListBase.add(ind, true);
    }
    return ind;
  }

  private Layer createIndicatorLayer() {
    Layer ind = new Layer(AppStrings.LYR_INDICATORS,
        new ListGeometryContainer(),
        new BasicStyle(AppConstants.INDICATOR_LINE_CLR, 
                        AppConstants.INDICATOR_FILL_CLR));
    ind.getLayerStyle().setVertices(false);
    return ind;
  }
  
  public void addIndicator(Geometry geom) {
    Layer lyr = getLayerIndicators();
    ListGeometryContainer src = (ListGeometryContainer) lyr.getSource();
    src.add(geom);   
  }
  
  public boolean hasLayer(String name) {
    return findLayer(name) != null;
  }
  
  private Layer findLayer(String name) {
    Layer lyr = layerListTop.find(name);
    if (lyr != null) return lyr;
    lyr = layerListBase.find(name);
    if (lyr != null) return lyr;
    return layerList.find(name);
  }
  
  private void initLayers()
  {  	
  	GeometryContainer geomCont0 = new IndexedGeometryContainer(geomEditModel, 0);
  	GeometryContainer geomCont1 = new IndexedGeometryContainer(geomEditModel, 1);
  	
    layerList.getLayer(LayerList.LYR_A).setSource(geomCont0);
    layerList.getLayer(LayerList.LYR_B).setSource(geomCont1);
    //layerList.getLayer(LayerList.LYR_SELECT).setSource(new ListGeometryContainer());
    
    if (geomEditModel != null)
      layerList.getLayer(LayerList.LYR_RESULT).setSource(
          new ResultGeometryContainer(geomEditModel));

    Layer lyrA = layerList.getLayer(LayerList.LYR_A);
    lyrA.setGeometryStyle(new BasicStyle(AppColors.GEOM_A_LINE_CLR,
        AppColors.GEOM_A_FILL_CLR));
    
    Layer lyrB = layerList.getLayer(LayerList.LYR_B);
    lyrB.setGeometryStyle(new BasicStyle(AppColors.GEOM_B_LINE_CLR,
        AppColors.GEOM_B_FILL_CLR));
    
    Layer lyrR = layerList.getLayer(LayerList.LYR_RESULT);
    lyrR.setGeometryStyle(new BasicStyle(AppColors.GEOM_RESULT_LINE_CLR,
        AppColors.GEOM_RESULT_FILL_CLR));
    
    layerSelect.setGeometryStyle(new BasicStyle(AppColors.GEOM_SELECT_LINE_CLR,
        AppColors.GEOM_SELECT_FILL_CLR));
  }

  public void pasteGeometry(int geomIndex) throws Exception {
    Geometry g = readGeometryFromClipboard();
    getGeometryEditModel().setGeometry(geomIndex, g);
  }
  
  public Geometry readGeometryFromClipboard() throws Exception {
    Object obj = SwingUtil.getFromClipboard();
    Geometry g = null;
    if ( obj instanceof String ) {
      return readGeometryText((String) obj, getGeometryFactory());
    } else
      return (Geometry) obj;
  }
    
  private static Geometry readGeometryText(String geomStr, GeometryFactory geomFact) 
  throws Exception
  {
    Geometry g = null;
    if (geomStr.length() > 0) {
      try {
        MultiFormatReader reader = new MultiFormatReader(geomFact);
        g = reader.read(geomStr);
      } catch (ParseException ex) {
        String msg = "Unable to parse data: '" + ExceptionFormatter.condense(geomStr) + "'";  
        throw new IllegalArgumentException(msg); 
      }
    }
    return g;
    }
    
  public void loadMultipleGeometriesFromFile(int geomIndex, String filename)
  throws Exception 
  {
    Geometry g = IOUtil.readFile(filename, getGeometryFactory());
    TestCaseEdit testCaseEdit = getCurrentCase();
    testCaseEdit.setGeometry(geomIndex, g);
    testCaseEdit.setName(filename);
    getGeometryEditModel().setTestCase(testCaseEdit);
  }
  
  public void loadGeometryText(String wktA, String wktB) throws ParseException, IOException {
    MultiFormatReader reader = new MultiFormatReader(new GeometryFactory(getPrecisionModel(),0));
    
    // read geom A
    Geometry g0 = null;
    if (wktA.length() > 0) {
      g0 = reader.read(wktA);
    }
    
    // read geom B
    Geometry g1 = null;
    if (wktB.length() > 0) {
      g1 = reader.read(wktB);
    }
    
    TestCaseEdit testCaseEdit = getCurrentCase();
    testCaseEdit.setGeometry(0, g0);
    testCaseEdit.setGeometry(1, g1);
    getGeometryEditModel().setTestCase(testCaseEdit);
  }
  
  //=============================================================
  
  private CaseList caseList = new CaseList(new CaseList.CaseFactory() {
    public TestCaseEdit create() {
      return new TestCaseEdit(precisionModel);
    }
  });

  public CaseList cases() {
    return caseList;
  }
  public TestCaseEdit getCurrentCase() {
    return caseList.getCurrentCase();
  }
  public int getCurrentCaseIndex() {
    return caseList.getCurrentTestIndex();
  }
  public int getCasesSize() {
    return caseList.getSize();
  }
  public List getCases() {
    return caseList.getCases();
  }
  public TestCaseList getTestCaseList() {
    return caseList.tcList;
  }
  public void addCase(Geometry[] geoms) {
    addCase(geoms, null);
  }

  public void addCase(Geometry[] geoms, String name) {
    TestCaseEdit tc = new TestCaseEdit(geoms, name);
    caseList.addCase(tc);
  }

  //================================================================= 
  
  public void openXmlFilesAndDirectories(File[] files) throws Exception {
     TestCaseList testCaseList = createTestCaseList(files);
    PrecisionModel precisionModel = new PrecisionModel();
    if (!testCaseList.getList().isEmpty()) {
      TestRunnerTestCaseAdapter a = (TestRunnerTestCaseAdapter) testCaseList.getList().get(0);
      precisionModel = a.getTestRunnerTestCase().getTestRun().getPrecisionModel();
    }
    if (getCases().size() == 1
         && ((Testable) getCases().get(0)).getGeometry(0) == null
         && ((Testable) getCases().get(0)).getGeometry(1) == null) {
      loadTestCaseList(testCaseList, precisionModel);
    }
    else {
      TestCaseList newList = new TestCaseList();
      newList.add(getTestCaseList());
      int firstIndex = newList.size();
      newList.add(testCaseList);
      loadTestCaseList(newList, precisionModel);
      caseList.setCurrentTestIndex(firstIndex);
    }
  }

  void loadTestCaseList(TestCaseList tcl, PrecisionModel precisionModel) throws Exception {
    setPrecisionModel(precisionModel);
    if (tcl != null) {
      loadEditList(tcl);
    }
  }

  public void loadEditList(TestCaseList tcl) throws ParseException {
    TestCaseList newTcl = new TestCaseList();
    for (Iterator i = tcl.getList().iterator(); i.hasNext();) {
      Testable tc = (Testable) i.next();

      if (tc instanceof TestCaseEdit) {
        newTcl.add((TestCaseEdit) tc);
      } else {
        newTcl.add(new TestCaseEdit(tc));
      }
    }
    caseList.init(newTcl);
  }

  private TestCaseList createTestCaseList(File[] filesAndDirectories) {
    TestCaseList testCaseList = new TestCaseList();
    for (int i = 0; i < filesAndDirectories.length; i++) {
      File fileOrDirectory = filesAndDirectories[i];
      if (fileOrDirectory.isFile()) {
        testCaseList.add(createTestCaseList(fileOrDirectory));
      }
      else if (fileOrDirectory.isDirectory()) {
        testCaseList.add(createTestCaseListFromDirectory(fileOrDirectory));
      }
    }
    return testCaseList;
  }

  private TestCaseList createTestCaseListFromDirectory(File directory) {
    Assert.isTrue(directory.isDirectory());
    TestCaseList testCaseList = new TestCaseList();
    List files = Arrays.asList(directory.listFiles());
    for (Iterator i = files.iterator(); i.hasNext(); ) {
      File file = (File) i.next();
      testCaseList.add(createTestCaseList(file));
    }
    return testCaseList;
  }

  private TestCaseList createTestCaseList(File xmlTestFile) 
  {
    TestReader testReader = new TestReader();
    TestRun testRun = testReader.createTestRun(xmlTestFile, 1);
    parseErrors = testReader.getParsingProblems();

    TestCaseList tcl = new TestCaseList();
    if (hasParseErrors()) {
      return tcl;
    }
    for (Iterator i = testRun.getTestCases().iterator(); i.hasNext(); ) {
      org.locationtech.jtstest.testrunner.TestCase testCase = (org.locationtech.jtstest.testrunner.TestCase) i.next();
      tcl.add(new TestRunnerTestCaseAdapter(testCase));
    }
    return tcl;
  }

  private List parseErrors = null;

  /**
   * 
   * @return empy list if no errors
   */
  public List getParsingProblems()
  {
    return parseErrors; 
  }
  
  public boolean hasParseErrors()
  {
    if (parseErrors == null) return false;
    return parseErrors.size() > 0;
  }
 
  public void setResult(Object result)
  {
  	currResult = result;
    if (result == null || result instanceof Geometry) {
    	getCurrentCase().setResult((Geometry) result);
    }
  }
  
  public Object getResult()
  {
  	return currResult;
  }
  public void setOpName(String opName)
  {
    if (opName == null) {
      this.opName = "";
    }
    else { 
      this.opName = StringUtil.capitalize(opName);
    }
  }
  
  public String getOpName()
  {
    return opName;
  }
  
  public void copyResult(boolean isFormatted)
  {
    SwingUtil.copyToClipboard(currResult, isFormatted);
  }

  private ArrayList wktABeforePMChange = new ArrayList();
  private ArrayList wktBBeforePMChange = new ArrayList();

  public void changePrecisionModel(PrecisionModel precisionModel)
  throws ParseException
  {
    saveWKTBeforePMChange();
    setPrecisionModel(precisionModel);
    loadWKTAfterPMChange();
  }
  
  private void saveWKTBeforePMChange() {
    wktABeforePMChange.clear();
    wktBBeforePMChange.clear();
    for (Iterator i = getCases().iterator(); i.hasNext(); ) {
      Testable testable = (Testable) i.next();
      Geometry a = testable.getGeometry(0);
      Geometry b = testable.getGeometry(1);
      wktABeforePMChange.add(a != null ? a.toText() : null);
      wktBBeforePMChange.add(b != null ? b.toText() : null);
    }
  }

  private void loadWKTAfterPMChange() throws ParseException {
    WKTReader reader = new WKTReader(new GeometryFactory(getPrecisionModel(), 0));
    for (int i = 0; i < getCases().size(); i++) {
      Testable testable = (Testable) getCases().get(i);
      String wktA = (String) wktABeforePMChange.get(i);
      String wktB = (String) wktBBeforePMChange.get(i);
      testable.setGeometry(0, wktA != null ? reader.read(wktA) : null);
      testable.setGeometry(1, wktB != null ? reader.read(wktB) : null);
    }
  }

  /**
   * Encapsulates test case cursor logic. 
   * @author Martin Davis
   *
   */
  public static class CaseList {
    
    public static interface CaseFactory {
      TestCaseEdit create();
    }

    private TestCaseList tcList = new TestCaseList();
    private int tcIndex = -1;
    private CaseFactory caseFactory;
  
    public CaseList(CaseFactory caseFactory) {
      this.caseFactory = caseFactory;
    }
    public void init()
    {
      tcList = new TestCaseList();
      // ensure that there is always a valid TestCase in the list
      createNew();
    }
    
    public void init(TestCaseList tcl) {
      tcList = tcl;
      if (tcList.size() > 0) {
        tcIndex = 0;
      }
      else {
        createNew();
      }
    }
  
    public List getCases() {
      return Collections.unmodifiableList(tcList.getList());
    }
  
    public void setCurrent(TestCaseEdit testCase) {
      for (int i = 0; i < tcList.size(); i++) {
        if (tcList.get(i) == testCase) {
          tcIndex = i;
          return;
        }
      }
    }
    
    public TestCaseEdit getCurrentCase()
    {
      return (TestCaseEdit) getCurrentTestable();
    }
    
    public Testable getCurrentTestable() {
      return (TestCaseEdit) tcList.get(tcIndex);
    }
  
    public int getCurrentTestIndex()
    {
      return tcIndex;
    }
    public void setCurrentTestIndex(int i) {
      tcIndex = MathUtil.clamp(i,  0, getSize() -1 );
    }
    public TestCaseList getTestList()
    {
      return tcList;
    }
    
    public int getSize()
    {
      return tcList.getList().size();
    }
    public void prevCase() {
      if (tcIndex > 0)
        tcIndex--;
    }
  
    public void nextCase() {
      if (tcIndex < tcList.size() - 1)
        tcIndex++;
    }
  
    public void copyCase() {
      TestCaseEdit copy = null;
      copy = new TestCaseEdit(getCurrentCase());
      addCase(copy);
    }
    
    public void createNew() {
      addCase( caseFactory.create());
    }
    
    private void addCase(TestCaseEdit testcase) {
      if (tcIndex < 0) {
        tcList.add(testcase);
      }
      else {
        tcList.add(testcase, tcIndex+1);
      }
      tcIndex++;
    }
  
    public void deleteCase() {
      tcList.remove(tcIndex);
      if (tcList.size() == 0) {
        createNew();
      }
      if (tcIndex >= tcList.size())
        tcIndex = tcList.size() - 1;
    }  
  
  }
  
  public Layer layerCopy(Layer lyr) {
    if (layerListTop.contains(lyr)) {
      return layerListTop.copy(lyr);
    }
    // get here if copying a base layer, OR copying a fixed layer
    return layerListBase.copy(lyr);
  }

  public void layerDelete(Layer lyr) {
    if (layerListBase.contains(lyr)) {
      layerListBase.remove(lyr);
    }
    else if (layerListTop.contains(lyr)) {
      layerListTop.remove(lyr);
    } 
  }

  public void layerUp(Layer lyr) {
    if (layerListBase.contains(lyr)) {
      if (layerListBase.isTop(lyr)) {
        layerListBase.remove(lyr);
        layerListTop.addBottom(lyr);
      }
      else {
        layerListBase.moveUp(lyr);
      }
    }
    else if (layerListTop.contains(lyr)) {
      layerListTop.moveUp(lyr);
    } 
  }

  public void layerDown(Layer lyr) {
    if (layerListBase.contains(lyr)) {
      layerListBase.moveDown(lyr);
    }
    else if (layerListTop.contains(lyr)) {
      if (layerListTop.isBottom(lyr)) {
        layerListTop.remove(lyr);
        layerListBase.addTop(lyr);
      }
      layerListTop.moveDown(lyr);
    } 
  }

  public void setSelection(Geometry geometry) {
    layerSelect.setGeometry(geometry);
  }

  /*
  public void addSelection(Geometry geometry) {
    ListGeometryContainer src = (ListGeometryContainer) layerSelect.getSource();
    src.add(geometry);
  }
*/
  
  public void clearSelection() {
    layerSelect.getSource().clear();
  }

}


