

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
package org.locationtech.jtstest.testbuilder.model;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.util.Assert;
import org.locationtech.jtstest.test.TestCaseList;
import org.locationtech.jtstest.test.Testable;
import org.locationtech.jtstest.testbuilder.AppStrings;
import org.locationtech.jtstest.testbuilder.BusyDialog;
import org.locationtech.jtstest.testbuilder.GeometryEditPanel;
import org.locationtech.jtstest.testrunner.BooleanResult;
import org.locationtech.jtstest.testrunner.Test;
import org.locationtech.jtstest.util.FileUtil;
import org.locationtech.jtstest.util.StringUtil;


/**
 *  An object that creates an .html file describing the test cases. .gif files
 *  are also created.
 *
 * @version 1.7
 */
public class HtmlWriter {
  private final static int IMAGE_WIDTH = 200;
  private final static int IMAGE_HEIGHT = 200;
  private final static int STACK_TRACE_DEPTH = 1;

  private boolean showingABwithSpatialFunction = true;
  private GeometryEditPanel geometryEditPanel = new GeometryEditPanel();
  private JFrame frame = new JFrame();
  private File outputDirectory;
  private BusyDialog busyDialog = null;

  public HtmlWriter() {
    geometryEditPanel.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
    geometryEditPanel.setGridEnabled(false);
    geometryEditPanel.setBorder(BorderFactory.createEmptyBorder());
    frame.getContentPane().add(geometryEditPanel);
  }

  public void setShowingABwithSpatialFunction(boolean showingABwithSpatialFunction) {
    this.showingABwithSpatialFunction = showingABwithSpatialFunction;
  }

  public void setBusyDialog(BusyDialog busyDialog) {
    this.busyDialog = busyDialog;
  }

  private static class MapAndList {
    public Map map;
    public List list;
  }

  public void write(File outputDirectory, TestCaseList testCaseList, PrecisionModel precisionModel) throws IOException {
    if (busyDialog != null) {
      busyDialog.setDescription("Saving .html and .gif files");
    }
    Assert.isTrue(outputDirectory.isDirectory());
    this.outputDirectory = outputDirectory;
    MapAndList runMapAndRuns = runMapAndRuns(testCaseList);
    Map runMap = runMapAndRuns.map;
    List runs  = runMapAndRuns.list;
    createHtmlFile("contents-frame.html", indexHtml(runs, runMap, precisionModel));
    createHtmlFile("index.html", testTopHtml());
    int runSkey = 0;
    for (Iterator i = runs.iterator(); i.hasNext(); ) {
      String runDescription = (String) i.next();
      runSkey++;
      List testables = (List) runMap.get(runDescription);
      int caseSkey = 0;
      for (Iterator m = testables.iterator(); m.hasNext(); ) {
        Testable testable = (Testable) m.next();
        caseSkey++;
        if (busyDialog != null) {
          busyDialog.setDescription("Saving .html and .gif files: " + caseSkey
               + " of " + testCaseList.getList().size() + " tests");
        }
        createHtmlFile("Run" + runSkey + AppStrings.LABEL_TEST_CASE + caseSkey + ".html", html(testable, runSkey, caseSkey));
      }
    }
  }

  private String html(Testable testable, int runSkey, int caseSkey) throws IOException {
    TestCaseEdit testCaseEdit = (TestCaseEdit) testable;
    String html =
        "<HTML>" + StringUtil.newLine
         + "<HEAD>" + StringUtil.newLine
         + "<TITLE>" + StringUtil.escapeHTML(testName(testCaseEdit, caseSkey)) + "</TITLE>" + StringUtil.newLine
         + "<link REL='STYLESHEET' HREF='../jts.css' TYPE='Text/css'>" + StringUtil.newLine
         + "</HEAD>" + StringUtil.newLine
         + "<BODY>" + StringUtil.newLine
         + "<div class='testTitle'>" + StringUtil.escapeHTML(testName(testCaseEdit, caseSkey)) + "</div>" + StringUtil.newLine
         + "<P>" + StringUtil.newLine;
    html += htmlForAB(testCaseEdit, runSkey, caseSkey);
    html += htmlForTests(testCaseEdit, runSkey, caseSkey);
    html += "</BODY>" + StringUtil.newLine + "</HTML>";
    return html;
  }

  private String deleteLastTag(String html) {
    if (html.lastIndexOf("<") == -1) {
      return html;
    }
    return html.substring(0, html.lastIndexOf("<"));
  }

