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
package org.locationtech.jts.operation.valid;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * @version 1.7
 */
public class IsValidTest extends GeometryTestCase {

  private PrecisionModel precisionModel = new PrecisionModel();
  private GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(IsValidTest.class);
  }

  public IsValidTest(String name) { super(name); }

  public void testInvalidCoordinate() throws Exception
  {
    Coordinate badCoord = new Coordinate(1.0, Double.NaN);
    Coordinate[] pts = { new Coordinate(0.0, 0.0), badCoord };
    Geometry line = geometryFactory.createLineString(pts);
    IsValidOp isValidOp = new IsValidOp(line);
    boolean valid = isValidOp.isValid();
    TopologyValidationError err = isValidOp.getValidationError();
    Coordinate errCoord = err.getCoordinate();

    assertEquals(TopologyValidationError.INVALID_COORDINATE, err.getErrorType());
    assertTrue(Double.isNaN(errCoord.y));
    assertEquals(false, valid);
  }

  public void testZeroAreaPolygon() {
    checkInvalid( "POLYGON((0 0, 0 0, 0 0, 0 0, 0 0))");
  }

  public void testValidSimplePolygon() {
    checkValid( "POLYGON ((10 89, 90 89, 90 10, 10 10, 10 89))");
  }

  public void testInvalidSimplePolygonRingSelfIntersection() {
    checkInvalid( TopologyValidationError.SELF_INTERSECTION,
        "POLYGON ((10 90, 90 10, 90 90, 10 10, 10 90))");
  }

  public void testInvalidPolygonInverted() {
    checkInvalid( TopologyValidationError.RING_SELF_INTERSECTION,
        "POLYGON ((70 250, 40 500, 100 400, 70 250, 80 350, 60 350, 70 250))");
  }

  public void testInvalidPolygonSelfCrossing() {
    checkInvalid( TopologyValidationError.SELF_INTERSECTION,
        "POLYGON ((70 250, 70 500, 80 400, 40 400, 70 250))");
  }

  public void testSimplePolygonHole() {
    checkValid(
          "POLYGON ((10 90, 90 90, 90 10, 10 10, 10 90), (60 20, 20 70, 90 90, 60 20))");
  }

  public void testPolygonTouchingHoleAtVertex() {
    checkValid(
          "POLYGON ((240 260, 40 260, 40 80, 240 80, 240 260), (140 180, 40 260, 140 240, 140 180))");
  }

  public void testPolygonMultipleHolesTouchAtSamePoint() {
    checkValid(
          "POLYGON ((10 90, 90 90, 90 10, 10 10, 10 90), (40 80, 60 80, 50 50, 40 80), (20 60, 20 40, 50 50, 20 60), (40 20, 60 20, 50 50, 40 20))");
  }

  public void testPolygonHoleOutsideShellAllTouch() {
    checkInvalid(TopologyValidationError.HOLE_OUTSIDE_SHELL,
          "POLYGON ((10 10, 30 10, 30 50, 70 50, 70 10, 90 10, 90 90, 10 90, 10 10), (50 50, 30 10, 70 10, 50 50))");
  }

  public void testPolygonHoleOutsideShellDoubleTouch() {
    checkInvalid(TopologyValidationError.HOLE_OUTSIDE_SHELL,
          "POLYGON ((10 90, 90 90, 90 10, 10 10, 10 90), (20 80, 80 80, 80 20, 20 20, 20 80), (90 70, 150 50, 90 20, 110 40, 90 70))");
  }

  public void testPolygonNestedHolesAllTouch() {
    checkInvalid(TopologyValidationError.NESTED_HOLES,
          "POLYGON ((10 90, 90 90, 90 10, 10 10, 10 90), (20 80, 80 80, 80 20, 20 20, 20 80), (50 80, 80 50, 50 20, 20 50, 50 80))");
  }

  public void testInvalidPolygonHoleProperIntersection() {
    checkInvalid( TopologyValidationError.SELF_INTERSECTION, 
        "POLYGON ((10 90, 50 50, 10 10, 10 90), (20 50, 60 70, 60 30, 20 50))");
  }

  public void testInvalidPolygonDisconnectedInterior() {
    checkInvalid( TopologyValidationError.DISCONNECTED_INTERIOR,
          "POLYGON ((10 90, 90 90, 90 10, 10 10, 10 90), (20 80, 30 80, 20 20, 20 80), (80 30, 20 20, 80 20, 80 30), (80 80, 30 80, 80 30, 80 80))");
  }

  public void testValidMultiPolygonTouchAtVertices() {
    checkValid(
          "MULTIPOLYGON (((10 10, 10 90, 90 90, 90 10, 80 80, 50 20, 20 80, 10 10)), ((90 10, 10 10, 50 20, 90 10)))");
  }

  public void testInvalidMultiPolygonHoleOverlapCrossing() {
    checkInvalid( TopologyValidationError.SELF_INTERSECTION,
        "MULTIPOLYGON (((20 380, 420 380, 420 20, 20 20, 20 380), (220 340, 180 240, 60 200, 140 100, 340 60, 300 240, 220 340)), ((60 200, 340 60, 220 340, 60 200)))");
  }

  public void testValidMultiPolygonTouchAtVerticesSegments() {
    checkValid(
          "MULTIPOLYGON (((60 40, 90 10, 90 90, 10 90, 10 10, 40 40, 60 40)), ((50 40, 20 20, 80 20, 50 40)))");
  }

  public void testInvalidMultiPolygonNestedAllTouchAtVertices() {
    checkInvalid( TopologyValidationError.NESTED_SHELLS,
          "MULTIPOLYGON (((10 10, 20 30, 10 90, 90 90, 80 30, 90 10, 50 20, 10 10)), ((80 30, 20 30, 50 20, 80 30)))");
  }

  public void testValidMultiPolygonHoleTouchVertices()  {
    checkValid(
          "MULTIPOLYGON (((20 380, 420 380, 420 20, 20 20, 20 380), (220 340, 80 320, 60 200, 140 100, 340 60, 300 240, 220 340)), ((60 200, 340 60, 220 340, 60 200)))");
  }
  
  public void testLineString() {
    checkInvalid( "LINESTRING(0 0, 0 0)");
  }
  
  public void testLinearRingTriangle() {
    checkValid( "LINEARRING (100 100, 150 200, 200 100, 100 100)");
  }

  public void testLinearRingSelfCrossing() {
    checkInvalid( TopologyValidationError.RING_SELF_INTERSECTION,
          "LINEARRING (150 100, 300 300, 100 300, 350 100, 150 100)");
  }

  public void testLinearRingSelfCrossing2() {
    checkInvalid( TopologyValidationError.RING_SELF_INTERSECTION,
          "LINEARRING (0 0, 100 100, 100 0, 0 100, 0 0)");
  }

  //=============================================
  
  private void checkValid(String wkt) {
    checkValid(true, wkt);
  }
 
  
  private void checkValid(boolean isExpectedValid, String wkt) {
    Geometry geom = read(wkt);
    boolean isValid = geom.isValid();
    assertEquals( isExpectedValid, isValid );
  }

  private void checkInvalid(String wkt) {
    checkValid(false, wkt);
  }
  
  private void checkInvalid(int exepctedErrType, String wkt) {
    Geometry geom = read(wkt);
    IsValidOp validOp = new IsValidOp(geom);
    TopologyValidationError err = validOp.getValidationError();
    assertEquals( exepctedErrType, err.getErrorType() );
  }
}
