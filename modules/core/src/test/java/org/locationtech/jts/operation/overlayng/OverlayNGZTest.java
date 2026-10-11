/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.CoordinateXYM;
import org.locationtech.jts.geom.Coordinates;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class OverlayNGZTest extends GeometryTestCase
{
  public static void main(String args[]) {
    TestRunner.run(OverlayNGZTest.class);
  }

  public OverlayNGZTest(String name) {
    super(name);
  }
  
  public void testPointXYPointDifference() {
    checkDifference("MULTIPOINT ((1 1), (5 5))", "POINT Z (5 5 99)",
        "POINT Z(1 1 99)");
  }

  // checks that Point Z is preserved
  public void testPointPolygonIntersection() {
    checkIntersection("POINT Z (5 5 99)", "POLYGON Z ((1 9 5, 9 9 9, 9 1 5, 1 1 1, 1 9 5))",
        "POINT Z(5 5 99)");
  }

  public void testLineIntersectionPointZInterpolated() {
    checkIntersection("LINESTRING (0 0 0, 10 10 10)", "LINESTRING (10 0 0, 0 10 10)",
        "POINT(5 5 5)");
  }

  public void testLineIntersectionPointZValue() {
    checkIntersection("LINESTRING (0 0 0, 10 10 10)", "LINESTRING (10 0 0, 5 5 999, 0 10 10)",
        "POINT(5 5 999)");
  }

  public void testLineOverlapUnion() {
    checkUnion("LINESTRING (0 0 0, 10 10 10)", "LINESTRING (5 5 990, 15 15 999)",
        "MULTILINESTRING Z((0 0 0, 5 5 990), (5 5 990, 10 10 10), (10 10 10, 15 15 999))");
  }

  public void testLineLineXYDifferenceLineInterpolated() {
    checkDifference("LINESTRING (0 0 0, 10 10 10)", "LINESTRING (5 5, 6 6)",
        "MULTILINESTRING ((0 0 0, 5 5 5), (6 6 6, 10 10 10))");
  }

  // from https://trac.osgeo.org/geos/ticket/435
  public void testLineXYLineIntersection() {
    checkIntersection("LINESTRING(0 0,0 10,10 10,10 0)", "LINESTRING(10 10 4,10 0 5,0 0 5)",
        "GEOMETRYCOLLECTION Z(POINT Z(0 0 5), LINESTRING Z(10 0 5, 10 10 4))");
  }

  public void testLinePolygonIntersection() {
    checkIntersection("LINESTRING Z (0 0 0, 5 5 5)", "POLYGON Z ((1 9 5, 9 9 9, 9 1 5, 1 1 1, 1 9 5))",
        "LINESTRING Z (1 1 1, 5 5 5)");
  }

  public void testLinePolygonDifference() {
    checkDifference("LINESTRING Z (0 5 0, 10 5 10)", "POLYGON Z ((1 9 5, 9 9 9, 9 1 5, 1 1 1, 1 9 5))",
        "MULTILINESTRING Z((0 5 0, 1 5 2), (9 5 8, 10 5 10))");
  }

  public void testPointXYPolygonIntersection() {
    checkIntersection("POINT (5 5)", "POLYGON Z ((1 9 50, 9 9 90, 9 1 50, 1 1 10, 1 9 50))",
        "POINT Z(5 5 50)");
  }

  // XY Polygon gets Z value from Point
  public void testPointPolygonXYUnionn() {
    checkUnion("POINT Z (5 5 77)", "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))",
        "POLYGON Z((1 1 77, 1 9 77, 9 9 77, 9 1 77, 1 1 77))");
  }

  public void testLinePolygonXYDifference() {
    checkDifference("LINESTRING Z (0 5 0, 10 5 10)", "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))",
        "MULTILINESTRING Z((0 5 0, 1 5 1), (9 5 9, 10 5 10))");
  }
  
  public void testLineXYPolygonDifference() {
    checkDifference("LINESTRING (0 5, 10 5)", "POLYGON Z ((1 9 50, 9 9 90, 9 1 50, 1 1 10, 1 9 50))",
        "MULTILINESTRING Z((0 5 50, 1 5 30), (9 5 70, 10 5 50))");
  }

  public void testPolygonXYPolygonIntersection() {
    checkIntersection("POLYGON ((4 12, 2 6, 7 6, 11 4, 15 15, 4 12))", "POLYGON Z ((1 9 50, 9 9 90, 9 1 50, 1 1 10, 1 9 50))",
        "POLYGON Z((2 6 50, 3 9 60, 9 9 90, 9 5 70, 7 6 90, 2 6 50))");
  }

  public void testPolygonXYPolygonUnion() {
    checkUnion("POLYGON ((0 3, 3 3, 3 0, 0 0, 0 3))", "POLYGON Z ((1 9 50, 9 9 90, 9 1 50, 1 1 10, 1 9 50))",
        "POLYGON Z((0 0 10, 0 3 50, 1 3 20, 1 9 50, 9 9 90, 9 1 50, 3 1 20, 3 0 50, 0 0 10))");
  }

  // Test that operation on XY geoms produces XY (Z = NaN)
  public void testPolygonXYPolygonXYIntersection() {
    checkIntersection("POLYGON ((4 12, 2 6, 7 6, 11 4, 15 15, 4 12))", "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))",
        "POLYGON ((2 6, 3 9, 9 9, 9 5, 7 6, 2 6))");
  }

  public void testPolygonCoordinateXYIntersection() {
    String expected = "POLYGON Z ((5 5 1, 10 5 1.5, 10 10 3, 5 10 2.5, 5 5 1))";
    checkOverlayCoordinateXY(OverlayNG.INTERSECTION, 25, expected, expected);
  }

  public void testPolygonCoordinateXYUnion() {
    String expected = "POLYGON Z ((0 0 2.5, 10 0 2.5, 10 5 1.5, 15 5 2, 15 15 3, 5 15 4, 5 10 2.5, 0 10 2.5, 0 0 2.5))";
    checkOverlayCoordinateXY(OverlayNG.UNION, 175, expected, expected);
  }

  public void testPolygonCoordinateXYDifference() {
    checkOverlayCoordinateXY(OverlayNG.DIFFERENCE, 75,
        "POLYGON Z ((0 0 2.5, 10 0 2.5, 10 5 1.5, 5 5 1, 5 10 2.5, 0 10 2.5, 0 0 2.5))",
        "POLYGON Z ((5 10 2.5, 10 10 3, 10 5 1.5, 15 5 2, 15 15 3, 5 15 4, 5 10 2.5))");
  }

  public void testPolygonCoordinateXYSymDifference() {
    String expected = "MULTIPOLYGON Z (((0 0 2.5, 10 0 2.5, 10 5 1.5, 5 5 1, 5 10 2.5, 0 10 2.5, 0 0 2.5)), ((5 10 2.5, 10 10 3, 10 5 1.5, 15 5 2, 15 15 3, 5 15 4, 5 10 2.5)))";
    checkOverlayCoordinateXY(OverlayNG.SYMDIFFERENCE, 150, expected, expected);
  }

  public void testPolygonCoordinateXYConstantZ() {
    Geometry a = createPolygonXY(getGeometryFactory());
    Geometry b = read("POLYGON Z ((5 5 7, 15 5 7, 15 15 7, 5 15 7, 5 5 7))");
    for (int op = OverlayNG.INTERSECTION; op <= OverlayNG.SYMDIFFERENCE; op++) {
      checkZ(OverlayNG.overlay(a, b, op), 7);
      checkZ(OverlayNG.overlay(b, a, op), 7);
    }
  }

  public void testPolygonCoordinateXYDisjoint() {
    Geometry a = createPolygonXY(getGeometryFactory());
    Geometry b = read("POLYGON Z ((20 20 7, 30 20 7, 30 30 7, 20 30 7, 20 20 7))");
    Geometry expected = read("MULTIPOLYGON (((0 0, 10 0, 10 10, 0 10, 0 0)), ((20 20, 30 20, 30 30, 20 30, 20 20)))");
    for (int order = 0; order < 2; order++) {
      Geometry first = order == 0 ? a : b;
      Geometry second = order == 0 ? b : a;
      checkEqual(read("POLYGON EMPTY"), OverlayNG.overlay(first, second, OverlayNG.INTERSECTION));
      Geometry result = OverlayNG.overlay(first, second, OverlayNG.UNION);
      checkEqual(expected, result);
      assertEquals(200.0, result.getArea(), 0.0);
      for (Coordinate coordinate : result.getCoordinates()) {
        if (coordinate.x <= 10) {
          assertTrue(Double.isNaN(coordinate.getZ()));
        } else {
          assertEquals(7.0, coordinate.getZ(), 0.0);
        }
      }
    }
  }

  public void testPolygonCoordinateXYWithXYZMInputsUnchanged() {
    Polygon a = createPolygonXY(getGeometryFactory());
    Polygon b = (Polygon) read("POLYGON ZM ((5 5 7 11, 15 5 7 12, 15 15 NaN 13, 5 15 7 14, 5 5 7 11))");
    checkMeasuredOverlayInputs(a, b);
  }

  public void testPolygonCoordinateXYMWithXYZMInputsUnchanged() {
    Polygon a = createPolygonXYM(getGeometryFactory());
    Polygon b = (Polygon) read("POLYGON ZM ((5 5 7 11, 15 5 7 12, 15 15 NaN 13, 5 15 7 14, 5 5 7 11))");
    checkMeasuredOverlayInputs(a, b);
  }

  public void testPolygonCoordinateXYMWithContainedXYZMInputsUnchanged() {
    Polygon a = getGeometryFactory().createPolygon(new Coordinate[] {
        new CoordinateXYM(0, 0, 101), new CoordinateXYM(20, 0, 102),
        new CoordinateXYM(20, 20, 103), new CoordinateXYM(0, 20, 104),
        new CoordinateXYM(0, 0, 101)
    });
    Polygon b = (Polygon) read("POLYGON ZM ((5 5 7 11, 15 5 7 12, 15 15 NaN 13, 5 15 7 14, 5 5 7 11))");
    Coordinate[] originalsA = a.getCoordinates();
    Coordinate[] originalsB = b.getCoordinates();
    Coordinate[] snapshotA = CoordinateArrays.copyDeep(originalsA);
    Coordinate[] snapshotB = CoordinateArrays.copyDeep(originalsB);
    Geometry expected = read("POLYGON ZM ((5 5 7 11, 15 5 7 12, 15 15 7 13, 5 15 7 14, 5 5 7 11))");
    for (int order = 0; order < 2; order++) {
      Geometry result = order == 0
          ? OverlayNG.overlay(a, b, OverlayNG.INTERSECTION)
          : OverlayNG.overlay(b, a, OverlayNG.INTERSECTION);
      checkEqualXYZM(expected, result);
      checkCoordinatesUnchanged(a.getExteriorRing().getCoordinateSequence(), originalsA, snapshotA, 3, 1);
      checkCoordinatesUnchanged(b.getExteriorRing().getCoordinateSequence(), originalsB, snapshotB, 4, 1);
    }
  }

  public void testPolygonCoordinateXYMWithXYZ() {
    Polygon a = createPolygonXYM(getGeometryFactory());
    Geometry b = read("POLYGON Z ((5 5 7, 15 5 7, 15 15 7, 5 15 7, 5 5 7))");
    for (int order = 0; order < 2; order++) {
      Polygon result = (Polygon) (order == 0
          ? OverlayNG.overlay(a, b, OverlayNG.UNION)
          : OverlayNG.overlay(b, a, OverlayNG.UNION));
      assertEquals(4, result.getExteriorRing().getCoordinateSequence().getDimension());
      assertEquals(1, result.getExteriorRing().getCoordinateSequence().getMeasures());
      checkZ(result, 7);
      assertEquals(102.0, findCoordinate(result, 10, 0).getM(), 0.0);
      assertTrue(Double.isNaN(findCoordinate(result, 15, 15).getM()));
    }
  }

  public void testLineCoordinateXYUnion() {
    Geometry a = getGeometryFactory().createLineString(new Coordinate[] {
        new CoordinateXY(0, 0), new CoordinateXY(10, 0)
    });
    Geometry b = read("LINESTRING Z (5 0 7, 15 0 7)");
    Geometry expected = read("MULTILINESTRING Z ((0 0 7, 5 0 7), (5 0 7, 10 0 7), (10 0 7, 15 0 7))");
    checkEqualXYZ(expected, OverlayNG.overlay(a, b, OverlayNG.UNION));
    checkEqualXYZ(expected, OverlayNG.overlay(b, a, OverlayNG.UNION));
  }

  public void testPolygonMixedCoordinatesPackedFactory() {
    GeometryFactory factory = new GeometryFactory(PackedCoordinateSequenceFactory.DOUBLE_FACTORY);
    Polygon a = createPolygonXYM(factory);
    Geometry b = read("POLYGON ZM ((5 5 7 11, 15 5 7 12, 15 15 7 13, 5 15 7 14, 5 5 7 11))");
    Polygon result = (Polygon) OverlayNG.overlay(a, b, OverlayNG.UNION);
    assertSame(factory, result.getFactory());
    assertEquals(4, result.getExteriorRing().getCoordinateSequence().getDimension());
    assertEquals(1, result.getExteriorRing().getCoordinateSequence().getMeasures());
    checkZ(result, 7);
    assertEquals(102.0, findCoordinate(result, 10, 0).getM(), 0.0);
    assertEquals(13.0, findCoordinate(result, 15, 15).getM(), 0.0);
  }

  public void testPolygonCoordinateXYFixedPrecision() {
    Geometry a = createPolygonXY(getGeometryFactory());
    Geometry b = read("POLYGON Z ((5 5 7, 15 5 7, 15 15 7, 5 15 7, 5 5 7))");
    Geometry result = OverlayNG.overlay(a, b, OverlayNG.UNION, new PrecisionModel(1));
    checkEqualXYZ(read("POLYGON Z ((0 0 7, 10 0 7, 10 5 7, 15 5 7, 15 15 7, 5 15 7, 5 10 7, 0 10 7, 0 0 7))"), result);
  }

  public void testMixedCoordinateOutputEdges() {
    Polygon a = createPolygonXYM(getGeometryFactory());
    Polygon b = (Polygon) read("POLYGON ZM ((5 5 7 11, 15 5 7 12, 15 15 7 13, 5 15 7 14, 5 5 7 11))");
    Coordinate[] originalsA = a.getCoordinates();
    Coordinate[] originalsB = b.getCoordinates();
    Coordinate[] snapshotA = CoordinateArrays.copyDeep(originalsA);
    Coordinate[] snapshotB = CoordinateArrays.copyDeep(originalsB);
    for (int mode = 0; mode < 3; mode++) {
      OverlayNG overlay = new OverlayNG(a, b, OverlayNG.UNION);
      if (mode == 0) overlay.setOutputEdges(true);
      if (mode == 1) overlay.setOutputNodedEdges(true);
      if (mode == 2) overlay.setOutputResultEdges(true);
      Geometry result = overlay.getResult();
      assertFalse(result.isEmpty());
      result.apply(new CoordinateSequenceFilter() {
        @Override
        public void filter(CoordinateSequence sequence, int i) {
          Coordinate coordinate = sequence.getCoordinate(i);
          assertEquals(sequence.getDimension(), Coordinates.dimension(coordinate));
          assertEquals(sequence.getMeasures(), Coordinates.measures(coordinate));
        }

        @Override
        public boolean isDone() {
          return false;
        }

        @Override
        public boolean isGeometryChanged() {
          return false;
        }
      });
      checkCoordinatesUnchanged(a.getExteriorRing().getCoordinateSequence(), originalsA, snapshotA, 3, 1);
      checkCoordinatesUnchanged(b.getExteriorRing().getCoordinateSequence(), originalsB, snapshotB, 4, 1);
    }
  }

  //=================================================
  
  private void checkIntersection(String wktA, String wktB, String wktExpected) {
    checkOverlay(OverlayNG.INTERSECTION, wktA, wktB, wktExpected);
  }
  private void checkDifference(String wktA, String wktB, String wktExpected) {
    checkOverlay(OverlayNG.DIFFERENCE, wktA, wktB, wktExpected);
  }
  
  private void checkUnion(String wktA, String wktB, String wktExpected) {
    checkOverlay(OverlayNG.UNION, wktA, wktB, wktExpected);
  }
  
  private void checkOverlay(int opCode, String wktA, String wktB, String wktExpected) {
    Geometry a = read(wktA);
    Geometry b = read(wktB);
    Geometry result = OverlayNG.overlay(a, b, opCode);
    Geometry expected = read(wktExpected);
    checkEqualXYZ(expected, result);
  }

  private Polygon createPolygonXY(GeometryFactory factory) {
    return factory.createPolygon(new Coordinate[] {
        new CoordinateXY(0, 0), new CoordinateXY(10, 0), new CoordinateXY(10, 10),
        new CoordinateXY(0, 10), new CoordinateXY(0, 0)
    });
  }

  private Polygon createPolygonXYM(GeometryFactory factory) {
    return factory.createPolygon(new Coordinate[] {
        new CoordinateXYM(0, 0, 101), new CoordinateXYM(10, 0, 102),
        new CoordinateXYM(10, 10, 103), new CoordinateXYM(0, 10, 104),
        new CoordinateXYM(0, 0, 101)
    });
  }

  private void checkOverlayCoordinateXY(int opCode, double area,
      String expected, String expectedReverse) {
    Geometry a = createPolygonXY(getGeometryFactory());
    Geometry b = read("POLYGON Z ((5 5 1, 15 5 2, 15 15 3, 5 15 4, 5 5 1))");
    Geometry result = OverlayNG.overlay(a, b, opCode);
    checkEqualXYZ(read(expected), result);
    assertEquals(area, result.getArea(), 0.0);
    Geometry resultReverse = OverlayNG.overlay(b, a, opCode);
    checkEqualXYZ(read(expectedReverse), resultReverse);
    assertEquals(area, resultReverse.getArea(), 0.0);
  }

  private void checkMeasuredOverlayInputs(Polygon a, Polygon b) {
    CoordinateSequence seqA = a.getExteriorRing().getCoordinateSequence();
    CoordinateSequence seqB = b.getExteriorRing().getCoordinateSequence();
    Coordinate[] originalsA = a.getCoordinates();
    Coordinate[] originalsB = b.getCoordinates();
    Coordinate[] snapshotA = CoordinateArrays.copyDeep(originalsA);
    Coordinate[] snapshotB = CoordinateArrays.copyDeep(originalsB);
    int dimensionA = seqA.getDimension();
    int measuresA = seqA.getMeasures();
    for (int order = 0; order < 2; order++) {
      Polygon result = (Polygon) (order == 0
          ? OverlayNG.overlay(a, b, OverlayNG.UNION)
          : OverlayNG.overlay(b, a, OverlayNG.UNION));
      assertEquals(4, result.getExteriorRing().getCoordinateSequence().getDimension());
      assertEquals(1, result.getExteriorRing().getCoordinateSequence().getMeasures());
      checkZ(result, 7);
      assertEquals(13.0, findCoordinate(result, 15, 15).getM(), 0.0);
      assertEquals(snapshotA[1].getM(), findCoordinate(result, 10, 0).getM(), 0.0);
      checkCoordinatesUnchanged(seqA, originalsA, snapshotA, dimensionA, measuresA);
      checkCoordinatesUnchanged(seqB, originalsB, snapshotB, 4, 1);
    }
  }

  private void checkCoordinatesUnchanged(CoordinateSequence sequence,
      Coordinate[] originals, Coordinate[] snapshot, int dimension, int measures) {
    assertEquals(dimension, sequence.getDimension());
    assertEquals(measures, sequence.getMeasures());
    for (int i = 0; i < snapshot.length; i++) {
      Coordinate actual = sequence.getCoordinate(i);
      assertSame(originals[i], actual);
      assertEquals(snapshot[i].getClass(), actual.getClass());
      assertEquals(snapshot[i].x, actual.x, 0.0);
      assertEquals(snapshot[i].y, actual.y, 0.0);
      assertEquals(snapshot[i].getZ(), actual.getZ(), 0.0);
      assertEquals(snapshot[i].getM(), actual.getM(), 0.0);
    }
  }

  private void checkZ(Geometry geometry, double z) {
    assertFalse(geometry.isEmpty());
    for (Coordinate coordinate : geometry.getCoordinates()) {
      assertEquals(z, coordinate.getZ(), 0.0);
    }
  }

  private Coordinate findCoordinate(Geometry geometry, double x, double y) {
    for (Coordinate coordinate : geometry.getCoordinates()) {
      if (coordinate.x == x && coordinate.y == y) return coordinate;
    }
    fail("Missing coordinate (" + x + ", " + y + ")");
    return null;
  }
}
