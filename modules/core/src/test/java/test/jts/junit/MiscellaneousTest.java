
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

package test.jts.junit;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;



/**
 * @version 1.7
 */
public class MiscellaneousTest extends TestCase {

  PrecisionModel precisionModel = new PrecisionModel(1);
  GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(MiscellaneousTest.class);
  }

  public void testEnvelopeCloned() throws Exception {
      Geometry a = reader.read("LINESTRING(0 0, 10 10)");
      //Envelope is lazily initialized [Jon Aquino]
      a.getEnvelopeInternal();
      Geometry b = (Geometry)a.clone();
      assertTrue(a.getEnvelopeInternal() != b.getEnvelopeInternal());
  }

  public MiscellaneousTest(String name) { super(name); }

  public void testCreateEmptyGeometry() throws Exception {
    assertTrue(geometryFactory.createPoint((Coordinate)null).isEmpty());
    assertTrue(geometryFactory.createLinearRing(new Coordinate[] { }).isEmpty());
    assertTrue(geometryFactory.createLineString(new Coordinate[] { }).isEmpty());
    assertTrue(geometryFactory.createPolygon(geometryFactory.createLinearRing(new Coordinate[] { }), new LinearRing[] { }).isEmpty());
    assertTrue(geometryFactory.createMultiPolygon(new Polygon[] { }).isEmpty());
    assertTrue(geometryFactory.createMultiLineString(new LineString[] { }).isEmpty());
    assertTrue(geometryFactory.createMultiPoint(new Point[] { }).isEmpty());

    assertTrue(geometryFactory.createPoint((Coordinate)null).isSimple());
    assertTrue(geometryFactory.createLinearRing(new Coordinate[] { }).isSimple());
/**
 * @todo Enable when #isSimple implemented
 */
//    assertTrue(geometryFactory.createLineString(new Coordinate[] { }).isSimple());
//    assertTrue(geometryFactory.createPolygon(geometryFactory.createLinearRing(new Coordinate[] { }), new LinearRing[] { }).isSimple());
//    assertTrue(geometryFactory.createMultiPolygon(new Polygon[] { }).isSimple());
//    assertTrue(geometryFactory.createMultiLineString(new LineString[] { }).isSimple());
//    assertTrue(geometryFactory.createMultiPoint(new Point[] { }).isSimple());

    assertTrue(geometryFactory.createPoint((Coordinate)null).getBoundary().isEmpty());
    assertTrue(geometryFactory.createLinearRing(new Coordinate[] { }).getBoundary().isEmpty());
    assertTrue(geometryFactory.createLineString(new Coordinate[] { }).getBoundary().isEmpty());
    assertTrue(geometryFactory.createPolygon(geometryFactory.createLinearRing(new Coordinate[] { }), new LinearRing[] { }).getBoundary().isEmpty());
    assertTrue(geometryFactory.createMultiPolygon(new Polygon[] { }).getBoundary().isEmpty());
    assertTrue(geometryFactory.createMultiLineString(new LineString[] { }).getBoundary().isEmpty());
    assertTrue(geometryFactory.createMultiPoint(new Point[] { }).getBoundary().isEmpty());

    assertTrue(geometryFactory.createLinearRing((CoordinateSequence)null).isEmpty());
    assertTrue(geometryFactory.createLineString((Coordinate[])null).isEmpty());
    assertTrue(geometryFactory.createPolygon(null, null).isEmpty());
    assertTrue(geometryFactory.createMultiPolygon(null).isEmpty());
    assertTrue(geometryFactory.createMultiLineString(null).isEmpty());
    assertTrue(geometryFactory.createMultiPoint((Point[]) null).isEmpty());

    assertEquals(-1, (geometryFactory.createPoint((Coordinate)null)).getBoundaryDimension());
    assertEquals(-1, (geometryFactory.createLinearRing((CoordinateSequence)null)).getBoundaryDimension());
    assertEquals(0, (geometryFactory.createLineString((Coordinate[])null)).getBoundaryDimension());
    assertEquals(1, (geometryFactory.createPolygon(null, null)).getBoundaryDimension());
    assertEquals(1, (geometryFactory.createMultiPolygon(null)).getBoundaryDimension());
    assertEquals(0, (geometryFactory.createMultiLineString(null)).getBoundaryDimension());
    assertEquals(-1, (geometryFactory.createMultiPoint((Point[]) null)).getBoundaryDimension());

    assertEquals(0, (geometryFactory.createPoint((Coordinate)null)).getNumPoints());
    assertEquals(0, (geometryFactory.createLinearRing((CoordinateSequence)null)).getNumPoints());
    assertEquals(0, (geometryFactory.createLineString((Coordinate[])null)).getNumPoints());
    assertEquals(0, (geometryFactory.createPolygon(null, null)).getNumPoints());
    assertEquals(0, (geometryFactory.createMultiPolygon(null)).getNumPoints());
    assertEquals(0, (geometryFactory.createMultiLineString(null)).getNumPoints());
    assertEquals(0, (geometryFactory.createMultiPoint((Point[]) null)).getNumPoints());

    assertEquals(0, (geometryFactory.createPoint((Coordinate)null)).getCoordinates().length);
    assertEquals(0, (geometryFactory.createLinearRing((CoordinateSequence)null)).getCoordinates().length);
    assertEquals(0, (geometryFactory.createLineString((Coordinate[])null)).getCoordinates().length);
    assertEquals(0, (geometryFactory.createPolygon(null, null)).getCoordinates().length);
    assertEquals(0, (geometryFactory.createMultiPolygon(null)).getCoordinates().length);
    assertEquals(0, (geometryFactory.createMultiLineString(null)).getCoordinates().length);
    assertEquals(0, (geometryFactory.createMultiPoint((Point[]) null)).getCoordinates().length);
  }

  public void testBoundaryOfEmptyGeometry() throws Exception {
    assertTrue(geometryFactory.createPoint((Coordinate)null).getBoundary().getClass() == GeometryCollection.class);
    assertTrue(geometryFactory.createLinearRing(new Coordinate[] { }).getBoundary().getClass() == MultiPoint.class);
    assertTrue(geometryFactory.createLineString(new Coordinate[] { }).getBoundary().getClass() == MultiPoint.class);
    assertTrue(geometryFactory.createPolygon(geometryFactory.createLinearRing(new Coordinate[] { }), new LinearRing[] { }).getBoundary().getClass() == MultiLineString.class);
    assertTrue(geometryFactory.createMultiPolygon(new Polygon[] { }).getBoundary().getClass() == MultiLineString.class);
    assertTrue(geometryFactory.createMultiLineString(new LineString[] { }).getBoundary().getClass() == MultiPoint.class);
    assertTrue(geometryFactory.createMultiPoint(new Point[] { }).getBoundary().getClass() == GeometryCollection.class);
    try {
      geometryFactory.createGeometryCollection(new Geometry[] { }).getBoundary();
      assertTrue(false);
    }
    catch (IllegalArgumentException e) {
    }
  }

  public void testToPointArray() {
    ArrayList list = new ArrayList();
    list.add(geometryFactory.createPoint(new Coordinate(0, 0)));
    list.add(geometryFactory.createPoint(new Coordinate(10, 0)));
    list.add(geometryFactory.createPoint(new Coordinate(10, 10)));
    list.add(geometryFactory.createPoint(new Coordinate(0, 10)));
    list.add(geometryFactory.createPoint(new Coordinate(0, 0)));
    Point[] points = GeometryFactory.toPointArray(list);
    assertEquals(10, points[1].getX(), 1E-1);
    assertEquals(0, points[1].getY(), 1E-1);
  }

  public void testPolygonGetCoordinates() throws Exception {
    Polygon p = (Polygon) reader.read(
          "POLYGON ( (0 0, 100 0, 100 100, 0 100, 0 0), "
        + "          (20 20, 20 80, 80 80, 80 20, 20 20)) ");
    Coordinate[] coordinates = p.getCoordinates();
    assertEquals(10, p.getNumPoints());
    assertEquals(10, coordinates.length);
    assertEquals(new Coordinate(0, 0), coordinates[0]);
    assertEquals(new Coordinate(20, 20), coordinates[9]);
  }

  public void testEmptyPoint() throws Exception {
    Point p = geometryFactory.createPoint((Coordinate)null);
    assertEquals(0, p.getDimension());
    assertEquals(new Envelope(), p.getEnvelopeInternal());
    assertTrue(p.isSimple());
    try {
      p.getX();
      assertTrue(false);
    }
    catch (IllegalStateException e1) {
    }
    try {
      p.getY();
      assertTrue(false);
    }
    catch (IllegalStateException e2) {
    }

    assertEquals("POINT EMPTY", p.toString());
    assertEquals("POINT EMPTY", p.toText());
  }

  public void testEmptyLineString() throws Exception {
    LineString l = geometryFactory.createLineString((Coordinate[])null);
    assertEquals(1, l.getDimension());
    assertEquals(new Envelope(), l.getEnvelopeInternal());
/**
 * @todo Enable when #isSimple implemented
 */
//    assertTrue(l.isSimple());
    assertEquals(null, l.getStartPoint());
    assertEquals(null, l.getEndPoint());
    assertTrue(! l.isClosed());
    assertTrue(! l.isRing());
  }

  public void testEmptyLinearRing() throws Exception {
    LineString l = geometryFactory.createLinearRing((CoordinateSequence)null);
    assertEquals(1, l.getDimension());
    assertEquals(new Envelope(), l.getEnvelopeInternal());
    assertTrue(l.isSimple());
    assertEquals(null, l.getStartPoint());
    assertEquals(null, l.getEndPoint());
    assertTrue(l.isClosed());
    assertTrue(l.isRing());
  }

  public void testEmptyPolygon() throws Exception {
    Polygon p = geometryFactory.createPolygon(null, null);
    assertEquals(2, p.getDimension());
    assertEquals(new Envelope(), p.getEnvelopeInternal());
    assertTrue(p.isSimple());
  }

  public void testEmptyGeometryCollection() throws Exception {
    GeometryCollection g = geometryFactory.createGeometryCollection(null);
    assertEquals(-1, g.getDimension());
    assertEquals(new Envelope(), g.getEnvelopeInternal());
    assertTrue(g.isSimple());
  }

  public void testEmptyMultiPoint() throws Exception {
    MultiPoint g = geometryFactory.createMultiPoint((Point[])null);
    assertEquals(0, g.getDimension());
    assertEquals(new Envelope(), g.getEnvelopeInternal());
/**
 * @todo Enable when #isSimple implemented
 */
//    assertTrue(g.isSimple());
  }

  public void testEmptyMultiLineString() throws Exception {
    MultiLineString g = geometryFactory.createMultiLineString(null);
    assertEquals(1, g.getDimension());
    assertEquals(new Envelope(), g.getEnvelopeInternal());
/**
 * @todo Enable when #isSimple implemented
 */
//    assertTrue(g.isSimple());
    assertTrue(! g.isClosed());
  }

  public void testEmptyMultiPolygon() throws Exception {
    MultiPolygon g = geometryFactory.createMultiPolygon(null);
    assertEquals(2, g.getDimension());
    assertEquals(new Envelope(), g.getEnvelopeInternal());
    assertTrue(g.isSimple());
  }

  public void testGetGeometryType() throws Exception {
    GeometryCollection g = geometryFactory.createMultiPolygon(null);
    assertEquals("MultiPolygon", g.getGeometryType());
  }

  public void testMultiPolygonIsSimple1() throws Exception {
    Geometry g = reader.read("MULTIPOLYGON (((10 10, 10 20, 20 20, 20 15, 10 10)), ((60 60, 70 70, 80 60, 60 60)))");
    assertTrue(g.isSimple());
  }

  public void testPointIsSimple() throws Exception {
    Geometry g = reader.read("POINT (10 10)");
    assertTrue(g.isSimple());
  }

  public void testPointGetBoundary() throws Exception {
    Geometry g = reader.read("POINT (10 10)");
    assertTrue(g.getBoundary().isEmpty());
  }

