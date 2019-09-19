/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.noding.snapround;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.NodingValidator;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;


/**
 * Test Snap Rounding
 *
 * @version 1.17
 */
public class SnapRoundingCorrectTest  extends GeometryTestCase {

  GeometryFactory geomFact = new GeometryFactory();
  
  public static void main(String args[]) {
    TestRunner.run(SnapRoundingCorrectTest.class);
  }

  public SnapRoundingCorrectTest(String name) { super(name); }

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
    Geometry result = snap(geom, scale);
/*
    for (Iterator it = nodedLines.iterator(); it.hasNext(); ) {
      System.out.println(it.next());
    }
    */    
    
    // only check if expected was provided
    if (expectedWKT == null) return;
    Geometry expected = read(expectedWKT);
    checkEqual(expected, result);
  }

  private Geometry snap(Geometry geom, double scale) {
    PrecisionModel pm = new PrecisionModel(scale);
    List<LineString> lines = LineStringExtracter.getLines(geom);
    List ssList = getSegmentStrings(lines);
    
    Noder ssr = getSnapRounder(pm);
    ssr.computeNodes(ssList);
    Collection<NodedSegmentString> nodedList = ssr.getNodedSubstrings();
    
    MultiLineString result = toLines(nodedList);
    System.out.println(result);
    
    // validate noding
    NodingValidator nv = new NodingValidator(nodedList);
    nv.checkValid();

    return result;
  }

  private Noder getSnapRounder(PrecisionModel pm) {
    return new SimpleSnapRounder(pm);
    //return new MCIndexSnapRounder(pm);
  }

  private MultiLineString toLines(Collection<NodedSegmentString> nodedList) {
    LineString[] lines = new LineString[ nodedList.size() ];
    int i = 0;
    for (NodedSegmentString nss : nodedList) {
      Coordinate[] pts = nss.getCoordinates();
      LineString line = geomFact.createLineString(pts);
      lines[i++] = line;
    }
    return geomFact.createMultiLineString(lines);
  }

  private List<NodedSegmentString> getSegmentStrings(List<LineString> lines) {
    List<NodedSegmentString> nssList = new ArrayList<NodedSegmentString>();
    for (LineString line : lines) {
      NodedSegmentString nss = new NodedSegmentString(line.getCoordinates(), line);
      nssList.add(nss);
    }
    return nssList;
  }



}
