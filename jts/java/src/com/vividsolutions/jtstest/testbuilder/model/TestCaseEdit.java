

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

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jtstest.test.TestCase;
import com.vividsolutions.jtstest.test.Testable;
import com.vividsolutions.jtstest.testbuilder.JTSTestBuilder;


/**
 * @version 1.7
 */
public class TestCaseEdit implements Testable {
  private Geometry[] geom = new Geometry[2];
  private Testable testable;
  
  private String opName = "";
  private Geometry resultGeom = null;
  

  public TestCaseEdit(PrecisionModel pm) {
    TestCase testCase = new TestCase();
    testCase.setPrecisionModel(pm);
    testable = testCase;
  }

  public TestCaseEdit(Testable tc) throws ParseException {
    this.testable = tc;
    testable.initGeometry();
    setGeometry(0, testable.getGeometry(0));
    setGeometry(1, testable.getGeometry(1));
  }

  public TestCaseEdit(TestCaseEdit tce)  {
    this.testable = new TestCase();
    setGeometry(0, tce.getGeometry(0));
    setGeometry(1, tce.getGeometry(1));
  }

  public TestCaseEdit(Geometry[] geom) {
    this.testable = new TestCase();
    setGeometry(0, geom[0]);
    setGeometry(1, geom[1]);
  }

  public TestCaseEdit(Geometry[] geom, String name) {
    this.testable = new TestCase();
    setGeometry(0, geom[0]);
    setGeometry(1, geom[1]);
    testable.setName(name);
  }

  private static Geometry cloneGeometry(Geometry geom)
  {
    if (geom == null) return null;
    return (Geometry) geom.clone();
  }
  
  public void setGeometry(int i, Geometry geom) {
    testable.setGeometry(i, geom);
  }

  public void setIntersectionMatrix(IntersectionMatrix im) {
    testable.setIntersectionMatrix(im);
  }

  public void setName(String name) {
    testable.setName(name);
  }

  public Geometry getResult() 
  {
    return resultGeom;
  }
  
  public void setResult(Geometry geom)
  {
    resultGeom = geom;
  }
  
  public String getOpName() { return opName; }
  
  public void setOpName(String name) { opName = name; }
  
  public void setExpectedIntersectionMatrix(String expectedIntersectionMatrix) {
    testable.setExpectedIntersectionMatrix(expectedIntersectionMatrix);
  }

  public void setExpectedConvexHull(Geometry expectedConvexHull) {
    testable.setExpectedConvexHull(expectedConvexHull);
  }

  public void setExpectedBoundary(Geometry expectedBoundary) {
    testable.setExpectedBoundary(expectedBoundary);
  }

  public void setExpectedIntersection(Geometry expectedIntersection) {
    testable.setExpectedIntersection(expectedIntersection);
  }

  public void setExpectedUnion(Geometry expectedUnion) {
    testable.setExpectedUnion(expectedUnion);
  }

  public void setExpectedDifference(Geometry expectedDifference) {
    testable.setExpectedDifference(expectedDifference);
  }

  public void setExpectedSymDifference(Geometry expectedSymDifference) {
    testable.setExpectedSymDifference(expectedSymDifference);
  }

  public void setExpectedCentroid(Geometry expectedCentroid) {
    testable.setExpectedCentroid(expectedCentroid);
  }

  public Geometry getGeometry(int i) {
//    return geom[i];
    return testable.getGeometry(i);
  }

  public IntersectionMatrix getIM() {
    runRelate();
    return testable.getIntersectionMatrix();
  }

  public Testable getTestable() {
    return testable;
  }

  public boolean isFailed() {
    return testable.isFailed();
  }

  public String getFailedMsg() {
    return testable.getFailedMsg();
  }

  public String getName() {
    return testable.getName();
  }

  public IntersectionMatrix getIntersectionMatrix() {
    return testable.getIntersectionMatrix();
  }

  public String getDescription() {
    return testable.getDescription();
  }

  public boolean isPassed() {
    return testable.isPassed();
  }

  public String getWellKnownText(int i) {
    return testable.getWellKnownText(i);
  }

  public String getExpectedIntersectionMatrix() {
    return testable.getExpectedIntersectionMatrix();
  }

  public Geometry getExpectedConvexHull() {
    return testable.getExpectedConvexHull();
  }

  public Geometry getExpectedBoundary() {
    return testable.getExpectedBoundary();
  }

  public Geometry getExpectedIntersection() {
    return testable.getExpectedIntersection();
  }

  public Geometry getExpectedUnion() {
    return testable.getExpectedUnion();
  }

  public Geometry getExpectedDifference() {
    return testable.getExpectedDifference();
  }

  public Geometry getExpectedSymDifference() {
    return testable.getExpectedSymDifference();
  }

  public void exchange() 
  {
    Geometry temp = testable.getGeometry(0);
    testable.setGeometry(0, testable.getGeometry(1));
    testable.setGeometry(1, temp);
  }

  public void runTest() throws ParseException {
    testable.runTest();
  }

  public void initGeometry() throws ParseException {
    testable.initGeometry();
  }

  public Geometry[] getGeometries() {
    return new Geometry[] {
        testable.getGeometry(0), testable.getGeometry(1)
        };
  }

  void runRelate() {
    Geometry[] geom = getGeometries();
    if (geom[0] == null || geom[1] == null) {
      return;
    }
    testable.setIntersectionMatrix(geom[0].relate(geom[1]));
  }
}


