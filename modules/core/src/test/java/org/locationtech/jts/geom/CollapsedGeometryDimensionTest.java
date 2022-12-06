package org.locationtech.jts.geom;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;


public class CollapsedGeometryDimensionTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(CollapsedGeometryDimensionTest.class);
  }

  private GeometryFactory factory = new GeometryFactory();
  private WKTReader reader = new WKTReader(factory);

  public CollapsedGeometryDimensionTest(String name) { super(name); }

  public void testMock()
  {
    assertTrue(true);
  }

  public void testPoint() throws ParseException
  {
    // empty
    assertTrue(reader.read("Point Empty").getDimension() == 0);
    // normal
    assertTrue(reader.read("Point(0 0)").getDimension() == 0);
    // collapsed
    assertTrue(reader.read("Point Empty").getDimension() == 0);
  }

  public void testLineString() throws ParseException
  {
    // empty
    assertTrue(reader.read("LineString Empty").getDimension() == 1);
    // normal (open/closed)
    assertTrue(reader.read("LineString(0 0, 1 0)").getDimension() == 1);
    assertTrue(reader.read("LineString(0 0, 1 0, 0.5 0.5, 0 0)").getDimension() == 1);
    // collapsed
    assertTrue(reader.read("LineString(0 0, 0 0)").getDimension() == 0);
  }

  public void testPolygon() throws ParseException
  {
    // empty
    assertTrue(reader.read("Polygon Empty").getDimension() == 2);
    // normal
    assertTrue(reader.read("Polygon((0 0, 1 0, 0.5 0.5, 0 0))").getDimension() == 2);
    // collapsed in dimension 1 (not yet detected -> dimension 2)
    assertTrue(reader.read("Polygon((0 0, 1 0, 2 0, 0 0))").getDimension() == 2);
    // collapsed
    assertTrue(reader.read("Polygon((0 0, 0 0, 0 0, 0 0))").getDimension() == 0);
    // cannot be initialized (linear ring not closed)
    //assertTrue(reader.read("Polygon((0 0, 1 0, 1 0, 1 0))").getDimension() == 1);
  }

  public void testLinearRing() throws ParseException
  {
    // empty
    assertTrue(reader.read("LinearRing Empty").getDimension() == 1);
    // normal
    assertTrue(reader.read("LinearRing(0 0, 1 0, 0.5 0.5, 0 0)").getDimension() == 1);
    assertTrue(reader.read("LinearRing(0 0, 1 0, 2 0, 0 0)").getDimension() == 1);
    // collapsed
    assertTrue(reader.read("LinearRing(0 0, 0 0, 0 0, 0 0)").getDimension() == 0);
  }

  public void testMultiPoint() throws ParseException
  {
    // empty
    assertTrue(reader.read("MultiPoint Empty").getDimension() == 0);
    // normal
    assertTrue(reader.read("MultiPoint((0 0), (1 1))").getDimension() == 0);
    // normal
    assertTrue(reader.read("MultiPoint((0 0), (0 0))").getDimension() == 0);
  }

  public void testMultiLineString() throws ParseException
  {
    // empty
    assertTrue(reader.read("MultiLineString Empty").getDimension() == 1);
    // normal (open/closed)
    assertTrue(reader.read("MultiLineString((0 0, 1 0), (2 0, 3 0))").getDimension() == 1);
    assertTrue(reader.read("MultiLineString((0 0, 1 0, 0.5 0.5, 0 0))").getDimension() == 1);
    // partially collapsed
    assertTrue(reader.read("MultiLineString((0 0, 0 0), (1 0, 2 0))").getDimension() == 1);
    // collapsed
    assertTrue(reader.read("MultiLineString((0 0, 0 0), (1 0, 1 0))").getDimension() == 0);
  }

  public void testMultiPolygon() throws ParseException
  {
    // empty
    assertTrue(reader.read("MultiPolygon Empty").getDimension() == 2);
    // normal
    assertTrue(reader.read("MultiPolygon(((0 0, 1 0, 0.5 0.5, 0 0)),((10 0, 11 0, 10.5 0.5, 10 0)))").getDimension() == 2);
    // partially collapsed
    assertTrue(reader.read("MultiPolygon(((0 0, 1 0, 0.5 0.5, 0 0)),((0 0, 0 0, 0 0, 0 0)))").getDimension() == 2);
    // collapsed
    assertTrue(reader.read("MultiPolygon(((0 0, 0 0, 0 0, 0 0)),((1 1, 1 1, 1 1, 1 1)))").getDimension() == 0);
  }

  public void tesGeometryCollection() throws ParseException
  {
    // empty
    assertTrue(reader.read("GeometryCollection Empty").getDimension() == 0);
    // normal
    assertTrue(reader.read("GeometryCollection (Point(0 0),LineString(0 0, 1 0)").getDimension() == 1);
    assertTrue(reader.read("GeometryCollection (Point(0 0),LineString(0 0, 1 0), Polygon((0 0, 1 0, 0.5 0.5, 0 0))").getDimension() == 2);
    // collapsed
    assertTrue(reader.read("GeometryCollection (Point(0 0),LineString(0 0, 0 0)").getDimension() == 0);
    assertTrue(reader.read("GeometryCollection (Point(0 0),Polygon((0 0, 0 0, 0 0, 0 0))").getDimension() == 0);
  }

}
