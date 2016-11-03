
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

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;


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