  private String deleteFirstTag(String html) {
    if (html.lastIndexOf(">") == -1) {
      return html;
    }
    return html.substring(html.indexOf(">") + 1);
  }

  private String htmlForTests(TestCaseEdit testCaseEdit, int runSkey, int caseSkey) throws IOException {
    String html = htmlForBinaryPredicates(testCaseEdit, caseSkey);
    html += htmlForSpatialFunctions(testCaseEdit, runSkey, caseSkey);
    html += htmlForTopologyMethods(testCaseEdit, runSkey, caseSkey);
    return html;
  }

  private String htmlForSpatialFunctionTest(TestCaseEdit testCaseEdit, int runSkey, int caseSkey,
      String geometryOpName, String first, String second) {
    String actualResultString = "&nbsp;";
    try {
      Geometry actualResult = (Geometry) actualResult(testCaseEdit, geometryOpName,
          first, second);
      String filenameNoPath = "Run" + runSkey + AppStrings.LABEL_TEST_CASE + caseSkey + geometryOpName + "Actual";
      if (first != null) {
        filenameNoPath += first;
      }
      if (second != null) {
        filenameNoPath += second;
      }
      filenameNoPath += ".gif";
      actualResultString = htmlImageHtmlTextTable(filenameNoPath, "<SPAN class=wktR>" + actualResult.toText() + "</SPAN>",
          0);
      createGifFile(filenameNoPath, testCaseEdit.getGeometry(0),
          testCaseEdit.getGeometry(1), actualResult,
          showingABwithSpatialFunction, IMAGE_WIDTH, IMAGE_HEIGHT);
    }
    catch (Exception e) {
      actualResultString = "<TD>" + StringUtil.replace(StringUtil.getStackTrace(e, STACK_TRACE_DEPTH),
          "\n", "<BR>", true) + "</TD>";
      e.printStackTrace(System.out);
    }
    String html
         = "  <TR>" + StringUtil.newLine
         + "    <TD class=methodTitle>" + geometryOpName + "</TD>" + StringUtil.newLine
         + actualResultString + StringUtil.newLine
         + "  </TR>" + StringUtil.newLine;
    return html;
  }

  private String htmlForRelateTest(TestCaseEdit testCaseEdit, int caseSkey) {
    String actualValue;
    try {
      actualValue = insertParagraphs(testCaseEdit.getGeometry(0).relate(testCaseEdit.getGeometry(1)).toString());
    }
    catch (Exception e) {
      actualValue = StringUtil.replace(StringUtil.getStackTrace(e, STACK_TRACE_DEPTH),
          "\n", "<BR>", true);
      e.printStackTrace(System.out);
    }
    String html
         = "  <TR>" + StringUtil.newLine
         + "    <TD class=methodTitle rowspan=9>relate</TD>" + StringUtil.newLine
         + "    <TD rowspan=9>" + actualValue + "</TD>" + StringUtil.newLine
         + "  </TR>" + StringUtil.newLine;
    return html;
  }

  private String insertParagraphs(String intersectionMatrix) {
    StringBuffer buffer = new StringBuffer(intersectionMatrix);
    buffer.insert(6, "<BR>");
    buffer.insert(3, "<BR>");
    return buffer.toString();
  }

  private String htmlForPredicateTest(TestCaseEdit testCaseEdit, int caseSkey,
      String opName, String first, String second) {
    String actualResultString;
    try {
      actualResultString = actualResult(testCaseEdit, opName, first, second).toString();
    }
    catch (Exception e) {
      actualResultString = StringUtil.replace(StringUtil.getStackTrace(e, STACK_TRACE_DEPTH),
          "\n", "<BR>", true);
      e.printStackTrace(System.out);
    }
    String html
         = "  <TR>" + StringUtil.newLine
         + "    <TD class=methodTitle>" + opName + "</TD>" + StringUtil.newLine
         + "    <TD class=resultFalse>" + actualResultString + "</TD>" + StringUtil.newLine
         + "  </TR>" + StringUtil.newLine;
    return html;
  }

