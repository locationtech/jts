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

package org.locationtech.jts.geom;

import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * Test spatial predicate optimizations for rectangles.
 *
 * @version 1.7
 */

public class RectanglePredicateTest
     extends TestCase
{
  private WKTReader rdr = new WKTReader();
  private GeometryFactory fact = new GeometryFactory();

  public static void main(String args[]) {
    TestRunner.run(RectanglePredicateTest.class);
  }

  public RectanglePredicateTest(String name) { super(name); }

  public void testShortAngleOnBoundary()
      throws Exception
  {
    String[] onBoundary =
    { "POLYGON ((10 10, 30 10, 30 30, 10 30, 10 10))",
      "LINESTRING (10 25, 10 10, 25 10)" } ;
    runRectanglePred(onBoundary);
  }

  public void testAngleOnBoundary()
      throws Exception
  {
    String[] onBoundary =
    { "POLYGON ((10 10, 30 10, 30 30, 10 30, 10 10))",
      "LINESTRING (10 30, 10 10, 30 10)" } ;
    runRectanglePred(onBoundary);
  }

  private void runRectanglePred(String[] wkt)
      throws Exception
  {
    Geometry rect = rdr.read(wkt[0]);
    Geometry b = rdr.read(wkt[1]);
    runRectanglePred(rect, b);
  }

  private void runRectanglePred(Geometry rect, Geometry testGeom) {
    boolean intersectsValue = rect.intersects(testGeom);
    boolean relateIntersectsValue = rect.relate(testGeom).isIntersects();
    boolean intersectsOK = intersectsValue == relateIntersectsValue;

    boolean containsValue = rect.contains(testGeom);
    boolean relateContainsValue = rect.relate(testGeom).isContains();
    boolean containsOK = containsValue == relateContainsValue;

    //System.out.println(testGeom);
    if (! intersectsOK || ! containsOK) {
      //System.out.println(testGeom);
    }
    assertTrue(intersectsOK);
    assertTrue(containsOK);
  }

}