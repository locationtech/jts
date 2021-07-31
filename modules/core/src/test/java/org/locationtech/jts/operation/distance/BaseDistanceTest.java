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
package org.locationtech.jts.operation.distance;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;

import test.jts.GeometryTestCase;

public abstract class BaseDistanceTest extends GeometryTestCase {

  public BaseDistanceTest(String name) { super(name); }

  public void testDisjointCollinearSegments() throws Exception {
    Geometry g1 = read("LINESTRING (0.0 0.0, 9.9 1.4)");
    Geometry g2 = read("LINESTRING (11.88 1.68, 21.78 3.08)");
    
    double dist = distance(g1, g2);
    assertEquals(1.9996999774966246, dist, 0.0001);
    
    assertTrue( ! isWithinDistance(g1, g2, 1) );
    assertTrue(   isWithinDistance(g1, g2, 3) );
  }

  public void testPolygonsDisjoint() {
    Geometry g1 = read("POLYGON ((40 320, 200 380, 320 80, 40 40, 40 320),  (180 280, 80 280, 100 100, 220 140, 180 280))");
    Geometry g2 = read("POLYGON ((160 240, 120 240, 120 160, 160 140, 160 240))");
    assertEquals(18.97366596, distance(g1, g2), 1E-5);
    
    assertTrue( ! isWithinDistance(g1, g2, 0) );
    assertTrue( ! isWithinDistance(g1, g2, 10) );
    assertTrue( isWithinDistance(g1, g2, 20) );
  }
 
  public void testPolygonsOverlapping() {
    Geometry g1 = read("POLYGON ((40 320, 200 380, 320 80, 40 40, 40 320),  (180 280, 80 280, 100 100, 220 140, 180 280))");
    Geometry g3 = read("POLYGON ((160 240, 120 240, 120 160, 180 100, 160 240))");

    assertEquals(0.0, distance(g1, g3), 1E-9);
    assertTrue( isWithinDistance(g1, g3, 0.0) );
  }
 
  public void testLinesIdentical() {
    LineString l1 = (LineString) read("LINESTRING(10 10, 20 20, 30 40)");
    assertEquals(0.0, distance(l1, l1), 1E-5);
    
    assertTrue( isWithinDistance(l1, l1, 0) );

  }
  
  public void testEmpty() {
    Geometry g1 = read("POINT (0 0)");
    Geometry g2 = read("POLYGON EMPTY");
    assertEquals(0.0, g1.distance(g2), 0.0);
  }

  public void testClosestPoints1() throws Exception {
    checkDistanceNearestPoints("POLYGON ((200 180, 60 140, 60 260, 200 180))", "POINT (140 280)", 57.05597791103589, new Coordinate(111.6923076923077, 230.46153846153845), new Coordinate(140, 280));
  }
  public void testClosestPoints2() throws Exception {
    checkDistanceNearestPoints("POLYGON ((200 180, 60 140, 60 260, 200 180))", "MULTIPOINT ((140 280), (140 320))", 57.05597791103589, new Coordinate(111.6923076923077, 230.46153846153845), new Coordinate(140, 280));
  }
  public void testClosestPoints3() throws Exception {
    checkDistanceNearestPoints("LINESTRING (100 100, 200 100, 200 200, 100 200, 100 100)", "POINT (10 10)", 127.27922061357856, new Coordinate(100, 100), new Coordinate(10, 10));
  }
  public void testClosestPoints4() throws Exception {
    checkDistanceNearestPoints("LINESTRING (100 100, 200 200)", "LINESTRING (100 200, 200 100)", 0.0, new Coordinate(150, 150), new Coordinate(150, 150));
  }
  public void testClosestPoints5() throws Exception {
    checkDistanceNearestPoints("LINESTRING (100 100, 200 200)", "LINESTRING (150 121, 200 0)", 20.506096654409877, new Coordinate(135.5, 135.5), new Coordinate(150, 121));
  }
  public void testClosestPoints6() throws Exception {
    checkDistanceNearestPoints("POLYGON ((76 185, 125 283, 331 276, 324 122, 177 70, 184 155, 69 123, 76 185), (267 237, 148 248, 135 185, 223 189, 251 151, 286 183, 267 237))", "LINESTRING (153 204, 185 224, 209 207, 238 222, 254 186)", 13.788860460124573, new Coordinate(139.4956500724988, 206.78661188980183), new Coordinate(153, 204));
  }
  public void testClosestPoints7() throws Exception {
    checkDistanceNearestPoints("POLYGON ((76 185, 125 283, 331 276, 324 122, 177 70, 184 155, 69 123, 76 185), (267 237, 148 248, 135 185, 223 189, 251 151, 286 183, 267 237))", "LINESTRING (120 215, 185 224, 209 207, 238 222, 254 186)", 0.0, new Coordinate(120, 215), new Coordinate(120, 215));
  }

  private static final double TOLERANCE = 1E-10;
  
  private void checkDistanceNearestPoints(String wkt0, String wkt1, double distance, 
                                   Coordinate p0, Coordinate p1) throws ParseException {
    Geometry g0 = read(wkt0);
    Geometry g1 = read(wkt1);
    
    Coordinate[] nearestPoints = nearestPoints(g0, g1);

    assertEquals(distance, nearestPoints[0].distance(nearestPoints[1]), TOLERANCE);
    assertEquals(p0.x, nearestPoints[0].x, TOLERANCE);
    assertEquals(p0.y, nearestPoints[0].y, TOLERANCE);
    assertEquals(p1.x, nearestPoints[1].x, TOLERANCE);
    assertEquals(p1.y, nearestPoints[1].y, TOLERANCE);    
  }  

  protected abstract double distance(Geometry g1, Geometry g2);
  
  protected abstract boolean isWithinDistance(Geometry g1, Geometry g2, double distance);

  protected abstract Coordinate[] nearestPoints(Geometry g1, Geometry g2);
}
