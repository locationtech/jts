
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
package test.jts.junit.algorithm;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.vividsolutions.jts.algorithm.MinimumDiameter;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

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