/**
 * @todo Enable when #isSimple implemented
 */
//  public void testMultiPointIsSimple1() throws Exception {
//    Geometry g = reader.read("MULTIPOINT(10 10, 20 20, 30 30)");
//    assertTrue(g.isSimple());
//  }

  public void testMultiPointGetBoundary() throws Exception {
    Geometry g = reader.read("MULTIPOINT(10 10, 20 20, 30 30)");
    assertTrue(g.getBoundary().isEmpty());
  }

/**
 * @todo Enable when #isSimple implemented
 */
//  public void testMultiPointIsSimple2() throws Exception {
//    Geometry g = reader.read("MULTIPOINT(10 10, 30 30, 30 30)");
//    assertTrue(! g.isSimple());
//  }

/**
 * @todo Enable when #isSimple implemented
 */
//  public void testLineStringIsSimple1() throws Exception {
//    Geometry g = reader.read("LINESTRING(10 10, 20 10, 15 20)");
//    assertTrue(g.isSimple());
//  }

  public void testLineStringGetBoundary1() throws Exception {
    LineString g = (LineString) reader.read("LINESTRING(10 10, 20 10, 15 20)");
    assertTrue(g.getBoundary() instanceof MultiPoint);
    MultiPoint boundary = (MultiPoint) g.getBoundary();
    assertTrue(boundary.getGeometryN(0).equals(g.getStartPoint()));
    assertTrue(boundary.getGeometryN(1).equals(g.getEndPoint()));
  }

  public void testLineStringGetBoundary2() throws Exception {
    LineString g = (LineString) reader.read("LINESTRING(10 10, 20 10, 15 20, 10 10)");
    assertTrue(g.getBoundary().isEmpty());
  }

