

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

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jtstest.test.TestCase;
import org.locationtech.jtstest.test.Testable;
import org.locationtech.jtstest.testbuilder.JTSTestBuilder;



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


