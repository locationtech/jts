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

package org.locationtech.jtslab.noding.anchorpoint;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;

import java.util.*;


/**
 * Test AnchorPoint noding
 *
 * @version 1.17
 */
public class AnchorPointNodingTest extends TestCase {

  private static final double SNAP_TOLERANCE = 1.0;
  private static final boolean verbose = true;

  private final WKTReader wktReader = new WKTReader();

  public static void main(String[] args) {
    TestRunner.run(AnchorPointNodingTest.class);
  }

  public AnchorPointNodingTest(String name) {
    super(name);
  }

  public void testPolyWithCloseNode() {
    String[] polyWithCloseNode = {
      "POLYGON ((20 0, 20 160, 140 1, 160 160, 160 1, 20 0))"
    };
    runAnchorNoding(polyWithCloseNode);
    runComparison(polyWithCloseNode);
  }

  public void testLineStringLongShort() {
    String[] geoms = {
      "LINESTRING (0 0, 2 0)",
      "LINESTRING (0 0, 10 -1)"
    };
    runAnchorNoding(geoms);
    runComparison(geoms);
  }

  public void testBadLines1() {
    String[] badLines1 = {
      "LINESTRING ( 171 157, 175 154, 170 154, 170 155, 170 156, 170 157, 171 158, 171 159, 172 160, 176 156, 171 156, 171 159, 176 159, 172 155, 170 157, 174 161, 174 156, 173 156, 172 156 )"
    };
    runAnchorNoding(badLines1);
    runComparison(badLines1);
  }

  public void testBadLines2() {
    String[] badLines2 = {
      "LINESTRING ( 175 222, 176 222, 176 219, 174 221, 175 222, 177 220, 174 220, 174 222, 177 222, 175 220, 174 221 )"
    };
    runAnchorNoding(badLines2);
    runComparison(badLines2);
  }

  public void testCollapse1() {
    String[] collapse1 = {
      "LINESTRING ( 362 177, 375 164, 374 164, 372 161, 373 163, 372 165, 373 164, 442 58 )"
    };
    runAnchorNoding(collapse1);
    runComparison(collapse1);
  }

  public void testCollapse2() {
    String[] collapse2 = {
      "LINESTRING ( 393 175, 391 173, 390 175, 391 174, 391 173 )"
    };
    runAnchorNoding(collapse2);
    runComparison(collapse2);
  }

  public void testBadNoding1() {
    String[] badNoding1 = {
      "LINESTRING ( 76 47, 81 52, 81 53, 85 57, 88 62, 89 64, 57 80, 82 55, 101 74, 76 99, 92 67, 94 68, 99 71, 103 75, 139 111 )"
    };
    runAnchorNoding(badNoding1);
    runComparison(badNoding1);
  }

  public void testBadNoding1Extract() {
    String[] badNoding1Extract = {
      "LINESTRING ( 82 55, 101 74 )",
      "LINESTRING ( 94 68, 99 71 )",
      "LINESTRING ( 85 57, 88 62 )"
    };
    runAnchorNoding(badNoding1Extract);
    runComparison(badNoding1Extract);
  }

  public void testBadNoding1ExtractShift() {
    String[] badNoding1ExtractShift = {
      "LINESTRING ( 0 0, 19 19 )",
      "LINESTRING ( 12 13, 17 16 )",
      "LINESTRING ( 3 2, 6 7 )"
    };
    runAnchorNoding(badNoding1ExtractShift);
    runComparison(badNoding1ExtractShift);
  }

  public void testGeos1() {

  String[] wkt = {"LINESTRING ("
    + "-1677607.6366504875 -588231.47100446,"
    + "-1674050.1010869485 -587435.2186255794,"
    + "-1670493.6527468169 -586636.7948791061,"
    + "-1424286.3681743187 -525586.1397894835,"
    + "-1670493.6527468169 -586636.7948791061,"
    + "-1674050.1010869485 -587435.2186255795,"
    + "-1677607.6366504875 -588231.47100446)"};

    runAnchorNoding(wkt, new PrecisionModel(1E5));
    runComparison(wkt);
    runAnchorNoding(wkt, new PrecisionModel(1.11E10));
    runComparison(wkt);
  }

  public void testGeos2() {
    String[] wkt = {"LINESTRING ("
      + "55121.54481117887 42694.49730855581,"
      + "55121.54481117887 42694.4973085558,"
      + "55121.458748617406 42694.419143944244,"
      + "55121.54481117887 42694.49730855581 )"};

    runAnchorNoding(wkt, new PrecisionModel(1.E5));
    runComparison(wkt, new PrecisionModel(1.E5));

    runAnchorNoding(wkt, new PrecisionModel(1.1131949079327356E11));
    runComparison(wkt, new PrecisionModel(1.1131949079327356E11));
  }