  private Object actualResult(TestCaseEdit testCaseEdit, String opName, String first,
      String second) throws Exception {
    try {
      Assert.isTrue((first.equalsIgnoreCase("A")) || (first.equalsIgnoreCase("B")));
      Class geometryClass = Class.forName("com.vividsolutions.jts.geom.Geometry");
      Geometry source = testCaseEdit.getGeometry(first.equalsIgnoreCase("A") ? 0 : 1);
      Object[] target;
      Class[] targetClasses;
      if (second == null) {
        target = new Object[]{};
        targetClasses = new Class[]{};
      }
      else {
        target = new Object[]{
            testCaseEdit.getGeometry(second.equalsIgnoreCase("A") ? 0 : 1)
            };
        targetClasses = new Class[]{
            geometryClass
            };
      }
      Method op = geometryClass.getMethod(opName, targetClasses);
      return op.invoke(source, target);
    }
    catch (InvocationTargetException e) {
      throw (Exception) e.getTargetException();
    }
  }

  private BooleanResult expectedPredicateResult(TestCaseEdit testCaseEdit,
      String opName, String first, String second) {
    if (!(testCaseEdit.getTestable() instanceof TestRunnerTestCaseAdapter)) {
      return null;
    }
    TestRunnerTestCaseAdapter adapter = (TestRunnerTestCaseAdapter) testCaseEdit.getTestable();
    org.locationtech.jtstest.testrunner.TestCase trTestCase = adapter.getTestRunnerTestCase();
    for (Iterator i = trTestCase.getTests().iterator(); i.hasNext(); ) {
      Test test = (Test) i.next();
      if (test.getOperation().equalsIgnoreCase(opName)
           && test.getGeometryIndex().equalsIgnoreCase(first)
           && (test.getArgumentCount() == 0
           || ((test.getArgument(0) != null && test.getArgument(0).equalsIgnoreCase(second))
           || (test.getArgument(0) == null && second.equalsIgnoreCase("null"))))) {
        return (BooleanResult) test.getExpectedResult();
      }
    }
    return null;
  }

  private String htmlForAB(TestCaseEdit testCaseEdit, int runSkey, int caseSkey) throws IOException {
    String wktHtml
         = "<span class=wktA>"
         + (testCaseEdit.getGeometry(0) == null ? " " : testCaseEdit.getGeometry(0).toText())
         + "</span>"
         + "<P>"
         + "<span class=wktB>"
         + (testCaseEdit.getGeometry(1) == null ? " " : testCaseEdit.getGeometry(1).toText())
         + "</span>";
    String html = StringUtil.newLine
         + "<TABLE BORDER=0>" + StringUtil.newLine
         + "  <TR>" + StringUtil.newLine
         + htmlImageHtmlTextTable("Run" + runSkey + AppStrings.LABEL_TEST_CASE + caseSkey + ".gif", wktHtml, 0)
         + "  </TR>" + StringUtil.newLine
         + "</TABLE>" + StringUtil.newLine;
    createGifFile("Run" + runSkey + AppStrings.LABEL_TEST_CASE + caseSkey + ".gif", testCaseEdit.getGeometry(0),
        testCaseEdit.getGeometry(1), null, true, IMAGE_WIDTH, IMAGE_HEIGHT, true);
    return html;
  }

  private String htmlImageTextTable(String imageFilename, String text, int border) {
    return htmlImageHtmlTextTable(imageFilename, StringUtil.escapeHTML(text), border);
  }

  private String htmlImageHtmlTextTable(String imageFilename, String html, int border) {
    return
        "    <TD>" + StringUtil.newLine
         + "      <IMG BORDER=\"1\" SRC=\"" + imageFilename + "\" WIDTH=" + IMAGE_WIDTH + " HEIGHT=" + IMAGE_HEIGHT + ">" + StringUtil.newLine
         + "    </TD>" + StringUtil.newLine
         + "    <TD>" + StringUtil.newLine
         + html + StringUtil.newLine
         + "    </TD>" + StringUtil.newLine;
  }

  private String testName(Testable testable, int caseSkey) {
    String name = testable.getName();
    if ((name == null || name.length() == 0) && testable instanceof TestCaseEdit) {
      name = ((TestCaseEdit) testable).getDescription();
    }
    String testTag = AppStrings.LABEL_TEST_CASE + " ";
    if (name == null || name.length() == 0) {
      name = testTag + caseSkey;
    }
    else {
      name = testTag + caseSkey + ": " + name;
    }
    return name;
  }

  private String runName(String runDescription, int runSkey) {
    return "Run " + runSkey + ": " + runDescription;
  }

  private String htmlTitle(PrecisionModel precisionModel) {
    String html = "Precision Model: scale=" + precisionModel.getScale()         
         + StringUtil.newLine;
    html = "<div class='precisionModel'>" + html + "</div>";
    return html;
  }

