/*
 * Copyright (c) 2026 Matthew de Detrich.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.geom;

import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.io.WKTReader;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests for {@link Geometry#getCoordinateDimension()}.
 */
public class GeometryCoordinateDimensionTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(GeometryCoordinateDimensionTest.class);
  }

  static GeometryFactory geomFact = new GeometryFactory(PackedCoordinateSequenceFactory.DOUBLE_FACTORY);

  /**
   * A reader with the legacy "unlabelled 3rd ordinate is Z" syntax disabled,
   * so that WKT lacking a Z or M tag is parsed as genuinely 2D.
   */
  static WKTReader strictReader = strictReader();

  private static WKTReader strictReader() {
    WKTReader reader = new WKTReader(geomFact);
    reader.setIsOldJtsCoordinateSyntaxAllowed(false);
    return reader;
  }

  static GeometryFactory arrayGeomFact = new GeometryFactory(CoordinateArraySequenceFactory.instance());

  static WKTReader arrayStrictReader = arrayStrictReader();

  private static WKTReader arrayStrictReader() {
    WKTReader reader = new WKTReader(arrayGeomFact);
    reader.setIsOldJtsCoordinateSyntaxAllowed(false);
    return reader;
  }

  public GeometryCoordinateDimensionTest(String name) { super(name); }

  public void testXY() {
    checkDimension("POINT (1 2)", 2);
  }

  public void testXYZ() {
    checkDimension("POINT Z (1 2 3)", 3);
  }

  public void testXYM() {
    checkDimension("POINT M (1 2 3)", 3);
  }

  public void testXYZM() {
    checkDimension("POINT ZM (1 2 3 4)", 4);
  }

  public void testLineStringXYZ() {
    checkDimension("LINESTRING Z (1 1 1, 2 2 2, 3 3 3)", 3);
  }

  public void testPolygonXYZ() {
    checkDimension("POLYGON Z ((1 9 2, 9 9 2, 9 1 2, 1 1 2, 1 9 2))", 3);
  }

  public void testEmptyPoint() {
    checkDimension("POINT EMPTY", 2);
  }

  public void testEmptyPointZ() {
    checkDimension("POINT Z EMPTY", 3);
  }

  public void testEmptyPointZM() {
    checkDimension("POINT ZM EMPTY", 4);
  }

  public void testEmptyLineStringZ() {
    checkDimension("LINESTRING Z EMPTY", 3);
  }

  public void testGeometryCollectionEmptyZ() {
    checkDimension("GEOMETRYCOLLECTION (POINT Z EMPTY, LINESTRING EMPTY)", 3);
  }

  public void testMultiPolygonXYZM() {
    checkDimension("MULTIPOLYGON ZM (((1 9 2 3, 9 9 2 3, 9 1 2 3, 1 1 2 3, 1 9 2 3)))", 4);
  }

  public void testGeometryCollectionMixedDimension() {
    checkDimension("GEOMETRYCOLLECTION (POINT (1 1), LINESTRING Z (1 1 1, 2 2 2))", 3);
  }

  public void testGeometryCollectionAllEmpty() {
    checkDimension("GEOMETRYCOLLECTION (POINT EMPTY, LINESTRING EMPTY)", 2);
  }

  public void testArraySequenceXY() {
    checkDimension(arrayStrictReader, "POINT (1 2)", 2);
  }

  public void testArraySequenceEmptyPointZ() {
    checkDimension(arrayStrictReader, "POINT Z EMPTY", 3);
  }

  /**
   * Empty geometries created by the factory use the
   * default coordinate sequence dimension (3).
   */
  public void testArraySequenceFactoryEmptyPoint() {
    assertEquals(3, arrayGeomFact.createPoint().getCoordinateDimension());
  }

  private void checkDimension(String wkt, int expectedDimension) {
    checkDimension(strictReader, wkt, expectedDimension);
  }

  private void checkDimension(WKTReader reader, String wkt, int expectedDimension) {
    Geometry geom = read(reader, wkt);
    int actual = geom.getCoordinateDimension();
    assertEquals(expectedDimension, actual);
  }
}
