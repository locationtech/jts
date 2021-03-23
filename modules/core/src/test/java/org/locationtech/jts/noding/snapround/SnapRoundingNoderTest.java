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

package org.locationtech.jts.noding.snapround;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.NodingTestUtil;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;


/**
 * Test Snap Rounding
 *
 * @version 1.17
 */
public class SnapRoundingNoderTest  extends GeometryTestCase {
  
  private static Noder getSnapRounder(PrecisionModel pm) {
    return new SnapRoundingNoder(pm);
  }
  
  public static void main(String args[]) {
    TestRunner.run(SnapRoundingNoderTest.class);
  }

  public SnapRoundingNoderTest(String name) { super(name); }

  public void testSimple() {
    String wkt =      "MULTILINESTRING ((1 1, 9 2), (3 3, 3 0))";
    String expected = "MULTILINESTRING ((1 1, 3 1), (3 1, 9 2), (3 3, 3 1), (3 1, 3 0))";
    checkRounding(wkt, 1, expected);
  }
  
  /**
   * A diagonal line is snapped to a vertex half a grid cell away
   */
  public void testSnappedDiagonalLine() {
    String wkt =      "LINESTRING (2 3, 3 3, 3 2, 2 3)";
    String expected = "MULTILINESTRING ((2 3, 3 3), (2 3, 3 3), (3 2, 3 3), (3 2, 3 3))";
    checkRounding(wkt, 1.0, expected);
  }
  
  /**
   * Rings with parallel narrow spikes are snapped to a simple ring and lines
   */
  public void testRingsWithParallelNarrowSpikes() {
    String wkt =      "MULTILINESTRING ((1 3.3, 1.3 1.4, 3.1 1.4, 3.1 0.9, 1.3 0.9, 1 -0.2, 0.8 1.3, 1 3.3), (1 2.9, 2.9 2.9, 2.9 1.3, 1.7 1, 1.3 0.9, 1 0.4, 1 2.9))";
    String expected = "MULTILINESTRING ((1 3, 1 1), (1 1, 2 1), (2 1, 3 1), (3 1, 2 1), (2 1, 1 1), (1 1, 1 0), (1 0, 1 1), (1 1, 1 3), (1 3, 3 3, 3 1), (3 1, 2 1), (2 1, 1 1), (1 1, 1 0), (1 0, 1 1), (1 1, 1 3))";
    checkRounding(wkt, 1.0, expected);
  }
  
  /**
   * This test checks the HotPixel test for overlapping horizontal line
   */
  public void testHorizontalLinesWithMiddleNode() {
    String wkt =      "MULTILINESTRING ((2.5117493 49.0278625,                      2.5144958 49.0278625), (2.511749 49.027863, 2.513123 49.027863, 2.514496 49.027863))";
    String expected = "MULTILINESTRING ((2.511749 49.027863, 2.513123 49.027863), (2.511749 49.027863, 2.513123 49.027863), (2.513123 49.027863, 2.514496 49.027863), (2.513123 49.027863, 2.514496 49.027863))";
    checkRounding(wkt, 1_000_000.0, expected);
  }

  public void testSlantAndHorizontalLineWithMiddleNode() {
    String wkt =      "MULTILINESTRING ((0.1565552 49.5277405, 0.1579285 49.5277405, 0.1593018 49.5277405), (0.1568985 49.5280838, 0.1589584 49.5273972))";
    String expected = "MULTILINESTRING ((0.156555 49.527741, 0.157928 49.527741), (0.156899 49.528084, 0.157928 49.527741), (0.157928 49.527741, 0.157929 49.527741, 0.159302 49.527741), (0.157928 49.527741, 0.158958 49.527397))";
    checkRounding(wkt, 1_000_000.0, expected);
  }
  
  public void testNearbyCorner() {

    String wkt = "MULTILINESTRING ((0.2 1.1, 1.6 1.4, 1.9 2.9), (0.9 0.9, 2.3 1.7))";
    String expected = "MULTILINESTRING ((0 1, 1 1), (1 1, 2 1), (1 1, 2 1), (2 1, 2 2), (2 1, 2 2), (2 2, 2 3))";
    checkRounding(wkt, 1.0, expected);
  }

  public void testNearbyShape() {

    String wkt = "MULTILINESTRING ((1.3 0.1, 2.4 3.9), (0 1, 1.53 1.48, 0 4))";
    String expected = "MULTILINESTRING ((1 0, 2 1), (2 1, 2 4), (0 1, 2 1), (2 1, 0 4))";
    checkRounding(wkt, 1.0, expected);
  }

