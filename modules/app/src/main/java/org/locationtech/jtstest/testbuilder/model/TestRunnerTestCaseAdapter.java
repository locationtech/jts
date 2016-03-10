

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
import java.util.Arrays;
import java.util.Iterator;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.Assert;
import org.locationtech.jtstest.geomop.*;
import org.locationtech.jtstest.test.Testable;
import org.locationtech.jtstest.testrunner.BooleanResult;
import org.locationtech.jtstest.testrunner.GeometryResult;
import org.locationtech.jtstest.testrunner.Result;
import org.locationtech.jtstest.testrunner.SimpleReportWriter;
import org.locationtech.jtstest.testrunner.Test;
import org.locationtech.jtstest.testrunner.TestCase;


/**
 * @version 1.7
 */
public class TestRunnerTestCaseAdapter
     implements Testable {
  private TestCase testCase;
  private boolean ranAtLeastOnce = false;
  private WKTWriter wktWriter = new WKTWriter();

  public TestRunnerTestCaseAdapter(org.locationtech.jtstest.testrunner.TestCase testCase) {
    this.testCase = testCase;
  }

  public void setGeometry(int index, Geometry g) {
    if (index == 0) {
      testCase.setGeometryA(g);
    }
    else if (index == 1) {
      testCase.setGeometryB(g);
    }
    else {
      Assert.shouldNeverReachHere();
    }
  }

  public void setIntersectionMatrix(IntersectionMatrix im) { }

  public void setName(String name) {
    testCase.setDescription(name);
  }

  public void setExpectedIntersectionMatrix(String expectedIntersectionMatrix) {
    getOrCreateABTest("relate").setArgument(1, expectedIntersectionMatrix);
  }

  public void setExpectedConvexHull(Geometry expectedConvexHull) {
    setExpectedSpatialFunction("convexhull", expectedConvexHull);
  }

  public void setExpectedBoundary(Geometry expectedBoundary) {
    setExpectedSpatialFunction("getboundary", expectedBoundary);
  }

  public void setExpectedIntersection(Geometry expectedIntersection) {
    setExpectedSpatialFunction("intersection", expectedIntersection);
  }

  public void setExpectedUnion(Geometry expectedUnion) {
    setExpectedSpatialFunction("union", expectedUnion);
  }

  public void setExpectedDifference(Geometry expectedDifference) {
    setExpectedSpatialFunction("difference", expectedDifference);
  }

  public void setExpectedSymDifference(Geometry expectedSymDifference) {
    setExpectedSpatialFunction("symdifference", expectedSymDifference);
  }

  public void setExpectedCentroid(Geometry expected) {
    setExpectedSpatialFunction("centroid", expected);
  }

  public boolean isFailed() {
    if (!ranAtLeastOnce) {
      return false;
    }
    for (Iterator i = testCase.getTests().iterator(); i.hasNext(); ) {
      org.locationtech.jtstest.testrunner.Test test = (org.locationtech.jtstest.testrunner.Test) i.next();
      if (!test.isPassed()) {
        return true;
      }
    }
    return false;
  }

  public String getFailedMsg() {
    if (!ranAtLeastOnce) {
      return "";
    }
    for (Iterator i = testCase.getTests().iterator(); i.hasNext(); ) {
      org.locationtech.jtstest.testrunner.Test test = (org.locationtech.jtstest.testrunner.Test) i.next();
      if (!test.isPassed()) {
        SimpleReportWriter reportWriter = new SimpleReportWriter(false);
        return reportWriter.write(test);
      }
    }
    return "";
  }

  public String getName() {
    return testCase.getDescription();
  }

  public Geometry getGeometry(int index) {
    if (index == 0) {
      return testCase.getGeometryA();
    }
    else if (index == 1) {
      return testCase.getGeometryB();
    }
    Assert.shouldNeverReachHere();
    return null;
  }

  public IntersectionMatrix getIntersectionMatrix() {
    return testCase.getGeometryA().relate(testCase.getGeometryB());
  }

  public String getDescription() {
    return testCase.getDescription();
  }

  public boolean isPassed() {
    if (!ranAtLeastOnce) {
      return false;
    }
    for (Iterator i = testCase.getTests().iterator(); i.hasNext(); ) {
      org.locationtech.jtstest.testrunner.Test test = (org.locationtech.jtstest.testrunner.Test) i.next();
      if (!test.isPassed()) {
        return false;
      }
    }
    return true;
  }

  public String getWellKnownText(int index) {
    if (index == 0) {
      if (testCase.getGeometryA() == null) {
        return null;
      }
      return wktWriter.write(testCase.getGeometryA());
    }
    else if (index == 1) {
      if (testCase.getGeometryB() == null) {
        return null;
      }
      return wktWriter.write(testCase.getGeometryB());
    }
    Assert.shouldNeverReachHere();
    return null;
  }

  public TestCase getTestRunnerTestCase() {
    return testCase;
  }

  public String getExpectedIntersectionMatrix() {
    Test test = getABTest("relate");
    if (test == null) {
      return null;
    }
    return test.getArgument(1);
  }

  public Geometry getExpectedConvexHull() {
    return toGeometry(getABTest("convexhull"));
  }

  public Geometry getExpectedBoundary() {
    return toGeometry(getABTest("getboundary"));
  }

  public Geometry getExpectedIntersection() {
    return toGeometry(getABTest("intersection"));
  }

  public Geometry getExpectedUnion() {
    return toGeometry(getABTest("union"));
  }

  public Geometry getExpectedDifference() {
    return toGeometry(getABTest("difference"));
  }

  public Geometry getExpectedSymDifference() {
    return toGeometry(getABTest("symdifference"));
  }

  public void runTest() throws ParseException {
    ranAtLeastOnce = true;
    testCase.run();
  }

  public void initGeometry() throws ParseException { }

  public Geometry toGeometry(Test test) {
    if (test == null) {
      return null;
    }
    Assert.isTrue(test.getExpectedResult() instanceof GeometryResult);
    return ((GeometryResult) test.getExpectedResult()).getGeometry();
  }

  private void setExpectedSpatialFunction(String opName, Geometry expectedGeometry) {
    if (expectedGeometry == null) {
      getOrCreateABTest(opName).getTestCase().remove(getOrCreateABTest(opName));
      return;
    }
    getOrCreateABTest(opName).setResult(new GeometryResult(expectedGeometry));
  }

  private Test getOrCreateABTest(String opName) {
    Test testToReturn = getABTest(opName);
    if (testToReturn == null) {
      testToReturn = new Test(testCase, maxTestIndex(testCase) + 1, null, opName, "A",
          Arrays.asList(new String[]{"B"}), 
          getDefaultResult(opName),
          0);
      testCase.add(testToReturn);
    }
    return testToReturn;
  }

  private Result getDefaultResult(String opName) {
    if (GeometryMethodOperation.isBooleanFunction(opName)) {
      return new BooleanResult(true);
    }
    if (GeometryMethodOperation.isGeometryFunction(opName)) {
      return new GeometryResult(
      new GeometryFactory(testCase.getTestRun().getPrecisionModel(),
			0).createGeometryCollection(null));
    }
    Assert.shouldNeverReachHere();
    return null;
  }

  private Test getABTest(String opName) {
    Assert.isTrue(GeometryMethodOperation.isBooleanFunction(opName) 
    		|| GeometryMethodOperation.isGeometryFunction(opName));
    for (Iterator i = testCase.getTests().iterator(); i.hasNext(); ) {
      Test test = (Test) i.next();
      if (test.getOperation().equalsIgnoreCase(opName)
           && ((!opName.equalsIgnoreCase("relate"))
           || test.getExpectedResult().equals(new BooleanResult(true)))
           && (test.getGeometryIndex().equalsIgnoreCase("A"))
           && ((test.getArgumentCount() == 0) || (
          test.getArgument(0) != null
           && (test.getArgument(0).equalsIgnoreCase("B"))))) {
        return test;
      }
    }
    return null;
  }

  private int maxTestIndex(TestCase testCase) {
    int maxTestIndex = -1;
    for (Iterator i = testCase.getTests().iterator(); i.hasNext(); ) {
      Test test = (Test) i.next();
      maxTestIndex = Math.max(maxTestIndex, test.getTestIndex());
    }
    return maxTestIndex;
  }
}

