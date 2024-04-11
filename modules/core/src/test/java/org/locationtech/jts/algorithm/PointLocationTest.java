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
import org.locationtech.jts.geom.LineString;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests {@link PointLocation}.
 * 
 * @version 1.15
 */
public class PointLocationTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(PointLocationTest.class);
  }

  public PointLocationTest(String name) {
    super(name);
  }

  public void testOnLineOnVertex() throws Exception {
    checkOnLine(20, 20, "LINESTRING (0 00, 20 20, 30 30)", true);
  }

  public void testOnLineInSegment() throws Exception {
    checkOnLine(10, 10, "LINESTRING (0 0, 20 20, 0 40)", true);
    checkOnLine(10, 30, "LINESTRING (0 0, 20 20, 0 40)", true);
  }

  public void testNotOnLine() throws Exception {
    checkOnLine(0, 100, "LINESTRING (10 10, 20 10, 30 10)", false);
  }

  public void testOnSegment() {
    checkOnSegment(5, 5, "LINESTRING(0 0, 9 9)", true);
    checkOnSegment(0, 0, "LINESTRING(0 0, 9 9)", true);
    checkOnSegment(9, 9, "LINESTRING(0 0, 9 9)", true);
  }
  
  public void testNotOnSegment() {
    checkOnSegment(5, 6, "LINESTRING(0 0, 9 9)", false);
    checkOnSegment(10, 10, "LINESTRING(0 0, 9 9)", false);
    checkOnSegment(9, 9.00001, "LINESTRING(0 0, 9 9)", false);
  }
  
  private void checkOnSegment(double x, double y, String wktLine, boolean expected) {
    LineString line = (LineString) read(wktLine);
    Coordinate p0 = line.getCoordinateN(0);
    Coordinate p1 = line.getCoordinateN(1);
    assertTrue(expected == PointLocation.isOnSegment(new Coordinate(x,y), p0, p1));
  }

  void checkOnLine(double x, double y, String wktLine, boolean expected) {
    LineString line = (LineString) read(wktLine);
    assertTrue(expected == PointLocation.isOnLine(new Coordinate(x,y), line.getCoordinates()));
    
    assertTrue(expected == PointLocation.isOnLine(new Coordinate(x,y), line.getCoordinateSequence()));
  }

}
