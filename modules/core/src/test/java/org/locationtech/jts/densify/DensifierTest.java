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
package org.locationtech.jts.densify;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class DensifierTest extends GeometryTestCase {
  private static final double TOLERANCE = 1e-6;

  public static void main(String args[]) {
    TestRunner.run(DensifierTest.class);
  }

  public DensifierTest(String name) { super(name); }
  
  public void testLine() {
    checkDensify("LINESTRING (0 0, 30 40, 35 35)", 
        10, "LINESTRING (0 0, 6 8, 12 16, 18 24, 24 32, 30 40, 35 35)");
  }

  public void testLineOfToleranceLength() {
    checkDensify("LINESTRING (0 0, 10 0)", 
        10, "LINESTRING (0 0, 10 0)");
  }

  public void testLineWithToleranceLengthSeg() {
    checkDensify("LINESTRING (0 0, 12 0, 22 0, 34 0)", 
        10, "LINESTRING (0 0, 6 0, 12 0, 22 0, 28 0, 34 0)");
  }

  public void testLineEmpty() {
    checkDensify("LINESTRING EMPTY", 
        10, "LINESTRING EMPTY");
  }

  public void testPointUnchanged() {
    checkDensify("POINT (0 0)", 
        10, "POINT (0 0)");
  }

  public void testPolygonEmpty() {
    checkDensify("POLYGON EMPTY", 
        10, "POLYGON EMPTY");
  }

  public void testBox() {
    checkDensify("POLYGON ((10 30, 30 30, 30 10, 10 10, 10 30))", 
        10, "POLYGON ((10 10, 10 20, 10 30, 20 30, 30 30, 30 20, 30 10, 20 10, 10 10))");
  }

  public void testBoxNoValidate() {
    checkDensifyNoValidate("POLYGON ((10 30, 30 30, 30 10, 10 10, 10 30))", 
        10, "POLYGON ((10 10, 10 20, 10 30, 20 30, 30 30, 30 20, 30 10, 20 10, 10 10))");
  }
  
  public void testDimension2d() {
      GeometryFactory gf = new GeometryFactory();
      LineString line = gf
              .createLineString(new Coordinate[] { new CoordinateXY(1, 2), new CoordinateXY(3, 4) });
      assertEquals(2, line.getCoordinateSequence().getDimension());
      
      line = (LineString) Densifier.densify(line, 0.1);
      assertEquals(2, line.getCoordinateSequence().getDimension());
  }
  
  public void testDimension3d() {
      GeometryFactory gf = new GeometryFactory();
      LineString line = gf
              .createLineString(new Coordinate[] { new Coordinate(1, 2, 3), new Coordinate(3, 4, 5) });
      assertEquals(3, line.getCoordinateSequence().getDimension());
      
      line = (LineString) Densifier.densify(line, 0.1);
      assertEquals(3, line.getCoordinateSequence().getDimension());
  }


  private void checkDensify(String wkt, double distanceTolerance, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry expected = read(wktExpected);
    Geometry actual = Densifier.densify(geom, distanceTolerance);
    checkEqual(expected, actual, TOLERANCE);
  }
  
  /**
   * Note: it's hard to construct a geometry which would actually be invalid when densified.
   * This test just checks that the code path executes.
   * 
   * @param wkt
   * @param distanceTolerance
   * @param wktExpected
   */
  private void checkDensifyNoValidate(String wkt, double distanceTolerance, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry expected = read(wktExpected);
    Densifier den = new Densifier(geom);
    den.setDistanceTolerance(distanceTolerance);
    den.setValidate(false);
    Geometry actual = den.getResultGeometry();
    checkEqual(expected, actual, TOLERANCE);
  }
  
}
