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
import org.locationtech.jts.geom.LinearRing;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class AreaTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(AreaTest.class);
  }

  public AreaTest(String name) { super(name); }

  public void testArea() {
    checkAreaOfRing("LINEARRING (100 200, 200 200, 200 100, 100 100, 100 200)", 10000.0);
  }
  
  public void testAreaSignedCW() {
    checkAreaOfRingSigned("LINEARRING (100 200, 200 200, 200 100, 100 100, 100 200)", 10000.0);
  }
  
  public void testAreaSignedCCW() {
    checkAreaOfRingSigned("LINEARRING (100 200, 100 100, 200 100, 200 200, 100 200)", -10000.0);
  }
  
  void checkAreaOfRing(String wkt, double expectedArea) {
    LinearRing ring = (LinearRing) read(wkt);
    
    Coordinate[] ringPts = ring.getCoordinates();
    double actual1 = Area.ofRing(ringPts);
    assertEquals(actual1, expectedArea);
    
    CoordinateSequence ringSeq = ring.getCoordinateSequence();
    double actual2 = Area.ofRing(ringSeq);
    assertEquals(actual2, expectedArea);
  }
  
  void checkAreaOfRingSigned(String wkt, double expectedArea) {
    LinearRing ring = (LinearRing) read(wkt);
    
    Coordinate[] ringPts = ring.getCoordinates();
    double actual1 = Area.ofRingSigned(ringPts);
    assertEquals(actual1, expectedArea);
    
    CoordinateSequence ringSeq = ring.getCoordinateSequence();
    double actual2 = Area.ofRingSigned(ringSeq);
    assertEquals(actual2, expectedArea);
  }
}
