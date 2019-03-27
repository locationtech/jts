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

package org.locationtech.jts.noding.snapround;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.snapround.MCIndexSnapRounder;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * Test for correctly created Noded Segment Strings
 * under an extreme usage of SnapRounding.
 * This test reveals a bug in SegmentNodeList.createSplitEdge()
 * which can create 1-point Segment Strings
 * if the input is incorrectly noded due to robustness issues.
 * 
 * See https://github.com/locationtech/jts/pull/395
 *
 * @version 1.7
 */
public class SegmentStringNodingTest  extends TestCase {

  WKTReader rdr = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(SegmentStringNodingTest.class);
  }

  public SegmentStringNodingTest(String name) { super(name); }
  
  public void testThinTriangle() throws Exception {
    String wkt = "LINESTRING ( 55121.54481117887 42694.49730855581, 55121.54481117887 42694.4973085558, 55121.458748617406 42694.419143944244, 55121.54481117887 42694.49730855581 )";
    Geometry g = new WKTReader().read(wkt);
    List<NodedSegmentString> strings = new ArrayList<>();
    strings.add(new NodedSegmentString(g.getCoordinates(), null));
    PrecisionModel pm = new PrecisionModel(1.1131949079327356E11);
    new MCIndexSnapRounder(pm).computeNodes(strings);
    strings.get(0).getNodeList().addSplitEdges(strings);
    for (NodedSegmentString s : strings) {
      assertTrue(s.size() >= 2);
  }
}
  
  public void testSegmentLength1Failure() throws Exception {
    PrecisionModel pm = new PrecisionModel(1.11E10);
    String wkt = "LINESTRING ( -1677607.6366504875 -588231.47100446, -1674050.1010869485 -587435.2186255794, -1670493.6527468169 -586636.7948791061, -1424286.3681743187 -525586.1397894835, -1670493.6527468169 -586636.7948791061, -1674050.1010869485 -587435.2186255795, -1677607.6366504875 -588231.47100446)";
    Geometry g = new WKTReader().read(wkt);
    List<NodedSegmentString> strings = new ArrayList<>();
    strings.add(new NodedSegmentString(g.getCoordinates(), null));
    new MCIndexSnapRounder(pm).computeNodes(strings);
    strings.get(0).getNodeList().addSplitEdges(strings);
    for (NodedSegmentString s : strings) {
      assertTrue(s.size() >= 2);
    }
  }
  
}