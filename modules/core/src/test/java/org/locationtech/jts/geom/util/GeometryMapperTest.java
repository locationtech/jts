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
package org.locationtech.jts.geom.util;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.GeometryMapper.MapOp;
import org.locationtech.jts.io.ParseException;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class GeometryMapperTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(GeometryMapperTest.class);
  }
  
	public GeometryMapperTest(String name) {
		super(name);
	}
	
	/**
	 * Mapping: 
	 *   LineString -> LineString, 
	 *   Point -> empty LineString, 
	 *   Polygon -> null
	 */
	static GeometryMapper.MapOp KEEP_LINE = new GeometryMapper.MapOp() {
    @Override
    public Geometry map(Geometry geom) {
      if (geom instanceof Point) {
        return geom.getFactory().createEmpty(1);
      }
      if (geom instanceof LineString)
        return geom;
      return null;
    }
  };
  
  static GeometryMapper.MapOp BOUNDARY = new GeometryMapper.MapOp() {
    @Override
    public Geometry map(Geometry geom) {
      return geom.getBoundary();
    }
  };
  
  public void testFlatMapInputEmpty() throws ParseException {
    checkFlatMap("GEOMETRYCOLLECTION( POINT EMPTY, LINESTRING EMPTY)",
        1, KEEP_LINE, "LINESTRING EMPTY");
  }

  public void testFlatMapInputMulti() throws ParseException {
    checkFlatMap("GEOMETRYCOLLECTION( MULTILINESTRING((0 0, 1 1), (1 1, 2 2)), LINESTRING(2 2, 3 3))",
        1, KEEP_LINE, "MULTILINESTRING ((0 0, 1 1), (1 1, 2 2), (2 2, 3 3))");
  }
  
  public void testFlatMapResultEmpty() throws ParseException {
    checkFlatMap("GEOMETRYCOLLECTION( LINESTRING(0 0, 1 1), LINESTRING(1 1, 2 2))",
        1, KEEP_LINE, "MULTILINESTRING((0 0, 1 1), (1 1, 2 2))");
    
    checkFlatMap("GEOMETRYCOLLECTION( POINT(0 0), POINT(0 0), LINESTRING(0 0, 1 1))",
        1, KEEP_LINE, "LINESTRING(0 0, 1 1)");
    
    checkFlatMap("MULTIPOINT((0 0), (1 1))",
        1, KEEP_LINE, "LINESTRING EMPTY");
  }
  
  public void testFlatMapResultNull() throws ParseException {
    checkFlatMap("GEOMETRYCOLLECTION( POINT(0 0), LINESTRING(0 0, 1 1), POLYGON ((1 1, 1 2, 2 1, 1 1)))",
        1, KEEP_LINE, "LINESTRING(0 0, 1 1)");
  }

  public void testFlatMapBoundary() throws ParseException {
    checkFlatMap("GEOMETRYCOLLECTION( POINT(0 0), LINESTRING(0 0, 1 1), POLYGON ((1 1, 1 2, 2 1, 1 1)))",
        0, BOUNDARY, "GEOMETRYCOLLECTION (POINT (0 0), POINT (1 1), LINEARRING (1 1, 1 2, 2 1, 1 1))");

    checkFlatMap("LINESTRING EMPTY",
        0, BOUNDARY, "POINT EMPTY");
  }


  private void checkFlatMap(String wkt, int dim, MapOp op, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = GeometryMapper.flatMap(geom, dim, op);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }

}