  /**
   * Currently fails, perhaps due to intersection lying right on a grid cell corner?
   * Fixed by ensuring intersections are forced into segments
   */
  public void testIntOnGridCorner() {

    String wkt = "MULTILINESTRING ((4.30166242 45.53438188, 4.30166243 45.53438187), (4.3011475 45.5328371, 4.3018341 45.5348969))";
    String expected = null;
    checkRounding(wkt, 100000000, expected);
  }

  /**
   * Currently fails, does not node correctly
   */
  public void testVertexCrossesLine() {

    String wkt = "MULTILINESTRING ((2.2164917 48.8864136, 2.2175217 48.8867569), (2.2175217 48.8867569, 2.2182083 48.8874435), (2.2182083 48.8874435, 2.2161484 48.8853836))";
    String expected = null;
    checkRounding(wkt, 1000000, expected);
  }

  /**
   * Currently fails, does not node correctly.
   * Fixed by NOT rounding lines extracted by Overlay
   */
  public void testVertexCrossesLine2() {

    String wkt = "MULTILINESTRING ((2.276916574988164 49.06082147500638, 2.2769165 49.0608215), (2.2769165 49.0608215, 2.2755432 49.0608215), (2.2762299 49.0615082, 2.276916574988164 49.06082147500638))";
    String expected = null;
    checkRounding(wkt, 1000000, expected);
  }

  /**
   * Looks like a very short line is stretched between two grid points, 
   * and for some reason the node at one end is not inserted in a line snapped to it
   */
  public void testShortLineNodeNotAdded() {

    String wkt = "LINESTRING (2.1279144 48.8445282, 2.126884443750796 48.84555818124935, 2.1268845 48.8455582, 2.1268845 48.8462448)";
    String expected = "MULTILINESTRING ((2.127914 48.844528, 2.126885 48.845558), (2.126885 48.845558, 2.126884 48.845558), (2.126884 48.845558, 2.126885 48.845558), (2.126885 48.845558, 2.126885 48.846245))";
    checkRounding(wkt, 1000000, expected);
  }

  /**
   * This test will fail if the diagonals of hot pixels are not checked.
   * Note that the nearby vertex is far enough from the long segment
   * to avoid being snapped as an intersection.
   */
  public void testDiagonalNotNodedRightUp() {

    String wkt = "MULTILINESTRING ((0 0, 10 10), ( 0 2, 4.55 5.4, 9 10 ))";
    String expected = null;
    checkRounding(wkt, 1, expected);
  }

  /**
   * Same diagonal test but flipped to test other diagonal
   */
  public void testDiagonalNotNodedLeftUp() {

    String wkt = "MULTILINESTRING ((10 0, 0 10), ( 10 2, 5.45 5.45, 1 10 ))";
    String expected = null;
    checkRounding(wkt, 1, expected);
  }

  /**
   * Original full-precision diagonal line case
   */
  public void testDiagonalNotNodedOriginal() {

    String wkt = "MULTILINESTRING (( 2.45167 48.96709, 2.45768 48.9731 ), (2.4526978 48.968811, 2.4537277 48.9691544, 2.4578476 48.9732742))";
    String expected = null;
    checkRounding(wkt, 100000, expected);
  }
  
  public void testLoopBackCreatesNode() {
    String wkt = "LINESTRING (2 2, 5 2, 8 4, 5 6, 4.8 2.3, 2 5)";
    String expected = "MULTILINESTRING ((2 2, 5 2), (5 2, 8 4, 5 6, 5 2), (5 2, 2 5))";
    checkRounding(wkt, 1, expected);
  }
  
  /**
   * An A vertex lies very close to a B segment.
   * The vertex is snapped across the segment, but the segment is not noded.
   * FIXED by adding intersection detection for near vertices to segments
   */
  public void testNearVertexNotNoded() {
    String wkt = "MULTILINESTRING ((2.4829102 48.8726807, 2.4830818249999997 48.873195575, 2.4839401 48.8723373), ( 2.4829102 48.8726807, 2.4832535 48.8737106 ))";
    String expected = null;
    checkRounding(wkt, 100000000, expected);
  }
  
  void checkRounding(String wkt, double scale, String expectedWKT)
  {
    Geometry geom = read(wkt);
    PrecisionModel pm = new PrecisionModel(scale);
    Noder noder = getSnapRounder(pm);
    Geometry result = NodingTestUtil.nodeValidated(geom, null, noder);  
    
    // only check if expected was provided
    if (expectedWKT == null) return;
    Geometry expected = read(expectedWKT);
    checkEqual(expected, result);
  }

}
