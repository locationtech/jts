
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

package org.locationtech.jts.geom;

import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryCollectionIterator;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;

import test.jts.GeometryTestCase;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Test for {@link GeometryCollectionIterator}.
 *
 * @version 1.7
 */
public class GeometryCollectionIteratorTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(GeometryCollectionIteratorTest.class);
  }

  public GeometryCollectionIteratorTest(String name) { super(name); }

  public void testGeometryCollection() throws Exception {
    GeometryCollection g = (GeometryCollection) read(
          "GEOMETRYCOLLECTION (GEOMETRYCOLLECTION (POINT (10 10)))");
    GeometryCollectionIterator i = new GeometryCollectionIterator(g);
    assertTrue(i.hasNext());
    assertTrue(i.next() instanceof GeometryCollection);
    assertTrue(i.hasNext());
    assertTrue(i.next() instanceof GeometryCollection);
    assertTrue(i.hasNext());
    assertTrue(i.next() instanceof Point);
    assertTrue(! i.hasNext());
 }

  public void testAtomic() throws Exception {
    Polygon g = (Polygon) read("POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))");
    GeometryCollectionIterator i = new GeometryCollectionIterator(g);
    assertTrue(i.hasNext());
    assertTrue(i.next() instanceof Polygon);
    assertTrue(! i.hasNext());
  }

}
