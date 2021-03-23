/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.union;


import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class SparsePolygonUnionTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(SparsePolygonUnionTest.class);
  }
  
  public SparsePolygonUnionTest(String name) {
    super(name);
  }
  
  public void testSimple() {
    check(
        "MULTIPOLYGON (((10 20, 20 20, 20 10, 10 10, 10 20)), ((30 10, 20 10, 20 20, 30 20, 30 10)))",
        "POLYGON ((10 20, 20 20, 30 20, 30 10, 20 10, 10 10, 10 20))");
  }

  public void testSimple3() {
    check(
        "MULTIPOLYGON (((10 20, 20 20, 20 10, 10 10, 10 20)), ((30 10, 20 10, 20 20, 30 20, 30 10)), ((25 30, 30 30, 30 20, 25 20, 25 30)))",
        "POLYGON ((10 10, 10 20, 20 20, 25 20, 25 30, 30 30, 30 20, 30 10, 20 10, 10 10))");
  }

  public void testDisjoint() {
    check(
        "MULTIPOLYGON (((10 20, 20 20, 20 10, 10 10, 10 20)), ((30 20, 40 20, 40 10, 30 10, 30 20)))",
        "MULTIPOLYGON (((10 20, 20 20, 20 10, 10 10, 10 20)), ((30 20, 40 20, 40 10, 30 10, 30 20)))");
  }

  private void check(String wkt, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry result = SparsePolygonUnion.union(geom);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result);
    System.out.println(result);
  }
}
