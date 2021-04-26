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
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
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

  public void testEmptyPolygon() throws Exception {
    String geomStr = "POLYGON(EMPTY)";
    new GeometryOperationValidator(
        DPSimplifierResult.getResult(
        geomStr,
        1))
        .setExpectedResult(geomStr)
        .test();
  }

  public void testPoint() throws Exception {
    String geomStr = "POINT (10 10)";
    new GeometryOperationValidator(
        DPSimplifierResult.getResult(
        geomStr,
        1))
        .setExpectedResult(geomStr)
        .test();
  }


  public void testPolygonNoReduction() throws Exception {
    new GeometryOperationValidator(
        DPSimplifierResult.getResult(
      "POLYGON ((20 220, 40 220, 60 220, 80 220, 100 220, 120 220, 140 220, 140 180, 100 180, 60 180,     20 180, 20 220))",
        10.0))
        .test();
  }
  public void testPolygonReductionWithSplit() throws Exception {
    new GeometryOperationValidator(
        DPSimplifierResult.getResult(
      "POLYGON ((40 240, 160 241, 280 240, 280 160, 160 240, 40 140, 40 240))",
        10.0))
        .test();
  }
  public void testPolygonReduction() throws Exception {
    new GeometryOperationValidator(
        DPSimplifierResult.getResult(
      "POLYGON ((120 120, 121 121, 122 122, 220 120, 180 199, 160 200, 140 199, 120 120))",
        10.0))
        .test();
  }
  public void testPolygonWithTouchingHole() throws Exception {
    new GeometryOperationValidator(
        DPSimplifierResult.getResult(
      "POLYGON ((80 200, 240 200, 240 60, 80 60, 80 200), (120 120, 220 120, 180 199, 160 200, 140 199, 120 120))",
        10.0))
        .setExpectedResult("POLYGON ((80 200, 240 200, 240 60, 80 60, 80 200), (120 120, 220 120, 180 199, 160 200, 140 199, 120 120))")
        .test();
  }
  public void testFlattishPolygon() throws Exception {
    new GeometryOperationValidator(
        DPSimplifierResult.getResult(
      "POLYGON ((0 0, 50 0, 53 0, 55 0, 100 0, 70 1,  60 1, 50 1, 40 1, 0 0))",
        10.0))
        .setExpectedResult("POLYGON EMPTY")
        .test();
  }
  public void testTinySquare() throws Exception {
    new GeometryOperationValidator(
        DPSimplifierResult.getResult(
      "POLYGON ((0 5, 5 5, 5 0, 0 0, 0 1, 0 5))",
        10.0))
        .test();
  }
  public void testTinyHole() throws Exception {
    new GeometryOperationValidator(
        DPSimplifierResult.getResult(
      "POLYGON ((10 10, 10 310, 370 310, 370 10, 10 10), (160 190, 180 190, 180 170, 160 190))",
        30.0))
        .testEmpty(false);
  }
  public void testTinyLineString() throws Exception {
    new GeometryOperationValidator(
        DPSimplifierResult.getResult(
      "LINESTRING (0 5, 1 5, 2 5, 5 5)",
        10.0))
        .test();
  }
  public void testMultiPoint() throws Exception {
    String geomStr = "MULTIPOINT(80 200, 240 200, 240 60, 80 60, 80 200, 140 199, 120 120)";
    new GeometryOperationValidator(
        TPSimplifierResult.getResult(
            geomStr,
        10.0))
        .setExpectedResult(geomStr)
        .test();
  }
  public void testMultiLineString() throws Exception {
    new GeometryOperationValidator(
        DPSimplifierResult.getResult(
      "MULTILINESTRING( (0 0, 50 0, 70 0, 80 0, 100 0), (0 0, 50 1, 60 1, 100 0) )",
        10.0))
        .test();
  }
  public void testMultiLineStringWithEmpty() throws Exception {
    new GeometryOperationValidator(
        DPSimplifierResult.getResult(
      "MULTILINESTRING( EMPTY, (0 0, 50 0, 70 0, 80 0, 100 0), (0 0, 50 1, 60 1, 100 0) )",
        10.0))
        .test();
  }
  public void testMultiPolygonWithEmpty() throws Exception {
    new GeometryOperationValidator(
        DPSimplifierResult.getResult(
      "MULTIPOLYGON (EMPTY, ((-36 91.5, 4.5 91.5, 4.5 57.5, -36 57.5, -36 91.5)), ((25.5 57.5, 61.5 57.5, 61.5 23.5, 25.5 23.5, 25.5 57.5)))",
        10.0))
        .test();
  }
  public void testGeometryCollection() throws Exception {
    new GeometryOperationValidator(
        DPSimplifierResult.getResult(
      "GEOMETRYCOLLECTION ("
      + "MULTIPOINT (80 200, 240 200, 240 60, 80 60, 80 200, 140 199, 120 120),"
      + "POLYGON ((80 200, 240 200, 240 60, 80 60, 80 200)),"
      + "LINESTRING (80 200, 240 200, 240 60, 80 60, 80 200, 140 199, 120 120)"
      + ")"
      ,10.0))
        .test();
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
  
  private void checkDP(String wkt, double tolerance, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry result = DouglasPeuckerSimplifier.simplify(geom, tolerance);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result);
  }
}

class DPSimplifierResult
{
  private static WKTReader rdr = new WKTReader();

  public static Geometry[] getResult(String wkt, double tolerance)
    throws ParseException
  {
    Geometry[] ioGeom = new Geometry[2];
    ioGeom[0] = rdr.read(wkt);
    ioGeom[1] = DouglasPeuckerSimplifier.simplify(ioGeom[0], tolerance);
    //System.out.println(ioGeom[1]);
    return ioGeom;
  }
}
