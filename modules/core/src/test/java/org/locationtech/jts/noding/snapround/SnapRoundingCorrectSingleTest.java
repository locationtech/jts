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
import org.locationtech.jts.geom.util.LinearComponentExtracter;
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
public class SnapRoundingCorrectSingleTest  extends GeometryTestCase {

  GeometryFactory geomFact = new GeometryFactory();
  
  public static void main(String args[]) {
    TestRunner.run(SnapRoundingCorrectSingleTest.class);
  }

  public SnapRoundingCorrectSingleTest(String name) { super(name); }

  public void xtestNearbyCorner() {

    String wkt = "MULTILINESTRING ((0.2 1.1, 1.6 1.4, 1.9 2.9), (0.9 0.9, 2.3 1.7))";
    String expected = "MULTILINESTRING ((0 1, 1 1), (1 1, 2 1), (1 1, 2 1), (2 1, 2 2), (2 1, 2 2), (2 2, 2 3))";
    checkRounding(wkt, 1.0, expected);
  }

  public void xtestNearbyShape() {

    String wkt = "MULTILINESTRING ((1.3 0.1, 2.4 3.9), (0 1, 1.53 1.48, 0 4))";
    String expected = "MULTILINESTRING ((1 0, 2 1), (2 1, 2 4), (0 1, 2 1), (2 1, 0 4))";
    checkRounding(wkt, 1.0, expected);
  }

  /**
   * Currently fails, perhaps due to intersection lying right on a grid cell corner?
   * Fixed by ensuring intersections are forced into segments
   */
  public void xtestIntOnGridCorner() {

    String wkt = "MULTILINESTRING ((4.30166242 45.53438188, 4.30166243 45.53438187), (4.3011475 45.5328371, 4.3018341 45.5348969))";
    String expected = null;
    checkRounding(wkt, 100000000, expected);
  }

  /**
   * Currently fails, does not node correctly
   * Fixed by not snapping line segments when testing against hot pixel
   */
  public void xtestVertexCrossesLine() {

    String wkt = "MULTILINESTRING ((2.2164917 48.8864136, 2.2175217 48.8867569), (2.2175217 48.8867569, 2.2182083 48.8874435), (2.2182083 48.8874435, 2.2161484 48.8853836))";
    String expected = null;
    checkRounding(wkt, 1000000, expected);
  }

  /**
   * Currently fails, does not node correctly.
   * 
   * FIXED by NOT rounding lines extracted by Overlay
   */
  public void xtestVertexCrossesLine2() {

    String wkt = "MULTILINESTRING ((2.276916574988164 49.06082147500638, 2.2769165 49.0608215), (2.2769165 49.0608215, 2.2755432 49.0608215), (2.2762299 49.0615082, 2.276916574988164 49.06082147500638))";
    String expected = null;
    checkRounding(wkt, 1000000, expected);
  }

  /**
   * Looks like a very short line is stretched between two grid points, 
   * and for some reason the node at one end is not inserted in a line snapped to it.
   * 
   * FIXED by ensuring that HotPixel intersection tests whether segment
   * endpoints lie inside pixel.
   */
  public void xtestShortLineNodeNotAdded() {

    String wkt = "LINESTRING (2.1279144 48.8445282, 2.126884443750796 48.84555818124935, 2.1268845 48.8455582, 2.1268845 48.8462448)";
    String expected = "MULTILINESTRING ((2.127914 48.844528, 2.126885 48.845558), (2.126885 48.845558, 2.126884 48.845558), (2.126884 48.845558, 2.126885 48.845558), (2.126885 48.845558, 2.126885 48.846245))";
    checkRounding(wkt, 1000000, expected);
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

  /**
   * A vertex lies near interior of horizontal segment.  
   * Both are moved by rounding, and vertex ends up conincident with segment.
   * But node is not created.
   * This is very subtle, since because the segment is horizontal the vertex lies exactly on it
   * and thus still reports as valid geometry (although a noding check reports failure).
   */
  public void xtestVertexNearHorizSegNotNoded() {
    String wkt = "MULTILINESTRING (( 2.5096893 48.9530182, 2.50762932500455 48.95233152500091, 2.5055695 48.9530182 ), ( 2.5090027 48.9523315, 2.5035095 48.9523315 ))";
    String expected = null;
    checkRounding(wkt, 1000000, expected);
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
    List<LineString> lines = LinearComponentExtracter.getLines(geom);
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