/**
 * @todo Enable when #isSimple implemented
 */
//  public void testLineStringIsSimple2() throws Exception {
//    Geometry g = reader.read("LINESTRING(10 10, 20 10, 15 20, 15 0)");
//    assertTrue(! g.isSimple());
//  }

  public void testLinearRingIsSimple() throws Exception {
    Coordinate[] coordinates = { new Coordinate(10, 10, 0),
                                 new Coordinate(10, 20, 0),
                                 new Coordinate(20, 20, 0),
                                 new Coordinate(20, 15, 0),
                                 new Coordinate(10, 10, 0) };
    LinearRing linearRing = geometryFactory.createLinearRing(coordinates);
    assertTrue(linearRing.isSimple());
  }

  public void testPolygonIsSimple() throws Exception {
    Geometry g = reader.read("POLYGON((10 10, 10 20, 202 0, 20 15, 10 10))");
    assertTrue(g.isSimple());
  }

  public void testPolygonGetBoundary() throws Exception {
    Geometry g = reader.read("POLYGON("
          + "(0 0, 40 0, 40 40, 0 40, 0 0),"
          + "(10 10, 30 10, 30 30, 10 30, 10 10))");
    Geometry b = reader.read("MULTILINESTRING("
          + "(0 0, 40 0, 40 40, 0 40, 0 0),"
          + "(10 10, 30 10, 30 30, 10 30, 10 10))");
    assertTrue(b.equalsExact(g.getBoundary()));
  }

  public void testMultiPolygonGetBoundary1() throws Exception {
    Geometry g = reader.read("MULTIPOLYGON("
          + "(  (0 0, 40 0, 40 40, 0 40, 0 0),"
          + "   (10 10, 30 10, 30 30, 10 30, 10 10)  ),"
          + "(  (200 200, 210 200, 210 210, 200 200) )  )");
    Geometry b = reader.read("MULTILINESTRING("
          + "(0 0, 40 0, 40 40, 0 40, 0 0),"
          + "(10 10, 30 10, 30 30, 10 30, 10 10),"
          + "(200 200, 210 200, 210 210, 200 200))");
    assertTrue(b.equalsExact(g.getBoundary()));
  }

  public void testMultiPolygonIsSimple2() throws Exception {
    Geometry g = reader.read("MULTIPOLYGON("
          + "((10 10, 10 20, 20 20, 20 15, 10 10)), "
          + "((60 60, 70 70, 80 60, 60 60))  )");
    assertTrue(g.isSimple());
  }

