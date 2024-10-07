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

package org.locationtech.jts.simplify;

import org.locationtech.jts.geom.Geometry;

import test.jts.GeometryTestCase;


/**
 * @version 1.7
 */
public class DouglasPeuckerSimplifierTest
    extends GeometryTestCase
{
  public DouglasPeuckerSimplifierTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(DouglasPeuckerSimplifierTest.class);
  }

  public void testPoint() {
    checkDPNoChange("POINT (10 10)", 1);
  }
  
  public void testPolygonEmpty() {
    checkDPNoChange("POLYGON(EMPTY)", 1);
  }

  public void testPolygonWithFlatVertices() {
    checkDP("POLYGON ((20 220, 40 220, 60 220, 80 220, 100 220, 120 220, 140 220, 140 180, 100 180, 60 180, 20 180, 20 220))", 
        10.0, 
        "POLYGON ((20 220, 140 220, 140 180, 20 180, 20 220))");
  }
  
  public void testPolygonReductionWithSplit() {
    checkDP("POLYGON ((40 240, 160 241, 280 240, 280 160, 160 240, 40 140, 40 240))", 
        1, 
        "MULTIPOLYGON (((40 240, 160 240, 40 140, 40 240)), ((160 240, 280 240, 280 160, 160 240)))");
  }
  
  public void testPolygonReduction() {
    checkDP("POLYGON ((120 120, 121 121, 122 122, 220 120, 180 199, 160 200, 140 199, 120 120))",
        10, 
        "POLYGON ((120 120, 220 120, 180 199, 160 200, 140 199, 120 120))");
  }
  
  public void testPolygonWithTouchingHole() {
    checkDP("POLYGON ((10 10, 10 90, 90 90, 90 10, 10 10), (80 20, 20 20, 20 80, 50 90, 80 80, 80 20))",
        10,
        "POLYGON ((10 10, 10 90, 90 90, 90 10, 10 10), (80 20, 20 20, 20 80, 80 80, 80 20))");
  }
  
  public void testPolygonFlattish() {
    checkDP("POLYGON ((0 0, 50 0, 53 0, 55 0, 100 0, 70 1,  60 1, 50 1, 40 1, 0 0))",
        10,
        "POLYGON EMPTY");
  }
  
  public void testPolygonTinySquare() {
    checkDP("POLYGON ((0 5, 5 5, 5 0, 0 0, 0 1, 0 5))",
        10,
        "POLYGON EMPTY");
  }
  
  public void testPolygonTinyHole() {
    checkDP("POLYGON ((10 10, 10 310, 370 310, 370 10, 10 10), (160 190, 180 190, 180 170, 160 190))",
        30,
        "POLYGON ((10 10, 10 310, 370 310, 370 10, 10 10))");
  }
  
  public void testLineStringTiny() {
    checkDP("LINESTRING (0 5, 1 5, 2 5, 5 5)",
        10,
        "LINESTRING (0 5, 5 5)");
  }

  public void testLineStringWithFourDimensions() {
    checkDPXYZM("LINESTRING ZM(0 5 114.6 1709024189000, 1 5 114.6 1709024190000, 2 5 114.5 1709024192000, 5 5 114.5 1709024196000)",
        10,
        "LINESTRING ZM(0 5 114.6 1709024189000, 5 5 114.5 1709024196000)");
  }
  
  public void testMultiPoint() {
    checkDPNoChange("MULTIPOINT(80 200, 240 200, 240 60, 80 60, 80 200, 140 199, 120 120)",
        10);
  }
  
  public void testMultiLineString() {
    checkDP("MULTILINESTRING((0 0, 50 0, 70 0, 80 0, 100 0), (0 0, 50 1, 60 1, 100 0) )",
        10,
        "MULTILINESTRING ((0 0, 100 0), (0 0, 100 0))");
  }
  
  public void testMultiLineStringWithEmpty() {
    checkDP("MULTILINESTRING( EMPTY, (0 0, 50 0, 70 0, 80 0, 100 0), (0 0, 50 1, 60 1, 100 0) )",
        10,
        "MULTILINESTRING ((0 0, 100 0), (0 0, 100 0))");
  }
  
  public void testMultiPolygonWithEmpty() {
    checkDP("MULTIPOLYGON (EMPTY, ((10 90, 10 10, 90 10, 50 60, 10 90)), ((70 90, 90 90, 90 70, 70 70, 70 90)))",
        10,
        "MULTIPOLYGON (((10 90, 10 10, 90 10, 10 90)), ((70 90, 90 90, 90 70, 70 70, 70 90)))");
  }
  
  public void testGeometryCollection() {
    checkDPNoChange("GEOMETRYCOLLECTION (MULTIPOINT (80 200, 240 200, 240 60, 80 60, 80 200, 140 199, 120 120), POLYGON ((80 200, 240 200, 240 60, 80 60, 80 200)), LINESTRING (80 200, 240 200, 240 60, 80 60, 80 200, 140 199, 120 120))",
      10.0);
  }
  
  /**
   * Test that a polygon made invalid by simplification
   * is fixed in a sensible way.
   * Fixed by buffer(0) area-base orientation
   * See https://github.com/locationtech/jts/issues/498
   */
  public void testInvalidPolygonFixed() {
    checkDP(
        "POLYGON ((21.32686 47.78723, 21.32386 47.79023, 21.32186 47.80223, 21.31486 47.81023, 21.32786 47.81123, 21.33986 47.80223, 21.33886 47.81123, 21.32686 47.82023, 21.32586 47.82723, 21.32786 47.82323, 21.33886 47.82623, 21.34186 47.82123, 21.36386 47.82223, 21.40686 47.81723, 21.32686 47.78723))", 
        0.0036,
        "POLYGON ((21.32686 47.78723, 21.31486 47.81023, 21.32786 47.81123, 21.33986 47.80223, 21.328068201892744 47.823286782334385, 21.33886 47.82623, 21.34186 47.82123, 21.40686 47.81723, 21.32686 47.78723))"
        );
  }

  /**
   * Test that a collapsed polygon is removed.
   * Not an error in JTS, but included to avoid regression errors in future.
   * 
   * See https://trac.osgeo.org/geos/ticket/1115
   */
  public void testPolygonCollapseRemoved() {
    checkDP(
        "MULTIPOLYGON (((-76.02716827 36.55671692, -75.99866486 36.55665207, -75.91191864 36.54253006, -75.92480469 36.47397614, -75.97727966 36.4780159, -75.97628784 36.51792526, -76.02716827 36.55671692)), ((-75.90198517 36.55619812, -75.8781662 36.55587387, -75.77315521 36.22925568, -75.78317261 36.22519302, -75.90198517 36.55619812)))", 
        0.05,
        "POLYGON ((-76.02716827 36.55671692, -75.91191864 36.54253006, -75.92480469 36.47397614, -76.02716827 36.55671692))"
        );
  }
  
  // see https://trac.osgeo.org/geos/ticket/1064
  public void testPolygonRemoveFlatEndpoint() {
    checkDP(
      "POLYGON ((42 42, 0 42, 0 100, 42 100, 100 42, 42 42))",
        1,
        "POLYGON ((100 42, 0 42, 0 100, 42 100, 100 42))"
        );
  }
  
  public void testPolygonEndpointCollapse() {
    checkDP(
      "POLYGON ((5 2, 9 1, 1 1, 5 2))",
        1,
        "POLYGON EMPTY"
        );
  }
  
  private void checkDP(String wkt, double tolerance, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry result = DouglasPeuckerSimplifier.simplify(geom, tolerance);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result);
  }
    private void checkDPXYZM(String wkt, double tolerance, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry result = DouglasPeuckerSimplifier.simplify(geom, tolerance);
    Geometry expected = read(wktExpected);
    checkEqualXYZM(expected, result);
  }

  private void checkDPNoChange(String wkt, double tolerance) {
    checkDP(wkt, tolerance, wkt);
  }
}
