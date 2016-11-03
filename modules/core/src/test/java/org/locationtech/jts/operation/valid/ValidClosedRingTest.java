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

package org.locationtech.jts.operation.valid;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;


/**
 * Tests validating geometries with
 * non-closed rings.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class ValidClosedRingTest
    extends TestCase
{
  private static WKTReader rdr = new WKTReader();

  public ValidClosedRingTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(ValidClosedRingTest.class);
  }

  public void testBadLinearRing()
  {
    LinearRing ring = (LinearRing) fromWKT("LINEARRING (0 0, 0 10, 10 10, 10 0, 0 0)");
    updateNonClosedRing(ring);
    checkIsValid(ring, false);
  }

  public void testGoodLinearRing()
  {
    LinearRing ring = (LinearRing) fromWKT("LINEARRING (0 0, 0 10, 10 10, 10 0, 0 0)");
    checkIsValid(ring, true);
  }

  public void testBadPolygonShell()
  {
    Polygon poly = (Polygon) fromWKT("POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))");
    updateNonClosedRing((LinearRing) poly.getExteriorRing());
    checkIsValid(poly, false);
  }

  public void testBadPolygonHole()
  {
    Polygon poly = (Polygon) fromWKT("POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0), (1 1, 2 1, 2 2, 1 2, 1 1) ))");
    updateNonClosedRing((LinearRing) poly.getInteriorRingN(0));
    checkIsValid(poly, false);
  }

  public void testGoodPolygon()
  {
    Polygon poly = (Polygon) fromWKT("POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))");
    checkIsValid(poly, true);
  }

  public void testBadGeometryCollection()
  {
    GeometryCollection gc = (GeometryCollection) fromWKT("GEOMETRYCOLLECTION ( POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0), (1 1, 2 1, 2 2, 1 2, 1 1) )), POINT(0 0) )");
    Polygon poly = (Polygon) gc.getGeometryN(0);
    updateNonClosedRing((LinearRing) poly.getInteriorRingN(0));
    checkIsValid(poly, false);
  }


  private void checkIsValid(Geometry geom, boolean expected)
  {
    IsValidOp validator = new IsValidOp(geom);
    boolean isValid = validator.isValid();
    assertTrue(isValid == expected);
  }

  Geometry fromWKT(String wkt)
  {
    Geometry geom = null;
    try {
      geom = rdr.read(wkt);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return geom;
  }

  private void updateNonClosedRing(LinearRing ring)
  {
    Coordinate[] pts = ring.getCoordinates();
    pts[0].x += 0.0001;
  }
}