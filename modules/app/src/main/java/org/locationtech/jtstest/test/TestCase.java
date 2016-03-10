

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
package org.locationtech.jtstest.test;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.*;
import org.locationtech.jts.util.Assert;


/**
 * @version 1.7
 */
public class TestCase implements Testable {
  private PrecisionModel pm = new PrecisionModel();
  private WKTWriter wktWriter = new WKTWriter();
  protected String name, description, expectedIM;
  protected boolean isRun = false;
  protected boolean failed = false;
  //protected boolean passed = false;
  protected String failedMsg = "";
  private Geometry expectedConvexHull = null;
  private Geometry expectedBoundary = null;
  private Geometry expectedIntersection = null;
  private Geometry expectedUnion = null;
  private Geometry expectedDifference = null;
  private Geometry expectedSymDifference = null;
  private Geometry expectedCentroid = null;
  private IntersectionMatrix im;
  private Geometry[] geom = new Geometry[2];
  private String wkta;
  private String wktb;

  public TestCase() {
    this(null, null, null, null, null, null, null, null, null, null);
  }

  public TestCase(String name) {
    this(name, null, null, null, null, null, null, null, null, null);
  }

  public TestCase(String name, String description, String wkta, String wktb,
      String expectedIM) {
    this(name, description, wkta, wktb, expectedIM, null, null, null, null,
        null);
  }

  public TestCase(String name, String description, String wkta, String wktb,
      String expectedIM, String expectedConvexHull, String expectedIntersection,
      String expectedUnion, String expectedDifference, String expectedSymDifference) {
    this(name, description, wkta, wktb, expectedIM, expectedConvexHull, expectedIntersection,
        expectedUnion, expectedDifference, expectedSymDifference, null);
  }

  public TestCase(String name, String description, String wkta, String wktb,
      String expectedIM, String expectedConvexHull, String expectedIntersection,
      String expectedUnion, String expectedDifference, String expectedSymDifference,
      String expectedBoundary) {
    try {
      init(name, description, wkta, wktb, expectedIM, toNullOrGeometry(expectedConvexHull),
          toNullOrGeometry(expectedIntersection), toNullOrGeometry(expectedUnion),
          toNullOrGeometry(expectedDifference), toNullOrGeometry(expectedSymDifference),
          toNullOrGeometry(expectedBoundary));
    }
    catch (ParseException e) {
      Assert.shouldNeverReachHere();
    }
  }

  public TestCase(TestCase tc) {
    init(tc.name, tc.description, tc.getWellKnownText(0), tc.getWellKnownText(1),
        tc.expectedIM, tc.getExpectedConvexHull(), tc.getExpectedIntersection(),
        tc.getExpectedUnion(), tc.getExpectedDifference(), tc.getExpectedSymDifference(),
        tc.getExpectedBoundary());
  }

  public void setGeometry(int index, Geometry g) {
    geom[index] = g;
  }

  public TestCase setPrecisionModel(PrecisionModel pm)
  {
    this.pm = pm;
    return this;
  }
  public void setIntersectionMatrix(IntersectionMatrix im) {
    this.im = im;
  }

  public void setExpectedIntersectionMatrix(String expectedIntersectionMatrix) {
    expectedIM = expectedIntersectionMatrix;
  }

  public TestCase setExpectedRelateMatrix(String expectedIntersectionMatrix) {
    expectedIM = expectedIntersectionMatrix;
    return this;
  }

