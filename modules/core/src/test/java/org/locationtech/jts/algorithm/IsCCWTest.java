/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests CGAlgorithms.isCCW
 * @version 1.7
 */
public class IsCCWTest extends TestCase {

  private WKTReader reader = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(IsCCWTest.class);
  }

  public IsCCWTest(String name) { super(name); }

  public void testCCW() throws Exception
  {
    Coordinate[] pts = getCoordinates("POLYGON ((60 180, 140 240, 140 240, 140 240, 200 180, 120 120, 60 180))");
    assertEquals(false, Orientation.isCCW(pts));
    CoordinateSequence seq = getCoordinateSequence("POLYGON ((60 180, 140 240, 140 240, 140 240, 200 180, 120 120, 60 180))");
    assertEquals(false, Orientation.isCCW(seq));

    Coordinate[] pts2 = getCoordinates("POLYGON ((60 180, 140 120, 100 180, 140 240, 60 180))");
    assertEquals(true, Orientation.isCCW(pts2));
    CoordinateSequence seq2 = getCoordinateSequence("POLYGON ((60 180, 140 120, 100 180, 140 240, 60 180))");
    assertEquals(true, Orientation.isCCW(seq2));

    // same pts list with duplicate top point - check that isCCW still works
    Coordinate[] pts2x = getCoordinates(             "POLYGON ((60 180, 140 120, 100 180, 140 240, 140 240, 60 180))");
    assertEquals(true, Orientation.isCCW(pts2x) );
    CoordinateSequence seq2x = getCoordinateSequence("POLYGON ((60 180, 140 120, 100 180, 140 240, 140 240, 60 180))");
    assertEquals(true, Orientation.isCCW(seq2x) );
  }

  private Coordinate[] getCoordinates(String wkt)
      throws ParseException
  {
    Geometry geom = reader.read(wkt);
    return geom.getCoordinates();
  }
  private CoordinateSequence getCoordinateSequence(String wkt)
          throws ParseException
  {
    Geometry geom = reader.read(wkt);
    if (geom.getGeometryType() != "Polygon")
      throw new IllegalArgumentException("wkt");
    Polygon poly = (Polygon)geom;
    return ((Polygon) geom).getExteriorRing().getCoordinateSequence();
  }
}