  private void createGifFile(String filenameNoPath, Geometry a, Geometry b,
      Geometry spatialFunction, boolean showingAB,
      int imageWidth, int imageHeight) throws FileNotFoundException, IOException {
    createGifFile(filenameNoPath, a, b, spatialFunction, showingAB, imageWidth, imageHeight, false);
  }

  /*
  private void createGifFile(String filenameNoPath, Geometry a, Geometry b,
      Geometry spatialFunction, boolean showingAB,
      int imageWidth, int imageHeight, boolean zoomToFullExtent) throws FileNotFoundException,
      IOException {
    GeometryBuilder builderA = a != null ? GeometryBuilder.create(a) : null;
    GeometryBuilder builderB = b != null ? GeometryBuilder.create(b) : null;
    GeometryBuilder builderSpatialFunction = spatialFunction != null ? GeometryBuilder.create(spatialFunction) : null;
    createGifFile(filenameNoPath, builderA, builderB, builderSpatialFunction,
        showingAB, imageWidth, imageHeight, zoomToFullExtent);
  }
*/
  
  private void createGifFile(String filenameNoPath, Geometry a,
      Geometry b,
      Geometry result,
      boolean showingAB,
      int imageWidth, int imageHeight, boolean zoomToFullExtent) throws FileNotFoundException,
      IOException {
  	TestBuilderModel tbModel = new TestBuilderModel();
    TestCaseEdit tc = new TestCaseEdit(new Geometry[]{ a, b });
    tc.setResult(result);
    tbModel.getGeometryEditModel().setTestCase(tc);
    geometryEditPanel.setModel(tbModel);
    if (zoomToFullExtent) {
      geometryEditPanel.zoomToFullExtent();
    }
    geometryEditPanel.setShowingResult(result != null);
    geometryEditPanel.setShowingGeometryA(a != null && showingAB);
    geometryEditPanel.setShowingGeometryB(b != null && showingAB);
    String filenameWithPath = outputDirectory.getPath() + "\\" + filenameNoPath;
    Image image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
    geometryEditPanel.paint(image.getGraphics());
    /*
    // disabled - should be replaced with PNG output
    FileOutputStream outputStream = new FileOutputStream(filenameWithPath,
        false);
    GifEncoder gifEncoder = new GifEncoder(image, outputStream);
    gifEncoder.setDimensions(imageWidth, imageHeight);
    gifEncoder.encode();
    outputStream.flush();
    outputStream.close();
    */
  }

  private void createHtmlFile(String filename, String html) throws IOException {
    String pathname = outputDirectory.getPath() + "\\" + filename;
    FileUtil.setContents(pathname, html);
  }

  private MapAndList runMapAndRuns(TestCaseList testCaseList) {
    Map runMap = new TreeMap();
    List runs = new ArrayList();
    for (Iterator i = testCaseList.getList().iterator(); i.hasNext(); ) {
      TestCaseEdit testCaseEdit = (TestCaseEdit) i.next();
      Testable testable = testCaseEdit.getTestable();
      if (testable instanceof TestRunnerTestCaseAdapter) {
        org.locationtech.jtstest.testrunner.TestCase testRunnerTestCase = ((TestRunnerTestCaseAdapter) testable).getTestRunnerTestCase();
        String runDescription = testRunnerTestCase.getTestRun().getTestFile().getName();
        runDescription = runDescription.indexOf(".") > -1 ? runDescription.substring(0, runDescription.indexOf(".")) : runDescription;
        addToListMapAndList(runDescription, testCaseEdit, runMap, runs);
      }
      else {
        addToListMapAndList("Other", testCaseEdit, runMap, runs);
      }
    }
    MapAndList runMapAndRuns = new MapAndList();
    runMapAndRuns.map = runMap;
    runMapAndRuns.list = runs;
    return runMapAndRuns;
  }

  private void addToListMapAndList(String key, Object valueItem, Map stringToList, List keyList) {
    if (stringToList.containsKey(key)) {
      List value = (List) stringToList.get(key);
      value.add(valueItem);
    }
    else {
      List value = new ArrayList();
      value.add(valueItem);
      stringToList.put(key, value);
      keyList.add(key);
    }
  }

