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

package org.locationtech.jts.geom;

import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;


/**
 * Tests for {@link GeometryFactory}.
 *
 * @version 1.13
 */
public class GeometryFactoryTest extends GeometryTestCase {

  PrecisionModel precisionModel = new PrecisionModel();
  GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(GeometryFactoryTest.class);
  }

  public GeometryFactoryTest(String name) { super(name); }

  public void testCreateGeometry()
  {
    checkCreateGeometryExact("POINT ( 10 20 )");
    checkCreateGeometryExact("MULTIPOINT ( (10 20), (30 40) )");
    checkCreateGeometryExact("LINESTRING(0 0, 10 10)");
    checkCreateGeometryExact("MULTILINESTRING ((50 100, 100 200), (100 100, 150 200))");
    checkCreateGeometryExact("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    checkCreateGeometryExact("MULTIPOLYGON (((100 200, 200 200, 200 100, 100 100, 100 200)), ((300 200, 400 200, 400 100, 300 100, 300 200)))");
    checkCreateGeometryExact("GEOMETRYCOLLECTION (POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200)), LINESTRING (250 100, 350 200), POINT (350 150))");
  }
  
  public void testCreateGeometryEmpty() {
    checkCreateGeometryExact("POINT EMPTY");
    checkCreateGeometryExact("LINESTRING EMPTY");
    checkCreateGeometryExact("POLYGON EMPTY");
    checkCreateGeometryExact("MULTIPOINT EMPTY");
    checkCreateGeometryExact("MULTILINESTRING EMPTY");
    checkCreateGeometryExact("MULTIPOLYGON EMPTY");
    checkCreateGeometryExact("GEOMETRYCOLLECTION EMPTY");
  }
  
  public void testCreateEmpty() {
    checkEmpty( geometryFactory.createEmpty(0), Point.class);
    checkEmpty( geometryFactory.createEmpty(1), LineString.class);
    checkEmpty( geometryFactory.createEmpty(2), Polygon.class);
    
    checkEmpty( geometryFactory.createPoint(), Point.class);
    checkEmpty( geometryFactory.createLineString(), LineString.class);
    checkEmpty( geometryFactory.createPolygon(), Polygon.class);
    
    checkEmpty( geometryFactory.createMultiPoint(), MultiPoint.class);
    checkEmpty( geometryFactory.createMultiLineString(), MultiLineString.class);
    checkEmpty( geometryFactory.createMultiPolygon(), MultiPolygon.class);
    checkEmpty( geometryFactory.createGeometryCollection(), GeometryCollection.class);
  }
  
  private void checkEmpty(Geometry geom, Class clz) {
    assertTrue(geom.isEmpty());
    assertTrue( geom.getClass() == clz );
  }

  public void testDeepCopy() 
  {
    Point g = (Point) read("POINT ( 10 10) ");
    Geometry g2 = geometryFactory.createGeometry(g);
    g.getCoordinateSequence().setOrdinate(0, 0, 99);
    assertTrue(! g.equalsExact(g2));
  }
  
  public void testMultiPointCS()
  {
    GeometryFactory gf = new GeometryFactory(new PackedCoordinateSequenceFactory());
    CoordinateSequence mpSeq = gf.getCoordinateSequenceFactory().create(1, 4);
    mpSeq.setOrdinate(0, 0, 50);
    mpSeq.setOrdinate(0, 1, -2);
    mpSeq.setOrdinate(0, 2, 10);
    mpSeq.setOrdinate(0, 3, 20);
    
    MultiPoint mp = gf.createMultiPoint(mpSeq);
    CoordinateSequence pSeq = ((Point)mp.getGeometryN(0)).getCoordinateSequence();
    assertEquals(4, pSeq.getDimension());
    for (int i = 0; i < 4; i++)
      assertEquals(mpSeq.getOrdinate(0, i), pSeq.getOrdinate(0, i));
  }
  
  /**
     * CoordinateArraySequences default their dimension to 3 unless explicitly told otherwise.
     * This test ensures that GeometryFactory.createGeometry() recreates the input dimension properly.
   * 
   * @throws ParseException
   */
  public void testCopyGeometryWithNonDefaultDimension() 
  {
    GeometryFactory gf = new GeometryFactory(CoordinateArraySequenceFactory.instance());
    CoordinateSequence mpSeq = gf.getCoordinateSequenceFactory().create(1, 2);
    mpSeq.setOrdinate(0, 0, 50);
    mpSeq.setOrdinate(0, 1, -2);
    
    Point g = gf.createPoint(mpSeq);
    CoordinateSequence pSeq = ((Point) g.getGeometryN(0)).getCoordinateSequence();
    assertEquals(2, pSeq.getDimension());
    
    Point g2 = (Point) geometryFactory.createGeometry(g);
    assertEquals(2, g2.getCoordinateSequence().getDimension());

  }
  
  private void checkCreateGeometryExact(String wkt) 
  {
    Geometry g = read(wkt);
    Geometry g2 = geometryFactory.createGeometry(g);
    assertTrue(g.equalsExact(g2));
    // check a copy has been made
    assertTrue(g != g2);
  }
  
}
