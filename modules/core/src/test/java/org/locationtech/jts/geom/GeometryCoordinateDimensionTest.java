package org.locationtech.jts.geom;

import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.io.WKTReader;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests for {@link Geometry#getCoordinateDimension(Geometry)}.
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

  public void testMultiPolygonXYZM() {
    checkDimension("MULTIPOLYGON ZM (((1 9 2 3, 9 9 2 3, 9 1 2 3, 1 1 2 3, 1 9 2 3)))", 4);
  }

  public void testGeometryCollectionMixedDimension() {
    checkDimension("GEOMETRYCOLLECTION (POINT (1 1), LINESTRING Z (1 1 1, 2 2 2))", 3);
  }

  public void testGeometryCollectionAllEmpty() {
    checkDimension("GEOMETRYCOLLECTION (POINT EMPTY, LINESTRING EMPTY)", 2);
  }

  private void checkDimension(String wkt, int expectedDimension) {
    Geometry geom = read(strictReader, wkt);
    int actual = Geometry.getCoordinateDimension(geom);
    assertEquals(expectedDimension, actual);
  }
}