  private String indexHtml(List runs, Map runMap, PrecisionModel precisionModel) {
    String html
         = "<HTML>" + StringUtil.newLine
         + "<HEAD>" + StringUtil.newLine
         + "<TITLE>JTS Test Suite Index</TITLE>" + StringUtil.newLine
         + "<link REL='STYLESHEET' HREF='../jts.css' TYPE='Text/css'>" + StringUtil.newLine
         + "<script LANGUAGE=\"JavaScript\">" + StringUtil.newLine
         + "  function LoadDetailFrame() {" + StringUtil.newLine
         + "        testNumber = document.main_form.test_combo.selectedIndex;" + StringUtil.newLine
         + "        testHtmlFile = document.main_form.test_combo.options[testNumber].value;" + StringUtil.newLine
         + "        parent.detail.location.href=testHtmlFile;" + StringUtil.newLine
         + "        document.main_form.test_combo.blur();" + StringUtil.newLine
         + "  }" + StringUtil.newLine
         + "  function onRunChange() {" + StringUtil.newLine
         + "        selectedIndex = document.main_form.run_combo.selectedIndex;" + StringUtil.newLine
         + "        selectedCode  = document.main_form.run_combo.options[selectedIndex].value;" + StringUtil.newLine;
    int runSkey = 0;
    for (Iterator i = runs.iterator(); i.hasNext(); ) {
      String runDescription = (String) i.next();
      runSkey++;
      html += "        if (selectedCode == 'Run" + runSkey + "') {" + StringUtil.newLine;
      List testables = (List) runMap.get(runDescription);
      int caseSkey = 0;
      for (Iterator m = testables.iterator(); m.hasNext(); ) {
        Testable testable = (Testable) m.next();
        caseSkey++;
        html += "              document.main_form.test_combo.length = " + caseSkey + ";" + StringUtil.newLine;
        html += "              document.main_form.test_combo.options[" + (caseSkey - 1) + "].text  = \"" + StringUtil.escapeHTML(testName(testable, caseSkey)) + "\";" + StringUtil.newLine;
        html += "              document.main_form.test_combo.options[" + (caseSkey - 1) + "].value  = 'Run" + runSkey + "Case" + caseSkey + ".html';" + StringUtil.newLine;
      }
      html += "        LoadDetailFrame();";
      html += "  }";
    }
    html += "  }" + StringUtil.newLine
         + "</script>" + StringUtil.newLine
         + "</HEAD>" + StringUtil.newLine
         + "<BODY>" + StringUtil.newLine
         + "<h1>JTS Validation Suite</h1>" + StringUtil.newLine
         + htmlTitle(precisionModel)
         + "<p>" + StringUtil.newLine
         + "<FORM id=\"main_form\" name=\"main_form\">" + StringUtil.newLine;

    html += "<select id=run_combo name=run_combo size='1' style='width:30%' onChange='onRunChange()'>" + StringUtil.newLine;
    runSkey = 0;
    for (Iterator j = runs.iterator(); j.hasNext(); ) {
      String runDescription = (String) j.next();
      runSkey++;
      html += "<OPTION VALUE='Run" + runSkey + "'>"
           + StringUtil.escapeHTML(runName(runDescription, runSkey))
           + "</OPTION>" + StringUtil.newLine;
    }
    html += "</select>" + StringUtil.newLine;

    html += "<select id=test_combo name=test_combo size='1' style='width:60%' onChange='LoadDetailFrame()'>" + StringUtil.newLine;
    String runDescription = (String) runs.iterator().next();
    List testables = (List) runMap.get(runDescription);
    int caseSkey = 0;
    for (Iterator m = testables.iterator(); m.hasNext(); ) {
      Testable testable = (Testable) m.next();
      caseSkey++;
      html += "<OPTION VALUE='Run1Case" + caseSkey + ".html'>"
           + StringUtil.escapeHTML(testName(testable, caseSkey))
           + "</OPTION>" + StringUtil.newLine;
    }
    html += "</select>" + StringUtil.newLine;

    html += "</FORM>" + StringUtil.newLine
         + "</BODY>" + StringUtil.newLine
         + "</HTML>" + StringUtil.newLine
         + "" + StringUtil.newLine;
    return html;
  }

