/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class UnaryUnionNGTest extends GeometryTestCase
{
  public static void main(String args[]) {
    TestRunner.run(UnaryUnionNGTest.class);
  }
  
  public UnaryUnionNGTest(String name) {
    super(name);
  }

  public void testMultiPolygonNarrowGap( ) {
    checkUnaryUnion("MULTIPOLYGON (((1 9, 5.7 9, 5.7 1, 1 1, 1 9)), ((9 9, 9 1, 6 1, 6 9, 9 9)))",
        1, 
        "POLYGON ((1 9, 6 9, 9 9, 9 1, 6 1, 1 1, 1 9))");
  }

  public void testPolygonsRounded( ) {
    checkUnaryUnion("GEOMETRYCOLLECTION (POLYGON ((1 9, 6 9, 6 1, 1 1, 1 9)), POLYGON ((9 1, 2 8, 9 9, 9 1)))",
        1, 
        "POLYGON ((1 9, 6 9, 9 9, 9 1, 6 4, 6 1, 1 1, 1 9))");
  }

  public void testPolygonsOverlapping( ) {
    checkUnaryUnion("GEOMETRYCOLLECTION (POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200)), POLYGON ((250 250, 250 150, 150 150, 150 250, 250 250)))",
        1, 
        "POLYGON ((100 200, 150 200, 150 250, 250 250, 250 150, 200 150, 200 100, 100 100, 100 200))");
  }

  public void testCollection( ) {
    checkUnaryUnion(new String[] {
        "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))",
        "POLYGON ((300 100, 200 100, 200 200, 300 200, 300 100))",
        "POLYGON ((100 300, 200 300, 200 200, 100 200, 100 300))",
        "POLYGON ((300 300, 300 200, 200 200, 200 300, 300 300))"
        },
        1, 
        "POLYGON ((100 100, 100 200, 100 300, 200 300, 300 300, 300 200, 300 100, 200 100, 100 100))");
  }

  public void testCollectionEmpty( ) {
    checkUnaryUnion(new String[0],
        1, 
        "GEOMETRYCOLLECTION EMPTY");
  }

  private void checkUnaryUnion(String wkt, double scaleFactor, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry expected = read(wktExpected);
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    Geometry result = UnaryUnionNG.union(geom, pm);
    checkEqual(expected, result);
  }
  
  private void checkUnaryUnion(String[] wkt, double scaleFactor, String wktExpected) {
    List geoms = readList(wkt);
    Geometry expected = read(wktExpected);
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    Geometry result;
    if (geoms.isEmpty()) {
      result = UnaryUnionNG.union(geoms, getGeometryFactory(), pm);            
    }
    else {
      result = UnaryUnionNG.union(geoms, pm);      
    }
    checkEqual(expected, result);
  }
}
