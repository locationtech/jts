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
package org.locationtech.jts.operation.overlayng;

import static org.locationtech.jts.operation.overlayng.OverlayNGOp.INTERSECTION;
import static org.locationtech.jts.operation.overlayng.OverlayNGOp.UNION;
import static org.locationtech.jts.operation.overlayng.OverlayNGOp.DIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNGOp.SYMDIFFERENCE;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.operation.overlayng.OverlayNGOp;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class OverlayNGOneTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(OverlayNGOneTest.class);
  }

  public OverlayNGOneTest(String name) { super(name); }
  
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
  
  public void testNestedShellsIntersection() {
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
  
  public static Geometry union(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNGOp.overlay(a, b, pm, UNION);
  }
  
  public static Geometry intersection(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNGOp.overlay(a, b, pm, INTERSECTION);
  }
}
