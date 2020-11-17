/*
 * Copyright (c) 2020 Martin Davis.
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
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.LineSegment;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests for Z computation for intersections.
 * 
 * @author mdavis
 *
 */
public class RobustLineIntersectorZTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(RobustLineIntersectorZTest.class);
  }

  public RobustLineIntersectorZTest(String name) {
    super(name);
  }
  
  public void testInterior() {
    checkIntersection( line(1, 1, 1, 3, 3, 3), line(1, 3, 10, 3, 1, 30), 
        pt(2, 2, 11));
  }

  public void testInterior2D() {
    checkIntersection( line(1, 1, 3, 3), line(1, 3, 3, 1), 
        pt(2, 2, Double.NaN));
  }

  public void testInterior3D2D() {
    checkIntersection( line(1, 1, 1, 3, 3, 3), line(1, 3, 3, 1), 
        pt(2, 2, 2));
  }

  public void testInterior2D3D() {
    checkIntersection( line(1, 1, 3, 3), line(1, 3, 10, 3, 1, 30), 
        pt(2, 2, 20));
  }
  
  public void testInterior2D3DPart() {
    // result is average of line1 interpolated and line2 p0 Z
    checkIntersection( line(1, 1, 1, 3, 3, 3), line(1, 3, 10, 3, 1, Double.NaN), 
        pt(2, 2, 6));
  }

  public void testEndpoint() {
    checkIntersection( line(1, 1, 1, 3, 3, 3), line(3, 3, 3, 3, 1, 30), 
        pt(3, 3, 3));
  }

  public void testEndpoint2D() {
    checkIntersection( line(1, 1, 3, 3), line(3, 3, 3, 1), 
        pt(3, 3, Double.NaN));
  }
  
  public void testEndpoint2D3D() {
    // result Z is from 3D point
    checkIntersection( line(1, 1, 1, 3, 3, 3), line(3, 3, 3, 1), 
        pt(3, 3, 3));
  }

  public void testInteriorEndpoint() {
    // result Z is from 3D point
    checkIntersection( line(1, 1, 1, 3, 3, 3), line(2, 2, 10, 3, 1, 30), 
        pt(2, 2, 10));
  }

  public void testInteriorEndpoint3D2D() {
    // result Z is interpolated
    checkIntersection( line(1, 1, 1, 3, 3, 3), line(2, 2, 3, 1), 
        pt(2, 2, 2));
  }

  public void testInteriorEndpoint2D3D() {
    // result Z is from 3D point
    checkIntersection( line(1, 1, 3, 3), line(2, 2, 10, 3, 1, 20), 
        pt(2, 2, 10));
  }

  public void testCollinearEqual() {
    checkIntersection( line(1, 1, 1, 3, 3, 3), line(1, 1, 1, 3, 3, 3), 
        pt(1, 1, 1), pt( 3, 3, 3));
  }

  public void testCollinearEqual3D2D() {
    checkIntersection( line(1, 1, 1, 3, 3, 3), line(1, 1, 3, 3), 
        pt(1, 1, 1), pt( 3, 3, 3));
  }

  public void testCollinearEndpoint() {
    checkIntersection( line(1, 1, 1, 3, 3, 3), line(3, 3, 3, 5, 5, 5), 
        pt(3, 3, 3));
  }

  public void testCollinearEndpoint3D2D() {
    // result Z is from 3D point
    checkIntersection( line(1, 1, 1, 3, 3, 3), line(3, 3, 5, 5), 
        pt(3, 3, 3));
  }

  public void testCollinearContained() {
    checkIntersection( line(1, 1, 1, 5, 5, 5), line(3, 3, 3, 4, 4, 4), 
        pt(3, 3, 3), pt(4, 4, 4));
  }

  public void testCollinearContained3D2D() {
    // result Z is interpolated
    checkIntersection( line(1, 1, 1, 5, 5, 5), line(3, 3, 4, 4), 
        pt(3, 3, 3), pt(4, 4, 4));
  }

  //----------------------------------
  
  public void testInteriorXY() {
    checkIntersection( 
        new LineSegment( new CoordinateXY(1, 1), new CoordinateXY(3, 3) ), 
        new LineSegment( new CoordinateXY(1, 3), new CoordinateXY(3, 1) ), 
        pt(2, 2));
  }

  public void testCollinearContainedXY() {
    checkIntersection( 
        new LineSegment( new CoordinateXY(1, 1), new CoordinateXY(5, 5) ), 
        new LineSegment( new CoordinateXY(3, 3), new CoordinateXY(4, 4) ), 
        pt(3, 3), pt(4, 4));
  }
  
  //======================================================

  private void checkIntersection(LineSegment line1, LineSegment line2, 
      Coordinate p1, Coordinate p2) {
    checkIntersectionDir(line1, line2, p1, p2);
    checkIntersectionDir(line2, line1, p1, p2);
    LineSegment line1Rev = new LineSegment(line1.p1, line1.p0);
    LineSegment line2Rev = new LineSegment(line2.p1, line2.p0);
    checkIntersectionDir(line1Rev, line2Rev, p1, p2);
    checkIntersectionDir(line2Rev, line1Rev, p1, p2);
  }

  private void checkIntersectionDir(LineSegment line1, LineSegment line2, Coordinate p1, Coordinate p2) {
    LineIntersector li = new RobustLineIntersector();
    li.computeIntersection(
        line1.p0, line1.p1,
        line2.p0, line2.p1);
    
    assertEquals(2, li.getIntersectionNum());
    
    Coordinate actual1 = li.getIntersection(0);
    Coordinate actual2 = li.getIntersection(1);
    // normalize actual results
    if (actual1.compareTo(actual2) > 0) {
      actual1 = li.getIntersection(1);
      actual2 = li.getIntersection(0);
    }
    
    checkEqualXYZ( p1, actual1 );
    checkEqualXYZ( p2, actual2 );
  }

  private void checkIntersection(LineSegment line1, LineSegment line2, Coordinate pt) {
    checkIntersectionDir(line1, line2, pt);
    checkIntersectionDir(line2, line1, pt);
    LineSegment line1Rev = new LineSegment(line1.p1, line1.p0);
    LineSegment line2Rev = new LineSegment(line2.p1, line2.p0);
    checkIntersectionDir(line1Rev, line2Rev, pt);
    checkIntersectionDir(line2Rev, line1Rev, pt);
  }
  
  private void checkIntersectionDir(LineSegment line1, LineSegment line2, Coordinate pt) {
    LineIntersector li = new RobustLineIntersector();
    li.computeIntersection(
        line1.p0, line1.p1,
        line2.p0, line2.p1);
    assertEquals(1, li.getIntersectionNum());
    Coordinate actual = li.getIntersection(0);
    checkEqualXYZ( pt, actual );
  }

  private static Coordinate pt(double x, double y, double z) {
    return new Coordinate(x, y, z);
  }

  private static Coordinate pt(double x, double y) {
    return pt(x, y, Double.NaN);
  }

  private static LineSegment line(double x1, double y1, double z1,
      double x2, double y2, double z2) {
    return new LineSegment(new Coordinate(x1, y1, z1),
        new Coordinate(x2, y2, z2));
  }
  private static LineSegment line(double x1, double y1,
      double x2, double y2) {
    return new LineSegment(new Coordinate(x1, y1),
        new Coordinate(x2, y2));
  }
}
