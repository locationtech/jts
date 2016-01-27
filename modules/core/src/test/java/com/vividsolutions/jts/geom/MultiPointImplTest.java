
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
 * Test for com.vividsolutions.jts.geom.impl.MultiPointImpl.
 *
 * @version 1.7
 */
public class MultiPointImplTest extends TestCase {

  PrecisionModel precisionModel = new PrecisionModel(1000);
  GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public MultiPointImplTest(String name) { super(name); }

  public static Test suite() { return new TestSuite(MultiPointImplTest.class); }

/**
 * @todo Enable when #isSimple implemented
 */
//  public void testIsSimple1() throws Exception {
//    MultiPoint m = (MultiPoint) reader.read("MULTIPOINT(1.111 2.222, 3.333 4.444, 5.555 6.666)");
//    assertTrue(m.isSimple());
//  }

/**
 * @todo Enable when #isSimple implemented
 */
//  public void testIsSimple2() throws Exception {
//    MultiPoint m = (MultiPoint) reader.read("MULTIPOINT(1.111 2.222, 3.333 4.444, 3.333 4.444)");
//    assertTrue(! m.isSimple());
//  }

  public void testGetGeometryN() throws Exception {
    MultiPoint m = (MultiPoint) reader.read("MULTIPOINT(1.111 2.222, 3.333 4.444, 3.333 4.444)");
    Geometry g = m.getGeometryN(1);
    assertTrue(g instanceof Point);
    Point p = (Point) g;
    Coordinate externalCoordinate = new Coordinate();
    Coordinate internal = p.getCoordinate();
    externalCoordinate.x = internal.x;
    externalCoordinate.y = internal.y;
    assertEquals(3.333, externalCoordinate.x, 1E-10);
    assertEquals(4.444, externalCoordinate.y, 1E-10);
  }

  public void testGetEnvelope() throws Exception {
    MultiPoint m = (MultiPoint) reader.read("MULTIPOINT(1.111 2.222, 3.333 4.444, 3.333 4.444)");
    Envelope e = m.getEnvelopeInternal();
    assertEquals(1.111, e.getMinX(), 1E-10);
    assertEquals(3.333, e.getMaxX(), 1E-10);
    assertEquals(2.222, e.getMinY(), 1E-10);
    assertEquals(4.444, e.getMaxY(), 1E-10);
  }

  public void testEquals() throws Exception {
    MultiPoint m1 = (MultiPoint) reader.read("MULTIPOINT(5 6, 7 8)");
    MultiPoint m2 = (MultiPoint) reader.read("MULTIPOINT(5 6, 7 8)");
    assertTrue(m1.equals(m2));
  }

}