  private void runAnchorNoding(String[] wkt) {
    runAnchorNoding(wkt, new PrecisionModel(SNAP_TOLERANCE));
  }

  private void runAnchorNoding(String[] wkt, PrecisionModel pm)
  {
    List geoms = fromWKT(wkt);
    GeometryNoder noder = new GeometryNoder(pm);
    noder.setValidate(true);
    List nodedLines = null;
    if (verbose) {
      for (int i = 0; i < geoms.size(); i++)
        System.out.println(geoms.get(i));
    }

    try {
      nodedLines = noder.node(geoms);
      if (verbose)
        System.out.println(new GeometryFactory().buildGeometry(nodedLines));
    }
    catch (RuntimeException reV) {
      try {
        noder.setValidate(false);
        nodedLines = noder.node(geoms);
        if (verbose) {
          System.out.println("Without noding validation:");
          System.out.println(new GeometryFactory().buildGeometry(nodedLines));
        }
      }
      catch (RuntimeException reNV) {
      }
      Assert.fail("noding validation failed:\n" + reV.toString());
    }
    if (verbose)
      System.out.println();

    assertTrue("all anchored", areAnchored(nodedLines, noder.getLastAnchorPoints(true)));
  }

  private void runComparison(String[] wkt) {
    runComparison(wkt, new PrecisionModel(SNAP_TOLERANCE));
  }

  private void runComparison(String[] wkt, PrecisionModel pm)
  {
    List geoms = fromWKT(wkt);

    org.locationtech.jtslab.noding.anchorpoint.GeometryNoder noder1 =
      new org.locationtech.jtslab.noding.anchorpoint.GeometryNoder(pm);

    org.locationtech.jts.noding.snapround.GeometryNoder noder2 =
      new org.locationtech.jts.noding.snapround.GeometryNoder(pm);

    List nodedLines1 = noder1.node(geoms);
    List nodedLines2 = noder2.node(geoms);

    GeometryFactory factory = ((Geometry)geoms.get(0)).getFactory();
    Geometry geom1 = factory.buildGeometry(nodedLines1);
    Geometry geom2 = factory.buildGeometry(nodedLines2);

    Geometry diff = geom1.difference(geom2);
    if (diff.isEmpty() || nodedLines1.size() != nodedLines2.size()) return;

    System.out.println("Input:");
    for (int i = 0; i < geoms.size(); i++)
      System.out.println(geoms.get(i));

    System.out.println("\nSnapRound:");
    System.out.println(geom2.toText());
    for (int i = 0; i < nodedLines2.size(); i++)
      System.out.println(nodedLines2.get(i));

    System.out.println("\nAnchorPoint:");
    System.out.println(geom1.toText());
    for (int i = 0; i < nodedLines1.size(); i++)
      System.out.println(nodedLines1.get(i));

  }

  private List fromWKT(String[] wkts)
  {
    List geomList = new ArrayList();
    if (wkts == null || wkts.length == 0)
      return geomList;


    for (int i = 0; i < wkts.length; i++) {
      try {
        geomList.add(wktReader.read(wkts[i]));
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    return geomList;
  }

  private static boolean areAnchored(Collection lines, List<AnchorPoint> anchorPoints)
  {
    Iterator it = lines.iterator();
    while(it.hasNext()) {
      LineString line = (LineString) it.next();
      for (int j = 0; j < anchorPoints.size(); j++) {
        if (!areAnchored(line, anchorPoints.get(j)))
          return false;
      }
    }
    return true;
  }

  private static boolean areAnchored(LineString line, AnchorPoint anchorPoint) {
    CoordinateSequence seq = line.getCoordinateSequence();
    for (int i = 0; i < seq.size(); i++)
    {
      Coordinate c = seq.getCoordinate(i);
      double d = c.distance(anchorPoint.getCoordinate());
      if (d > anchorPoint.getMaxAnchorDistance()) continue;
      if (d != 0d) {
        if (verbose) {
          System.out.println("areAnchored fails:");
          System.out.println(line.toText());
          System.out.println("with");
          System.out.println(anchorPoint.getCoordinate());
          System.out.println("  at index = " + i + " => " + line.getPointN(i));
          System.out.println("  distance = " + d);
        }
        return false;
      }
    }
    return true;
  }
}
