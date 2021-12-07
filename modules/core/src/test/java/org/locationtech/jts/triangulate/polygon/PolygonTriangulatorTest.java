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
package org.locationtech.jts.triangulate.polygon;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class PolygonTriangulatorTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(PolygonTriangulatorTest.class);
  }
  
  public PolygonTriangulatorTest(String name) {
    super(name);
  }

  public void testQuad() {
    checkTri("POLYGON ((10 10, 20 40, 90 90, 90 10, 10 10))"
        ,"GEOMETRYCOLLECTION (POLYGON ((10 10, 20 40, 90 90, 10 10)), POLYGON ((90 90, 90 10, 10 10, 90 90)))");
  }
  
  public void testPent() {
    checkTri("POLYGON ((10 10, 20 40, 90 90, 100 50, 90 10, 10 10))"
        ,"GEOMETRYCOLLECTION (POLYGON ((10 10, 20 40, 90 90, 10 10)), POLYGON ((90 90, 100 50, 90 10, 90 90)), POLYGON ((90 10, 10 10, 90 90, 90 10)))");
  }
  
  public void testHoleCW() {
    checkTri("POLYGON ((10 90, 90 90, 90 20, 10 10, 10 90), (30 70, 80 70, 50 30, 30 70))"
        ,"GEOMETRYCOLLECTION (POLYGON ((10 10, 10 90, 30 70, 10 10)), POLYGON ((80 70, 30 70, 10 90, 80 70)), POLYGON ((10 10, 30 70, 50 30, 10 10)), POLYGON ((80 70, 10 90, 90 90, 80 70)), POLYGON ((90 20, 10 10, 50 30, 90 20)), POLYGON ((80 70, 90 90, 90 20, 80 70)), POLYGON ((90 20, 50 30, 80 70, 90 20)))");
  }
  
  public void testTouchingHoles() {
    checkTri("POLYGON ((10 90, 90 90, 90 10, 10 10, 10 90), (20 80, 50 70, 30 30, 20 80), (70 20, 50 70, 80 80, 70 20))"
        ,"GEOMETRYCOLLECTION (POLYGON ((10 10, 10 90, 20 80, 10 10)), POLYGON ((50 70, 20 80, 10 90, 50 70)), POLYGON ((10 10, 20 80, 30 30, 10 10)), POLYGON ((30 30, 50 70, 70 20, 30 30)), POLYGON ((80 80, 50 70, 10 90, 80 80)), POLYGON ((90 10, 10 10, 30 30, 90 10)), POLYGON ((80 80, 10 90, 90 90, 80 80)), POLYGON ((90 10, 30 30, 70 20, 90 10)), POLYGON ((70 20, 80 80, 90 90, 70 20)), POLYGON ((90 90, 90 10, 70 20, 90 90)))");
  }
  
  public void testRepeatedPoints() {
    checkTri("POLYGON ((71 195, 178 335, 178 335, 239 185, 380 210, 290 60, 110 70, 71 195))"
        ,"GEOMETRYCOLLECTION (POLYGON ((71 195, 178 335, 239 185, 71 195)), POLYGON ((71 195, 239 185, 290 60, 71 195)), POLYGON ((71 195, 290 60, 110 70, 71 195)), POLYGON ((239 185, 380 210, 290 60, 239 185)))");
  }
  
  public void testEmpty() {
    checkTri("POLYGON EMPTY"
        ,"GEOMETRYCOLLECTION EMPTY");
  }
  
  public void testMultiPolygon() {
    checkTri("MULTIPOLYGON (((10 10, 20 50, 50 50, 40 20, 10 10)), ((20 60, 60 60, 90 20, 90 90, 20 60)), ((10 90, 10 70, 40 70, 50 90, 10 90)))"
        ,"GEOMETRYCOLLECTION (POLYGON ((10 10, 20 50, 50 50, 10 10)), POLYGON ((50 50, 40 20, 10 10, 50 50)), POLYGON ((90 90, 90 20, 60 60, 90 90)), POLYGON ((60 60, 20 60, 90 90, 60 60)), POLYGON ((10 70, 10 90, 50 90, 10 70)), POLYGON ((50 90, 40 70, 10 70, 50 90)))");
  }
  
  public void testCeeShape() {
    checkTri(
  "POLYGON ((110 170, 138 272, 145 286, 152 296, 160 307, 303 307, 314 301, 332 287, 343 278, 352 270, 385 99, 374 89, 359 79, 178 89, 167 91, 153 99, 146 107, 173 157, 182 163, 191 170, 199 176, 208 184, 218 194, 226 203, 198 252, 188 247, 182 239, 175 231, 167 223, 161 213, 156 203, 155 198, 110 170))"
        );
  }  
  
  /**
   * Ear clipping creates a collapsed corner (A-B-A), which was not detected by flat corner removal
   */
  public void testCollapsedCorner() {
    checkTri(
  "POLYGON ((186 90, 71 17, 74 10, 65 0, 0 121, 186 90), (73 34, 67 41, 71 17, 73 34))"
        );
  }
  
  private void checkTri(String wkt, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = PolygonTriangulator.triangulate(geom);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
  
  /**
   * Check union of result equals original geom
   * @param wkt
   */
  private void checkTri(String wkt) {
    Geometry geom = read(wkt);
    Geometry actual = PolygonTriangulator.triangulate(geom);
    Geometry actualUnion = actual.union();
    checkEqual(geom, actualUnion);
  }
}
