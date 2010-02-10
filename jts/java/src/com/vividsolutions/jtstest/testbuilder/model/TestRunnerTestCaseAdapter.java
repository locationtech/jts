

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
package com.vividsolutions.jtstest.testbuilder.model;
import java.util.Arrays;
import java.util.Iterator;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jtstest.geomop.*;
import com.vividsolutions.jtstest.test.Testable;
import com.vividsolutions.jtstest.testrunner.BooleanResult;
import com.vividsolutions.jtstest.testrunner.GeometryResult;
import com.vividsolutions.jtstest.testrunner.Result;
import com.vividsolutions.jtstest.testrunner.SimpleReportWriter;
import com.vividsolutions.jtstest.testrunner.Test;
import com.vividsolutions.jtstest.testrunner.TestCase;

/**
 * @version 1.7
 */
public class TestRunnerTestCaseAdapter
     implements Testable {
  private TestCase testCase;
  private boolean ranAtLeastOnce = false;
  private WKTWriter wktWriter = new WKTWriter();

  public TestRunnerTestCaseAdapter(com.vividsolutions.jtstest.testrunner.TestCase testCase) {
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
      com.vividsolutions.jtstest.testrunner.Test test = (com.vividsolutions.jtstest.testrunner.Test) i.next();
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
      com.vividsolutions.jtstest.testrunner.Test test = (com.vividsolutions.jtstest.testrunner.Test) i.next();
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
      com.vividsolutions.jtstest.testrunner.Test test = (com.vividsolutions.jtstest.testrunner.Test) i.next();
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

