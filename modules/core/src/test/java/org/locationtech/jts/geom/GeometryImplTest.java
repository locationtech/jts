
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

import java.util.Arrays;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;




/**
 * @version 1.7
 */
public class GeometryImplTest extends TestCase {
    PrecisionModel precisionModel = new PrecisionModel(1);
    GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
    WKTReader reader = new WKTReader(geometryFactory);
    WKTReader readerFloat = new WKTReader();

    public GeometryImplTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(GeometryImplTest.class);
    }

    public void testPolygonRelate() throws Exception {
        Geometry bigPolygon = reader.read(
                "POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
        Geometry smallPolygon = reader.read(
                "POLYGON ((10 10, 10 30, 30 30, 30 10, 10 10))");
        assertTrue(bigPolygon.contains(smallPolygon));
    }
    
    public void testEmptyGeometryCentroid() throws Exception {
      assertTrue(reader.read("POINT EMPTY").getCentroid().isEmpty());
      assertTrue(reader.read("POLYGON EMPTY").getCentroid().isEmpty());
      assertTrue(reader.read("LINESTRING EMPTY").getCentroid().isEmpty());
      assertTrue(reader.read("GEOMETRYCOLLECTION EMPTY").getCentroid().isEmpty());
      assertTrue(reader.read("GEOMETRYCOLLECTION(GEOMETRYCOLLECTION EMPTY, GEOMETRYCOLLECTION EMPTY)").getCentroid().isEmpty());
      assertTrue(reader.read("MULTIPOLYGON EMPTY").getCentroid().isEmpty());
      assertTrue(reader.read("MULTILINESTRING EMPTY").getCentroid().isEmpty());
      assertTrue(reader.read("MULTIPOINT EMPTY").getCentroid().isEmpty());
    }

    public void testNoOutgoingDirEdgeFound() throws Exception {
        doTestFromCommcast2003AtYahooDotCa(reader);
    }

    public void testOutOfMemoryError() throws Exception {
        doTestFromCommcast2003AtYahooDotCa(new WKTReader());
    }
    
  

    public void testDepthMismatchAssertionFailedException() throws Exception {
      //register@robmeek.com reported an assertion failure 
      //("depth mismatch at (160.0, 300.0, Nan)") [Jon Aquino 10/28/2003]
      reader
          .read("MULTIPOLYGON (((100 300, 100 400, 200 400, 200 300, 100 300)),"
              + "((160 300, 160 400, 260 400, 260 300, 160 300)),"
              + "((160 300, 160 200, 260 200, 260 300, 160 300)))").buffer(0);
    }

    private void doTestFromCommcast2003AtYahooDotCa(WKTReader reader)
        throws ParseException {
    	readerFloat.read(
            "POLYGON ((708653.498611049 2402311.54647056, 708708.895756966 2402203.47250014, 708280.326454234 2402089.6337791, 708247.896591321 2402252.48269854, 708367.379593851 2402324.00761653, 708248.882609455 2402253.07294874, 708249.523621829 2402244.3124463, 708261.854734465 2402182.39086576, 708262.818392579 2402183.35452387, 708653.498611049 2402311.54647056))")
              .intersection(reader.read(
                "POLYGON ((708258.754920656 2402197.91172757, 708257.029447455 2402206.56901508, 708652.961095455 2402312.65463437, 708657.068786251 2402304.6356364, 708258.754920656 2402197.91172757))"));
    }

    public void testEquals() throws Exception {
        Geometry g = reader.read("POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
        Geometry same = reader.read("POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
        Geometry differentStart = reader.read(
                "POLYGON ((0 50, 50 50, 50 0, 0 0, 0 50))");
        Geometry differentFourth = reader.read(
                "POLYGON ((0 0, 0 50, 50 50, 50 -99, 0 0))");
        Geometry differentSecond = reader.read(
                "POLYGON ((0 0, 0 99, 50 50, 50 0, 0 0))");
        doTestEquals(g, same, true, true, true, true);
        doTestEquals(g, differentStart, true, false, false, true);
        doTestEquals(g, differentFourth, false, false, false, false);
        doTestEquals(g, differentSecond, false, false, false, false);
    }

    private void doTestEquals(Geometry a, Geometry b, boolean equalsGeometry,
        boolean equalsObject, boolean equalsExact, boolean equalsHash) {
        assertEquals(equalsGeometry, a.equals(b));
        assertEquals(equalsObject, a.equals((Object) b));
        assertEquals(equalsExact, a.equalsExact(b));
        assertEquals(equalsHash, a.hashCode() == b.hashCode());
    }

    public void testInvalidateEnvelope() throws Exception {
        Geometry g = reader.read("POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
        assertEquals(new Envelope(0, 50, 0, 50), g.getEnvelopeInternal());
        g.apply(new CoordinateFilter() {
                public void filter(Coordinate coord) {
                    coord.x += 1;
                    coord.y += 1;
                }
            });
        assertEquals(new Envelope(0, 50, 0, 50), g.getEnvelopeInternal());
        g.geometryChanged();
        assertEquals(new Envelope(1, 51, 1, 51), g.getEnvelopeInternal());
    }

    public void testEquals1() throws Exception {
        Geometry polygon1 = reader.read(
                "POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
        Geometry polygon2 = reader.read(
                "POLYGON ((50 50, 50 0, 0 0, 0 50, 50 50))");
        assertTrue(polygon1.equals(polygon2));
    }

  public void testEqualsWithNull() throws Exception
  {
    Geometry polygon = reader.read("POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
    assertTrue(! polygon.equals(null));
    final Object g = null;
    assertTrue(! polygon.equals(g));
  }

    //  public void testEquals2() throws Exception {
    //    Geometry lineString = reader.read("LINESTRING(0 0, 0 50, 50 50, 50 0, 0 0)");
    //    Geometry geometryCollection = reader.read("GEOMETRYCOLLECTION ( LINESTRING(0 0  , 0  50), "
    //                                                                 + "LINESTRING(0 50 , 50 50), "
    //                                                                 + "LINESTRING(50 50, 50 0 ), "
    //                                                                 + "LINESTRING(50 0 , 0  0 ) )");
    //    assertTrue(lineString.equals(geometryCollection));
    //  }
    public void testEqualsExactForLinearRings() throws Exception {
        LinearRing x = geometryFactory.createLinearRing(new Coordinate[] {
                    new Coordinate(0, 0), new Coordinate(100, 0),
                    new Coordinate(100, 100), new Coordinate(0, 0)
                });
        LinearRing somethingExactlyEqual = geometryFactory.createLinearRing(new Coordinate[] {
                    new Coordinate(0, 0), new Coordinate(100, 0),
                    new Coordinate(100, 100), new Coordinate(0, 0)
                });
        LinearRing somethingNotEqualButSameClass = geometryFactory.createLinearRing(new Coordinate[] {
                    new Coordinate(0, 0), new Coordinate(100, 0),
                    new Coordinate(100, 555), new Coordinate(0, 0)
                });
        LinearRing sameClassButEmpty = geometryFactory.createLinearRing((CoordinateSequence)null);
        LinearRing anotherSameClassButEmpty = geometryFactory.createLinearRing((CoordinateSequence)null);
        CollectionFactory collectionFactory = new CollectionFactory() {
                public Geometry createCollection(Geometry[] geometries) {
                    return geometryFactory.createMultiLineString(GeometryFactory.toLineStringArray(
                            Arrays.asList(geometries)));
                }
            };

        doTestEqualsExact(x, somethingExactlyEqual,
            somethingNotEqualButSameClass, sameClassButEmpty,
            anotherSameClassButEmpty, collectionFactory);

        //    LineString somethingEqualButNotExactly = geometryFactory.createLineString(new Coordinate[] {
        //          new Coordinate(0, 0), new Coordinate(100, 0), new Coordinate(100, 100),
        //          new Coordinate(0, 0) });
        //
        //    doTestEqualsExact(x, somethingExactlyEqual, somethingEqualButNotExactly,
        //          somethingNotEqualButSameClass);
    }

    public void testEqualsExactForLineStrings() throws Exception {
        LineString x = geometryFactory.createLineString(new Coordinate[] {
                    new Coordinate(0, 0), new Coordinate(100, 0),
                    new Coordinate(100, 100)
                });
        LineString somethingExactlyEqual = geometryFactory.createLineString(new Coordinate[] {
                    new Coordinate(0, 0), new Coordinate(100, 0),
                    new Coordinate(100, 100)
                });
        LineString somethingNotEqualButSameClass = geometryFactory.createLineString(new Coordinate[] {
                    new Coordinate(0, 0), new Coordinate(100, 0),
                    new Coordinate(100, 555)
                });
        LineString sameClassButEmpty = geometryFactory.createLineString((Coordinate[])null);
        LineString anotherSameClassButEmpty = geometryFactory.createLineString((Coordinate[])null);
        CollectionFactory collectionFactory = new CollectionFactory() {
                public Geometry createCollection(Geometry[] geometries) {
                    return geometryFactory.createMultiLineString(GeometryFactory.toLineStringArray(
                            Arrays.asList(geometries)));
                }
            };

        doTestEqualsExact(x, somethingExactlyEqual,
            somethingNotEqualButSameClass, sameClassButEmpty,
            anotherSameClassButEmpty, collectionFactory);

        CollectionFactory collectionFactory2 = new CollectionFactory() {
                public Geometry createCollection(Geometry[] geometries) {
                    return geometryFactory.createMultiLineString(GeometryFactory.toLineStringArray(
                            Arrays.asList(geometries)));
                }
            };

        doTestEqualsExact(x, somethingExactlyEqual,
            somethingNotEqualButSameClass, sameClassButEmpty,
            anotherSameClassButEmpty, collectionFactory2);
    }

    public void testEqualsExactForPoints() throws Exception {
        Point x = geometryFactory.createPoint(new Coordinate(100, 100));
        Point somethingExactlyEqual = geometryFactory.createPoint(new Coordinate(
                    100, 100));
        Point somethingNotEqualButSameClass = geometryFactory.createPoint(new Coordinate(
                    999, 100));
        Point sameClassButEmpty = geometryFactory.createPoint((Coordinate)null);
        Point anotherSameClassButEmpty = geometryFactory.createPoint((Coordinate)null);
        CollectionFactory collectionFactory = new CollectionFactory() {
                public Geometry createCollection(Geometry[] geometries) {
                    return geometryFactory.createMultiPoint(GeometryFactory.toPointArray(
                            Arrays.asList(geometries)));
                }
            };

        doTestEqualsExact(x, somethingExactlyEqual,
            somethingNotEqualButSameClass, sameClassButEmpty,
            anotherSameClassButEmpty, collectionFactory);
    }

    public void testEqualsExactForPolygons() throws Exception {
        Polygon x = (Polygon) reader.read(
                "POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
        Polygon somethingExactlyEqual = (Polygon) reader.read(
                "POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
        Polygon somethingNotEqualButSameClass = (Polygon) reader.read(
                "POLYGON ((50 50, 50 0, 0 0, 0 50, 50 50))");
        Polygon sameClassButEmpty = (Polygon) reader.read("POLYGON EMPTY");
        Polygon anotherSameClassButEmpty = (Polygon) reader.read(
                "POLYGON EMPTY");
        CollectionFactory collectionFactory = new CollectionFactory() {
                public Geometry createCollection(Geometry[] geometries) {
                    return geometryFactory.createMultiPolygon(GeometryFactory.toPolygonArray(
                            Arrays.asList(geometries)));
                }
            };

        doTestEqualsExact(x, somethingExactlyEqual,
            somethingNotEqualButSameClass, sameClassButEmpty,
            anotherSameClassButEmpty, collectionFactory);
    }

    public void testEqualsExactForGeometryCollections()
        throws Exception {
        Geometry polygon1 = (Polygon) reader.read(
                "POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
        Geometry polygon2 = (Polygon) reader.read(
                "POLYGON ((50 50, 50 0, 0 0, 0 50, 50 50))");
        GeometryCollection x = geometryFactory.createGeometryCollection(new Geometry[] {
                    polygon1, polygon2
                });
        GeometryCollection somethingExactlyEqual = geometryFactory.createGeometryCollection(new Geometry[] {
                    polygon1, polygon2
                });
        GeometryCollection somethingNotEqualButSameClass = geometryFactory.createGeometryCollection(new Geometry[] {
                    polygon2
                });
        GeometryCollection sameClassButEmpty = geometryFactory.createGeometryCollection(null);
        GeometryCollection anotherSameClassButEmpty = geometryFactory.createGeometryCollection(null);
        CollectionFactory collectionFactory = new CollectionFactory() {
                public Geometry createCollection(Geometry[] geometries) {
                    return geometryFactory.createGeometryCollection(geometries);
                }
            };

        doTestEqualsExact(x, somethingExactlyEqual,
            somethingNotEqualButSameClass, sameClassButEmpty,
            anotherSameClassButEmpty, collectionFactory);
    }

    private void doTestEqualsExact(Geometry x, 
        Geometry somethingExactlyEqual,
        Geometry somethingNotEqualButSameClass, 
        Geometry sameClassButEmpty,
        Geometry anotherSameClassButEmpty, 
        CollectionFactory collectionFactory)
        throws Exception {
        Geometry emptyDifferentClass;

        if (x instanceof Point) {
            emptyDifferentClass = geometryFactory.createGeometryCollection(null);
        } else {
            emptyDifferentClass = geometryFactory.createPoint((Coordinate)null);
        }

        Geometry somethingEqualButNotExactly = geometryFactory.createGeometryCollection(new Geometry[] {
                    x
                });
        
        doTestEqualsExact(x, somethingExactlyEqual,
            collectionFactory.createCollection(new Geometry[] { x }),
            somethingNotEqualButSameClass);
        
        doTestEqualsExact(sameClassButEmpty, anotherSameClassButEmpty,
            emptyDifferentClass, x);
        
        /**
         * Test comparison of non-empty versus empty.
         */
        doTestEqualsExact(x, somethingExactlyEqual,
            sameClassButEmpty, sameClassButEmpty);
        
        doTestEqualsExact(collectionFactory.createCollection(
                new Geometry[] { x, x }),
            collectionFactory.createCollection(
                new Geometry[] { x, somethingExactlyEqual }),
            somethingEqualButNotExactly,
            collectionFactory.createCollection(
                new Geometry[] { x, somethingNotEqualButSameClass }));
    }

    private void doTestEqualsExact(Geometry x, 
        Geometry somethingExactlyEqual,
        Geometry somethingEqualButNotExactly,
        Geometry somethingNotEqualButSameClass) throws Exception {
        Geometry differentClass;

        if (x instanceof Point) {
            differentClass = reader.read(
                    "POLYGON ((0 0, 0 50, 50 43949, 50 0, 0 0))");
        } else {
            differentClass = reader.read("POINT ( 2351 1563 )");
        }

        assertTrue(x.equalsExact(x));
        assertTrue(x.equalsExact(somethingExactlyEqual));
        assertTrue(somethingExactlyEqual.equalsExact(x));
        assertTrue(!x.equalsExact(somethingEqualButNotExactly));
        assertTrue(!somethingEqualButNotExactly.equalsExact(x));
        assertTrue(!x.equalsExact(somethingEqualButNotExactly));
        assertTrue(!somethingEqualButNotExactly.equalsExact(x));
        assertTrue(!x.equalsExact(differentClass));
        assertTrue(!differentClass.equalsExact(x));
    }

    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(suite());
    }

    private interface CollectionFactory {
        Geometry createCollection(Geometry[] geometries);
    }
}
