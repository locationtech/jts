
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
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * @version 1.7
 */
public class MinimumDiameterTest extends TestCase {

  private PrecisionModel precisionModel = new PrecisionModel(1);
  private GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(MinimumDiameterTest.class);
  }

  public MinimumDiameterTest(String name) { super(name); }

  public void testMinimumDiameter1() throws Exception {
    doMinimumDiameterTest(true, "POINT (0 240)", new Coordinate(0, 240), new Coordinate(0, 240));
  }
  public void testMinimumDiameter2() throws Exception {
    doMinimumDiameterTest(true, "LINESTRING (0 240, 220 240)", new Coordinate(0, 240), new Coordinate(0, 240));
  }
  public void testMinimumDiameter3() throws Exception {
    doMinimumDiameterTest(true, "POLYGON ((0 240, 220 240, 220 0, 0 0, 0 240))", new Coordinate(220, 240), new Coordinate(0, 240));
  }
  public void testMinimumDiameter4() throws Exception {
    doMinimumDiameterTest(true, "POLYGON ((0 240, 220 240, 220 0, 0 0, 0 240))", new Coordinate(220, 240), new Coordinate(0, 240));
  }
  public void testMinimumDiameter5() throws Exception {
    doMinimumDiameterTest(true, "POLYGON ((0 240, 160 140, 220 0, 0 0, 0 240))", new Coordinate(185.86206896551724, 79.65517241379311), new Coordinate(0, 0));
  }
  public void testMinimumDiameter6() throws Exception {
    doMinimumDiameterTest(false, "LINESTRING ( 39 119, 162 197, 135 70, 95 35, 33 66, 111 82, 97 131, 48 160, -4 182, 57 195, 94 202, 90 174, 75 134, 47 114, 0 100, 59 81, 123 60, 136 43, 163 75, 145 114, 93 136, 92 159, 105 175 )", new Coordinate(64.46262341325811, 196.41184767277855), new Coordinate(95, 35));
  }  

  private void doMinimumDiameterTest(boolean convex, String wkt, Coordinate c0, Coordinate c1) throws ParseException {
    Coordinate[] minimumDiameter = new MinimumDiameter(new WKTReader().read(wkt), convex).getDiameter().getCoordinates();
    double tolerance = 1E-10;
    assertEquals(c0.x, minimumDiameter[0].x, tolerance);
    assertEquals(c0.y, minimumDiameter[0].y, tolerance);
    assertEquals(c1.x, minimumDiameter[1].x, tolerance);
    assertEquals(c1.y, minimumDiameter[1].y, tolerance);
  }
  

}