//  public void testGeometryCollectionIsSimple1() throws Exception {
//    Geometry g = reader.read("GEOMETRYCOLLECTION("
//          + "LINESTRING(0 0,  100 0),"
//          + "LINESTRING(0 10, 100 10))");
//    assertTrue(g.isSimple());
//  }

//  public void testGeometryCollectionIsSimple2() throws Exception {
//    Geometry g = reader.read("GEOMETRYCOLLECTION("
//          + "LINESTRING(0 0,  100 0),"
//          + "LINESTRING(50 0, 100 10))");
//    assertTrue(! g.isSimple());
//  }

/**
 * @todo Enable when #isSimple implemented
 */
//  public void testMultiLineStringIsSimple1() throws Exception {
//    Geometry g = reader.read("MULTILINESTRING("
//          + "(0 0,  100 0),"
//          + "(0 10, 100 10))");
//    assertTrue(g.isSimple());
//  }

/**
 * @todo Enable when #isSimple implemented
 */
//  public void testMultiLineStringIsSimple2() throws Exception {
//    Geometry g = reader.read("MULTILINESTRING("
//          + "(0 0,  100 0),"
//          + "(50 0, 100 10))");
//    assertTrue(! g.isSimple());
//  }

  public void testMultiLineStringGetBoundary1() throws Exception {
    Geometry g = reader.read("MULTILINESTRING("
          + "(0 0,  100 0, 50 50),"
          + "(50 50, 50 -50))");
    Geometry m = reader.read("MULTIPOINT(0 0, 50 -50)");
    assertTrue(m.equalsExact(g.getBoundary()));
  }

  public void testMultiLineStringGetBoundary2() throws Exception {
    Geometry g = reader.read("MULTILINESTRING("
          + "(0 0,  100 0, 50 50),"
          + "(50 50, 50 0))");
    Geometry m = reader.read("MULTIPOINT(0 0, 50 0)");
    assertTrue(m.equalsExact(g.getBoundary()));
  }

