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
package org.locationtech.jts.noding;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import test.jts.GeometryTestCase;

public class NodedSegmentStringTest extends GeometryTestCase {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(NodedSegmentStringTest.class);
  }
  public NodedSegmentStringTest(String name) {
    super(name);
  }

  /**
   * Tests a case which involves nodes added when using the SnappingNoder.
   * In this case one of the added nodes is relatively "far" from its segment, 
   * and "near" the start vertex of the segment.
   * Computing the noding correctly requires the fix to {@link SegmentNode#compareTo(Object)}
   * added in https://github.com/locationtech/jts/pull/399
   * 
   * See https://trac.osgeo.org/geos/ticket/1051
   */
  public void testSegmentNodeOrderingForSnappedNodes() {
    checkNoding("LINESTRING (655103.6628454948 1794805.456674405, 655016.20226 1794940.10998, 655014.8317182435 1794941.5196832407)",
        "MULTIPOINT((655016.29615051334 1794939.965427252), (655016.20226531825 1794940.1099718122), (655016.20226 1794940.10998), (655016.20225819293 1794940.1099794197))",
        new int[] { 0, 0, 1, 1},
        "MULTILINESTRING ((655014.8317182435 1794941.5196832407, 655016.2022581929 1794940.1099794197), (655016.2022581929 1794940.1099794197, 655016.20226 1794940.10998), (655016.20226 1794940.10998, 655016.2022653183 1794940.1099718122), (655016.2022653183 1794940.1099718122, 655016.2961505133 1794939.965427252), (655016.2961505133 1794939.965427252, 655103.6628454948 1794805.456674405))");
  }
  
  private void checkNoding(String wktLine, String wktNodes, int[] segmentIndex, String wktExpected) {
    Geometry line = read(wktLine);
    Geometry pts = read(wktNodes);
    
    NodedSegmentString nss = new NodedSegmentString(line.getCoordinates(), null);
    Coordinate[] node = pts.getCoordinates();
    
    for (int i = 0; i < node.length; i++) {
      nss.addIntersection(node[i], segmentIndex[i]);
    }
    
    List nodedSS = NodingTestUtil.getNodedSubstrings(nss);
    Geometry result = NodingTestUtil.toLines(nodedSS, line.getFactory());
    //System.out.println(result);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result);
  }

}
