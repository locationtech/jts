/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import static org.locationtech.jts.operation.overlayng.OverlayNG.INTERSECTION;
import static org.locationtech.jts.operation.overlayng.OverlayNG.UNION;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class OverlayNGTestOne extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(OverlayNGTestOne.class);
  }

  public OverlayNGTestOne(String name) { super(name); }
  
  //======  Tests for semantic of including collapsed edges as lines in result
  
  public void xxtestCollapseTriBoxIntersection() {
    Geometry a = read("POLYGON ((1 2, 1 1, 9 1, 1 2))");
    Geometry b = read("POLYGON ((9 2, 9 1, 8 1, 8 2, 9 2))");
    Geometry expected = read("LINESTRING (8 1, 9 1)");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void testCollapseTriBoxesIntersection() {
    Geometry a = read("MULTIPOLYGON (((1 4, 1 1, 2 1, 2 4, 1 4)), ((9 4, 9 1, 10 1, 10 4, 9 4)))");
    Geometry b = read("POLYGON ((0 2, 11 3, 11 2, 0 2))");
    Geometry expected = read("GEOMETRYCOLLECTION (LINESTRING (1 2, 2 2), POLYGON ((9 2, 9 3, 10 3, 10 2, 9 2)))");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestCollapseBoxCutByTriangleUnion() {
    Geometry a = read("POLYGON ((100 10, 0 10, 100 11, 100 10))");
    Geometry b = read("POLYGON ((20 20, 0 20, 0 0, 20 0, 20 20))");
    Geometry expected = read("MULTIPOLYGON (((0 0, 0 10, 0 20, 20 20, 20 10, 20 0, 0 0)), ((20 10, 100 11, 100 10, 20 10)))");
    checkEqual(expected, OverlayNGTest.union(a, b, 1));
  }
  
  public void xtestCollapseBoxTriangleUnion() {
    Geometry a = read("POLYGON ((10 10, 100 10, 10 11, 10 10))");
    Geometry b = read("POLYGON ((90 0, 200 0, 200 200, 90 200, 90 0))");
    Geometry expected = read("MULTIPOLYGON (((90 10, 10 10, 10 11, 90 10)), ((90 10, 90 200, 200 200, 200 0, 90 0, 90 10)))");
    checkEqual(expected, OverlayNGTest.union(a, b, 1));
  }
  
  //==============================================
  
  public void xtestParallelSpikes() {
    Geometry a = read("POLYGON ((1 3.3, 1.3 1.4, 3.1 1.4, 3.1 0.9, 1.3 0.9, 1 -0.2, 0.8 1.3, 1 3.3))");
    Geometry b = read("POLYGON ((1 2.9, 2.9 2.9, 2.9 1.3, 1.7 1, 1.3 0.9, 1 0.4, 1 2.9))");
    Geometry expected = read("POLYGON EMPTY");
    checkEqual(expected, OverlayNGTest.intersection(a, b, 1));
  }
  
  public void xtestBoxHoleCollapseAlongBEdgeDifference() {
    Geometry a = read("POLYGON ((0 3, 3 3, 3 0, 0 0, 0 3), (1 1.2, 1 1.1, 2.3 1.1, 1 1.2))");
    Geometry b = read("POLYGON ((1 1, 2 1, 2 0, 1 0, 1 1))");
    Geometry expected = read("POLYGON EMPTY");
    checkEqual(expected, OverlayNGTest.difference(b, a, 1));
  }
  
  public void xtestPolyPolyTouchIntersection() {
    Geometry a = read("POLYGON ((300 0, 100 0, 100 100, 300 100, 300 0))");
    Geometry b = read("POLYGON ((100 200, 300 200, 300 100, 200 100, 200 0, 100 0, 100 200))");
    Geometry expected = read("GEOMETRYCOLLECTION (LINESTRING (200 100, 300 100), POLYGON ((200 0, 100 0, 100 100, 200 100, 200 0)))");
    checkEqual(expected, OverlayNGTest.intersection(a, b, 1));
  }
  
  public void xtestBoxHoleCollapseAlongBEdgeUnion() {
    Geometry a = read("POLYGON ((0 3, 3 3, 3 0, 0 0, 0 3), (1 1.2, 1 1.1, 2.3 1.1, 1 1.2))");
    Geometry b = read("POLYGON ((1 1, 2 1, 2 0, 1 0, 1 1))");
    Geometry expected = read("POLYGON ((0 0, 0 3, 3 3, 3 0, 2 0, 1 0, 0 0))");
    checkEqual(expected, OverlayNGTest.union(b, a, 1));
  }
  
  public void xtestRoundedBoxesIntersection() {
    Geometry a = read("POLYGON ((0.6 0.1, 0.6 1.9, 2.9 1.9, 2.9 0.1, 0.6 0.1))");
    Geometry b = read("POLYGON ((1.1 3.9, 2.9 3.9, 2.9 2.1, 1.1 2.1, 1.1 3.9))");
    Geometry expected = read("LINESTRING (1 2, 3 2)");
    checkEqual(expected, OverlayNGTest.intersection(a, b, 1));
  }
  
  public void xtestRoundedLinesIntersection() {
    Geometry a = read("LINESTRING (3 2, 3 4)");
    Geometry b = read("LINESTRING (1.1 1.6, 3.8 1.9)");
    Geometry expected = read("POINT (3 2)");
    checkEqual(expected, OverlayNGTest.intersection(a, b, 1));
  }
  
  public void xtestRoundedPointsIntersection() {
    Geometry a = read("POINT (10.1 10)");
    Geometry b = read("POINT (10 10.1)");
    Geometry expected = read("POINT (10 10)");
    checkEqual(expected, OverlayNGTest.intersection(a, b, 1));
  }
  
  public void xtestLineLineIntersectionFloat() {
    Geometry a = read("LINESTRING (10 10, 20 20)");
    Geometry b = read("LINESTRING (13 13, 10 10, 10 20, 20 20, 17 17)");
    Geometry expected = read("LINESTRING (17 17, 20 20, 10 20, 10 10, 13 13, 17 17)");
    Geometry actual = OverlayNG.overlay(a, b, UNION);
    checkEqual(expected, actual);
  }
  
  public void xtestPolygonPointIntersection() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry b = read("MULTIPOINT ((150 150), (250 150))");
    Geometry expected = read("POINT (150 150)");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestPolygonPointUnion() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry b = read("MULTIPOINT ((150 150), (250 150))");
    Geometry expected = read("GEOMETRYCOLLECTION (POINT (250 150), POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200)))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestPolygoPolygonWithLineTouchIntersection() {
    Geometry a = read("POLYGON ((360 200, 220 200, 220 180, 300 180, 300 160, 300 140, 360 200))");
    Geometry b = read("MULTIPOLYGON (((280 180, 280 160, 300 160, 300 180, 280 180)), ((220 230, 240 230, 240 180, 220 180, 220 230)))");
    Geometry expected = read("POLYGON ((220 200, 240 200, 240 180, 220 180, 220 200))");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestLinePolygonIntersectionAlongCollapse() {
    Geometry a = read("POLYGON ((100 300, 300 300, 300 200, 130 200, 300 199.9, 300 100, 100 100, 100 300))");
    Geometry b = read("LINESTRING (130 200, 200 200)");
    Geometry expected = read("LINESTRING (130 200, 200 200)");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestLinePolygonIntersectionAlongPolyBoundary() {
    Geometry a = read("LINESTRING (150 300, 250 300)");
    Geometry b = read("POLYGON ((100 400, 200 400, 200 300, 100 300, 100 400))");
    Geometry expected = read("LINESTRING (200 300, 150 300)");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestPolygonMultiLineUnion() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry b = read("MULTILINESTRING ((150 250, 150 50), (250 250, 250 50))");
    Geometry expected = read("GEOMETRYCOLLECTION (LINESTRING (150 50, 150 100), LINESTRING (150 200, 150 250), LINESTRING (250 50, 250 250), POLYGON ((100 100, 100 200, 150 200, 200 200, 200 100, 150 100, 100 100)))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestLinePolygonUnion() {
    Geometry a = read("LINESTRING (50 150, 150 150)");
    Geometry b = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry expected = read("GEOMETRYCOLLECTION (LINESTRING (50 150, 100 150), POLYGON ((100 200, 200 200, 200 100, 100 100, 100 150, 100 200)))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestBoxGoreIntersection() {
    Geometry a = read("MULTIPOLYGON (((1 1, 5 1, 5 0, 1 0, 1 1)), ((1 1, 5 2, 5 4, 1 4, 1 1)))");
    Geometry b = read("POLYGON ((1 0, 1 2, 2 2, 2 0, 1 0))");
    Geometry expected = read("POLYGON ((2 0, 1 0, 1 1, 1 2, 2 2, 2 1, 2 0))");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestBoxGoreUnion() {
    Geometry a = read("MULTIPOLYGON (((1 1, 5 1, 5 0, 1 0, 1 1)), ((1 1, 5 2, 5 4, 1 4, 1 1)))");
    Geometry b = read("POLYGON ((1 0, 1 2, 2 2, 2 0, 1 0))");
    Geometry expected = read("POLYGON ((2 0, 1 0, 1 1, 1 2, 1 4, 5 4, 5 2, 2 1, 5 1, 5 0, 2 0))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestCollapseBoxGoreIntersection() {
    Geometry a = read("MULTIPOLYGON (((1 1, 5 1, 5 0, 1 0, 1 1)), ((1 1, 5 2, 5 4, 1 4, 1 1)))");
    Geometry b = read("POLYGON ((1 0, 1 2, 2 2, 2 0, 1 0))");
    Geometry expected = read("POLYGON ((2 0, 1 0, 1 1, 1 2, 2 2, 2 1, 2 0))");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestCollapseTriBoxIntersection() {
    Geometry a = read("POLYGON ((1 2, 1 1, 9 1, 1 2))");
    Geometry b = read("POLYGON ((9 2, 9 1, 8 1, 8 2, 9 2))");
    Geometry expected = read("POLYGON EMPTY");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void XtestCollapseTriBoxUnion() {
    Geometry a = read("POLYGON ((1 2, 1 1, 9 1, 1 2))");
    Geometry b = read("POLYGON ((9 2, 9 1, 8 1, 8 2, 9 2))");
    Geometry expected = read("MULTIPOLYGON (((1 1, 1 2, 8 1, 1 1)), ((8 1, 8 2, 9 2, 9 1, 8 1)))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestAdjacentBoxesUnion() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry b = read("POLYGON ((300 200, 300 100, 200 100, 200 200, 300 200))");
    Geometry expected = read("POLYGON ((100 100, 100 200, 200 200, 300 200, 300 100, 200 100, 100 100))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestBoxTriIntersection() {
    Geometry a = read("POLYGON ((0 6, 4 6, 4 2, 0 2, 0 6))");
    Geometry b = read("POLYGON ((1 0, 2 5, 3 0, 1 0))");
    Geometry expected = read("POLYGON ((3 2, 1 2, 2 5, 3 2))");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  

  public void xtestBoxTriUnion() {
    Geometry a = read("POLYGON ((0 6, 4 6, 4 2, 0 2, 0 6))");
    Geometry b = read("POLYGON ((1 0, 2 5, 3 0, 1 0))");
    Geometry expected = read("POLYGON ((0 6, 4 6, 4 2, 3 2, 3 0, 1 0, 1 2, 0 2, 0 6))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestMultiHoleBoxUnion() {
    Geometry a = read("MULTIPOLYGON (((0 200, 200 200, 200 0, 0 0, 0 200), (50 50, 190 50, 50 200, 50 50), (20 20, 20 50, 50 50, 50 20, 20 20)), ((60 100, 50 50, 100 60, 60 100)))");
    Geometry b = read("POLYGON ((60 110, 100 110, 100 60, 60 60, 60 110))");
    Geometry expected = read("POLYGON ((3 2, 1 2, 2 5, 3 2))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestNestedPolysUnion() {
    Geometry a = read("MULTIPOLYGON (((0 200, 200 200, 200 0, 0 0, 0 200), (50 50, 190 50, 50 200, 50 50)), ((60 100, 100 60, 50 50, 60 100)))");
    Geometry b = read("POLYGON ((135 176, 180 176, 180 130, 135 130, 135 176))");
    Geometry expected = read("MULTIPOLYGON (((0 0, 0 200, 50 200, 200 200, 200 0, 0 0), (50 50, 190 50, 50 200, 50 50)), ((50 50, 60 100, 100 60, 50 50)))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  // TODO: check this when it has be implemented...
  public void xtestMultiHoleSideTouchingBoxUnion() {
    Geometry a = read("MULTIPOLYGON (((0 200, 200 200, 200 0, 0 0, 0 200), (50 50, 190 50, 50 200, 50 50), (20 20, 20 50, 50 50, 50 20, 20 20)))");
    Geometry b = read("POLYGON ((100 100, 100 50, 50 50, 50 100, 100 100))");
    Geometry expected = read("POLYGON ((3 2, 1 2, 2 5, 3 2))");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestNestedShellsIntersection() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry b = read("POLYGON ((120 180, 180 180, 180 120, 120 120, 120 180))");
    Geometry expected = read("POLYGON ((120 180, 180 180, 180 120, 120 120, 120 180))");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestNestedShellsUnion() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry b = read("POLYGON ((120 180, 180 180, 180 120, 120 120, 120 180))");
    Geometry expected = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestBoxLineIntersection() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry b = read("LINESTRING (50 150, 150 150)");
    Geometry expected = read("LINESTRING (100 150, 150 150)");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  public void xtestBoxLineUnion() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry b = read("LINESTRING (50 150, 150 150)");
    Geometry expected = read("GEOMETRYCOLLECTION (LINESTRING (50 150, 100 150), POLYGON ((100 200, 200 200, 200 100, 100 100, 100 150, 100 200)))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestAdjacentBoxesIntersection() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry b = read("POLYGON ((300 200, 300 100, 200 100, 200 200, 300 200))");
    Geometry expected = read("LINESTRING (200 100, 200 200)");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestBoxContainingPolygonCollapseIntersection() {
    Geometry a = read("POLYGON ((100 200, 300 200, 300 0, 100 0, 100 200))");
    Geometry b = read("POLYGON ((250 100, 150 100, 150 100.4, 250 100))");
    Geometry expected = read("LINESTRING (150 100, 250 100)");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestBoxContainingPolygonCollapseManyPtsIntersection() {
    Geometry a = read("POLYGON ((100 200, 300 200, 300 0, 100 0, 100 200))");
    Geometry b = read("POLYGON ((250 100, 150 100, 150 100.4, 160 100.2, 170 100.1, 250 100))");
    Geometry expected = read("MULTILINESTRING ((150 100, 160 100), (160 100, 170 100), (170 100, 250 100))");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestPolygonsSpikeCollapseIntersection() {
    Geometry a = read("POLYGON ((2.33906 48.78994, 2.33768 48.78857, 2.33768 48.78788, 2.33974 48.78719, 2.34009 48.78616, 2.33974 48.78513, 2.33871 48.78479, 2.33734 48.78479, 2.33631 48.78445, 2.33597 48.78342, 2.33631 48.78239, 2.337 48.7817, 2.33734 48.78067, 2.33734 48.7793, 2.337 48.77827, 2.3178 48.7849, 2.32099 48.79376, 2.33906 48.78994))");
    Geometry b = read("POLYGON ((2.33768 48.78857, 2.33768 48.78788, 2.33974 48.78719, 2.34009 48.78616, 2.33974 48.78513, 2.33871 48.78479, 2.33734 48.78479, 2.33631 48.78445, 2.3362 48.7841, 2.33562 48.78582, 2.33425 48.78719, 2.33768 48.78857))");
    Geometry expected = read("MULTILINESTRING ((150 100, 160 100), (160 100, 170 100), (170 100, 250 100))");
    Geometry actual = intersection(a, b, 100000);
    checkEqual(expected, actual);
  }
  
  /**
   * Fails because polygon A collapses totally, but one
   * L edge is still labelled with location A:iL due to being located
   * inside original A polygon by PiP test for incomplete edges.
   * That edge is then marked as in-result-area, but result ring can't
   * be formed because ring is incomplete
   */
  public void xtestCollapseAIncompleteRingUnion() {
    Geometry a = read("POLYGON ((0.9 1.7, 1.3 1.4, 2.1 1.4, 2.1 0.9, 1.3 0.9, 0.9 0, 0.9 1.7))");
    Geometry b = read("POLYGON ((1 3, 3 3, 3 1, 1.3 0.9, 1 0.4, 1 3))");
    Geometry expected = read("GEOMETRYCOLLECTION (LINESTRING (1 0, 1 1), POLYGON ((1 1, 1 2, 1 3, 3 3, 3 1, 2 1, 1 1)))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestCollapseHoleAlongEdgeOfBIntersection() {
    Geometry a = read("POLYGON ((0 3, 3 3, 3 0, 0 0, 0 3), (1 1.2, 1 1.1, 2.3 1.1, 1 1.2))");
    Geometry b = read("POLYGON ((1 1, 2 1, 2 0, 1 0, 1 1))");
    Geometry expected = read("POLYGON ((1 1, 2 1, 2 0, 1 0, 1 1))");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestCollapseResultShouldHavePolygonUnion() {
    Geometry a = read("POLYGON ((1 3.3, 1.3 1.4, 3.1 1.4, 3.1 0.9, 1.3 0.9, 1 -0.2, 0.8 1.3, 1 3.3))");
    Geometry b = read("POLYGON ((1 2.9, 2.9 2.9, 2.9 1.3, 1.7 1, 1.3 0.9, 1 0.4, 1 2.9))");
    Geometry expected = read("POLYGON ((1 1, 1 3, 3 3, 3 1, 2 1, 1 1))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestVerySmallBIntersection() {
    Geometry a = read("POLYGON ((2.526855443750341 48.82324221874807, 2.5258255 48.8235855, 2.5251389 48.8242722, 2.5241089 48.8246155, 2.5254822 48.8246155, 2.5265121 48.8242722, 2.526855443750341 48.82324221874807))");
    Geometry b = read("POLYGON ((2.526512100000002 48.824272199999996, 2.5265120999999953 48.8242722, 2.5265121 48.8242722, 2.526512100000002 48.824272199999996))");
    Geometry expected = read("POLYGON EMPTY");
    Geometry actual = intersection(a, b, 100000000);
    checkEqual(expected, actual);
  }
  
  /**
   * Currently noding is incorrect, producing one 2pt edge which is coincident
   * with a 3-pt edge.  The EdgeMerger doesn't check that merged edges are identical,
   * so merges the 3pt edge into the 2-pt edge 
   */
  public void xtestEdgeDisappears() {
    Geometry a = read("LINESTRING (2.1279144 48.8445282, 2.126884443750796 48.84555818124935, 2.1268845 48.8455582, 2.1268845 48.8462448)");
    Geometry b = read("LINESTRING EMPTY");
    Geometry expected = read("LINESTRING EMPTY");
    Geometry actual = intersection(a, b, 1000000);
    checkEqual(expected, actual);
  }
  
  /**
   * Probably due to B collapsing completely and disconnected edges being located incorrectly in B interior.
   * Have seen other cases of this as well.
   * Also - a B edge is marked as a Hole, which is incorrect
   */
  public void xtestBcollapseLocateIssue() {
    Geometry a = read("POLYGON ((2.3442078 48.9331054, 2.3435211 48.9337921, 2.3428345 48.9358521, 2.3428345 48.9372253, 2.3433495 48.9370537, 2.3440361 48.936367, 2.3442078 48.9358521, 2.3442078 48.9331054))");
    Geometry b = read("POLYGON ((2.3442078 48.9331054, 2.3435211 48.9337921, 2.3433494499999985 48.934307100000005, 2.3438644 48.9341354, 2.3442078 48.9331055, 2.3442078 48.9331054))");
    Geometry expected = read("POLYGON EMPTY");
    Geometry actual = intersection(a, b, 1000);
    checkEqual(expected, actual);
  }

  /**
   * A component of B collapses completely.
   * Labelling marks a single collapsed edge as B:i.
   * Edge is only connected to two other edges both marked B:e.
   * B:i edge is included in area result edges, and faild because it does not form a ring.
   * 
   * Perhaps a fix is to ignore connected single Bi edges which do not form a ring?
   * This may be dangerous since it may hide other labelling problems? 
   * 
   * FIXED by requiring both endpoints of edge to lie in Interior to be located as i
   */
  public void xtestBcollapseEdgeLabeledInterior() {
    Geometry a = read("POLYGON ((2.384376506250038 48.91765596875102, 2.3840332 48.916626, 2.3840332 48.9138794, 2.3833466 48.9118195, 2.3812866 48.9111328, 2.37854 48.9111328, 2.3764801 48.9118195, 2.3723602 48.9159393, 2.3703003 48.916626, 2.3723602 48.9173126, 2.3737335 48.9186859, 2.3757935 48.9193726, 2.3812866 48.9193726, 2.3833466 48.9186859, 2.384376506250038 48.91765596875102))");
    Geometry b = read("MULTIPOLYGON (((2.3751067666731345 48.919143677778855, 2.3757935 48.9193726, 2.3812866 48.9193726, 2.3812866 48.9179993, 2.3809433 48.9169693, 2.3799133 48.916626, 2.3771667 48.916626, 2.3761368 48.9169693, 2.3754501 48.9190292, 2.3751067666731345 48.919143677778855)), ((2.3826108673454116 48.91893115612326, 2.3833466 48.9186859, 2.3840331750033394 48.91799930833141, 2.3830032 48.9183426, 2.3826108673454116 48.91893115612326)))");
    Geometry expected = read("POLYGON ((2.375 48.91833333333334, 2.375 48.92, 2.381666666666667 48.92, 2.381666666666667 48.91833333333334, 2.381666666666667 48.916666666666664, 2.38 48.916666666666664, 2.3766666666666665 48.916666666666664, 2.375 48.91833333333334))");
    Geometry actual = intersection(a, b, 600);
    checkEqual(expected, actual);
  }

  public void xtestBcollapseNullEdgeInRingIssue() {
    Geometry a = read("POLYGON ((2.2494507 48.8864136, 2.2484207 48.8867569, 2.2477341 48.8874435, 2.2470474 48.8874435, 2.2463608 48.8853836, 2.2453308 48.8850403, 2.2439575 48.8850403, 2.2429276 48.8853836, 2.2422409 48.8860703, 2.2360611 48.8970566, 2.2504807 48.8956833, 2.2494507 48.8864136))");
    Geometry b = read("POLYGON ((2.247734099999997 48.8874435, 2.2467041 48.8877869, 2.2453308 48.8877869, 2.2443008 48.8881302, 2.243957512499544 48.888473487500455, 2.2443008 48.8888168, 2.2453308 48.8891602, 2.2463608 48.8888168, 2.247734099999997 48.8874435))");
    Geometry expected = read("POLYGON EMPTY");
    Geometry actual = intersection(a, b, 200);
    checkEqual(expected, actual);
  }
  
  public void xtestLineUnion() {
    Geometry a = read("LINESTRING (0 0, 1 1)");
    Geometry b = read("LINESTRING (1 1, 2 2)");
    Geometry expected = read("LINESTRING (0 0, 1 1, 2 2)");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestLine2Union() {
    Geometry a = read("LINESTRING (0 0, 1 1, 0 1)");
    Geometry b = read("LINESTRING (1 1, 2 2, 3 3)");
    Geometry expected = read("MULTILINESTRING ((0 0, 1 1), (0 1, 1 1), (1 1, 2 2, 3 3))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestLine3Union() {
    Geometry a = read("MULTILINESTRING ((0 1, 1 1), (2 2, 2 0))");
    Geometry b = read("LINESTRING (0 0, 1 1, 2 2, 3 3)");
    Geometry expected = read("MULTILINESTRING ((0 0, 1 1), (0 1, 1 1), (1 1, 2 2), (2 0, 2 2), (2 2, 3 3))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestLine4Union() {
    Geometry a = read("LINESTRING (100 300, 200 300, 200 100, 100 100)");
    Geometry b = read("LINESTRING (300 300, 200 300, 200 300, 200 100, 300 100)");
    Geometry expected = read("MULTILINESTRING ((200 100, 100 100), (300 300, 200 300), (200 300, 200 100), (200 100, 300 100), (100 300, 200 300))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestLineFigure8Union() {
    Geometry a = read("LINESTRING (5 1, 2 2, 5 3, 2 4, 5 5)");
    Geometry b = read("LINESTRING (5 1, 8 2, 5 3, 8 4, 5 5)");
    Geometry expected = read("MULTILINESTRING ((5 3, 2 2, 5 1, 8 2, 5 3), (5 3, 2 4, 5 5, 8 4, 5 3))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestLineRingUnion() {
    Geometry a = read("LINESTRING (1 1, 5 5, 9 1)");
    Geometry b = read("LINESTRING (1 1, 9 1)");
    Geometry expected = read("LINESTRING (1 1, 5 5, 9 1, 1 1)");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  /**
   * Failure due to B hole collapsing and edges being labeled Exterior.
   * They are coincident with an A hole edge, but because labeled E are not
   * included in Intersection result.
   */
  public void xtestBCollapsedHoleEdgeLabelledExterior() {
    Geometry a = read("POLYGON ((309500 3477900, 309900 3477900, 309900 3477600, 309500 3477600, 309500 3477900), (309741.87561330193 3477680.6737848604, 309745.53718649445 3477677.607851833, 309779.0333599192 3477653.585555199, 309796.8051681937 3477642.143583868, 309741.87561330193 3477680.6737848604))");
    Geometry b = read("POLYGON ((309500 3477900, 309900 3477900, 309900 3477600, 309500 3477600, 309500 3477900), (309636.40806633036 3477777.2910157656, 309692.56085444096 3477721.966349552, 309745.53718649445 3477677.607851833, 309779.0333599192 3477653.585555199, 309792.0991800499 3477645.1734264474, 309779.03383125085 3477653.5853248164, 309745.53756275156 3477677.6076231804, 309692.5613257677 3477721.966119165, 309636.40806633036 3477777.2910157656))");
    Geometry expected = read("POLYGON ((309500 3477600, 309500 3477900, 309900 3477900, 309900 3477600, 309500 3477600), (309741.88 3477680.67, 309745.54 3477677.61, 309779.03 3477653.59, 309792.1 3477645.17, 309796.81 3477642.14, 309741.88 3477680.67))");
    Geometry actual = intersection(a, b, 100);
    checkEqual(expected, actual);
  }
  public static Geometry union(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, UNION, pm);
  }
  
  public static Geometry intersection(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, INTERSECTION, pm);
  }
}
