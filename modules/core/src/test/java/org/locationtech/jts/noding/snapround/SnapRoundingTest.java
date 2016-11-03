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
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * Test Snap Rounding
 *
 * @version 1.7
 */
public class SnapRoundingTest  extends TestCase {

  WKTReader rdr = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(SnapRoundingTest.class);
  }

  public SnapRoundingTest(String name) { super(name); }

  public void testPolyWithCloseNode() {
    String[] polyWithCloseNode = {
      "POLYGON ((20 0, 20 160, 140 1, 160 160, 160 1, 20 0))"
    };
    runRounding(polyWithCloseNode);
  }

  public void testLineStringLongShort() {
    String[] geoms = {
        "LINESTRING (0 0, 2 0)",
        "LINESTRING (0 0, 10 -1)"
    };
    runRounding(geoms);
  }

  public void testBadLines1() {
    String[] badLines1 = {
      "LINESTRING ( 171 157, 175 154, 170 154, 170 155, 170 156, 170 157, 171 158, 171 159, 172 160, 176 156, 171 156, 171 159, 176 159, 172 155, 170 157, 174 161, 174 156, 173 156, 172 156 )"
    };
    runRounding(badLines1);
  }
  public void testBadLines2() {
    String[] badLines2 = {
      "LINESTRING ( 175 222, 176 222, 176 219, 174 221, 175 222, 177 220, 174 220, 174 222, 177 222, 175 220, 174 221 )"
    };
    runRounding(badLines2);
  }
  public void testCollapse1() {
    String[] collapse1 = {
      "LINESTRING ( 362 177, 375 164, 374 164, 372 161, 373 163, 372 165, 373 164, 442 58 )"
    };
    runRounding(collapse1);
  }

  public void testCollapse2() {
    String[] collapse2 = {
      "LINESTRING ( 393 175, 391 173, 390 175, 391 174, 391 173 )"
    };
    runRounding(collapse2);
  }
  
  public void testBadNoding1() {
    String[] badNoding1 = {
      "LINESTRING ( 76 47, 81 52, 81 53, 85 57, 88 62, 89 64, 57 80, 82 55, 101 74, 76 99, 92 67, 94 68, 99 71, 103 75, 139 111 )"
    };
    runRounding(badNoding1);
  }

  public void testBadNoding1Extract() {
    String[] badNoding1Extract = {
      "LINESTRING ( 82 55, 101 74 )",
      "LINESTRING ( 94 68, 99 71 )",
      "LINESTRING ( 85 57, 88 62 )"
    };
    runRounding(badNoding1Extract);
  }
  public void testBadNoding1ExtractShift() {
    String[] badNoding1ExtractShift = {
      "LINESTRING ( 0 0, 19 19 )",
      "LINESTRING ( 12 13, 17 16 )",
      "LINESTRING ( 3 2, 6 7 )"
    };
    runRounding(badNoding1ExtractShift);
  }

  static final double SNAP_TOLERANCE = 1.0;
  
  void runRounding(String[] wkt)
  {
    List geoms = fromWKT(wkt);
    PrecisionModel pm = new PrecisionModel(SNAP_TOLERANCE);
    GeometryNoder noder = new GeometryNoder(pm);
    noder.setValidate(true);
    List nodedLines = noder.node(geoms);
/*
    for (Iterator it = nodedLines.iterator(); it.hasNext(); ) {
      System.out.println(it.next());
    }
    */
    assertTrue(isSnapped(nodedLines, SNAP_TOLERANCE));
  }

  List fromWKT(String[] wkts)
  {
    List geomList = new ArrayList();
    for (int i = 0; i < wkts.length; i++) {
      try {
        geomList.add(rdr.read(wkts[i]));
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    return geomList;
  }

  boolean isSnapped(List lines, double tol)
  {
    for (int i = 0; i < lines.size(); i++) {
      LineString line = (LineString) lines.get(i);
      for (int j = 0; j < line.getNumPoints(); j++) {
        Coordinate v = line.getCoordinateN(j);
          if (! isSnapped(v, lines)) return false;
        
      }
    }
    return true;
  }

  private boolean isSnapped(Coordinate v, List lines)
  {
    for (int i = 0; i < lines.size(); i++) {
      LineString line = (LineString) lines.get(i);
      for (int j = 0; j < line.getNumPoints() - 1; j++) {
        Coordinate p0 = line.getCoordinateN(j);
        Coordinate p1 = line.getCoordinateN(j+1);
        if (! isSnapped(v, p0, p1)) return false;
      }
    }
    return true;
  }

  private boolean isSnapped(Coordinate v, Coordinate p0, Coordinate p1)
  {
    if (v.equals2D(p0)) return true;
    if (v.equals2D(p1)) return true;
    LineSegment seg = new LineSegment(p0, p1);
    double dist = seg.distance(v);
    if (dist < SNAP_TOLERANCE / 2.05) return false;
    return true;
  }
  
  
}