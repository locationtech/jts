
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.vividsolutions.jts.geom;

import test.jts.GeometryTestCase;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.vividsolutions.jts.io.WKTReader;

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
