
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

import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;



/**
 * @version 1.7
 */
public class NormalizeTest extends TestCase {

  PrecisionModel precisionModel = new PrecisionModel(1);
  GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public NormalizeTest(String Name_) {
    super(Name_);
  }

  public static void main(String[] args) {
    String[] testCaseName = {NormalizeTest.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }

  public void testNormalizePoint() throws Exception {
    Point point = (Point) reader.read("POINT (30 30)");
    point.normalize();
    assertEquals(new Coordinate(30, 30), point.getCoordinate());
  }

  public void testNormalizeEmptyPoint() throws Exception {
    Point point = (Point) reader.read("POINT EMPTY");
    point.normalize();
    assertEquals(null, point.getCoordinate());
  }

  public void testComparePoint() throws Exception {
    Point p1 = (Point) reader.read("POINT (30 30)");
    Point p2 = (Point) reader.read("POINT (30 40)");
    assertTrue(p1.compareTo(p2) < 0);
  }

  public void testCompareEmptyPoint() throws Exception {
    Point p1 = (Point) reader.read("POINT (30 30)");
    Point p2 = (Point) reader.read("POINT EMPTY");
    assertTrue(p1.compareTo(p2) > 0);
  }

  public void testNormalizeMultiPoint() throws Exception {
    MultiPoint m = (MultiPoint) reader.read(
          "MULTIPOINT(30 20, 10 10, 20 20, 30 30, 20 10)");
    m.normalize();
    MultiPoint expectedValue = (MultiPoint) reader.read(
          "MULTIPOINT(10 10, 20 10, 20 20, 30 20, 30 30)");
    assertEqualsExact(expectedValue, m);
    MultiPoint unexpectedValue = (MultiPoint) reader.read(
          "MULTIPOINT(20 10, 20 20, 30 20, 30 30, 10 10)");
    assertTrue(! m.equalsExact(unexpectedValue));
  }

  public void testNormalizeLineString1() throws Exception {
    LineString l = (LineString) reader.read(
          "LINESTRING (20 20, 160 40, 160 100, 100 120, 60 60)");
    l.normalize();
    LineString expectedValue = (LineString) reader.read(
          "LINESTRING (20 20, 160 40, 160 100, 100 120, 60 60)");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeLineString2() throws Exception {
    LineString l = (LineString) reader.read(
          "LINESTRING (20 20, 160 40, 160 100, 100 120, 60 60)");
    l.normalize();
    LineString expectedValue = (LineString) reader.read(
          "LINESTRING (20 20, 160 40, 160 100, 100 120, 60 60)");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeLineString3() throws Exception {
    LineString l = (LineString) reader.read(
          "LINESTRING (200 240, 140 160, 80 160, 160 80, 80 80)");
    l.normalize();
    LineString expectedValue = (LineString) reader.read(
          "LINESTRING (80 80, 160 80, 80 160, 140 160, 200 240)");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeLineString4() throws Exception {
    LineString l = (LineString) reader.read(
          "LINESTRING (200 240, 140 160, 80 160, 160 80, 80 80)");
    l.normalize();
    LineString expectedValue = (LineString) reader.read(
          "LINESTRING (80 80, 160 80, 80 160, 140 160, 200 240)");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeLineString5() throws Exception {
    LineString l = (LineString) reader.read(
          "LINESTRING (200 340, 140 240, 140 160, 60 240, 140 240, 200 340)");
    l.normalize();
    LineString expectedValue = (LineString) reader.read(
          "LINESTRING (200 340, 140 240, 60 240, 140 160, 140 240, 200 340)");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeEmptyLineString() throws Exception {
    LineString l = (LineString) reader.read("LINESTRING EMPTY");
    l.normalize();
    LineString expectedValue = (LineString) reader.read("LINESTRING EMPTY");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeEmptyPolygon() throws Exception {
    Polygon actualValue = (Polygon) reader.read("POLYGON EMPTY");
    actualValue.normalize();
    Polygon expectedValue = (Polygon) reader.read("POLYGON EMPTY");
    assertEqualsExact(expectedValue, actualValue);
  }

  public void testNormalizePolygon1() throws Exception {
    Polygon actualValue = (Polygon) reader.read(
          "POLYGON ((120 320, 240 200, 120 80, 20 200, 120 320), (60 200, 80 220, 80 200, 60 200), (160 200, 180 200, 180 220, 160 200), (120 140, 140 140, 140 160, 120 140), (140 240, 140 220, 120 260, 140 240))");
    actualValue.normalize();
    Polygon expectedValue = (Polygon) reader.read(
          "POLYGON ((20 200, 120 320, 240 200, 120 80, 20 200), (60 200, 80 200, 80 220, 60 200), (120 140, 140 140, 140 160, 120 140), (120 260, 140 220, 140 240, 120 260), (160 200, 180 200, 180 220, 160 200))");
    assertEqualsExact(expectedValue, actualValue);
  }

  public void testNormalizeMultiLineString() throws Exception {
    MultiLineString actualValue = (MultiLineString) reader.read(
          "MULTILINESTRING ((200 260, 180 320, 260 340), (120 180, 140 100, 40 80), (200 180, 220 160, 200 180), (100 280, 120 260, 140 260, 140 240, 120 240, 120 260, 100 280))");
    actualValue.normalize();
    MultiLineString expectedValue = (MultiLineString) reader.read(
          "MULTILINESTRING ((40 80, 140 100, 120 180), (100 280, 120 260, 120 240, 140 240, 140 260, 120 260, 100 280), (200 180, 220 160, 200 180), (200 260, 180 320, 260 340))");
    assertEqualsExact(expectedValue, actualValue);
  }

  public void testNormalizeMultiPolygon() throws Exception {
    MultiPolygon actualValue = (MultiPolygon) reader.read(
          "MULTIPOLYGON (((40 360, 40 280, 140 280, 140 360, 40 360), (60 340, 60 300, 120 300, 120 340, 60 340)), ((140 200, 260 200, 260 100, 140 100, 140 200), (160 180, 240 180, 240 120, 160 120, 160 180)))");
    actualValue.normalize();
    MultiPolygon expectedValue = (MultiPolygon) reader.read(
          "MULTIPOLYGON (((40 280, 40 360, 140 360, 140 280, 40 280), (60 300, 120 300, 120 340, 60 340, 60 300)), ((140 100, 140 200, 260 200, 260 100, 140 100), (160 120, 240 120, 240 180, 160 180, 160 120)))");
    assertEqualsExact(expectedValue, actualValue);
  }

  public void testNormalizeGeometryCollection() throws Exception {
    GeometryCollection actualValue = (GeometryCollection) reader.read(
          "GEOMETRYCOLLECTION (LINESTRING (200 300, 200 280, 220 280, 220 320, 180 320), POINT (140 220), POLYGON ((100 80, 100 160, 20 160, 20 80, 100 80), (40 140, 40 100, 80 100, 80 140, 40 140)), POINT (100 240))");
    actualValue.normalize();
    GeometryCollection expectedValue = (GeometryCollection) reader.read(
          "GEOMETRYCOLLECTION (POINT (100 240), POINT (140 220), LINESTRING (180 320, 220 320, 220 280, 200 280, 200 300), POLYGON ((20 80, 20 160, 100 160, 100 80, 20 80), (40 100, 80 100, 80 140, 40 140, 40 100)))");
    assertEqualsExact(expectedValue, actualValue);
  }

  public void testNormalizePackedCoordinateSequence() throws Exception {
    GeometryFactory pcsFactory = new GeometryFactory(PackedCoordinateSequenceFactory.DOUBLE_FACTORY);
    WKTReader pcsReader = new WKTReader(pcsFactory);
    Geometry geom = pcsReader.read("LINESTRING (100 100, 0 0)");
    geom.normalize();
    // force PackedCoordinateSequence to be copied with empty coordinate cache
    Geometry clone = (Geometry) geom.clone();
    assertEqualsExact(geom, clone);
  }

  private void assertEqualsExact(Geometry expectedValue, Geometry actualValue) {
    assertTrue("Expected " + expectedValue + " but encountered " + actualValue,
          actualValue.equalsExact(expectedValue));
  }
}