//  public void testGeometryCollectionGetBoundary1() throws Exception {
//    Geometry g = reader.read("GEOMETRYCOLLECTION("
//          + "POLYGON((0 0, 100 0, 100 100, 0 100, 0 0)),"
//          + "LINESTRING(200 100, 200 0))");
//    Geometry b = reader.read("GEOMETRYCOLLECTION("
//          + "LINESTRING(0 0, 100 0, 100 100, 0 100, 0 0),"
//          + "LINESTRING(200 100, 200 0))");
//    assertEquals(b, g.getBoundary());
//    assertTrue(! g.equals(g.getBoundary()));
//  }

//  public void testGeometryCollectionGetBoundary2() throws Exception {
//    Geometry g = reader.read("GEOMETRYCOLLECTION("
//          + "POLYGON((0 0, 100 0, 100 100, 0 100, 0 0)),"
//          + "LINESTRING(50 50, 60 60))");
//    Geometry b = reader.read("GEOMETRYCOLLECTION("
//          + "LINESTRING(0 0, 100 0, 100 100, 0 100, 0 0))");
//    assertEquals(b, g.getBoundary());
//  }

//  public void testGeometryCollectionGetBoundary3() throws Exception {
//    Geometry g = reader.read("GEOMETRYCOLLECTION("
//          + "POLYGON((0 0, 100 0, 100 100, 0 100, 0 0)),"
//          + "LINESTRING(50 50, 150 50))");
//    Geometry b = reader.read("GEOMETRYCOLLECTION("
//          + "LINESTRING(0 0, 100 0, 100 100, 0 100, 0 0),"
//          + "POINT(150 50))");
//    assertEquals(b, g.getBoundary());
//  }

  public void testCoordinateNaN() {
    Coordinate c1 = new Coordinate();
    assertTrue(! Double.isNaN(c1.x));
    assertTrue(! Double.isNaN(c1.y));
    assertTrue(Double.isNaN(c1.z));

    Coordinate c2 = new Coordinate(3,4);
    assertEquals(3,c2.x,1E-10);
    assertEquals(4,c2.y,1E-10);
    assertTrue(Double.isNaN(c2.z));

    assertEquals(c1,c1);
    assertEquals(c2,c2);
    assertTrue(! c1.equals(c2));
    assertEquals(new Coordinate(),new Coordinate(0,0));
    assertEquals(new Coordinate(3,5),new Coordinate(3,5));
    assertEquals(new Coordinate(3,5,Double.NaN),new Coordinate(3,5,Double.NaN));
    assertTrue(new Coordinate(3,5,0).equals(new Coordinate(3,5,Double.NaN)));
  }

  public void testPredicatesReturnFalseForEmptyGeometries() {
    Point p1 = new GeometryFactory().createPoint((Coordinate)null);
    Point p2 = new GeometryFactory().createPoint(new Coordinate(5,5));
    assertEquals(false, p1.equals(p2));
    assertEquals(true, p1.disjoint(p2));
    assertEquals(false, p1.intersects(p2));
    assertEquals(false, p1.touches(p2));
    assertEquals(false, p1.crosses(p2));
    assertEquals(false, p1.within(p2));
    assertEquals(false, p1.contains(p2));
    assertEquals(false, p1.overlaps(p2));

    assertEquals(false, p2.equals(p1));
    assertEquals(true, p2.disjoint(p1));
    assertEquals(false, p2.intersects(p1));
    assertEquals(false, p2.touches(p1));
    assertEquals(false, p2.crosses(p1));
    assertEquals(false, p2.within(p1));
    assertEquals(false, p2.contains(p1));
    assertEquals(false, p2.overlaps(p1));
  }

}
