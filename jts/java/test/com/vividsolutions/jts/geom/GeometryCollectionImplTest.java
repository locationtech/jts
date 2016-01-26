
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package com.vividsolutions.jts.geom;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.vividsolutions.jts.io.WKTReader;

/**
 * Test for com.vividsolutions.jts.geom.GeometryCollectionImpl.
 *
 * @version 1.7
 */
public class GeometryCollectionImplTest extends TestCase {

  PrecisionModel precisionModel = new PrecisionModel(1000);
  GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public GeometryCollectionImplTest(String name) { super(name); }

  public static Test suite() { return new TestSuite(GeometryCollectionImplTest.class); }

  public void testGetDimension() throws Exception {
    GeometryCollection g = (GeometryCollection) reader.read("GEOMETRYCOLLECTION (POINT (10 10), POINT (30 30), LINESTRING (15 15, 20 20))");
    assertEquals(1, g.getDimension());
  }

  public void testGetCoordinates() throws Exception {
    GeometryCollection g = (GeometryCollection) reader.read("GEOMETRYCOLLECTION (POINT (10 10), POINT (30 30), LINESTRING (15 15, 20 20))");
    Coordinate[] coordinates = g.getCoordinates();
    assertEquals(4, g.getNumPoints());
    assertEquals(4, coordinates.length);
    assertEquals(new Coordinate(10, 10), coordinates[0]);
    assertEquals(new Coordinate(20, 20), coordinates[3]);
  }

  public void testGeometryCollectionIterator() throws Exception {
    GeometryCollection g = (GeometryCollection) reader.read(
          "GEOMETRYCOLLECTION (GEOMETRYCOLLECTION (POINT (10 10)))");
    GeometryCollectionIterator i = new GeometryCollectionIterator(g);
    assertTrue(i.hasNext());
    assertTrue(i.next() instanceof GeometryCollection);
    assertTrue(i.next() instanceof GeometryCollection);
    assertTrue(i.next() instanceof Point);
  }

  public void testGetLength() throws Exception{
    GeometryCollection g = (GeometryCollection) new WKTReader().read(
          "MULTIPOLYGON("
          + "((0 0, 10 0, 10 10, 0 10, 0 0), (3 3, 3 7, 7 7, 7 3, 3 3)),"
          + "((100 100, 110 100, 110 110, 100 110, 100 100), (103 103, 103 107, 107 107, 107 103, 103 103)))");
    assertEquals(112, g.getLength(), 1E-15);
  }
  

  

}