  private String testTopHtml() {
    return
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Frameset//EN\"\"http://www.w3.org/TR/REC-html40/frameset.dtd\">" + StringUtil.newLine
         + "<HTML>" + StringUtil.newLine
         + "<HEAD>" + StringUtil.newLine
         + "<TITLE>" + StringUtil.newLine
         + "JTS Validation Suite" + StringUtil.newLine
         + "</TITLE>" + StringUtil.newLine
         + "</HEAD>" + StringUtil.newLine
         + "<FRAMESET rows=\"120px,*\" framespacing=0 frameborder=0>" + StringUtil.newLine
         + "<FRAME id=contents name=\"contents\"  FRAMEBORDER=0 src=\"contents-frame.html\" scrolling=no>" + StringUtil.newLine
         + "<FRAME id=detail name=\"detail\"	FRAMEBORDER=0 src=\"Run1Case1.html\" >" + StringUtil.newLine
         + "</FRAMESET>" + StringUtil.newLine
         + "<NOFRAMES>" + StringUtil.newLine
         + "<H2>" + StringUtil.newLine
         + "Frame Alert</H2>" + StringUtil.newLine
         + "" + StringUtil.newLine
         + "<P>" + StringUtil.newLine
         + "This site is designed to be viewed using frames. " + StringUtil.newLine
         + "If you see this message, you are using a non-frame-capable web client." + StringUtil.newLine
         + "</HTML>" + StringUtil.newLine;
  }

  private String htmlForBinaryPredicates(TestCaseEdit testCaseEdit, int caseSkey) {
    String html = "";
    if (testCaseEdit.getGeometry(1) != null) {
      html += htmlForRelateTest(testCaseEdit, caseSkey);
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "equals", "A", "B");
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "disjoint", "A", "B");
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "intersects", "A", "B");
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "touches", "A", "B");
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "crosses", "A", "B");
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "within", "A", "B");
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "contains", "A", "B");
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "overlaps", "A", "B");

      html = "<h2>Binary Predicates</h2>" + StringUtil.newLine
           + "<TABLE WIDTH=50% BORDER=1>" + StringUtil.newLine
           + html
           + "</TABLE>" + StringUtil.newLine;
    }
    return html;
  }

  private String htmlForSpatialFunctions(TestCaseEdit testCaseEdit, int runSkey, int caseSkey) {
    if (testCaseEdit.getExpectedConvexHull() == null
         && testCaseEdit.getExpectedIntersection() == null
         && testCaseEdit.getExpectedUnion() == null
         && testCaseEdit.getExpectedDifference() == null
         && testCaseEdit.getExpectedSymDifference() == null) {
      return "";
    }
    String html = "";
    html += htmlForSpatialFunctionTest(testCaseEdit, runSkey, caseSkey, "convexHull", "A", null);
    if (testCaseEdit.getGeometry(1) != null) {
      html += htmlForSpatialFunctionTest(testCaseEdit, runSkey, caseSkey, "intersection", "A", "B");
      html += htmlForSpatialFunctionTest(testCaseEdit, runSkey, caseSkey, "union", "A", "B");
      html += htmlForSpatialFunctionTest(testCaseEdit, runSkey, caseSkey, "difference", "A", "B");
      html += htmlForSpatialFunctionTest(testCaseEdit, runSkey, caseSkey, "symDifference", "A", "B");
    }

    html = "<h2>Spatial Analysis Methods</h2>" + StringUtil.newLine
         + "<TABLE BORDER=1>" + StringUtil.newLine
         + html
         + "</TABLE>" + StringUtil.newLine;
    return html;
  }

  private String htmlForTopologyMethods(TestCaseEdit testCaseEdit, int runSkey, int caseSkey) {
    boolean isSimpleSpecified = expectedPredicateResult(testCaseEdit, "isSimple", "A", null) != null;
    boolean getBoundarySpecified = testCaseEdit.getExpectedBoundary() != null;
    boolean isValidSpecified = expectedPredicateResult(testCaseEdit, "isValid", "A", null) != null;
    if (! isSimpleSpecified && ! getBoundarySpecified && ! isValidSpecified) {
      return "";
    }

    String html = "";
    if (isSimpleSpecified) {
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "isSimple", "A", null);
    }
    if (isValidSpecified) {
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "isValid", "A", null);
    }
    if (getBoundarySpecified) {
      html += htmlForSpatialFunctionTest(testCaseEdit, runSkey, caseSkey, "getBoundary", "A", null);
    }

    html = "<h2>Topology Methods (on A)</h2>" + StringUtil.newLine
         + "<TABLE BORDER=1>" + StringUtil.newLine
         + html
         + "</TABLE>" + StringUtil.newLine;
    return html;
  }
}

