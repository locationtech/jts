
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
package test.jts.junit.operation.distance;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.distance.DistanceOp;


/**
 * @version 1.7
 */
public class DistanceTest extends TestCase {

  private PrecisionModel precisionModel = new PrecisionModel(1);
  private GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(DistanceTest.class);
  }

  public DistanceTest(String name) { super(name); }

  public void testDisjointCollinearSegments() throws Exception {
    Geometry g1 = reader.read("LINESTRING (0.0 0.0, 9.9 1.4)");
    Geometry g2 = reader.read("LINESTRING (11.88 1.68, 21.78 3.08)");
    assertEquals(2.23606, g1.distance(g2), 0.0001);
  }

  public void testEverything() throws Exception {
    Geometry g1 = reader.read("POLYGON ((40 320, 200 380, 320 80, 40 40, 40 320),  (180 280, 80 280, 100 100, 220 140, 180 280))");
    Geometry g2 = reader.read("POLYGON ((160 240, 120 240, 120 160, 160 140, 160 240))");
    assertEquals(18.97366596, g1.distance(g2), 1E-5);

    g2 = reader.read("POLYGON ((160 240, 120 240, 120 160, 180 100, 160 240))");
    assertEquals(0.0, g1.distance(g2), 1E-5);

    LineString l1 = (LineString) reader.read("LINESTRING(10 10, 20 20, 30 40)");
    LineString l2 = (LineString) reader.read("LINESTRING(10 10, 20 20, 30 40)");
    assertEquals(0.0, l1.distance(l2), 1E-5);
  }
  
  public void testEmpty() throws Exception {
    Geometry g1 = reader.read("POINT (0 0)");
    Geometry g2 = reader.read("POLYGON EMPTY");
    assertEquals(0.0, g1.distance(g2), 0.0);
  }

  public void testClosestPoints1() throws Exception {
    doNearestPointsTest("POLYGON ((200 180, 60 140, 60 260, 200 180))", "POINT (140 280)", 57.05597791103589, new Coordinate(111.6923076923077, 230.46153846153845), new Coordinate(140, 280));
  }
  public void testClosestPoints2() throws Exception {
    doNearestPointsTest("POLYGON ((200 180, 60 140, 60 260, 200 180))", "MULTIPOINT ((140 280), (140 320))", 57.05597791103589, new Coordinate(111.6923076923077, 230.46153846153845), new Coordinate(140, 280));
  }
  public void testClosestPoints3() throws Exception {
    doNearestPointsTest("LINESTRING (100 100, 200 100, 200 200, 100 200, 100 100)", "POINT (10 10)", 127.27922061357856, new Coordinate(100, 100), new Coordinate(10, 10));
  }
  public void testClosestPoints4() throws Exception {
    doNearestPointsTest("LINESTRING (100 100, 200 200)", "LINESTRING (100 200, 200 100)", 0.0, new Coordinate(150, 150), new Coordinate(150, 150));
  }
  public void testClosestPoints5() throws Exception {
    doNearestPointsTest("LINESTRING (100 100, 200 200)", "LINESTRING (150 121, 200 0)", 20.506096654409877, new Coordinate(135.5, 135.5), new Coordinate(150, 121));
  }
  public void testClosestPoints6() throws Exception {
    doNearestPointsTest("POLYGON ((76 185, 125 283, 331 276, 324 122, 177 70, 184 155, 69 123, 76 185), (267 237, 148 248, 135 185, 223 189, 251 151, 286 183, 267 237))", "LINESTRING (153 204, 185 224, 209 207, 238 222, 254 186)", 13.788860460124573, new Coordinate(139.4956500724988, 206.78661188980183), new Coordinate(153, 204));
  }
  public void testClosestPoints7() throws Exception {
    doNearestPointsTest("POLYGON ((76 185, 125 283, 331 276, 324 122, 177 70, 184 155, 69 123, 76 185), (267 237, 148 248, 135 185, 223 189, 251 151, 286 183, 267 237))", "LINESTRING (120 215, 185 224, 209 207, 238 222, 254 186)", 0.0, new Coordinate(120, 215), new Coordinate(120, 215));
  }

  private void doNearestPointsTest(String wkt0, String wkt1, double distance, 
                                   Coordinate p0, Coordinate p1) throws ParseException {
    DistanceOp op = new DistanceOp(new WKTReader().read(wkt0), new WKTReader().read(wkt1));
    double tolerance = 1E-10;
    assertEquals(distance, op.nearestPoints()[0].distance(op.nearestPoints()[1]), tolerance);
    assertEquals(p0.x, op.nearestPoints()[0].x, tolerance);
    assertEquals(p0.y, op.nearestPoints()[0].y, tolerance);
    assertEquals(p1.x, op.nearestPoints()[1].x, tolerance);
    assertEquals(p1.y, op.nearestPoints()[1].y, tolerance);    
  }  
}
