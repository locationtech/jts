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
public class SnapRoundingCorrectnessTest  extends GeometryTestCase {

  GeometryFactory geomFact = new GeometryFactory();
  
  public static void main(String args[]) {
    TestRunner.run(SnapRoundingCorrectnessTest.class);
  }

  public SnapRoundingCorrectnessTest(String name) { super(name); }

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
   */
  public void testIntOnGridCorner() {

    String wkt = "MULTILINESTRING ((4.30166242 45.53438188, 4.30166243 45.53438187), (4.3011475 45.5328371, 4.3018341 45.5348969))";
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
    
    // validate noding
    NodingValidator nv = new NodingValidator(nodedList);
    nv.checkValid();

    MultiLineString result = toLines(nodedList);
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