  public TestCase setTestName(String name) {
    this.name = name;
    return this;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setExpectedConvexHull(Geometry expectedConvexHull) {
    this.expectedConvexHull = expectedConvexHull;
  }

  public void setExpectedBoundary(Geometry expectedBoundary) {
    this.expectedBoundary = expectedBoundary;
  }

  public void setExpectedIntersection(Geometry expectedIntersection) {
    this.expectedIntersection = expectedIntersection;
  }

  public void setExpectedUnion(Geometry expectedUnion) {
    this.expectedUnion = expectedUnion;
  }

  public void setExpectedDifference(Geometry expectedDifference) {
    this.expectedDifference = expectedDifference;
  }

  public void setExpectedSymDifference(Geometry expectedSymDifference) {
    this.expectedSymDifference = expectedSymDifference;
  }

  public void setExpectedCentroid(Geometry expectedCentroid) {
    this.expectedCentroid = expectedCentroid;
  }

  public TestCase setExpectedIntersection(String wkt) {
    try {
      this.expectedIntersection = toNullOrGeometry(wkt);
    }
    catch (ParseException e) {
      Assert.shouldNeverReachHere();
    }
    return this;
  }

  public TestCase setExpectedBoundary(String wkt) {
    try {
      this.expectedBoundary = toNullOrGeometry(wkt);
    }
    catch (ParseException e) {
      Assert.shouldNeverReachHere();
    }
    return this;
  }

  public TestCase setA(String wkta) {
    this.wkta = wkta;
    return this;
  }

  public TestCase setB(String wktb) {
    this.wktb = wktb;
    return this;
  }

  public Geometry getGeometry(int index) {
    return geom[index];
  }

  public IntersectionMatrix getIntersectionMatrix() {
    return im;
  }

  public String getExpectedIntersectionMatrix() {
    return expectedIM;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public boolean isFailed() {
    return failed;
  }

  public String getFailedMsg() {
    return failedMsg;
  }

  public boolean isPassed() {
    return isRun && !failed;
  }

  public boolean isRun() {
    return isRun;
  }

  public String getWellKnownText(int i) {
    if (geom[i] == null) {
      return null;
    }
    return wktWriter.write(geom[i]);
  }

  public Geometry getExpectedConvexHull() {
    return expectedConvexHull;
  }

  public Geometry getExpectedBoundary() {
    return expectedBoundary;
  }

  public Geometry getExpectedIntersection() {
    return expectedIntersection;
  }

  public Geometry getExpectedUnion() {
    return expectedUnion;
  }

  public Geometry getExpectedDifference() {
    return expectedDifference;
  }

  public Geometry getExpectedSymDifference() {
    return expectedSymDifference;
  }

  public Geometry[] getGeometries() {
    return geom;
  }

  public void runTest() throws ParseException {
    failed = false;
    isRun = true;
    initGeometry();
    if (expectedIM != null) {
      IntersectionMatrix im = null;
      if (geom[0] != null && geom[1] != null) {
        im = relate(geom[0], geom[1]);
      }
      if (im != null) {
        String msg = " expected " + expectedIM + ", found " + im.toString();
        assertTrue(im.matches(expectedIM), msg);
      }
    }
    if (expectedBoundary != null) {
      Geometry result = geom[0].getBoundary();
      assertEqualsExact(expectedBoundary, result, " expected boundary "
           + expectedBoundary.toText() + " , found " + result.toText());
    }
    if (expectedConvexHull != null) {
      Geometry result = geom[0].convexHull();
      assertEqualsExact(expectedConvexHull, result, " expected convex hull "
           + expectedConvexHull.toText() + " , found " + result.toText());
    }
    if (expectedIntersection != null) {
      Geometry result = geom[0].intersection(geom[1]);
      assertEqualsExact(expectedIntersection, result, " expected intersection "
           + expectedIntersection.toText() + " , found " + result.toText());
    }
    if (expectedUnion != null) {
      Geometry result = geom[0].union(geom[1]);
      assertEqualsExact(expectedUnion, result, " expected union "
           + expectedUnion.toText() + " , found " + result.toText());
    }
    if (expectedDifference != null) {
      Geometry result = geom[0].difference(geom[1]);
      assertEqualsExact(expectedDifference, result, " expected difference "
           + expectedDifference.toText() + " , found " + result.toText());
    }
    if (expectedSymDifference != null) {
      Geometry result = geom[0].symDifference(geom[1]);
      assertEqualsExact(expectedSymDifference, result, " expected sym difference "
           + expectedSymDifference.toText() + " , found " + result.toText());
    }
  }

  public void initGeometry() throws ParseException {
    GeometryFactory fact = new GeometryFactory(pm, 0);
    WKTReader wktRdr = new WKTReader(fact);
    if (geom[0] != null) {
      return;
    }
    if (wkta != null) {
      geom[0] = wktRdr.read(wkta);
    }
    if (wktb != null) {
      geom[1] = wktRdr.read(wktb);
    }
  }

  void init(String name, String description, String wkta, String wktb, String expectedIM,
      Geometry expectedConvexHull, Geometry expectedIntersection, Geometry expectedUnion,
      Geometry expectedDifference, Geometry expectedSymDifference, Geometry expectedBoundary) {
    this.name = name;
    this.description = description;
    this.wkta = wkta;
    this.wktb = wktb;
    this.expectedIM = expectedIM;
    this.expectedConvexHull = expectedConvexHull;
    this.expectedBoundary = expectedBoundary;
    this.expectedIntersection = expectedIntersection;
    this.expectedUnion = expectedUnion;
    this.expectedDifference = expectedDifference;
    this.expectedSymDifference = expectedSymDifference;
  }

  IntersectionMatrix relate(Geometry a, Geometry b) {
    return a.relate(b);
  }

  void assertEquals(Object o1, Object o2, String msg) {
    assertTrue(o1.equals(o2), msg);
  }

  void assertEqualsExact(Geometry g1, Geometry g2, String msg) {
    Geometry g1Clone = (Geometry) g1.clone();
    Geometry g2Clone = (Geometry) g2.clone();
    g1Clone.normalize();
    g2Clone.normalize();
    assertTrue(g1Clone.equalsExact(g2Clone), msg);
  }

  void assertTrue(boolean val, String msg) {
    if (!val) {
      failed = true;
      failedMsg = msg;
    }
  }

  private Geometry toNullOrGeometry(String wellKnownText) throws ParseException {
    if (wellKnownText == null) {
      return null;
    }
    GeometryFactory fact = new GeometryFactory(pm, 0);
    WKTReader wktRdr = new WKTReader(fact);
    return wktRdr.read(wellKnownText);
  }

}

